package com.example.timedrop.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timedrop.data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.timedrop.data.settings.SettingsDataStore
import com.example.timedrop.data.SyncRepository


data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)


class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val db = AppDatabase.getDatabase(application)
    private val settingsStore = SettingsDataStore(application)
    private val syncRepository = SyncRepository(db)


    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun signIn() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val user = userDao.findUserByEmail(state.email)
                if (user != null && user.password == state.password) {
                    settingsStore.setCurrentUserEmail(state.email)
                    
                    // --- SYNC WITH FIREBASE ---
                    // 1. Login to Firebase
                    syncRepository.signInWithEmail(state.email, state.password)
                    
                    // 2. Download data from Cloud
                    val cloudEvents = syncRepository.downloadAllEvents()
                    val cloudNotes = syncRepository.downloadAllNotes()
                    val cloudSettings = syncRepository.downloadUserSettings()
                    
                    // Restore Streak and Settings (Complete Restoration)
                    cloudSettings?.let { settings ->
                        (settings["streakCount"] as? Long)?.let { settingsStore.setStreakCount(it.toInt()) }
                        (settings["longestStreak"] as? Long)?.let { settingsStore.setLongestStreak(it.toInt()) }
                        (settings["lastLoginDate"] as? String)?.let { settingsStore.setLastLoginDate(it) }
                        (settings["use24Hour"] as? Boolean)?.let { settingsStore.setUse24Hour(it) }
                        (settings["themeMode"] as? String)?.let { modeStr ->
                            try {
                                val mode = com.example.timedrop.ui.screens.settings.ThemeMode.valueOf(modeStr)
                                settingsStore.setThemeMode(mode)
                            } catch (e: Exception) {}
                        }
                    }
                    
                    // Give DataStore half a second to settle and emit new values
                    delay(500)
                    
                    // 3. Save to local database (avoiding duplicates)
                    cloudEvents.forEach { event ->
                        db.calendarEventDao().insertEvent(event) // Room handle conflicts if needed
                    }
                    cloudNotes.forEach { note ->
                        db.noteDao().insertNote(note)
                    }
                    
                    // 4. Manual sync mode: We no longer start real-time listening automatically
                    // syncRepository.startRealtimeSync()
                    
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, 
                        error = "Invalid email or password"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}
