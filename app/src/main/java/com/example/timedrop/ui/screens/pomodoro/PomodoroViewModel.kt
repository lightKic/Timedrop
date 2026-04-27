package com.example.timedrop.ui.screens.pomodoro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timedrop.data.local.AppDatabase
import com.example.timedrop.data.local.CalendarEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

enum class PomodoroMode(val minutes: Int) {
    WORK(25),
    SHORT_BREAK(5),
    LONG_BREAK(15)
}

data class PomodoroState(
    val remainingSeconds: Int = PomodoroMode.WORK.minutes * 60,
    val isRunning: Boolean = false,
    val currentMode: PomodoroMode = PomodoroMode.WORK,
    val completedWorkSessions: Int = 0,
    val totalSeconds: Int = PomodoroMode.WORK.minutes * 60,
    val selectedTask: CalendarEvent? = null
)

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {
    private val eventDao = AppDatabase.getDatabase(application).calendarEventDao()
    
    private val _uiState = MutableStateFlow(PomodoroState())
    val uiState: StateFlow<PomodoroState> = _uiState.asStateFlow()

    val uncompletedTasks: StateFlow<List<CalendarEvent>> = eventDao.getUncompletedTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var timerJob: Job? = null

    fun toggleTimer() {
        if (_uiState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isRunning = true) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning && _uiState.value.remainingSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(remainingSeconds = max(0, it.remainingSeconds - 1)) }
            }
            if (_uiState.value.remainingSeconds == 0) {
                moveToNextMode()
            }
        }
    }

    private fun moveToNextMode() {
        _uiState.update { state ->
            val nextMode: PomodoroMode
            var nextSessions = state.completedWorkSessions
            
            when (state.currentMode) {
                PomodoroMode.WORK -> {
                    nextSessions++
                    nextMode = if (nextSessions % 4 == 0) {
                        PomodoroMode.LONG_BREAK
                    } else {
                        PomodoroMode.SHORT_BREAK
                    }
                }
                PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> {
                    nextMode = PomodoroMode.WORK
                }
            }
            
            state.copy(
                isRunning = false,
                currentMode = nextMode,
                completedWorkSessions = nextSessions,
                remainingSeconds = nextMode.minutes * 60,
                totalSeconds = nextMode.minutes * 60
            )
        }
    }

    private fun pauseTimer() {
        _uiState.update { it.copy(isRunning = false) }
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _uiState.update { 
            it.copy(
                remainingSeconds = it.totalSeconds,
                isRunning = false
            )
        }
    }

    fun skipSession() {
        pauseTimer()
        moveToNextMode()
    }

    fun selectTask(task: CalendarEvent?) {
        _uiState.update { it.copy(selectedTask = task) }
    }
}
