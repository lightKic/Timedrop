package com.example.timedrop.ui.screens.stopwatch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.core.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance

// ── Design palette ──
private val Lavender = Color(0xFFA5A5FF)
private val LavenderDim = Color(0xFF9392FF)
// Design palette is now dynamic from MaterialTheme.colorScheme in the composable
private val OnPrimaryContainer = Color(0xFF10007F)
private val ErrorColor = Color(0xFFFF6E84)

@Composable
fun StopwatchRoute(onBack: () -> Unit, animationsEnabled: Boolean) {
    val vm: StopwatchViewModel = viewModel()
    val isRunning by vm.isRunning.collectAsState()
    val elapsedMs by vm.elapsedMs.collectAsState()
    val laps by vm.laps.collectAsState()

    val colors = MaterialTheme.colorScheme
    val Lavender = colors.primary
    val Orchid = colors.secondary
    val onBg = colors.onBackground
    val Slate = colors.onSurfaceVariant
    val surfaceContainer = colors.surfaceContainer
    val SurfaceHighest = colors.surfaceVariant
    val SurfaceHigh = colors.surfaceVariant
    val SurfaceLow = colors.surface
    val OnPrimaryContainer = colors.onPrimaryContainer

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(colors.background)) {
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
        
        val landscape = maxWidth > maxHeight
        
        if (landscape) {
            Row(
                modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Timer Ring
                Box(
                    modifier = Modifier.weight(1.2f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    StopwatchDisplay(elapsedMs, lavender = Lavender, orchid = Orchid, slate = Slate, lavenderDim = LavenderDim, displaySize = 230.dp, fontSize = 52.sp, animationsEnabled = animationsEnabled, isRunning = isRunning)
                }
                
                // Right Column: Controls and Laps
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 24.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(20.dp))
                    Text("Stopwatch", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    
                    Spacer(Modifier.weight(1f)) // This pushes the controls to the vertical center
                    
                    ControlsRow(isRunning, onReset = vm::reset, onToggle = vm::toggle, onLap = vm::lap, lavender = Lavender, orchid = Orchid, surfaceHighest = SurfaceHighest, onPrimaryContainer = OnPrimaryContainer)
                    
                    Spacer(Modifier.weight(1f)) // This maintains the controls in the center
                    
                    Column(Modifier.heightIn(max = 120.dp).verticalScroll(rememberScrollState())) {
                        LapsList(laps, slate = Slate, lavender = Lavender, orchid = Orchid, surfaceLow = SurfaceLow, surfaceHigh = SurfaceHigh)
                    }
                }
            }
            
            // Back button as overlay in landscape
            IconButton(onClick = onBack, modifier = Modifier.statusBarsPadding().padding(start = 16.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Slate)
            }
        } else {
            // Portrait layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
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
                    Text("TimeDrop", style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, brush = Brush.linearGradient(listOf(Lavender, Orchid))))
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.size(48.dp))
                }

                Text("Stopwatch", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White), modifier = Modifier.padding(bottom = 32.dp))

                StopwatchDisplay(elapsedMs, lavender = Lavender, orchid = Orchid, slate = Slate, lavenderDim = LavenderDim, animationsEnabled = animationsEnabled, isRunning = isRunning)

                Spacer(Modifier.height(48.dp))

                ControlsRow(isRunning, onReset = vm::reset, onToggle = vm::toggle, onLap = vm::lap, lavender = Lavender, orchid = Orchid, surfaceHighest = SurfaceHighest, onPrimaryContainer = OnPrimaryContainer)

                Spacer(Modifier.height(48.dp))

                LapsList(laps, slate = Slate, lavender = Lavender, orchid = Orchid, surfaceLow = SurfaceLow, surfaceHigh = SurfaceHigh)
            }
        }
    }
}

@Composable
private fun StopwatchDisplay(
    elapsedMs: Long,
    lavender: Color,
    orchid: Color,
    slate: Color,
    lavenderDim: Color,
    displaySize: androidx.compose.ui.unit.Dp = 280.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 64.sp,
    animationsEnabled: Boolean,
    isRunning: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerOffset"
    )

    Box(
        modifier = Modifier.size(displaySize),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring (mock)
        Box(modifier = Modifier.fillMaxSize().border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape))
        
        // Flow Shimmer
        if (isRunning && animationsEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .blur(20.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.Transparent, lavender.copy(alpha = 0.1f), Color.Transparent),
                            start = Offset(shimmerOffset - 500f, shimmerOffset - 500f),
                            end = Offset(shimmerOffset, shimmerOffset)
                        ),
                        CircleShape
                    )
            )
        }
        // Dashed border
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            drawCircle(color = lavender.copy(alpha = 0.2f), style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)))
        }
        // Gradient Progress Ring
        val progress = (elapsedMs % 60000) / 60000f
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val stroke = 6.dp.toPx()
            val radius = size.minDimension / 2 - stroke / 2
            
            // Subtle glow under the ring
            if (progress > 0) {
                drawArc(
                    brush = Brush.linearGradient(listOf(lavender.copy(alpha = 0.3f), orchid.copy(alpha = 0.3f))),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke * 1.5f, cap = StrokeCap.Round)
                )
                
                drawArc(
                    brush = Brush.linearGradient(listOf(lavender, orchid)),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        // Time Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val totalSec = elapsedMs / 1000
            val m = totalSec / 60
            val s = totalSec % 60
            val ms = (elapsedMs % 1000) / 10
            Text(buildAnnotatedString { append("%02d:%02d".format(m, s)); withStyle(SpanStyle(color = lavenderDim)) { append(".%02d".format(ms)) } }, style = TextStyle(fontSize = fontSize, fontWeight = FontWeight.ExtraLight, color = Color.White, letterSpacing = (-2).sp))
            Spacer(Modifier.height(8.dp))
            Text("CURRENT INTERVAL", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp, color = slate))
        }
    }
}

@Composable
private fun ControlsRow(isRunning: Boolean, onReset: () -> Unit, onToggle: () -> Unit, onLap: () -> Unit, lavender: Color, orchid: Color, surfaceHighest: Color, onPrimaryContainer: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(onClick = onReset, shape = CircleShape, color = surfaceHighest, modifier = Modifier.size(56.dp)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Refresh, "Reset", tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
        Surface(onClick = onToggle, shape = RoundedCornerShape(999.dp), color = Color.Transparent, modifier = Modifier.weight(1f).height(64.dp).padding(horizontal = 12.dp), shadowElevation = 16.dp) {
            Row(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(lavender, orchid))), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = onPrimaryContainer)
                Spacer(Modifier.width(8.dp))
                Text(if (isRunning) "PAUSE" else "START", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onPrimaryContainer, letterSpacing = 1.sp))
            }
        }
        Surface(onClick = onLap, shape = CircleShape, color = surfaceHighest, modifier = Modifier.size(56.dp)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Timer, "Lap", tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
private fun LapsList(laps: List<LapData>, slate: Color, lavender: Color, orchid: Color, surfaceLow: Color, surfaceHigh: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("RECORDED LAPS", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp, color = slate))
            Text("${laps.size} Laps", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, color = lavender))
        }
        Spacer(Modifier.height(16.dp))
        laps.forEach { lapData ->
            Surface(shape = RoundedCornerShape(20.dp), color = surfaceLow, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(12.dp)).background(surfaceHigh), contentAlignment = Alignment.Center) {
                            Text("%02d".format(lapData.lapNumber), style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (lapData.differenceMs < 0) orchid else slate))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(lapData.title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        val s = (lapData.splitTimeMs / 1000) % 60; val m = (lapData.splitTimeMs / 60000); val ms = (lapData.splitTimeMs % 1000) / 10
                        Text("%02d:%02d.%02d".format(m, s, ms), style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }
    }
}
