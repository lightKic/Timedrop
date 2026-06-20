package com.example.timedrop.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.ChronoUnit
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.blur
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakDetailsScreen(
    currentStreak: Int,
    longestStreak: Int,
    isAdminMode: Boolean = false,
    onSetStreak: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    val surface = Color(0xFF0E0E0E)
    val primary = Color(0xFFA5A5FF)
    val secondary = Color(0xFFD277FF)
    val onSurface = Color.White
    val onSurfaceVariant = Color(0xFFADAAAA)
    val surfaceContainer = Color(0xFF1A1A1A)
    val surfaceContainerLow = Color(0xFF131313)
    val surfaceContainerHighest = Color(0xFF262626)

    val today = LocalDate.now()
    val startOfWeek = today.with(DayOfWeek.MONDAY)
    val days = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    
    val quotes = listOf(
        "\"La felicidad de tu vida depende de la calidad de tus pensamientos.\"\n— Marco Aurelio",
        "\"Tienes poder sobre tu mente, no sobre los eventos externos. Date cuenta de esto y encontrarás fuerza.\"\n— Marco Aurelio",
        "\"Ya no pierdas el tiempo discutiendo sobre lo que debe ser un buen hombre. Sé uno.\"\n— Marco Aurelio",
        "\"La mejor venganza es no parecerte a quien te hizo daño.\"\n— Marco Aurelio",
        "\"Nunca se logró nada grandioso sin peligro.\"\n— Nicolás Maquiavelo",
        "\"Es mucho más seguro ser temido que amado, si es que no puedes ser ambas cosas.\"\n— Nicolás Maquiavelo",
        "\"Donde la voluntad es grande, las dificultades no pueden serlo.\"\n— Nicolás Maquiavelo",
        "\"No hay otra forma de protegerte de la adulación que haciendo entender a los demás que decirte la verdad no te ofende.\"\n— Nicolás Maquiavelo"
    )
    val dayIndex = (today.toEpochDay() % quotes.size).toInt()
    val quoteOfTheDay = quotes[dayIndex]

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(surface)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.HourglassTop, contentDescription = null, tint = primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "TimeDrop",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.horizontalGradient(listOf(primary, secondary))
                        )
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var showAdminDialog by remember { mutableStateOf(false) }
                    
                    if (isAdminMode) {
                        IconButton(
                            onClick = { showAdminDialog = true },
                            modifier = Modifier.background(surfaceContainerHighest, CircleShape).size(40.dp)
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = "Admin Set Streak", tint = Color(0xFFFFB300))
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    
                    if (showAdminDialog) {
                        var streakInput by remember { mutableStateOf(currentStreak.toString()) }
                        AlertDialog(
                            onDismissRequest = { showAdminDialog = false },
                            containerColor = surfaceContainer,
                            titleContentColor = onSurface,
                            textContentColor = onSurfaceVariant,
                            title = { Text("Set Streak (Admin)") },
                            text = {
                                OutlinedTextField(
                                    value = streakInput,
                                    onValueChange = { streakInput = it },
                                    label = { Text("Streak Days") },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primary,
                                        focusedLabelColor = primary,
                                        unfocusedBorderColor = surfaceContainerHighest,
                                        unfocusedLabelColor = onSurfaceVariant,
                                        cursorColor = primary,
                                        focusedTextColor = onSurface,
                                        unfocusedTextColor = onSurface
                                    )
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    val newStreak = streakInput.toIntOrNull() ?: currentStreak
                                    onSetStreak(newStreak)
                                    showAdminDialog = false
                                }) {
                                    Text("Set", color = primary, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAdminDialog = false }) {
                                    Text("Cancel", color = onSurfaceVariant)
                                }
                            }
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(surfaceContainerHighest, CircleShape).size(40.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = onSurfaceVariant)
                    }
                }
            }

            val StatsBlock: @Composable () -> Unit = {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Current
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(surfaceContainer, RoundedCornerShape(32.dp))
                            .border(2.dp, primary, RoundedCornerShape(32.dp))
                            .padding(24.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, tint = primary, modifier = Modifier.size(32.dp))
                            Text("CURRENT", color = onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("$currentStreak", color = onSurface, fontSize = 48.sp, fontWeight = FontWeight.Black)
                        Text("Day Streak", color = onSurfaceVariant, fontSize = 14.sp)
                    }
                    // Best
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(surfaceContainerLow, RoundedCornerShape(32.dp))
                            .padding(24.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = secondary, modifier = Modifier.size(32.dp))
                            Text("BEST", color = onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                        val bestStreak = maxOf(longestStreak, currentStreak)
                        Text("$bestStreak", color = onSurface, fontSize = 48.sp, fontWeight = FontWeight.Black)
                        Text("Longest Streak", color = onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            }

            val QuoteBlock: @Composable () -> Unit = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceContainer, RoundedCornerShape(32.dp))
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida/ADBb0uh-gEu9qLS-z0uz3GuEoNgXOBqgoZcwDYcQXwmBZR1o9PSO2h_7lL3GRUXrYeIUhuDhQ43sNPFWTymLFdnCSors0MKNReCm8TT7jmJ9S4_6-7goD2pjKg86W3ZhiuxlgjTe_doa3IYOURHqyJFe58fHcJgzNRwlpljn1PVSnijESOursL5Qfn1UgxExMcImxATz_XAjwLMGZAdK5R_uJ8eV3YCIbh39SD3iedfMIhVs5oXeHpB8G_jqiA",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).alpha(0.5f)
                    )
                    Spacer(Modifier.width(24.dp))
                    Text(
                        quoteOfTheDay,
                        color = onSurfaceVariant,
                        fontSize = 14.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val WeeklyProgressBlock: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Column {
                            Text("Weekly Progress", color = onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text("Current Week", color = onSurfaceVariant, fontSize = 14.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(surfaceContainerHighest, CircleShape).padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(primary, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("Completed", color = onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        days.forEach { date ->
                            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3)
                            val isPastOrToday = !date.isAfter(today)
                            val daysAgo = ChronoUnit.DAYS.between(date, today).toInt()
                            val isChecked = isPastOrToday && (daysAgo < currentStreak)
                            val isToday = date == today

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (isChecked) Brush.linearGradient(listOf(primary, secondary))
                                            else if (isToday && !isChecked) androidx.compose.ui.graphics.SolidColor(Color.Transparent)
                                            else androidx.compose.ui.graphics.SolidColor(surfaceContainerHighest),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            if (isToday && !isChecked) 2.dp else 0.dp,
                                            if (isToday && !isChecked) primary else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChecked) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = onSurface, modifier = Modifier.size(20.dp))
                                    } else if (isToday) {
                                        Box(modifier = Modifier.size(8.dp).background(primary, CircleShape))
                                    } else {
                                        Icon(Icons.Filled.HourglassEmpty, contentDescription = null, tint = onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(dayName.uppercase(), color = if (isToday) primary else if (isChecked) onSurface else onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 88.dp) // Below header
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        WeeklyProgressBlock()
                        StatsBlock()
                        QuoteBlock()
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        HeroFireHourglass()
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 88.dp) // Below header
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(48.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                        HeroFireHourglass()
                        StatsBlock()
                        QuoteBlock()
                    }
                    WeeklyProgressBlock()
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun HeroFireHourglass() {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -12f,
        animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOut), RepeatMode.Reverse),
        label = "float"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.015f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "pulse"
    )

    val burnScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1250, easing = EaseInOut), RepeatMode.Reverse),
        label = "burnScale"
    )
    val burnRotate by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1250, easing = EaseInOut), RepeatMode.Reverse),
        label = "burnRotate"
    )

    val fireMoveY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -20f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "fireMoveY"
    )
    val fireMoveScaleY by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "fireMoveScaleY"
    )

    val glowOpacity by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
        label = "glow"
    )

    var particles by remember { mutableStateOf(listOf<Particle>()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(80)
            if (particles.size < 40) {
                particles = particles + Particle(
                    x = Random.nextFloat(),
                    radius = Random.nextFloat() * 6f + 2f,
                    speed = Random.nextFloat() * 3f + 3f,
                    progress = 0f
                )
            }
        }
    }
    
    val frameTime = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while(true) {
            withFrameMillis { frameTime.value = it }
        }
    }
    
    LaunchedEffect(frameTime.value) {
        particles = particles.map { 
            it.copy(progress = it.progress + (0.016f / it.speed)) 
        }.filter { it.progress < 1f }
    }

    val primaryFire = Color(0xFF7D4DFF).copy(alpha = 0.85f)
    val secondaryFire = Color(0xFF67DBFF).copy(alpha = 0.95f)
    val tertiaryFire = Color(0xFFBC52FF).copy(alpha = 0.8f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f/4f)
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.radialGradient(listOf(Color(0xFF060816), Color.Black)))
            .padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = floatY.dp.toPx()
                },
            contentAlignment = Alignment.Center
        ) {
            LoopingVideoPlayer(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    },
                videoRes = com.example.timedrop.R.raw.logo_video
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF0E0E0E))))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(32.dp)
        ) {
            Text("ELITE PERFORMANCE", color = Color(0xFFA5A5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Stay Focused.", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

data class Particle(val x: Float, val radius: Float, val speed: Float, val progress: Float)

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun LoopingVideoPlayer(modifier: Modifier = Modifier, videoRes: Int) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val exoPlayer = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            val uri = android.net.Uri.parse("android.resource://${context.packageName}/$videoRes")
            val mediaItem = androidx.media3.common.MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
            volume = 0f // Mute video
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            androidx.media3.ui.PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier
    )
}
