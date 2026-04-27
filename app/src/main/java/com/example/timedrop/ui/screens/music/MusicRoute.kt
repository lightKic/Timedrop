package com.example.timedrop.ui.screens.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.foundation.horizontalScroll
import com.example.timedrop.services.AmbientSoundManager
import com.example.timedrop.services.AmbientSoundState
import com.example.timedrop.services.AmbientSoundType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.app.NotificationManagerCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance

@Composable
fun MusicRoute(
    onBack: () -> Unit,
    viewModel: MusicViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val currentTrack = uiState.currentTrack
    val upNext = uiState.upNext
    val isPlaying = uiState.isPlaying
    val progressFraction = uiState.progressFraction

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 1000), // Match VM ticker
        label = "MusicProgress"
    )

    val colors = MaterialTheme.colorScheme
    val Lavender = colors.primary
    val Orchid = colors.secondary
    val BlueTertiary = colors.tertiary
    val Slate = colors.onSurfaceVariant
    val surfaceContainer = colors.surface
    val surfaceHighest = colors.surfaceVariant
    val onBg = colors.onBackground
    val DeepBg = colors.background
    val Outline = colors.outline
    val OnPrimaryContainer = colors.onPrimaryContainer
    val SurfaceLow = colors.surfaceContainerLow
    val SurfaceHigh = colors.surfaceContainerHigh
    
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName))
    }
    
    val ambientSounds by AmbientSoundManager.sounds.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    val elapsedSec = currentTrack?.let { (progressFraction * it.durationSec).toInt() } ?: 0

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val landscape = maxWidth > maxHeight
        
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
            Modifier
                .size(if (landscape) 240.dp else 320.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .blur(120.dp)
                .background(Orchid.copy(alpha = 0.05f), CircleShape)
        )

        @Composable
        fun HeroSection() {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            ) {
                Text(
                    if (!hasPermission || currentTrack?.albumArt == null) "TIME TO FOCUS" else "NOW PLAYING",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Lavender),
                )
            }
        }

        @Composable
        fun PlayerCard(isLandscape: Boolean = false) {
            Surface(
                shape = RoundedCornerShape(40.dp),
                color = Color(0xFF2C2C2C).copy(alpha = 0.40f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Box {
                    Box(Modifier.size(200.dp).align(Alignment.TopEnd).offset(x = 60.dp, y = (-60).dp).blur(100.dp).background(Lavender.copy(alpha = 0.10f), CircleShape))
                    
                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                                Box(Modifier.size(160.dp).blur(24.dp).background(Brush.radialGradient(listOf(Lavender.copy(alpha = 0.25f), Orchid.copy(alpha = 0.15f), Color.Transparent)), CircleShape))
                                Box(modifier = Modifier.size(148.dp).clip(CircleShape).background(Brush.linearGradient(listOf(surfaceHighest, surfaceContainer))), contentAlignment = Alignment.Center) {
                                    Box(modifier = Modifier.size(136.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Lavender.copy(alpha = 0.3f), Orchid.copy(alpha = 0.2f), DeepBg))), contentAlignment = Alignment.Center) {
                                        if (currentTrack?.albumArt != null) {
                                            Image(bitmap = currentTrack.albumArt.asImageBitmap(), contentDescription = "Album Art", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            Icon(Icons.Filled.MusicNote, null, tint = Lavender.copy(alpha = 0.6f), modifier = Modifier.size(40.dp))
                                        }
                                    }
                                }
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(currentTrack?.title ?: "No Track", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(currentTrack?.artist ?: "", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp, color = Slate), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                
                                Spacer(Modifier.height(16.dp))
                                
                                Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(999.dp)).background(surfaceHighest)) {
                                    Box(Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().clip(RoundedCornerShape(999.dp)).background(Brush.horizontalGradient(listOf(Lavender, Orchid))))
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    IconButton(onClick = { viewModel.skipPrevious() }, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.SkipPrevious, "Prev", tint = Slate, modifier = Modifier.size(24.dp)) }
                                    Spacer(Modifier.width(16.dp))
                                    Surface(onClick = { viewModel.togglePlayPause() }, shape = CircleShape, color = Color.Transparent, modifier = Modifier.size(56.dp)) {
                                        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Lavender, Orchid)), CircleShape), contentAlignment = Alignment.Center) {
                                            Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = OnPrimaryContainer, modifier = Modifier.size(28.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    IconButton(onClick = { viewModel.skipNext() }, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.SkipNext, "Next", tint = Slate, modifier = Modifier.size(24.dp)) }
                                }

                                Spacer(Modifier.height(16.dp))
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.VolumeUp, null, tint = Lavender.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Slider(
                                        value = uiState.musicVolume,
                                        onValueChange = { viewModel.setMusicVolume(it) },
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color.White,
                                            activeTrackColor = Lavender,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.05f)
                                        ),
                                        modifier = Modifier.weight(1f).height(24.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 32.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                                    Box(Modifier.size(180.dp).blur(24.dp).background(Brush.radialGradient(listOf(Lavender.copy(alpha = 0.25f), Orchid.copy(alpha = 0.15f), Color.Transparent)), CircleShape))
                                    Box(modifier = Modifier.size(168.dp).clip(CircleShape).background(Brush.linearGradient(listOf(surfaceHighest, surfaceContainer))), contentAlignment = Alignment.Center) {
                                        Box(modifier = Modifier.size(152.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Lavender.copy(alpha = 0.3f), Orchid.copy(alpha = 0.2f), DeepBg))), contentAlignment = Alignment.Center) {
                                            if (currentTrack?.albumArt != null) {
                                                Image(bitmap = currentTrack.albumArt.asImageBitmap(), contentDescription = "Album Art", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            } else {
                                                Icon(Icons.Filled.MusicNote, null, tint = Lavender.copy(alpha = 0.6f), modifier = Modifier.size(48.dp))
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                Text(currentTrack?.title ?: "No Track", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White))
                                Spacer(Modifier.height(4.dp))
                                Text(currentTrack?.artist ?: "", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp, color = Slate))
                                Spacer(Modifier.height(28.dp))
                                Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(999.dp)).background(surfaceHighest)) {
                                    Box(Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().clip(RoundedCornerShape(999.dp)).background(Brush.horizontalGradient(listOf(Lavender, Orchid))))
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(formatTime(elapsedSec), style = TextStyle(fontSize = 10.sp, letterSpacing = 2.sp, color = Outline))
                                    Text(formatTime(currentTrack?.durationSec ?: 0), style = TextStyle(fontSize = 10.sp, letterSpacing = 2.sp, color = Outline))
                                }
                                Spacer(Modifier.height(24.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    IconButton(onClick = { viewModel.skipPrevious() }) { Icon(Icons.Filled.SkipPrevious, "Prev", tint = Slate, modifier = Modifier.size(32.dp)) }
                                    Spacer(Modifier.width(16.dp))
                                    Surface(onClick = { viewModel.togglePlayPause() }, shape = CircleShape, color = Color.Transparent, modifier = Modifier.size(76.dp), shadowElevation = 16.dp) {
                                        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Lavender, Orchid)), CircleShape), contentAlignment = Alignment.Center) {
                                            Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (isPlaying) "Pause" else "Play", tint = OnPrimaryContainer, modifier = Modifier.size(36.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    IconButton(onClick = { viewModel.skipNext() }) { Icon(Icons.Filled.SkipNext, "Next", tint = Slate, modifier = Modifier.size(32.dp)) }
                                }
                                Spacer(Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    IconButton(onClick = { }, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.Shuffle, "Shuffle", tint = Outline, modifier = Modifier.size(20.dp)) }
                                    IconButton(onClick = { }, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.Repeat, "Repeat", tint = Outline, modifier = Modifier.size(20.dp)) }
                                    IconButton(onClick = { }, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.Share, "Share", tint = Outline, modifier = Modifier.size(20.dp)) }
                                }

                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(0.9f).background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.VolumeUp, null, tint = Lavender.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Slider(
                                        value = uiState.musicVolume,
                                        onValueChange = { viewModel.setMusicVolume(it) },
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color.White,
                                            activeTrackColor = Lavender,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.05f)
                                        ),
                                        modifier = Modifier.weight(1f).height(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun QueueSection() {
            Column {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("UP NEXT", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Slate))
                    Text("${upNext.size} Tracks", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Lavender))
                }
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                    upNext.forEach { track ->
                        QueueItem(
                            title = track.title, 
                            artist = track.artist, 
                            duration = if (track.durationSec > 0) formatTime(track.durationSec) else "",
                            surfaceHighest = surfaceHighest,
                            lavender = Lavender,
                            slate = Slate,
                            outline = Outline,
                            onClick = { viewModel.playQueueItem(track.queueId) }
                        )
                    }
                }
            }
        }

        @Composable
        fun AmbientMixerSection() {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    "AMBIENT MIXER",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Lavender),
                )
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AmbientSoundType.values().forEach { type ->
                        val soundState = ambientSounds[type] ?: AmbientSoundState(type)
                        AmbientControlCard(
                            type = type,
                            state = soundState,
                            lavender = Lavender,
                            colors = colors,
                            onToggle = { AmbientSoundManager.toggleSound(context, type) },
                            onVolumeChange = { AmbientSoundManager.setVolume(type, it) }
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Slate) }
                Spacer(Modifier.weight(1f))
                Text("Music", style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, brush = Brush.linearGradient(listOf(Lavender, Orchid))))
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(48.dp))
            }

            if (landscape) {
                Row(modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 24.dp)) {
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
                        if (!hasPermission) {
                            Surface(shape = RoundedCornerShape(24.dp), color = SurfaceHigh, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                                Column(Modifier.padding(24.dp)) {
                                    Text("Enable Music Sync", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
                                    Spacer(Modifier.height(8.dp))
                                    Text("TimeDrop needs 'Notification Access' to sync with your active music (like Spotify).", style = TextStyle(fontSize = 14.sp, color = Slate))
                                    Spacer(Modifier.height(16.dp))
                                    Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }, colors = ButtonDefaults.buttonColors(containerColor = Orchid)) { Text("Grant Permission", color = Color.White) }
                                }
                            }
                        }
                        HeroSection()
                        Spacer(Modifier.height(16.dp))
                        PlayerCard(isLandscape = true)
                        Spacer(Modifier.height(32.dp))
                        AmbientMixerSection()
                    }
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
                        Spacer(Modifier.height(12.dp))
                        QueueSection()
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().verticalScroll(rememberScrollState())) {
                    if (!hasPermission) {
                        Surface(shape = RoundedCornerShape(24.dp), color = SurfaceHigh, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                            Column(Modifier.padding(24.dp)) {
                                Text("Enable Music Sync", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
                                Spacer(Modifier.height(8.dp))
                                Text("TimeDrop needs 'Notification Access' to sync with your active music (like Spotify).", style = TextStyle(fontSize = 14.sp, color = Slate))
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }, colors = ButtonDefaults.buttonColors(containerColor = Orchid)) { Text("Grant Permission", color = Color.White) }
                            }
                        }
                    }
                    HeroSection()
                    Spacer(Modifier.height(16.dp))
                    PlayerCard(isLandscape = false)
                    Spacer(Modifier.height(24.dp))
                    AmbientMixerSection()
                    Spacer(Modifier.height(24.dp))
                    QueueSection()
                    Spacer(Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
fun QueueItem(title: String, artist: String, duration: String, surfaceHighest: Color, lavender: Color, slate: Color, outline: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            surfaceHighest,
                            lavender.copy(alpha = 0.15f),
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = null,
                tint = lavender.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                artist,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = slate,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            duration,
            style = TextStyle(
                fontSize = 12.sp,
                color = outline,
            ),
        )
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

@Composable
fun AmbientControlCard(
    type: AmbientSoundType,
    state: AmbientSoundState,
    lavender: Color,
    colors: androidx.compose.material3.ColorScheme,
    onToggle: () -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    Surface(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF2C2C2C).copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val icon = when(type) {
                AmbientSoundType.RAIN -> Icons.Filled.WaterDrop
                AmbientSoundType.WAVES -> Icons.Filled.Waves
                AmbientSoundType.FOREST -> Icons.Filled.Park
            }
            
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(48.dp).background(if (state.isPlaying) lavender.copy(alpha = 0.2f) else Color.Transparent, CircleShape)
            ) {
                Icon(icon, null, tint = if (state.isPlaying) lavender else Color.White.copy(alpha = 0.4f))
            }
            
            Text(type.displayName, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
            
            Spacer(Modifier.height(8.dp))
            
            Slider(
                value = state.volume,
                onValueChange = onVolumeChange,
                colors = SliderDefaults.colors(
                    thumbColor = lavender,
                    activeTrackColor = lavender.copy(alpha = 0.3f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.05f)
                ),
                modifier = Modifier.height(24.dp)
            )
        }
    }
}
