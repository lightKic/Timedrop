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
import com.example.timedrop.data.SyncRepository
import com.example.timedrop.data.local.AppDatabase
import kotlinx.coroutines.flow.collectLatest
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SettingsDataStore(app.applicationContext)
    private val db = AppDatabase.getDatabase(app)
    private val syncRepository = SyncRepository(db)
    private val _syncProgress = MutableStateFlow(0f)

    val uiState: StateFlow<SettingsUiState> =
        combine(
            store.settingsFlow,
            syncRepository.getUserIdFlow(),
            db.noteDao().getAllNotes(),
            db.calendarEventDao().getAllEvents(),
            _syncProgress
        ) { settings, uid, notes, events, progress ->
            settings.copy(
                firebaseUserId = uid ?: "No conectado",
                localNotesCount = notes.size,
                localEventsCount = events.size,
                syncProgress = progress
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = SettingsUiState(),
        )

    init {
        // La sincronización ahora es estrictamente MANUAL por petición del usuario
        // para ahorrar cuota de Firebase.
    }

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

    fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { store.setAutoSyncEnabled(enabled) }
    }

    fun setAdminModeEnabled(enabled: Boolean) {
        viewModelScope.launch { store.setAdminModeEnabled(enabled) }
    }

    fun clearAppHistory() {
        viewModelScope.launch {
            val db = com.example.timedrop.data.local.AppDatabase.getDatabase(getApplication())
            db.calendarEventDao().deleteAllEvents()
            db.noteDao().deleteAllNotes()
        }
    }

    suspend fun checkAndUpdateStreak(): Boolean {
        val settings = store.settingsFlow.first()
        val today = LocalDate.now()
        val todayStr = today.toString()
        
        if (settings.lastLoginDate == todayStr) return false
        
        val lastDate = if (settings.lastLoginDate.isNotEmpty()) {
            LocalDate.parse(settings.lastLoginDate)
        } else null
        
        val newStreak = when {
            lastDate == null -> if (settings.streakCount > 0) settings.streakCount else 1
            lastDate.plusDays(1) == today -> settings.streakCount + 1
            lastDate == today -> settings.streakCount
            else -> 1
        }
        
        store.setLastLoginDate(todayStr)
        store.setStreakCount(newStreak)
        
        val changed = newStreak != settings.streakCount
        if (newStreak > settings.longestStreak) {
            store.setLongestStreak(newStreak)
        }
        return changed
    }

    fun setStreakCount(count: Int) {
        viewModelScope.launch {
            store.setStreakCount(count)
            if (count > store.settingsFlow.first().longestStreak) {
                store.setLongestStreak(count)
            }
        }
    }

    /**
     * Cierra la sesión del usuario.
     * Ahora intentará subir la racha un último segundo antes de borrar todo.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                val currentSettings = store.settingsFlow.first()
                val settingsMap = hashMapOf(
                    "streakCount" to currentSettings.streakCount,
                    "longestStreak" to currentSettings.longestStreak,
                    "lastLoginDate" to currentSettings.lastLoginDate,
                    "updatedAt" to System.currentTimeMillis()
                )
                // Intento de guardado rápido (sin timeout largo para no trabar al usuario)
                withTimeoutOrNull(3000) {
                    syncRepository.syncUserSettings(settingsMap)
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "No se pudo subir racha final", e)
            }

            store.clearAll()
            FirebaseAuth.getInstance().signOut()
        }
    }

    /**
     * Sube todos los datos locales a la nube.
     */
    fun syncAllToCloud() {
        viewModelScope.launch {
            _syncProgress.value = 0.1f
            try {
                val currentSettings = store.settingsFlow.first()
                val events = db.calendarEventDao().getAllEvents().first()
                val notes = db.noteDao().getAllNotes().first()
                
                // 1. Subir ajustes y racha
                _syncProgress.value = 0.3f
                val settingsMap = hashMapOf(
                    "streakCount" to currentSettings.streakCount,
                    "longestStreak" to currentSettings.longestStreak,
                    "lastLoginDate" to currentSettings.lastLoginDate,
                    "themeMode" to currentSettings.themeMode.name,
                    "use24Hour" to currentSettings.use24Hour,
                    "updatedAt" to System.currentTimeMillis()
                )
                syncRepository.syncUserSettings(settingsMap)
                
                // 2. Subir eventos y notas (Batch)
                _syncProgress.value = 0.6f
                syncRepository.uploadAllEvents(events)
                _syncProgress.value = 0.8f
                syncRepository.uploadAllNotes(notes)
                
                _syncProgress.value = 1.0f
                Toast.makeText(getApplication(), "¡Upload Exitoso!\nSincronizados: ${events.size} eventos y ${notes.size} notas", Toast.LENGTH_LONG).show()
                
                // Reset progress after a delay
                delay(2000)
                _syncProgress.value = 0f
            } catch (e: Exception) {
                _syncProgress.value = 0f
                Toast.makeText(getApplication(), "Error al subir: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Descarga todos los datos de la nube al celular.
     */
    fun syncAllFromCloud() {
        viewModelScope.launch {
            _syncProgress.value = 0.1f
            try {
                // 1. Descargar ajustes (Racha, etc.)
                _syncProgress.value = 0.3f
                val cloudSettings = syncRepository.downloadUserSettings()
                cloudSettings?.let { settings ->
                    (settings["streakCount"] as? Long)?.let { store.setStreakCount(it.toInt()) }
                    (settings["longestStreak"] as? Long)?.let { store.setLongestStreak(it.toInt()) }
                    (settings["lastLoginDate"] as? String)?.let { store.setLastLoginDate(it) }
                }

                // 2. Descargar eventos y notas
                _syncProgress.value = 0.6f
                val cloudEvents = syncRepository.downloadAllEvents()
                _syncProgress.value = 0.8f
                val cloudNotes = syncRepository.downloadAllNotes()
                
                // 3. Guardar en Room
                cloudEvents.forEach { db.calendarEventDao().insertEvent(it) }
                cloudNotes.forEach { db.noteDao().insertNote(it) }
                
                _syncProgress.value = 1.0f
                val msg = "¡Sincronizado! (${cloudEvents.size} eventos, ${cloudNotes.size} notas)"
                Toast.makeText(getApplication(), msg, Toast.LENGTH_LONG).show()
                
                delay(2000)
                _syncProgress.value = 0f
            } catch (e: Exception) {
                _syncProgress.value = 0f
                Toast.makeText(getApplication(), "Error al descargar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

