package com.example.timedrop.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.blur
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance

// Design palette is now dynamic from MaterialTheme.colorScheme in the composable

// Accent presets removed per user request

@Composable
fun SettingsRoute(
    state: SettingsUiState,
    onBack: () -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onToggleClockAnimation: (Boolean) -> Unit,
    onToggleImmersiveAnimations: (Boolean) -> Unit,
    onSetUse24Hour: (Boolean) -> Unit,
    onSetKeepScreenOn: (Boolean) -> Unit,
    onSetHapticEnabled: (Boolean) -> Unit,
    onSetNotificationsEnabled: (Boolean) -> Unit,
    onSetAppLockEnabled: (Boolean) -> Unit,
    onSetDiagnosticsEnabled: (Boolean) -> Unit,
    onClearAppHistory: () -> Unit,
    onSignOut: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val Lavender = colors.primary
    val Orchid = colors.secondary
    val BlueTertiary = colors.tertiary
    val Slate = Color(0xFFADAAAA)
    val surfaceContainer = colors.surface
    val surfaceHigh = colors.surfaceVariant
    val surfaceHighest = colors.surfaceVariant
    val onBg = colors.onBackground
    val errorColor = colors.error
    val errorContainer = colors.errorContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        var showPrivacyModal by remember { mutableStateOf(false) }
        var showClearDataDialog by remember { mutableStateOf(false) }
        // ── Ambient glow blurs ──
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .blur(120.dp)
                .background(Lavender.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .blur(100.dp)
                .background(Orchid.copy(alpha = 0.05f), CircleShape)
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Top bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Slate)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Settings",
                    style = TextStyle(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        brush = Brush.linearGradient(listOf(Lavender, Orchid)),
                    ),
                )
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(48.dp))
            }

            // ── Header ──
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp)
            ) {
                Text(
                    "Settings",
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onBackground,
                        letterSpacing = (-1).sp,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Personalize your digital sanctuary experience.",
                    style = TextStyle(fontSize = 15.sp, color = Slate),
                )
            }

            Spacer(Modifier.height(12.dp))

            // ══════════════════════════════════════════
            //  Visual Preferences
            // ══════════════════════════════════════════
            SectionLabel("Visual Preferences", Slate)

            Surface(
                shape = RoundedCornerShape(32.dp),
                color = surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Column {
                    // Dark Mode
                    ToggleSettingItem(
                        icon = Icons.Filled.DarkMode,
                        iconTint = Lavender,
                        title = "Dark Mode",
                        subtitle = "Deep obsidian interface for comfort",
                        checked = state.themeMode == ThemeMode.Dark || state.themeMode == ThemeMode.System,
                        trackColor = Lavender.copy(alpha = 0.30f),
                        thumbColor = Lavender,
                        onCheckedChange = { dark ->
                            onSetThemeMode(if (dark) ThemeMode.Dark else ThemeMode.Light)
                        },
                        surfaceHighest = surfaceHighest,
                        textColor = colors.onSurface
                    )

                    // Clock Animation
                    ToggleSettingItem(
                        icon = Icons.Filled.MotionPhotosOn,
                        iconTint = Orchid,
                        title = "Clock Animation",
                        subtitle = "Fluid motion for time transitions",
                        checked = state.clockAnimationEnabled,
                        trackColor = Orchid.copy(alpha = 0.25f),
                        thumbColor = Orchid,
                        onCheckedChange = onToggleClockAnimation,
                        surfaceHighest = surfaceHighest,
                        textColor = colors.onSurface
                    )

                    // Immersive Animations
                    ToggleSettingItem(
                        icon = Icons.Filled.AutoAwesome,
                        iconTint = colors.tertiary,
                        title = "Immersive Animations",
                        subtitle = "Breathtaking flow state visual effects",
                        checked = state.immersiveAnimationsEnabled,
                        trackColor = colors.tertiary.copy(alpha = 0.25f),
                        thumbColor = colors.tertiary,
                        onCheckedChange = onToggleImmersiveAnimations,
                        surfaceHighest = surfaceHighest,
                        textColor = colors.onSurface
                    )

                    // 24-Hour Format
                    ToggleSettingItem(
                        icon = Icons.Filled.AccessTime,
                        iconTint = BlueTertiary,
                        title = "24-Hour Format",
                        subtitle = "Use military time display",
                        checked = state.use24Hour,
                        trackColor = BlueTertiary.copy(alpha = 0.25f),
                        thumbColor = BlueTertiary,
                        onCheckedChange = onSetUse24Hour,
                        surfaceHighest = surfaceHighest,
                        textColor = colors.onSurface
                    )

                    // Keep Screen On
                    ToggleSettingItem(
                        icon = Icons.Filled.Vibration,
                        iconTint = colors.tertiary,
                        title = "Keep Screen On",
                        subtitle = "Prevent device from sleeping while app is open",
                        checked = state.keepScreenOn,
                        trackColor = colors.tertiary.copy(alpha = 0.25f),
                        thumbColor = colors.tertiary,
                        onCheckedChange = onSetKeepScreenOn,
                        surfaceHighest = surfaceHighest,
                        textColor = colors.onSurface
                    )

                    // Haptic Feedback
                    ToggleSettingItem(
                        icon = Icons.Filled.Vibration,
                        iconTint = Slate,
                        title = "Haptic Feedback",
                        subtitle = "Tactile response on key actions",
                        checked = state.hapticEnabled,
                        trackColor = Slate.copy(alpha = 0.25f),
                        thumbColor = Slate,
                        onCheckedChange = onSetHapticEnabled,
                        surfaceHighest = surfaceHighest,
                        textColor = colors.onSurface
                    )

                    // Notification Pulse
                    ToggleSettingItem(
                        icon = Icons.Filled.Notifications,
                        iconTint = Lavender,
                        title = "Notification Pulse",
                        subtitle = "Enable alerts for Pomodoro and events",
                        checked = state.notificationsEnabled,
                        trackColor = Lavender.copy(alpha = 0.25f),
                        thumbColor = Lavender,
                        onCheckedChange = onSetNotificationsEnabled,
                        surfaceHighest = surfaceHighest,
                        textColor = colors.onSurface
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ══════════════════════════════════════════
            //  Info Cards (Bento Grid)
            // ══════════════════════════════════════════
            SectionLabel("Privacy & Permissions", Slate)

            val context = androidx.compose.ui.platform.LocalContext.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InfoCard(
                    icon = Icons.Filled.Security,
                    iconTint = Lavender,
                    title = "Privacy",
                    subtitle = "Manage data and session settings.",
                    modifier = Modifier.weight(1f),
                    surfaceContainer = surfaceContainer,
                    surfaceHighest = surfaceHighest,
                    textColor = colors.onSurface,
                    subTextColor = Slate,
                    onClick = { showPrivacyModal = true }
                )
                InfoCard(
                    icon = Icons.Filled.Notifications,
                    iconTint = Orchid,
                    title = "System Permissions",
                    subtitle = "Manage notification access.",
                    modifier = Modifier.weight(1f),
                    surfaceContainer = surfaceContainer,
                    surfaceHighest = surfaceHighest,
                    textColor = colors.onSurface,
                    subTextColor = Slate,
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Danger zone ──
            Surface(
                onClick = onSignOut,
                shape = RoundedCornerShape(32.dp),
                color = errorContainer.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, errorContainer.copy(alpha = 0.20f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(errorContainer.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = errorColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Sign Out / Reset",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = errorColor),
                        )
                        Text(
                            "Clear all data and restore factory defaults",
                            style = TextStyle(fontSize = 12.sp, color = Slate.copy(alpha = 0.6f)),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Version
            Text(
                "TimeDrop v1.0.0",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                style = TextStyle(
                    fontSize = 11.sp,
                    color = Slate.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        // ── Privacy Modal ──
        if (showPrivacyModal) {
            PrivacyOverlay(
                state = state,
                onDismiss = { showPrivacyModal = false },
                onSetAppLockEnabled = onSetAppLockEnabled,
                onSetDiagnosticsEnabled = onSetDiagnosticsEnabled,
                onClearDataClick = { showClearDataDialog = true },
                colors = colors,
                surfaceContainer = surfaceContainer,
                surfaceHighest = surfaceHighest,
                Lavender = Lavender,
                Orchid = Orchid,
                Slate = Slate
            )
        }

        // ── Clear Data Confirmation ──
        if (showClearDataDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                containerColor = surfaceContainer,
                titleContentColor = colors.onSurface,
                textContentColor = Slate,
                title = {
                    Text(
                        "Clear All Data?",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    )
                },
                text = {
                    Text(
                        "This will permanently delete all your notes, tasks, and calendar events. This action cannot be undone.",
                        style = TextStyle(fontSize = 14.sp)
                    )
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            onClearAppHistory()
                            showClearDataDialog = false
                            showPrivacyModal = false
                        }
                    ) {
                        Text("Delete Everything", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showClearDataDialog = false }
                    ) {
                        Text("Cancel", color = Slate)
                    }
                }
            )
        }
    }
}

@Composable
private fun PrivacyOverlay(
    state: SettingsUiState,
    onDismiss: () -> Unit,
    onSetAppLockEnabled: (Boolean) -> Unit,
    onSetDiagnosticsEnabled: (Boolean) -> Unit,
    onClearDataClick: () -> Unit,
    colors: androidx.compose.material3.ColorScheme,
    surfaceContainer: Color,
    surfaceHighest: Color,
    Lavender: Color,
    Orchid: Color,
    Slate: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {}, // Prevent dismiss when clicking the card
            shape = RoundedCornerShape(32.dp),
            color = surfaceContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Security, null, tint = Lavender, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Privacy Settings",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                Text(
                    "Manage your data security and experience preferences.",
                    style = TextStyle(fontSize = 13.sp, color = Slate)
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Section: App Security
                SectionLabel("Security", Slate)
                ToggleSettingItem(
                    icon = Icons.Filled.Security,
                    iconTint = Orchid,
                    title = "App Lock",
                    subtitle = "Always require biometric unlock",
                    checked = state.isAppLockEnabled,
                    trackColor = Orchid.copy(alpha = 0.3f),
                    thumbColor = Orchid,
                    onCheckedChange = onSetAppLockEnabled,
                    surfaceHighest = surfaceHighest,
                    textColor = colors.onSurface
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Section: Data & Analytics
                SectionLabel("Data usage", Slate)
                ToggleSettingItem(
                    icon = Icons.Filled.AutoAwesome, // Repurposing as "Analytics" icon
                    iconTint = colors.tertiary,
                    title = "Anonymous Diagnostics",
                    subtitle = "Help us improve TimeDrop",
                    checked = state.isDiagnosticsEnabled,
                    trackColor = colors.tertiary.copy(alpha = 0.3f),
                    thumbColor = colors.tertiary,
                    onCheckedChange = onSetDiagnosticsEnabled,
                    surfaceHighest = surfaceHighest,
                    textColor = colors.onSurface
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Section: Danger Zone
                SectionLabel("Local data", Slate)
                Surface(
                    onClick = onClearDataClick,
                    shape = RoundedCornerShape(20.dp),
                    color = colors.errorContainer.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Reset App Content", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.error))
                            Text("Delete all notes and events", style = TextStyle(fontSize = 12.sp, color = colors.error.copy(alpha = 0.6f)))
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                // Privacy statement
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        "TimeDrop is designed with privacy at its core. Your data is stored locally on this device and is never uploaded to external servers.",
                        style = TextStyle(fontSize = 12.sp, color = Slate.copy(alpha = 0.8f), lineHeight = 18.sp)
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Close button
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(24.dp),
                    color = colors.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Done",
                        modifier = Modifier.padding(14.dp),
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

// ── Reusable composables ──

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(
        text.uppercase(),
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
        style = TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = color.copy(alpha = 0.6f),
        ),
    )
}

@Composable
private fun ToggleSettingItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    trackColor: Color,
    thumbColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    surfaceHighest: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(surfaceHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textColor),
            )
            Text(
                subtitle,
                style = TextStyle(fontSize = 13.sp, color = iconTint.copy(alpha = 0.7f)),
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = thumbColor,
                checkedTrackColor = trackColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = surfaceHighest,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    surfaceContainer: Color,
    surfaceHighest: Color,
    textColor: Color,
    subTextColor: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        color = surfaceContainer,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(surfaceHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(
                    title,
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = TextStyle(fontSize = 12.sp, color = subTextColor, lineHeight = 18.sp),
                )
            }
        }
    }
}
