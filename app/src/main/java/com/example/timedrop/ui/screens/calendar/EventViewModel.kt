package com.example.timedrop.ui.screens.calendar

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timedrop.data.local.AppDatabase
import com.example.timedrop.data.local.CalendarEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.example.timedrop.util.NotificationHelper
import com.example.timedrop.data.SyncRepository
import com.example.timedrop.data.settings.SettingsDataStore


class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val eventDao = db.calendarEventDao()
    private val syncRepository = SyncRepository(db)
    private val settingsStore = SettingsDataStore(application)

    init {
        viewModelScope.launch {
            syncRepository.signInAnonymously()
            settingsStore.settingsFlow.collect { settings ->
                if (settings.autoSyncEnabled) {
                    syncRepository.startRealtimeSync()
                } else {
                    syncRepository.stopRealtimeSync()
                }
            }
        }
    }


    var currentMonth by mutableStateOf(YearMonth.now())
        private set

    var selectedDate by mutableStateOf(LocalDate.now())
        private set

    // All events (for Home and general list)
    val allEvents: StateFlow<List<CalendarEvent>> = eventDao.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val todayStats: StateFlow<Pair<Int, Int>> = allEvents.map { events ->
        val todayStr = LocalDate.now().toString()
        val todayTasks = events.filter { it.isTask && it.date == todayStr }
        val completed = todayTasks.count { it.isCompleted }
        completed to todayTasks.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0 to 0)

    val focusTrend: StateFlow<List<Float>> = allEvents.map { _ ->
        // Return a mock trend for the premium visual look
        listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.95f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f))

    // Filtered events for the current month (handling repetitions for dots)
    val monthEvents: StateFlow<List<CalendarEvent>> = combine(allEvents, snapshotFlow { currentMonth }) { events, month ->
        // This is a simplification. For each day in the month, check which events apply.
        // For dots, we just need to know which dates have events.
        events.filter { event ->
            val eventDate = LocalDate.parse(event.date)
            val eventMonth = YearMonth.from(eventDate)
            
            when (event.repeatInterval) {
                "daily" -> eventDate <= month.atEndOfMonth()
                "weekly" -> eventDate <= month.atEndOfMonth()
                "mon-fri" -> eventDate <= month.atEndOfMonth()
                else -> eventMonth == month
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun isEventOnDate(event: CalendarEvent, queryDate: LocalDate): Boolean {
        val eventDate = LocalDate.parse(event.date)
        if (eventDate == queryDate) return true
        if (eventDate > queryDate) return false
        
        return when (event.repeatInterval) {
            "daily" -> true
            "weekly" -> eventDate.dayOfWeek == queryDate.dayOfWeek
            "mon-fri" -> queryDate.dayOfWeek.value in 1..5
            else -> false
        }
    }

    // Backwards compatibility for Home screen logic (temporarily)
    val events: StateFlow<List<CalendarEvent>> = allEvents

    fun nextMonth() {
        currentMonth = currentMonth.plusMonths(1)
    }

    fun previousMonth() {
        currentMonth = currentMonth.minusMonths(1)
    }

    fun selectDate(date: LocalDate) {
        selectedDate = date
    }

    fun toggleTaskCompletion(event: CalendarEvent) {
        val completing = !event.isCompleted
        val timestamp = if (completing) System.currentTimeMillis() else 0L
        updateEvent(event.copy(isCompleted = completing, completedAt = timestamp))
    }

    fun addEvent(
        title: String, 
        date: LocalDate, 
        time: String, 
        colorArgb: Int, 
        description: String = "",
        isTask: Boolean = false,
        repeatInterval: String = "none"
    ) {
        viewModelScope.launch {
            val event = CalendarEvent(
                title = title,
                date = date.toString(),
                time = time,
                colorArgb = colorArgb,
                description = description,
                isTask = isTask,
                isCompleted = false,
                repeatInterval = repeatInterval
            )
            val id = eventDao.insertEvent(event)
            val eventWithId = event.copy(id = id.toInt())
            NotificationHelper.scheduleEventAlarms(getApplication(), eventWithId)
            
            // Sync to Cloud
            if (settingsStore.settingsFlow.first().autoSyncEnabled) {
                syncRepository.syncEvent(eventWithId)
            }
        }

    }

    fun updateEvent(event: CalendarEvent) {
        viewModelScope.launch {
            eventDao.updateEvent(event)
            NotificationHelper.scheduleEventAlarms(getApplication(), event)
            
            // Sync to Cloud
            if (settingsStore.settingsFlow.first().autoSyncEnabled) {
                syncRepository.syncEvent(event)
            }
        }

    }

    fun removeEvent(event: CalendarEvent) {
        viewModelScope.launch {
            eventDao.deleteEvent(event)
            NotificationHelper.cancelEventAlarms(getApplication(), event)
            
            // Delete from Cloud
            if (settingsStore.settingsFlow.first().autoSyncEnabled) {
                syncRepository.deleteEvent(event.id)
            }
        }

    }
}
