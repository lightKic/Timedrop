package com.example.timedrop.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.timedrop.ui.screens.settings.SettingsUiState
import com.example.timedrop.ui.screens.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CLOCK_ANIMATION_ENABLED = booleanPreferencesKey("clock_animation_enabled")
        val IMMERSIVE_ANIMATIONS_ENABLED = booleanPreferencesKey("immersive_animations_enabled")
        val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val NAV_ORDER = stringPreferencesKey("nav_order")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val DIAGNOSTICS_ENABLED = booleanPreferencesKey("diagnostics_enabled")
        val CURRENT_USER_EMAIL = stringPreferencesKey("current_user_email")
        val LAST_LOGIN_DATE = stringPreferencesKey("last_login_date")
        val STREAK_COUNT = intPreferencesKey("streak_count")
    }

    val settingsFlow: Flow<SettingsUiState> =
        context.dataStore.data.map { prefs ->
            SettingsUiState(
                themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: ThemeMode.System,
                clockAnimationEnabled = prefs[Keys.CLOCK_ANIMATION_ENABLED] ?: true,
                immersiveAnimationsEnabled = prefs[Keys.IMMERSIVE_ANIMATIONS_ENABLED] ?: true,
                use24Hour = prefs[Keys.USE_24_HOUR] ?: true,
                navOrder = prefs[Keys.NAV_ORDER]?.split(",")?.filter { it.isNotBlank() } ?: SettingsUiState.DEFAULT_NAV_ORDER,
                hapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: false,
                keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                isAppLockEnabled = prefs[Keys.APP_LOCK_ENABLED] ?: false,
                isDiagnosticsEnabled = prefs[Keys.DIAGNOSTICS_ENABLED] ?: true,
                currentUserEmail = prefs[Keys.CURRENT_USER_EMAIL] ?: "",
                lastLoginDate = prefs[Keys.LAST_LOGIN_DATE] ?: "",
                streakCount = prefs[Keys.STREAK_COUNT] ?: 0
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setClockAnimationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CLOCK_ANIMATION_ENABLED] = enabled }
    }

    suspend fun setImmersiveAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IMMERSIVE_ANIMATIONS_ENABLED] = enabled }
    }

    suspend fun setUse24Hour(use24Hour: Boolean) {
        context.dataStore.edit { it[Keys.USE_24_HOUR] = use24Hour }
    }

    suspend fun setNavOrder(navOrder: List<String>) {
        context.dataStore.edit { it[Keys.NAV_ORDER] = navOrder.joinToString(",") }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setCurrentUserEmail(email: String) {
        context.dataStore.edit { it[Keys.CURRENT_USER_EMAIL] = email }
    }

    suspend fun setLastLoginDate(date: String) {
        context.dataStore.edit { it[Keys.LAST_LOGIN_DATE] = date }
    }

    suspend fun setStreakCount(count: Int) {
        context.dataStore.edit { it[Keys.STREAK_COUNT] = count }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setDiagnosticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DIAGNOSTICS_ENABLED] = enabled }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}

