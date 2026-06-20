package com.example.timedrop.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import com.example.timedrop.ui.screens.pomodoro.PomodoroMode
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloat
import com.example.timedrop.ui.components.clock.FlipSegment
import com.example.timedrop.ui.screens.calendar.EventViewModel
import com.example.timedrop.ui.screens.pomodoro.PomodoroViewModel
import com.example.timedrop.ui.screens.music.MusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.timedrop.ui.screens.calendar.CreateEventModal as DetailModal
import com.example.timedrop.ui.screens.profile.ProfileOverlay
import com.example.timedrop.data.local.AppDatabase
import com.example.timedrop.data.local.User
import com.example.timedrop.data.local.CalendarEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.viewmodel.compose.viewModel

// Color palette is now dynamic from MaterialTheme.colorScheme in the composable

@Composable
fun HomeRoute(
    eventViewModel: EventViewModel,
    pomodoroViewModel: PomodoroViewModel,
    musicViewModel: MusicViewModel,
    animationEnabled: Boolean,
    use24Hour: Boolean,
    onNavigatePomodoro: () -> Unit,
    onNavigateStopwatch: () -> Unit,
    onNavigateWorldClock: () -> Unit,
    onNavigateMusic: () -> Unit,
    onNavigateSettings: () -> Unit,
    currentUserEmail: String
) {
    val colors = MaterialTheme.colorScheme
    val surface = colors.surface
    val onBg = colors.onBackground
    val Lavender = colors.primary
    val Orchid = colors.secondary
    val BlueTertiary = colors.tertiary
    val Slate = Color(0xFFADAAAA) // Slate is usually neutral
    val surfaceHigh = colors.surfaceVariant
    val surfaceLow = colors.surface
    var selectedHomeEvent by remember { mutableStateOf<com.example.timedrop.data.local.CalendarEvent?>(null) }
    var showStreakDetails by remember { mutableStateOf(false) }

    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    val pomodoroState by pomodoroViewModel.uiState.collectAsState()
    val musicState by musicViewModel.uiState.collectAsState()
    // We no longer need local recentlyCompletedIds since we use event.completedAt timestamp

    val context = LocalContext.current
    
    val settingsVm: com.example.timedrop.ui.screens.settings.SettingsViewModel = viewModel()
    val settingsState by settingsVm.uiState.collectAsState()
    
    var showCelebration by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (settingsVm.checkAndUpdateStreak()) {
            showCelebration = true
        }
    }
    
    val todayStats by eventViewModel.todayStats.collectAsState()
    val focusTrend by eventViewModel.focusTrend.collectAsState()
    val nextEvents by eventViewModel.events.collectAsState()
    val allEvents by eventViewModel.allEvents.collectAsState()
    
    var showTodayTasksDialog by remember { mutableStateOf(false) }
    val allTasksGroupedByDate = remember(allEvents) {
        val today = LocalDate.now()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM. dd", Locale.US)
        
        allEvents
            .filter { it.isTask && java.time.LocalDate.parse(it.date) >= today }
            .sortedWith(compareBy({ it.date }, { it.time }))
            .groupBy { 
                val d = java.time.LocalDate.parse(it.date)
                if (d == today) "Today" else formatter.format(d)
            }
    }
    
    val nextEventsList = remember(nextEvents, now) {
        val today = LocalDate.now()
        val currentTime = LocalTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        
        nextEvents.filter { event ->
            val eventDate = LocalDate.parse(event.date)
            val isOnToday = eventViewModel.isEventOnDate(event, today)
            val isFutureDate = eventDate > today
            
            // 5-minute grace period based on completedAt timestamp (synced across devices)
            val isRecentlyCompleted = event.isCompleted && (System.currentTimeMillis() - event.completedAt) < 300_000L
            
            if (isRecentlyCompleted) return@filter true
            
            if (isFutureDate) true
            else if (isOnToday) {
                try {
                    val eventTime = LocalTime.parse(event.time, timeFormatter)
                    // Show if it hasn't started
                    
                    // If it's an incomplete task, keep it visible so user doesn't lose track
                    if (event.isTask && !event.isCompleted) true
                    else {
                        val limitTime = eventTime.plusMinutes(5)
                        currentTime.isBefore(limitTime)
                    }
                } catch (e: Exception) { true }
            } else if (eventDate < today) {
                // Keep it ONLY if it's an incomplete task from a past day
                event.isTask && !event.isCompleted
            } else false
        }.sortedWith(compareBy({ it.date }, { 
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
            try { LocalTime.parse(it.time, timeFormatter) } catch(e: Exception) { LocalTime.MIDNIGHT }
        })).take(3)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val prevDate = now.toLocalDate()
            now = ZonedDateTime.now()
            if (now.toLocalDate().isAfter(prevDate)) {
                if (settingsVm.checkAndUpdateStreak()) {
                    showCelebration = true
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val landscape = maxWidth > maxHeight
        val hour24 = now.hour
        val minute = now.minute
        val second = now.second
        val hour12 = ((hour24 + 11) % 12) + 1
        val ampm = if (hour24 < 12) "AM" else "PM"
        val hh = if (use24Hour) "%02d".format(hour24) else "%02d".format(hour12)
        val mm = "%02d".format(minute)
        val ss = "%02d".format(second)

        // ── Background Ambient Glow ──
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            // ── TopAppBar ──
            if (!landscape) {
                // ── Portrait TopAppBar ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Timer, "Logo", tint = Lavender, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "TimeDrop",
                            style = TextStyle(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                letterSpacing = (-1).sp,
                                brush = Brush.linearGradient(listOf(Lavender, Orchid)),
                            ),
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (settingsState.adminModeEnabled) {
                            androidx.compose.material3.TextButton(onClick = { showCelebration = true }) {
                                Text("Test Racha", color = Lavender)
                            }
                        }
                        Box(modifier = Modifier.size(48.dp))
                    }
                }
            }

            // ── Removed Search and Profile Overlay ──

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (landscape) {
                        // ── Landscape TopAppBar (Inline with scroll) ──
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 24.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Timer, "Logo", tint = Lavender, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "TimeDrop",
                                    style = TextStyle(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        brush = Brush.linearGradient(listOf(Lavender, Orchid)),
                                    ),
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (settingsState.adminModeEnabled) {
                                    androidx.compose.material3.TextButton(onClick = { showCelebration = true }) {
                                        Text("Test Racha", color = Lavender)
                                    }
                                }
                                Box(modifier = Modifier.size(40.dp))
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (landscape) 16.dp else 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BentoStatsSection(todayStats, focusTrend, settingsState.streakCount, colors, Lavender, Orchid, BlueTertiary, Slate, onStreakClick = { showStreakDetails = true }, onTasksClick = { showTodayTasksDialog = true })
                        
                        Spacer(Modifier.height(32.dp))

                        // Clock Layout
                    if (landscape) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val landscapeClockSize = 180
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FlipSegment(text = hh, animateDigits = animationEnabled, fontSizeSp = landscapeClockSize, containerColor = Color.Transparent, contentColor = colors.onBackground, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp))
                                Text("HOURS", style = TextStyle(fontSize = 11.sp, letterSpacing = 2.sp, color = Slate, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(top = 8.dp))
                            }
                            Text(":", style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.Light, color = Lavender.copy(alpha = 0.4f)), modifier = Modifier.offset(y = (-16).dp).padding(horizontal = 12.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FlipSegment(text = mm, animateDigits = animationEnabled, fontSizeSp = landscapeClockSize, containerColor = Color.Transparent, contentColor = colors.onBackground, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp))
                                Text("MINUTES", style = TextStyle(fontSize = 11.sp, letterSpacing = 2.sp, color = Slate, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(top = 8.dp))
                            }
                            Text(":", style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.Light, color = Lavender.copy(alpha = 0.4f)), modifier = Modifier.offset(y = (-16).dp).padding(horizontal = 12.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FlipSegment(text = ss, animateDigits = animationEnabled, fontSizeSp = landscapeClockSize, containerColor = Color.Transparent, contentColor = Orchid, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp))
                                Text("SECONDS", style = TextStyle(fontSize = 11.sp, letterSpacing = 2.sp, color = Slate, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(top = 8.dp))
                            }
                            if (!use24Hour) {
                                Spacer(Modifier.width(24.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(ampm, style = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Bold, color = Slate))
                                    Text("PM", style = TextStyle(fontSize = 12.sp, letterSpacing = 2.sp, color = Slate, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                        
                        // Date Display (Landscape)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'del' yyyy '('EEEE')'", Locale("es", "ES"))).capitalize(),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Lavender,
                                letterSpacing = 1.sp
                            )
                        )
                    } else {
                        // Portrait: Stack HH:MM and SS below
                        val portraitClockSize = 140
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FlipSegment(text = hh, animateDigits = animationEnabled, fontSizeSp = portraitClockSize, containerColor = Color.Transparent, contentColor = colors.onBackground, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp))
                                Text("HOURS", style = TextStyle(fontSize = 10.sp, letterSpacing = 2.sp, color = Slate, fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 12.dp))
                            }
                            Text(":", style = TextStyle(fontSize = 84.sp, fontWeight = FontWeight.Light, color = Lavender.copy(alpha = 0.4f)), modifier = Modifier.offset(y = (-20).dp).padding(horizontal = 8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FlipSegment(text = mm, animateDigits = animationEnabled, fontSizeSp = portraitClockSize, containerColor = Color.Transparent, contentColor = colors.onBackground, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp))
                                Text("MINUTES", style = TextStyle(fontSize = 10.sp, letterSpacing = 2.sp, color = Slate, fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 12.dp))
                            }
                        }
                        Spacer(Modifier.height(32.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FlipSegment(text = ss, animateDigits = animationEnabled, fontSizeSp = portraitClockSize, containerColor = Color.Transparent, contentColor = Orchid, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp))
                                Text("SECONDS", style = TextStyle(fontSize = 10.sp, letterSpacing = 2.sp, color = Slate, fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 12.dp))
                            }
                            if (!use24Hour) {
                                Spacer(Modifier.width(32.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(ampm, style = TextStyle(fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Slate))
                                    Text("MERIDIEM", style = TextStyle(fontSize = 10.sp, letterSpacing = 2.sp, color = Slate, fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 12.dp))
                                }
                            }
                        }

                        // Date Display (Portrait)
                        Spacer(Modifier.height(32.dp))
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'del' yyyy '('EEEE')'", Locale("es", "ES"))).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() },
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                brush = Brush.linearGradient(listOf(Lavender, Orchid)),
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Spacer(Modifier.height(if (landscape) 24.dp else 48.dp))

                    // ── Focus Status or Event Card ──
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = colors.surfaceVariant.copy(alpha = 0.40f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Events List
                            if (nextEventsList.isNotEmpty()) {
                                nextEventsList.forEach { event ->
                                    val eventDate = LocalDate.parse(event.date)
                                    val today = LocalDate.now()
                                    val datePrefix = when {
                                        eventDate == today -> "Today"
                                        eventDate == today.plusDays(1) -> "Tomorrow"
                                        else -> eventDate.format(DateTimeFormatter.ofPattern("MMM dd"))
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedHomeEvent = event }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(Modifier.size(8.dp).background(Color(event.colorArgb), CircleShape))
                                        Spacer(Modifier.width(12.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(datePrefix, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Lavender))
                                                Spacer(Modifier.width(8.dp))
                                                Text(event.time, style = TextStyle(fontSize = 12.sp, color = Slate))
                                                
                                                val isDoneGrace = event.isCompleted && (System.currentTimeMillis() - event.completedAt) < 300_000L
                                                val labelData = if (isDoneGrace) {
                                                    "DONE" to Color(0xFF4CAF50)
                                                } else try {
                                                    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
                                                    val eventTime = LocalTime.parse(event.time, timeFormatter)
                                                    val currentTime = LocalTime.now()
                                                    val isNow = eventDate == today && !currentTime.isBefore(eventTime) && currentTime.isBefore(eventTime.plusMinutes(5))
                                                    val isSoon = eventDate == today && currentTime.isAfter(eventTime.minusMinutes(5)) && currentTime.isBefore(eventTime)
                                                    val isOverdue = event.isTask && !event.isCompleted && (eventDate < today || (eventDate == today && currentTime.isAfter(eventTime.plusMinutes(5))))

                                                    when {
                                                        isOverdue -> "OVERDUE" to Color.Red
                                                        isNow -> "NOW" to Orchid
                                                        isSoon -> "SOON" to Lavender
                                                        else -> null
                                                    }
                                                } catch (e: Exception) { null }

                                                if (labelData != null) {
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        labelData.first, 
                                                        style = TextStyle(
                                                            fontSize = 10.sp, 
                                                            fontWeight = FontWeight.Black, 
                                                            color = labelData.second,
                                                            letterSpacing = 1.sp
                                                        )
                                                    )
                                                }
                                            }
                                            Text(
                                                event.title,
                                                style = TextStyle(
                                                    fontSize = 15.sp,
                                                    // DONE grace: tachado + gris (igual que calendario)
                                                    // Completed sin gracia: tachado + gris
                                                    // Incompleto: bold + color normal
                                                    fontWeight = if (event.isTask && event.isCompleted) FontWeight.Normal else FontWeight.Bold,
                                                    color = if (event.isTask && event.isCompleted) Slate else colors.onSurface,
                                                    textDecoration = if (event.isTask && event.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                                )
                                            )
                                        }
                                        
                                        if (event.isTask) {
                                            IconButton(
                                                onClick = { eventViewModel.toggleTaskCompletion(event) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (event.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                                    contentDescription = "Toggle Task",
                                                    tint = if (event.isCompleted) Color(0xFF4CAF50) else Lavender,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Filled.CalendarMonth, 
                                                contentDescription = null, 
                                                tint = Orchid, 
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(Modifier.size(8.dp).background(Slate.copy(alpha=0.5f), CircleShape))
                                    Spacer(Modifier.width(12.dp))
                                    Text("No Upcoming Events", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate))
                                }
                            }
                            
                            // Pomodoro Row
                            if (pomodoroState.isRunning) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(Modifier.size(8.dp).background(Lavender, CircleShape))
                                    Spacer(Modifier.width(12.dp))
                                    val taskTitle = pomodoroState.selectedTask?.title ?: "Focus Session"
                                    Text(taskTitle, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.onSurface))
                                    
                                    Spacer(Modifier.width(16.dp))
                                    Box(Modifier.width(1.dp).height(16.dp).background(Slate.copy(alpha = 0.3f)))
                                    Spacer(Modifier.width(16.dp))
                                    
                                    val remMin = pomodoroState.remainingSeconds / 60
                                    val remSec = pomodoroState.remainingSeconds % 60
                                    val modeShort = when(pomodoroState.currentMode) {
                                        PomodoroMode.WORK -> "WORK"
                                        PomodoroMode.SHORT_BREAK -> "S-BREAK"
                                        PomodoroMode.LONG_BREAK -> "L-BREAK"
                                    }
                                    Icon(Icons.Filled.Timer, null, tint = Lavender, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("$modeShort — %02d:%02d".format(remMin, remSec), style = TextStyle(fontSize = 14.sp, color = Slate))
                                }
                            }

                            // Music Row
                            // Music Widget (Hide if no track)
                            if (musicState.currentTrack != null) {
                                val track = musicState.currentTrack!!
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onNavigateMusic() },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Circular Album Art or Music Icon
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(surfaceHigh),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (track.albumArt != null) {
                                                Image(
                                                    bitmap = track.albumArt.asImageBitmap(),
                                                    contentDescription = "Album Art",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Filled.MusicNote,
                                                    null,
                                                    tint = Lavender.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        
                                        Spacer(Modifier.width(12.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                track.title, 
                                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.onSurface),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                if (musicState.isPlaying) track.artist else "Paused", 
                                                style = TextStyle(fontSize = 11.sp, color = Slate),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    
                                    Spacer(Modifier.width(8.dp))
                                    
                                    // Player Controls
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { musicViewModel.skipPrevious() }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Filled.SkipPrevious, "Prev", tint = Slate, modifier = Modifier.size(20.dp))
                                        }
                                        
                                        Spacer(Modifier.width(4.dp))
                                        
                                        IconButton(
                                            onClick = { musicViewModel.togglePlayPause() },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(colors.surfaceVariant, CircleShape)
                                        ) {
                                            Icon(
                                                if (musicState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, 
                                                null, 
                                                tint = colors.onSurface,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        
                                        Spacer(Modifier.width(4.dp))
                                        
                                        IconButton(onClick = { musicViewModel.skipNext() }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Filled.SkipNext, "Next", tint = Slate, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(if (landscape) 100.dp else 120.dp))
                }
            }
        }

        if (selectedHomeEvent != null) {
            DetailModal(
                selectedDate = LocalDate.parse(selectedHomeEvent!!.date),
                editingEvent = selectedHomeEvent,
                isReadOnly = true,
                onDismiss = { selectedHomeEvent = null },
                onSave = { _, _, _, _, _, _, _ -> /* Read-only on Home */ },
                onDelete = { /* Read-only on Home */ },
                Primary = Lavender,
                Slate = Slate,
                SurfaceHigh = surfaceHigh,
                SurfaceLow = surfaceLow
            )
        }

        if (showStreakDetails) {
            StreakDetailsScreen(
                currentStreak = settingsState.streakCount,
                longestStreak = settingsState.longestStreak,
                isAdminMode = settingsState.adminModeEnabled,
                onSetStreak = { settingsVm.setStreakCount(it) },
                onDismiss = { showStreakDetails = false }
            )
        }

        if (showCelebration) {
            StreakCelebrationScreen(
                currentStreak = settingsState.streakCount,
                onDismiss = { showCelebration = false }
            )
        }

        if (showTodayTasksDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showTodayTasksDialog = false },
                containerColor = surfaceLow,
                titleContentColor = colors.onSurface,
                textContentColor = Slate,
                title = { Text("All Pending Tasks", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (allTasksGroupedByDate.isEmpty()) {
                            Text("No pending tasks.", style = TextStyle(fontSize = 14.sp, color = Slate))
                        } else {
                            allTasksGroupedByDate.forEach { (dateGroup, tasks) ->
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Tasks $dateGroup",
                                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Orchid, letterSpacing = 1.sp)
                                    )
                                    tasks.forEach { task ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { eventViewModel.toggleTaskCompletion(task) }
                                                .background(if (task.isCompleted) surfaceHigh.copy(alpha = 0.5f) else surfaceHigh, RoundedCornerShape(12.dp))
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (task.isCompleted) Color(0xFF4CAF50) else Lavender,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = task.title,
                                                    style = TextStyle(
                                                        fontSize = 14.sp,
                                                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Bold,
                                                        color = if (task.isCompleted) Slate else colors.onSurface,
                                                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                                    )
                                                )
                                                Text(
                                                    text = task.time,
                                                    style = TextStyle(
                                                        fontSize = 12.sp,
                                                        color = Slate,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { showTodayTasksDialog = false }) {
                        Text("Close", color = Lavender, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
    }
 }

@Composable
private fun HourglassFireGlow(streakCount: Int, colors: androidx.compose.material3.ColorScheme, orchid: Color) {
    if (streakCount > 0) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
            com.example.timedrop.ui.screens.home.LoopingVideoPlayer(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                videoRes = com.example.timedrop.R.raw.logo_video
            )
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Filled.HourglassTop,
                contentDescription = "Inactive Streak",
                tint = colors.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun BentoStatsSection(
    todayStats: Pair<Int, Int>,
    focusTrend: List<Float>,
    streakCount: Int,
    colors: androidx.compose.material3.ColorScheme,
    lavender: Color,
    orchid: Color,
    blue: Color,
    slate: Color,
    onStreakClick: () -> Unit = {},
    onTasksClick: () -> Unit = {}
) {
    // Landscape might need a different spacing, but let's stick to simple row for now
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val (done, total) = todayStats
        
        // Daily Progress (Only show if there are tasks)
        if (total > 0) {
            Surface(
                modifier = Modifier.weight(1f).height(120.dp).clickable { onTasksClick() },
                shape = RoundedCornerShape(24.dp),
                color = colors.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Box(Modifier.size(28.dp).background(lavender.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.CheckCircle, null, tint = lavender, modifier = Modifier.size(14.dp))
                    }
                    Column {
                        if (done == total) {
                            Text("$done/$total", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))) // Green
                            Text("All Done! 🎉", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = slate, letterSpacing = 0.5.sp))
                        } else {
                            Text("$done/$total", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = colors.onSurface))
                            Text("Tasks Done", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = slate, letterSpacing = 0.5.sp))
                        }
                    }
                }
            }
        }

        // Daily Consistency (Hourglass Streak)
        Surface(
            modifier = Modifier.weight(1f).height(120.dp).clickable { onStreakClick() },
            shape = RoundedCornerShape(24.dp),
            color = colors.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                HourglassFireGlow(streakCount, colors, orchid)
                
                Column {
                    Text("$streakCount Days", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = colors.onSurface))
                    Text("Consistency", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = slate, letterSpacing = 0.5.sp))
                }
            }
        }
    }
}
