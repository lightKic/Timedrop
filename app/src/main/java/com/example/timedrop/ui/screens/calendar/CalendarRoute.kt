package com.example.timedrop.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.timedrop.data.local.CalendarEvent
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.*
import android.app.DatePickerDialog
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.style.TextDecoration

// Design palette is now dynamic from MaterialTheme.colorScheme in the composable

@Composable
fun CalendarRoute(eventViewModel: EventViewModel) {
    var showEventModal by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    val monthEvents by eventViewModel.monthEvents.collectAsState()
    val allEvents by eventViewModel.allEvents.collectAsState()
    
    val colors = MaterialTheme.colorScheme
    val Primary = colors.primary
    val Secondary = colors.secondary
    val Tertiary = colors.tertiary
    val Slate = Color(0xFFADAAAA)
    val onBg = colors.onBackground
    val surfaceHigh = colors.surfaceVariant
    val surfaceLow = colors.surface
    
    val selectedDateEvents = remember(allEvents, eventViewModel.selectedDate) {
        allEvents.filter { it.date == eventViewModel.selectedDate.toString() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEventModal = true },
                shape = CircleShape,
                containerColor = Color.Transparent,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .background(Brush.linearGradient(listOf(Primary, Secondary)), CircleShape)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Event", tint = colors.onPrimary)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val landscape = maxWidth > maxHeight
            if (landscape) {
                Row(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 16.dp, bottom = 48.dp)
                    ) {
                        CalendarContent(eventViewModel, monthEvents, Primary, Secondary, Tertiary, Slate, surfaceHigh, onBg)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 32.dp, bottom = 48.dp, end = 16.dp)
                    ) {
                        EventsContent(
                            selectedDate = eventViewModel.selectedDate,
                            events = allEvents,
                            onEventClick = { 
                                editingEvent = it
                                showEventModal = true
                            },
                            onToggleTask = { eventViewModel.toggleTaskCompletion(it) },
                            Slate = Slate,
                            onBg = onBg,
                            Primary = Primary,
                            surfaceLow = surfaceLow
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
                    CalendarContent(eventViewModel, monthEvents, Primary, Secondary, Tertiary, Slate, surfaceHigh, onBg)
                    Spacer(Modifier.height(32.dp))
                    EventsContent(
                        selectedDate = eventViewModel.selectedDate,
                        events = allEvents,
                        onEventClick = { 
                            editingEvent = it
                            showEventModal = true
                        },
                        onToggleTask = { eventViewModel.toggleTaskCompletion(it) },
                        Slate = Slate,
                        onBg = onBg,
                        Primary = Primary,
                        surfaceLow = surfaceLow
                    )
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }

    if (showEventModal) {
        CreateEventModal(
            selectedDate = eventViewModel.selectedDate,
            editingEvent = editingEvent,
            onDismiss = { 
                showEventModal = false
                editingEvent = null
            },
            onSave = { title, date, time, color, desc, isTask, repeat ->
                if (editingEvent != null) {
                    eventViewModel.updateEvent(editingEvent!!.copy(
                        title = title,
                        date = date.toString(),
                        time = time,
                        colorArgb = color.toArgb(),
                        description = desc,
                        isTask = isTask,
                        repeatInterval = repeat
                    ))
                } else {
                    eventViewModel.addEvent(title, date, time, color.toArgb(), desc, isTask, repeat)
                }
                showEventModal = false
                editingEvent = null
            },
            onDelete = {
                editingEvent?.let { eventViewModel.removeEvent(it) }
                showEventModal = false
                editingEvent = null
            },
            Primary = Primary,
            Slate = Slate,
            SurfaceHigh = surfaceHigh,
            SurfaceLow = surfaceLow
        )
    }
}

@Composable
private fun CalendarContent(viewModel: EventViewModel, events: List<CalendarEvent>, Primary: Color, Secondary: Color, Tertiary: Color, Slate: Color, SurfaceHigh: Color, onBg: Color) {
    val currentMonth = viewModel.currentMonth
    val selectedDate = viewModel.selectedDate
    val today = LocalDate.now()

    // Header
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                currentMonth.month.getDisplayName(JavaTextStyle.FULL, Locale.getDefault()).uppercase(),
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 3.sp, color = Primary)
            )
            Text(
                currentMonth.year.toString(),
                style = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = onBg, letterSpacing = (-2).sp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            Surface(onClick = { viewModel.previousMonth() }, shape = CircleShape, color = SurfaceHigh, modifier = Modifier.size(40.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.ChevronLeft, null, tint = onBg) }
            }
            Surface(onClick = { viewModel.nextMonth() }, shape = CircleShape, color = SurfaceHigh, modifier = Modifier.size(40.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.ChevronRight, null, tint = onBg) }
            }
        }
    }

    // Days of week
    Row(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
            Text(day, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Slate), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }
    }

    // Grid
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
    
    val totalSlots = 42 // 6 rows of 7
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        for (week in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (day in 1..7) {
                    val slotIndex = week * 7 + day
                    val dayOfMonth = slotIndex - (firstDayOfWeek - 1)
                    
                    if (dayOfMonth in 1..daysInMonth) {
                        val date = currentMonth.atDay(dayOfMonth)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val dayEvents = events.filter { viewModel.isEventOnDate(it, date) }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.selectDate(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = when {
                                                isSelected -> Primary
                                                isToday -> Primary.copy(alpha = 0.1f)
                                                else -> Color.Transparent
                                            },
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = if (isToday && !isSelected) 1.dp else 0.dp,
                                            color = if (isToday && !isSelected) Primary.copy(alpha = 0.5f) else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        dayOfMonth.toString(),
                                        style = TextStyle(
                                            fontSize = 14.sp, 
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else onBg
                                        )
                                    )
                                }
                                if (dayEvents.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 2.dp)) {
                                        dayEvents.take(3).forEach { ev ->
                                            Box(Modifier.size(4.dp).background(Color(ev.colorArgb), CircleShape))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).height(48.dp))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun EventsContent(
    selectedDate: LocalDate, 
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    onToggleTask: (CalendarEvent) -> Unit,
    Slate: Color,
    onBg: Color,
    Primary: Color,
    surfaceLow: Color
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("SCHEDULE", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = onBg))
            Text(selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, EEEE")), style = TextStyle(fontSize = 12.sp, color = Slate))
        }

        if (events.isEmpty()) {
            Text("No events for this day", color = Slate, modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(), textAlign = TextAlign.Center)
        } else {
            val sortedEvents = events.filter { (it.date == selectedDate.toString()) || 
                (it.repeatInterval == "daily") || 
                (it.repeatInterval == "weekly" && LocalDate.parse(it.date).dayOfWeek == selectedDate.dayOfWeek) ||
                (it.repeatInterval == "mon-fri" && selectedDate.dayOfWeek.value in 1..5)
            }.sortedWith(compareBy({ it.date }, { 
                val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
                try { LocalTime.parse(it.time, timeFormatter) } catch(e: Exception) { LocalTime.MIDNIGHT }
            }))

            if (sortedEvents.isEmpty()) {
                Text("No events for this day", color = Slate, modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(), textAlign = TextAlign.Center)
            } else {
                sortedEvents.forEach { event ->
                    EventCard(
                        event = event, 
                        onClick = { onEventClick(event) },
                        onToggleTask = { onToggleTask(event) },
                        Slate = Slate,
                        onBg = onBg,
                        Primary = Primary,
                        surfaceLow = surfaceLow
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: CalendarEvent, onClick: () -> Unit, onToggleTask: () -> Unit = {}, Slate: Color, onBg: Color, Primary: Color, surfaceLow: Color) {
    val isCompleted = event.isTask && event.isCompleted
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isCompleted) surfaceLow.copy(alpha = 0.5f) else surfaceLow,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(80.dp)) {
            Box(Modifier.width(4.dp).fillMaxSize().background(if (isCompleted) Slate else Color(event.colorArgb)))
            Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (event.isTask) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) Primary else Color.Transparent)
                            .border(2.dp, if (isCompleted) Primary else Slate.copy(alpha = 0.5f), CircleShape)
                            .clickable { onToggleTask() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                }
                
                Column(modifier = Modifier.width(60.dp)) {
                    Text(
                        event.time, 
                        style = TextStyle(
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = if (isCompleted) Slate else onBg,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                        )
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    Text(
                        event.title, 
                        style = TextStyle(
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.SemiBold, 
                            color = if (isCompleted) Slate else onBg,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                        )
                    )
                    if (event.description.isNotEmpty()) {
                        Text(event.description, style = TextStyle(fontSize = 12.sp, color = Slate), maxLines = 1)
                    }
                }
                Icon(Icons.Filled.ChevronRight, null, tint = Slate.copy(alpha = 0.3f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventModal(
    selectedDate: LocalDate,
    editingEvent: CalendarEvent? = null,
    isReadOnly: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String, LocalDate, String, Color, String, Boolean, String) -> Unit,
    onDelete: () -> Unit = {},
    Primary: Color,
    Slate: Color,
    SurfaceHigh: Color,
    SurfaceLow: Color
) {
    var title by remember { mutableStateOf(editingEvent?.title ?: "") }
    var description by remember { mutableStateOf(editingEvent?.description ?: "") }
    var eventDate by remember { mutableStateOf(editingEvent?.localDate ?: selectedDate) }
    var isTask by remember { mutableStateOf(editingEvent?.isTask ?: false) }
    var repeatInterval by remember { mutableStateOf(editingEvent?.repeatInterval ?: "none") }
    
    // Parse time if editing (e.g., "12:00 PM")
    val initialHour = remember(editingEvent) {
        editingEvent?.time?.split(":")?.firstOrNull()?.toIntOrNull() ?: 12
    }
    val initialMinute = remember(editingEvent) {
        editingEvent?.time?.split(":")?.getOrNull(1)?.split(" ")?.firstOrNull()?.toIntOrNull() ?: 0
    }
    val initialIsAm = remember(editingEvent) {
        editingEvent?.time?.contains("AM") ?: true
    }

    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }
    var isAm by remember { mutableStateOf(initialIsAm) }
    var selectedColor by remember { mutableStateOf(if (editingEvent != null) Color(editingEvent.colorArgb) else Primary) }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, y, m, d -> eventDate = LocalDate.of(y, m + 1, d) },
            eventDate.year,
            eventDate.monthValue - 1,
            eventDate.dayOfMonth
        )
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = SurfaceHigh,
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.95f else 0.9f)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isReadOnly) "Event Details" else if (editingEvent != null) "Edit Event" else "New Event", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    if (editingEvent != null && !isReadOnly) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))

                if (isLandscape) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        // Column 1: Core Info
                        Column(modifier = Modifier.weight(1f)) {
                            TextField(
                                value = title,
                                onValueChange = { if (!isReadOnly) title = it },
                                readOnly = isReadOnly,
                                placeholder = { Text("Event Title", color = Slate) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceLow,
                                    unfocusedContainerColor = SurfaceLow,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = Primary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            
                            Spacer(Modifier.height(16.dp))

                            TextField(
                                value = description,
                                onValueChange = { if (!isReadOnly) description = it },
                                readOnly = isReadOnly,
                                placeholder = { Text(if (isReadOnly) "" else "Add description...", color = Slate) },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceLow,
                                    unfocusedContainerColor = SurfaceLow,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = Primary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(Modifier.height(24.dp))

                            Text("COLOR", style = TextStyle(fontSize = 10.sp, color = Slate, letterSpacing = 1.sp))
                            Spacer(Modifier.height(12.dp))
                            
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                val colorList1 = listOf(Primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary, Color(0xFFFF5252), Color(0xFF4CAF50))
                                val colorList2 = listOf(Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFF7CB342), Color(0xFFFDD835), Color(0xFFD81B60))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    colorList1.forEach { color ->
                                        Box(modifier = Modifier.size(32.dp).background(color, CircleShape).border(if (selectedColor == color) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape).clickable { selectedColor = color })
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    colorList2.forEach { color ->
                                        Box(modifier = Modifier.size(32.dp).background(color, CircleShape).border(if (selectedColor == color) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape).clickable { selectedColor = color })
                                    }
                                }
                            }
                        }

                        // Column 2: Date, Time and Settings
                        Column(modifier = Modifier.weight(1f)) {
                            // Date Picker Button
                            Column(modifier = Modifier.clickable(enabled = !isReadOnly) { datePickerDialog.show() }) {
                                Text("DATE", style = TextStyle(fontSize = 10.sp, color = Slate, letterSpacing = 1.sp))
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Event, null, tint = Primary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(eventDate.format(DateTimeFormatter.ofPattern("EEEE, MMM dd")), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(Modifier.height(20.dp))
                            
                            // Time Pickers
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WheelPicker(count = 12, initialIndex = hour - 1, onSelectionChanged = { if (!isReadOnly) hour = it + 1 }, label = { (it + 1).toString() }, isEnabled = !isReadOnly, Slate = Slate)
                                Text(":", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                WheelPicker(count = 60, initialIndex = minute, onSelectionChanged = { if (!isReadOnly) minute = it }, label = { it.toString().padStart(2, '0') }, isEnabled = !isReadOnly, Slate = Slate)
                                Spacer(Modifier.width(12.dp))
                                Row {
                                    Text("AM", color = if (isAm) Primary else Slate, modifier = Modifier.clickable(enabled = !isReadOnly) { isAm = true }, fontWeight = if (isAm) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("PM", color = if (!isAm) Primary else Slate, modifier = Modifier.clickable(enabled = !isReadOnly) { isAm = false }, fontWeight = if (!isAm) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                                }
                            }

                            if (!isReadOnly) {
                                Spacer(Modifier.height(20.dp))
                                Switch(checked = isTask, onCheckedChange = { isTask = it }, colors = SwitchDefaults.colors(checkedThumbColor = Primary))
                                Text(if (isTask) "Task Mode" else "Event Mode", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                
                                Spacer(Modifier.height(20.dp))
                                Text("REPEAT", style = TextStyle(fontSize = 10.sp, color = Slate, letterSpacing = 1.sp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 8.dp)) {
                                    listOf("none", "daily", "weekly", "mon-fri").forEach { interval ->
                                        Surface(onClick = { repeatInterval = interval }, shape = RoundedCornerShape(8.dp), color = if (repeatInterval == interval) Primary else SurfaceLow, modifier = Modifier.weight(1f)) {
                                            Box(Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                                Text(interval.take(4).uppercase(), color = if (repeatInterval == interval) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Portrait Layout (Original)
                    TextField(
                        value = title,
                        onValueChange = { if (!isReadOnly) title = it },
                        readOnly = isReadOnly,
                        placeholder = { Text("Event Title", color = Slate) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceLow,
                            unfocusedContainerColor = SurfaceLow,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = Primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    Spacer(Modifier.height(16.dp))

                    TextField(
                        value = description,
                        onValueChange = { if (!isReadOnly) description = it },
                        readOnly = isReadOnly,
                        placeholder = { Text(if (isReadOnly) "" else "Add description...", color = Slate) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceLow,
                            unfocusedContainerColor = SurfaceLow,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = Primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Date Picker Button
                    Column(modifier = Modifier.clickable(enabled = !isReadOnly) { datePickerDialog.show() }) {
                        Text("DATE", style = TextStyle(fontSize = 10.sp, color = Slate, letterSpacing = 1.sp))
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Event, null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(eventDate.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Time Pickers
                    Text("TIME", style = TextStyle(fontSize = 10.sp, color = Slate, letterSpacing = 1.sp))
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        WheelPicker(count = 12, initialIndex = hour - 1, onSelectionChanged = { if (!isReadOnly) hour = it + 1 }, label = { (it + 1).toString() }, isEnabled = !isReadOnly, Slate = Slate)
                        Text(":", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        WheelPicker(count = 60, initialIndex = minute, onSelectionChanged = { if (!isReadOnly) minute = it }, label = { it.toString().padStart(2, '0') }, isEnabled = !isReadOnly, Slate = Slate)
                        Spacer(Modifier.width(16.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AM", color = if (isAm) Primary else Slate, modifier = Modifier.clickable(enabled = !isReadOnly) { isAm = true }, fontWeight = if (isAm) FontWeight.Bold else FontWeight.Normal)
                            Spacer(Modifier.height(8.dp))
                            Text("PM", color = if (!isAm) Primary else Slate, modifier = Modifier.clickable(enabled = !isReadOnly) { isAm = false }, fontWeight = if (!isAm) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    
                    if (!isReadOnly) {
                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth().clickable { isTask = !isTask }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("TASK MODE", style = TextStyle(fontSize = 10.sp, color = Slate, letterSpacing = 1.sp))
                                Text(if (isTask) "Treat as actionable task" else "Treat as calendar event", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }
                            Switch(checked = isTask, onCheckedChange = { isTask = it }, colors = SwitchDefaults.colors(checkedThumbColor = Primary))
                        }
                        Spacer(Modifier.height(24.dp))
                        Column {
                            Text("REPEAT", style = TextStyle(fontSize = 10.sp, color = Slate, letterSpacing = 1.sp))
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("none", "daily", "weekly", "mon-fri").forEach { interval ->
                                    Surface(onClick = { repeatInterval = interval }, shape = RoundedCornerShape(12.dp), color = if (repeatInterval == interval) Primary else SurfaceLow, modifier = Modifier.weight(1f)) {
                                        Box(Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                            Text(interval.uppercase(), color = if (repeatInterval == interval) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(32.dp))
                        Text("COLOR", style = TextStyle(fontSize = 10.sp, color = Slate, letterSpacing = 1.sp))
                        Spacer(Modifier.height(12.dp))
                        val colorList1 = listOf(Primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary, Color(0xFFFF5252), Color(0xFF4CAF50))
                        val colorList2 = listOf(Color(0xFF8E24AA), Color(0xFF00ACC1), Color(0xFF7CB342), Color(0xFFFDD835), Color(0xFFD81B60))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                colorList1.forEach { color -> Box(modifier = Modifier.size(36.dp).background(color, CircleShape).border(if (selectedColor == color) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape).clickable { selectedColor = color }) }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                colorList2.forEach { color -> Box(modifier = Modifier.size(36.dp).background(color, CircleShape).border(if (selectedColor == color) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape).clickable { selectedColor = color }) }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                if (!isReadOnly) {
                    Button(
                        onClick = { 
                            val formattedTime = "%02d:%02d %s".format(hour, minute, if (isAm) "AM" else "PM")
                            onSave(title, eventDate, formattedTime, selectedColor, description, isTask, repeatInterval) 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (editingEvent != null) "Update Item" else "Save Item", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
                    }
                } else {
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = SurfaceLow), shape = RoundedCornerShape(16.dp)) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WheelPicker(
    count: Int,
    initialIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    label: (Int) -> String,
    isEnabled: Boolean = true,
    Slate: Color
) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    
    // Simplistic snap implementation
    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress && isEnabled) {
            val centerIndex = state.firstVisibleItemIndex
            onSelectionChanged(centerIndex % count)
        }
    }

    Box(modifier = Modifier.height(80.dp).width(50.dp), contentAlignment = Alignment.Center) {
        // Overlay for center highlight
        Box(Modifier.fillMaxWidth().height(30.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)))
        
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = isEnabled,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 25.dp)
        ) {
            items(1000 * count) { index ->
                val realIndex = index % count
                Text(
                    text = label(realIndex),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = if (state.firstVisibleItemIndex == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (state.firstVisibleItemIndex == index) Color.White else Slate.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

private fun Color.toArgb(): Int {
    return (this.alpha * 255.0f + 0.5f).toInt() shl 24 or
           ((this.red * 255.0f + 0.5f).toInt() shl 16) or
           ((this.green * 255.0f + 0.5f).toInt() shl 8) or
           (this.blue * 255.0f + 0.5f).toInt()
}
