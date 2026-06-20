package com.example.timedrop.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.timedrop.ui.screens.calendar.CalendarRoute
import com.example.timedrop.ui.screens.home.HomeRoute
import com.example.timedrop.ui.screens.music.MusicRoute
import com.example.timedrop.ui.screens.pomodoro.PomodoroRoute
import com.example.timedrop.ui.screens.settings.SettingsRoute
import com.example.timedrop.ui.screens.settings.SettingsUiState
import com.example.timedrop.ui.screens.settings.ThemeMode
import com.example.timedrop.ui.screens.stopwatch.StopwatchRoute
import com.example.timedrop.ui.screens.worldclock.WorldClockRoute
import com.example.timedrop.ui.screens.auth.SignUpRoute
import com.example.timedrop.ui.screens.auth.LoginRoute

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timedrop.ui.screens.settings.SettingsViewModel
import com.example.timedrop.ui.screens.calendar.EventViewModel
import com.example.timedrop.ui.screens.notes.NotesViewModel
import com.example.timedrop.ui.screens.notes.NotesRoute
import com.example.timedrop.ui.screens.notes.NoteEditorRoute
import com.example.timedrop.ui.screens.monitoring.MonitoringRoute
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import com.example.timedrop.ui.components.profile.GlobalProfileHub

// ── Design palette for BottomNav ──
private val Lavender = Color(0xFFA5A5FF)
private val Orchid = Color(0xFFD277FF)
private val Slate = Color(0xFFADAAAA)
private val SurfaceContainer = Color(0xFF1A1A1A)

@Composable
fun TimeDropNavGraph(
    settingsViewModel: SettingsViewModel,
    eventViewModel: EventViewModel,
    settings: SettingsUiState,
    onToggleClockAnimation: (Boolean) -> Unit,
    onToggleImmersiveAnimations: (Boolean) -> Unit,
    onSetUse24Hour: (Boolean) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetHapticEnabled: (Boolean) -> Unit,
    onSetKeepScreenOn: (Boolean) -> Unit,
    onSetNotificationsEnabled: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    onUploadToCloud: () -> Unit,
    onDownloadFromCloud: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val pomodoroViewModel: com.example.timedrop.ui.screens.pomodoro.PomodoroViewModel = viewModel()
    val musicViewModel: com.example.timedrop.ui.screens.music.MusicViewModel = viewModel()
    val notesViewModel: NotesViewModel = viewModel()

    val onSignOutAndNavigate = {
        onSignOut()
        navController.navigate(TimeDropDestination.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    val startDestination = if (settings.currentUserEmail.isNotEmpty()) {
        "main_container"
    } else {
        TimeDropDestination.SignUp.route
    }

    val pagerState = rememberPagerState(pageCount = { 
        settings.navOrder.size.coerceAtLeast(1) 
    })
    val scope = rememberCoroutineScope()

    // Sync Pager selection with NavController (virtual destinations)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // When pager changes, if it was caused by a swipe, we might want to update some state
    // but we'll primarily rely on the pagerState.currentPage to define what's visible.

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable("main_container") {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true,
                    key = { index -> settings.navOrder.getOrNull(index) ?: index }
                ) { pageIndex ->
                    val route = settings.navOrder.getOrNull(pageIndex) ?: return@HorizontalPager
                    when (route) {
                        TimeDropDestination.Home.route -> {
                            HomeRoute(
                                eventViewModel = eventViewModel,
                                pomodoroViewModel = pomodoroViewModel,
                                musicViewModel = musicViewModel,
                                animationEnabled = settings.clockAnimationEnabled,
                                use24Hour = settings.use24Hour,
                                onNavigatePomodoro = { scope.launch { pagerState.animateScrollToPage(settings.navOrder.indexOf(TimeDropDestination.Pomodoro.route)) } },
                                onNavigateStopwatch = { scope.launch { pagerState.animateScrollToPage(settings.navOrder.indexOf(TimeDropDestination.Stopwatch.route)) } },
                                onNavigateWorldClock = { scope.launch { pagerState.animateScrollToPage(settings.navOrder.indexOf(TimeDropDestination.WorldClock.route)) } },
                                onNavigateMusic = { scope.launch { pagerState.animateScrollToPage(settings.navOrder.indexOf(TimeDropDestination.Music.route)) } },
                                onNavigateSettings = { scope.launch { pagerState.animateScrollToPage(settings.navOrder.indexOf(TimeDropDestination.Settings.route)) } },
                                currentUserEmail = settings.currentUserEmail
                            )
                        }
                        TimeDropDestination.Calendar.route -> CalendarRoute(eventViewModel = eventViewModel)
                        TimeDropDestination.WorldClock.route -> WorldClockRoute(onBack = { scope.launch { pagerState.animateScrollToPage(0) } })
                        TimeDropDestination.Music.route -> MusicRoute(onBack = { scope.launch { pagerState.animateScrollToPage(0) } }, viewModel = musicViewModel)
                        TimeDropDestination.Notes.route -> {
                            NotesRoute(
                                viewModel = notesViewModel,
                                onNavigateToEditor = { noteId ->
                                    navController.navigate(TimeDropDestination.NoteEditor.createRoute(noteId))
                                }
                            )
                        }
                        TimeDropDestination.Pomodoro.route -> {
                            PomodoroRoute(
                                onBack = { scope.launch { pagerState.animateScrollToPage(0) } }, 
                                viewModel = pomodoroViewModel,
                                animationsEnabled = settings.immersiveAnimationsEnabled,
                                onNavigateMusic = { scope.launch { pagerState.animateScrollToPage(settings.navOrder.indexOf(TimeDropDestination.Music.route)) } }
                            )
                        }
                        TimeDropDestination.Stopwatch.route -> StopwatchRoute(
                            onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                            animationsEnabled = settings.immersiveAnimationsEnabled
                        )
                        TimeDropDestination.Settings.route -> {
                            SettingsRoute(
                                state = settings,
                                onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                                onToggleClockAnimation = onToggleClockAnimation,
                                onToggleImmersiveAnimations = onToggleImmersiveAnimations,
                                onSetUse24Hour = onSetUse24Hour,
                                onSetThemeMode = onSetThemeMode,
                                onSetHapticEnabled = onSetHapticEnabled,
                                onSetKeepScreenOn = onSetKeepScreenOn,
                                onSetNotificationsEnabled = onSetNotificationsEnabled,
                                onSetAppLockEnabled = { settingsViewModel.setAppLockEnabled(it) },
                                onSetDiagnosticsEnabled = { settingsViewModel.setDiagnosticsEnabled(it) },
                                onClearAppHistory = { settingsViewModel.clearAppHistory() },
                                onSignOut = onSignOutAndNavigate,
                                onSetAdminModeEnabled = { settingsViewModel.setAdminModeEnabled(it) },
                                onSetAutoSyncEnabled = { settingsViewModel.setAutoSyncEnabled(it) },
                                onUploadToCloud = onUploadToCloud,
                                onDownloadFromCloud = onDownloadFromCloud
                            )
                        }
                    }
                }
            }

            // Detail Screens (Non-swipable)
            composable(TimeDropDestination.Pomodoro.route) { 
                PomodoroRoute(
                    onBack = { navController.popBackStack() }, 
                    viewModel = pomodoroViewModel,
                    animationsEnabled = settings.immersiveAnimationsEnabled,
                    onNavigateMusic = { navController.navigate(TimeDropDestination.Music.route) }
                ) 
            }
            composable(TimeDropDestination.Stopwatch.route) { 
                StopwatchRoute(
                    onBack = { navController.popBackStack() },
                    animationsEnabled = settings.immersiveAnimationsEnabled
                ) 
            }
            composable(TimeDropDestination.Settings.route) {
                SettingsRoute(
                    state = settings,
                    onBack = { navController.popBackStack() },
                    onToggleClockAnimation = onToggleClockAnimation,
                    onToggleImmersiveAnimations = onToggleImmersiveAnimations,
                    onSetUse24Hour = onSetUse24Hour,
                    onSetThemeMode = onSetThemeMode,
                    onSetHapticEnabled = onSetHapticEnabled,
                    onSetKeepScreenOn = onSetKeepScreenOn,
                    onSetNotificationsEnabled = onSetNotificationsEnabled,
                    onSetAppLockEnabled = { settingsViewModel.setAppLockEnabled(it) },
                    onSetDiagnosticsEnabled = { settingsViewModel.setDiagnosticsEnabled(it) },
                    onClearAppHistory = { settingsViewModel.clearAppHistory() },
                    onSignOut = onSignOutAndNavigate,
                    onSetAdminModeEnabled = { settingsViewModel.setAdminModeEnabled(it) },
                    onSetAutoSyncEnabled = { settingsViewModel.setAutoSyncEnabled(it) },
                    onUploadToCloud = onUploadToCloud,
                    onDownloadFromCloud = onDownloadFromCloud,
                    onNavigateToMonitoring = { navController.navigate(TimeDropDestination.Monitoring.route) }
                )
            }
            composable(TimeDropDestination.SignUp.route) {
                SignUpRoute(
                    onNavigateToLogin = { navController.navigate(TimeDropDestination.Login.route) },
                    onSignUpSuccess = { 
                        navController.navigate("main_container") {
                            popUpTo(TimeDropDestination.SignUp.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(TimeDropDestination.Login.route) {
                LoginRoute(
                    onNavigateToSignUp = { navController.navigate(TimeDropDestination.SignUp.route) },
                    onLoginSuccess = { 
                        navController.navigate("main_container") {
                            popUpTo(TimeDropDestination.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(TimeDropDestination.NoteEditor.route) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: 0
                NoteEditorRoute(
                    noteId = noteId,
                    viewModel = notesViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(TimeDropDestination.Monitoring.route) {
                MonitoringRoute(onBack = { navController.popBackStack() })
            }
        }

        // Global Bottom Nav Bar (Floating at the bottom)
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val currentRoute = currentDestination?.route

        val navigateTo = { route: String ->
            val pageIndex = settings.navOrder.indexOf(route)
            if (currentRoute == "main_container" && pageIndex != -1) {
                scope.launch {
                    pagerState.animateScrollToPage(pageIndex)
                }
                Unit
            } else {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }


        var isBottomBarVisible by remember { mutableStateOf(true) }
        val isAuthRoute = currentRoute == TimeDropDestination.SignUp.route || 
                         currentRoute == TimeDropDestination.Login.route

        if (!isAuthRoute) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Toggle Handle
                IconButton(
                    onClick = { isBottomBarVisible = !isBottomBarVisible },
                    modifier = Modifier
                        .background(
                            color = SurfaceContainer.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .size(width = 48.dp, height = 32.dp)
                ) {
                    Icon(
                        imageVector = if (isBottomBarVisible) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Toggle Navigation",
                        tint = Lavender
                    )
                }

                AnimatedVisibility(
                    visible = isBottomBarVisible,
                    enter = slideInVertically(initialOffsetY = { it }), // Slide up from bottom
                    exit = slideOutVertically(targetOffsetY = { it })   // Slide down to bottom
                ) {
                    val effectiveRoute = if (currentRoute == "main_container") {
                        settings.navOrder.getOrNull(pagerState.currentPage) ?: currentRoute
                    } else currentRoute

                    ReorderableBottomBar(
                        currentRoute = effectiveRoute,
                        navOrder = settings.navOrder,
                        onOrderChanged = { newOrder -> settingsViewModel.setNavOrder(newOrder) },
                        onNavigate = navigateTo
                    )
                }
            }
        }

        // Global Profile Hub Overlay
        val isProfileVisible = !isAuthRoute && 
                              currentRoute != TimeDropDestination.Settings.route &&
                              currentRoute?.startsWith(TimeDropDestination.NoteEditor.route.split("/")[0]) == false
        if (isProfileVisible) {
            GlobalProfileHub(
                currentUserEmail = settings.currentUserEmail,
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding()
            )
        }
    }
}
