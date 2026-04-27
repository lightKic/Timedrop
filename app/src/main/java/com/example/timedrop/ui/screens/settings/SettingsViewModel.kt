package com.example.timedrop.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timedrop.data.settings.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SettingsDataStore(app.applicationContext)

    val uiState: StateFlow<SettingsUiState> =
        store.settingsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = SettingsUiState(),
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { store.setThemeMode(mode) }
    }

    fun setClockAnimationEnabled(enabled: Boolean) {
        viewModelScope.launch { store.setClockAnimationEnabled(enabled) }
    }

    fun setImmersiveAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch { store.setImmersiveAnimationsEnabled(enabled) }
    }

    fun setUse24Hour(use24Hour: Boolean) {
        viewModelScope.launch { store.setUse24Hour(use24Hour) }
    }

    fun setNavOrder(navOrder: List<String>) {
        viewModelScope.launch { store.setNavOrder(navOrder) }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { store.setHapticEnabled(enabled) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { store.setKeepScreenOn(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { store.setNotificationsEnabled(enabled) }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch { store.setAppLockEnabled(enabled) }
    }

    fun setDiagnosticsEnabled(enabled: Boolean) {
        viewModelScope.launch { store.setDiagnosticsEnabled(enabled) }
    }

    fun clearAppHistory() {
        viewModelScope.launch {
            val db = com.example.timedrop.data.local.AppDatabase.getDatabase(getApplication())
            db.calendarEventDao().deleteAllEvents()
            db.noteDao().deleteAllNotes()
        }
    }

    fun checkAndUpdateStreak() {
        viewModelScope.launch {
            val settings = store.settingsFlow.first()
            val today = LocalDate.now()
            val todayStr = today.toString()
            
            if (settings.lastLoginDate == todayStr) return@launch
            
            val lastDate = if (settings.lastLoginDate.isNotEmpty()) {
                LocalDate.parse(settings.lastLoginDate)
            } else null
            
            val newStreak = when {
                lastDate == null -> 1
                lastDate.plusDays(1) == today -> settings.streakCount + 1
                else -> 1
            }
            
            store.setLastLoginDate(todayStr)
            store.setStreakCount(newStreak)
        }
    }

    fun signOut() {
        viewModelScope.launch { store.clearAll() }
    }
}

