package com.example.timedrop.ui.screens.stopwatch

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timedrop.data.stopwatch.StopwatchHistoryItem
import com.example.timedrop.data.stopwatch.StopwatchHistoryStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

data class LapData(
    val lapNumber: Int,
    val title: String,
    val splitTimeMs: Long,
    val differenceMs: Long
)

class StopwatchViewModel(app: Application) : AndroidViewModel(app) {
    private val store = StopwatchHistoryStore(app.applicationContext)

    val history: StateFlow<List<StopwatchHistoryItem>> =
        store.historyFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private var startTime: Long = 0L
    private var baseTime: Long = 0L
    private var timerJob: Job? = null

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _laps = MutableStateFlow<List<LapData>>(emptyList())
    val laps: StateFlow<List<LapData>> = _laps.asStateFlow()

    fun toggle() {
        if (_isRunning.value) {
            pause()
        } else {
            start()
        }
    }

    private fun start() {
        _isRunning.value = true
        startTime = SystemClock.elapsedRealtime()
        timerJob = viewModelScope.launch {
            while (isActive) {
                _elapsedMs.value = baseTime + (SystemClock.elapsedRealtime() - startTime)
                delay(16) // ~60fps refresh
            }
        }
    }

    private fun pause() {
        timerJob?.cancel()
        _isRunning.value = false
        baseTime += SystemClock.elapsedRealtime() - startTime
        _elapsedMs.value = baseTime
    }

    fun lap() {
        if (_elapsedMs.value == 0L || !_isRunning.value) return
        val currentLaps = _laps.value
        val lapNum = currentLaps.size + 1
        val currentSplit = _elapsedMs.value
        
        // Time taken for this lap
        val previousSplit = if (currentLaps.isEmpty()) 0L else currentLaps.first().splitTimeMs
        val currentLapLength = currentSplit - previousSplit
        
        // Time taken for previous lap
        val previousLapLength = if (currentLaps.isNotEmpty()) {
            val p2Split = if (currentLaps.size > 1) currentLaps[1].splitTimeMs else 0L
            previousSplit - p2Split
        } else {
            0L
        }
        
        val diff = if (currentLaps.isEmpty()) 0L else currentLapLength - previousLapLength

        val title = when {
            lapNum == 1 -> "Initial Run"
            diff < 0 -> "Fastest Lap"
            else -> "Consistent"
        }

        val newLap = LapData(
            lapNumber = lapNum,
            title = title,
            splitTimeMs = currentSplit,
            differenceMs = diff
        )
        _laps.value = listOf(newLap) + currentLaps
    }

    fun reset() {
        pause()
        baseTime = 0L
        _elapsedMs.value = 0L
        _laps.value = emptyList()
    }

    fun save(name: String, elapsedMs: Long) {
        val trimmed = name.trim().ifEmpty { "Cronómetro" }
        viewModelScope.launch {
            store.add(
                StopwatchHistoryItem(
                    id = UUID.randomUUID().toString(),
                    name = trimmed,
                    elapsedMs = elapsedMs,
                    createdAtEpochMs = System.currentTimeMillis(),
                )
            )
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { store.delete(id) }
    }

    fun clearAll() {
        viewModelScope.launch { store.clear() }
    }
}
