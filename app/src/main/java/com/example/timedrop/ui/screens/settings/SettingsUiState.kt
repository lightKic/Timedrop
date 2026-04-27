package com.example.timedrop.ui.screens.settings

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.System,
    val clockAnimationEnabled: Boolean = true,
    val immersiveAnimationsEnabled: Boolean = true,
    val use24Hour: Boolean = true,
    val navOrder: List<String> = DEFAULT_NAV_ORDER,
    val hapticEnabled: Boolean = false,
    val keepScreenOn: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val isAppLockEnabled: Boolean = false,
    val isDiagnosticsEnabled: Boolean = true,
    val currentUserEmail: String = "",
    val lastLoginDate: String = "",
    val streakCount: Int = 0
) {
    companion object {
        val DEFAULT_NAV_ORDER = listOf(
            "home",
            "calendar",
            "stopwatch",
            "pomodoro",
            "music",
            "world_clock",
            "notes",
            "settings"
        )
    }
}

