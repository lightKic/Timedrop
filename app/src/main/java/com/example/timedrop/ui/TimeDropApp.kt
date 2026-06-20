package com.example.timedrop.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timedrop.ui.navigation.TimeDropNavGraph
import com.example.timedrop.ui.screens.calendar.EventViewModel
import com.example.timedrop.ui.screens.music.NowPlayingBubbleHost
import com.example.timedrop.ui.screens.settings.SettingsViewModel
import com.example.timedrop.ui.theme.TimeDropTheme

@Composable
fun TimeDropApp() {
    val settingsVm: SettingsViewModel = viewModel()
    val eventVm: EventViewModel = viewModel()
    val settings by settingsVm.uiState.collectAsState()

    TimeDropTheme(
        themeMode = settings.themeMode,
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize()) {
                TimeDropNavGraph(
                    settingsViewModel = settingsVm,
                    eventViewModel = eventVm,
                    settings = settings,
                    onToggleClockAnimation = settingsVm::setClockAnimationEnabled,
                    onToggleImmersiveAnimations = settingsVm::setImmersiveAnimationsEnabled,
                    onSetUse24Hour = settingsVm::setUse24Hour,
                    onSetThemeMode = settingsVm::setThemeMode,
                    onSetHapticEnabled = settingsVm::setHapticEnabled,
                    onSetKeepScreenOn = settingsVm::setKeepScreenOn,
                    onSetNotificationsEnabled = settingsVm::setNotificationsEnabled,
                    onSignOut = settingsVm::signOut,
                    onUploadToCloud = settingsVm::syncAllToCloud,
                    onDownloadFromCloud = settingsVm::syncAllFromCloud,
                )

                NowPlayingBubbleHost()
            }
        }
    }
}

