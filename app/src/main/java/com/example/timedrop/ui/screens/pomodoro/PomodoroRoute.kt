package com.example.timedrop.ui.screens.pomodoro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max
import com.example.timedrop.services.MusicManager
import com.example.timedrop.util.NotificationHelper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextDecoration

// Design palette is now dynamic from MaterialTheme.colorScheme in the composable

@Composable
fun PomodoroRoute(
    onBack: () -> Unit,
    viewModel: PomodoroViewModel,
    animationsEnabled: Boolean,
    onNavigateMusic: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val musicTrack by MusicManager.currentTrack.collectAsState()
    val tasks by viewModel.uncompletedTasks.collectAsState()
    
    var showTaskPicker by remember { mutableStateOf(false) }
    
    val totalSeconds = uiState.totalSeconds
    val remaining = uiState.remainingSeconds

    val colors = MaterialTheme.colorScheme
    val Lavender = colors.primary
    val Orchid = colors.secondary
    val BlueTertiary = colors.tertiary
    val Slate = Color(0xFFADAAAA)
    val ringTrack = colors.surfaceVariant
    val surfaceContainer = colors.surface
    val onBg = colors.onBackground
    val running = uiState.isRunning
    
    // ── Flow State Animations ──
    val infiniteTransition = rememberInfiniteTransition(label = "FlowState")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreatheScale"
    )
    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreatheAlpha"
    )
    val sessionCount = uiState.completedWorkSessions
    val progress = if (totalSeconds > 0) remaining.toFloat() / totalSeconds.toFloat() else 0f
    
    val SurfaceHigh = colors.surfaceVariant
    val SurfaceLow = colors.surfaceContainerLow
    val LavenderDim = colors.primary.copy(alpha = 0.7f)
    val OnPrimaryDark = colors.onPrimary
    val Primary = colors.primary

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(remaining) {
        if (remaining == 0 && !running && sessionCount > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            NotificationHelper.showNotification(
                context,
                "Session Complete!",
                "Great job! Time for a well-deserved break."
            )
        }
    }

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val landscape = maxWidth > maxHeight
        
        // ── Ambient glows (fade in light mode) ──
        val glowAlpha = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) 0.05f else 0.02f
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .blur(120.dp)
                .background(Lavender.copy(alpha = glowAlpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(if (maxWidth > maxHeight) 200.dp else 300.dp)
                .align(Alignment.BottomEnd)
                .blur(100.dp)
                .background(Orchid.copy(alpha = glowAlpha), CircleShape)
        )

        @Composable
        fun Header() {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // ── Top bar ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Slate)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Pomodoro",
                        style = TextStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            brush = Brush.linearGradient(listOf(Lavender, Orchid)),
                        ),
                    )
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.size(48.dp))
                }

                Spacer(Modifier.height(20.dp))

                // ── Focus label pill ──
                Surface(shape = RoundedCornerShape(999.dp), color = SurfaceHigh) {
                    val modeLabel = when(uiState.currentMode) {
                        PomodoroMode.WORK -> "FOCUS SESSION ${uiState.completedWorkSessions % 4 + 1} / 4"
                        PomodoroMode.SHORT_BREAK -> "SHORT BREAK"
                        PomodoroMode.LONG_BREAK -> "LONG BREAK"
                    }
                    Text(
                        modeLabel,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp, color = LavenderDim),
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── Session title ──
                Text(
                    uiState.selectedTask?.title ?: "Creative Session",
                    style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = colors.onBackground, letterSpacing = (-0.5).sp),
                    maxLines = 1
                )
            }
        }

        @Composable
        fun TimerSection(size: androidx.compose.ui.unit.Dp = 280.dp) {
            Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
                // Flow Glow
                if (running && animationsEnabled) {
                    Box(
                        Modifier
                            .size(size * breatheScale)
                            .blur(40.dp)
                            .background(Brush.radialGradient(listOf(Lavender.copy(alpha = breatheAlpha), Color.Transparent)), CircleShape)
                    )
                }

                GradientProgressRing(progress = progress, modifier = Modifier.fillMaxSize())
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val mm = remaining / 60
                    val ss = remaining % 60
                    Text(
                        text = "%02d:%02d".format(mm, ss),
                        style = TextStyle(fontSize = if (size < 250.dp) 56.sp else 72.sp, fontWeight = FontWeight.ExtraBold, color = colors.onBackground, letterSpacing = (-3).sp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "REMAINING",
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 3.sp, color = Slate),
                    )
                }
            }
        }

        @Composable
        fun ControlsAndStats() {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // ── Controls ──
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    ControlButton(
                        icon = { Icon(Icons.Filled.Refresh, "Reset", tint = Slate, modifier = Modifier.size(22.dp)) },
                        label = "RESET",
                        onClick = { viewModel.resetTimer() },
                    )
                    Spacer(Modifier.width(28.dp))
                    Surface(
                        onClick = { viewModel.toggleTimer() },
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier.size(76.dp),
                        shadowElevation = 12.dp,
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Lavender, Orchid)), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(imageVector = if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = if (running) "Pause" else "Start", tint = OnPrimaryDark, modifier = Modifier.size(36.dp))
                        }
                    }
                    Spacer(Modifier.width(28.dp))
                    ControlButton(
                        icon = { Icon(Icons.AutoMirrored.Filled.List, "Tasks", tint = if (uiState.selectedTask != null) Primary else Slate, modifier = Modifier.size(22.dp)) },
                        label = "TASKS",
                        onClick = { showTaskPicker = true },
                    )
                }

                Spacer(Modifier.height(32.dp))

                // ── Bento info cards ──
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BentoCard(
                        icon = { Icon(Icons.Filled.LocalFireDepartment, null, tint = Orchid) },
                        label = "SESSION STREAK",
                        value = "$sessionCount Sessions",
                        color = SurfaceLow,
                        modifier = Modifier.weight(1f),
                    )
                    BentoCard(
                        icon = { Icon(Icons.Filled.MusicNote, null, tint = BlueTertiary) },
                        label = if (musicTrack != null) "NOW PLAYING" else "AMBIENT SOUND",
                        value = musicTrack?.title ?: "Lo-Fi Rain",
                        color = SurfaceHigh,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateMusic
                    )
                }
            }
        }

        if (maxWidth > maxHeight) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.Center) {
                    TimerSection(size = 240.dp)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center
                ) {
                    Header()
                    Spacer(Modifier.height(32.dp))
                    ControlsAndStats()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Header()
                Spacer(Modifier.weight(1f))
                TimerSection()
                Spacer(Modifier.weight(1f))
                ControlsAndStats()
            }
        }
    }

    if (showTaskPicker) {
        AlertDialog(
            onDismissRequest = { showTaskPicker = false },
            title = { Text("Select Focus Task", color = colors.onSurface) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    item {
                        Surface(
                            onClick = { 
                                viewModel.selectTask(null)
                                showTaskPicker = false
                            },
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("None (General Session)", color = Slate, modifier = Modifier.padding(16.dp))
                        }
                    }
                    items(tasks) { task ->
                        Surface(
                            onClick = { 
                                viewModel.selectTask(task)
                                showTaskPicker = false
                            },
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(12.dp).background(Color(task.colorArgb), CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(task.title, color = colors.onSurface, fontWeight = FontWeight.Bold)
                                    Text(task.time, color = Slate, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTaskPicker = false }) {
                    Text("Close", color = Lavender)
                }
            },
            containerColor = SurfaceHigh,
            titleContentColor = colors.onSurface
        )
    }
}

// ── Sub-composables ──

@Composable
private fun ControlButton(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    val surfaceContainer = MaterialTheme.colorScheme.surfaceVariant
    val Slate = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = surfaceContainer,
            modifier = Modifier.size(48.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { icon() }
        }
        Text(
            label,
            style = TextStyle(
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = Slate,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun BentoCard(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val Slate = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = color,
        modifier = modifier.height(120.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            icon()
            Column {
                Text(
                    label,
                    style = TextStyle(
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = Slate,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    value,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                    ),
                )
            }
        }
    }
}

@Composable
private fun GradientProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val sweepAngle = progress * 360f
    val ringTrack = MaterialTheme.colorScheme.surfaceVariant
    val Orchid = MaterialTheme.colorScheme.secondary
    val Lavender = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val strokePx = 6.dp.toPx()
        val trackStrokePx = 4.dp.toPx()
        val radius = (size.minDimension - strokePx) / 2f
        val topLeft = Offset(
            (size.width - radius * 2f) / 2f,
            (size.height - radius * 2f) / 2f,
        )
        val arcSize = Size(radius * 2f, radius * 2f)

        // Track circle
        drawCircle(
            color = ringTrack,
            radius = radius,
            style = Stroke(width = trackStrokePx),
        )

        // Progress arc with gradient
        if (sweepAngle > 0f) {
            drawArc(
                brush = Brush.sweepGradient(
                    colorStops = arrayOf(
                        0.0f to Orchid,
                        0.5f to Lavender,
                        1.0f to Orchid,
                    ),
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
    }
}
