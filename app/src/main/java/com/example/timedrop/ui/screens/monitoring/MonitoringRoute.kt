package com.example.timedrop.ui.screens.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timedrop.data.monitoring.FirebaseMonitorStore
import com.example.timedrop.data.monitoring.OpLog
import com.example.timedrop.data.monitoring.OpType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringRoute(
    onBack: () -> Unit,
    viewModel: MonitoringViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme
    val Primary   = colors.primary
    val Secondary = colors.secondary
    val Tertiary  = colors.tertiary
    val Slate     = Color(0xFFADAAAA)
    val surface   = colors.surface
    val surfaceHigh = colors.surfaceVariant

    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Brush.linearGradient(listOf(Primary, Secondary)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👑", fontSize = 16.sp)
                        }
                        Column {
                            Text("Firebase Monitor", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = colors.onBackground)
                            Text("Admin Only", fontSize = 10.sp, color = Primary, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, null, tint = colors.onBackground)
                    }
                },
                actions = {
                    // Test button: manually record a test entry to verify pipeline
                    TextButton(onClick = {
                        FirebaseMonitorStore.record(OpLog(type = OpType.WRITE, source = "test"))
                    }) {
                        Text("TEST", color = Primary, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Filled.DeleteSweep, "Clear logs", tint = Color.Red.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
        ) {
            // ── Quotas del plan Spark ──
            item {
                SectionTitle("CUOTAS DEL PLAN SPARK", Slate)
                Spacer(Modifier.height(10.dp))
                QuotaCard(
                    label = "Escrituras",
                    emoji = "✍️",
                    used = state.todayWrites,
                    limit = state.dailyWriteLimit,
                    color = Primary,
                    surface = surfaceHigh
                )
                Spacer(Modifier.height(10.dp))
                QuotaCard(
                    label = "Lecturas",
                    emoji = "📖",
                    used = state.todayReads,
                    limit = state.dailyReadLimit,
                    color = Secondary,
                    surface = surfaceHigh
                )
                Spacer(Modifier.height(10.dp))
                QuotaCard(
                    label = "Eliminaciones",
                    emoji = "🗑️",
                    used = state.todayDeletes,
                    limit = state.dailyDeleteLimit,
                    color = Tertiary,
                    surface = surfaceHigh
                )
            }

            // ── Stats grid ──
            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle("OPERACIONES POR PERÍODO", Slate)
                Spacer(Modifier.height(10.dp))
                PeriodStatsTable(state = state, primary = Primary, secondary = Secondary, tertiary = Tertiary, slate = Slate, surface = surfaceHigh)
            }

            // ── Recent logs ──
            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle("REGISTRO RECIENTE (últimas 50)", Slate)
                Spacer(Modifier.height(6.dp))
            }

            if (state.recentLogs.isEmpty()) {
                item {
                    Text(
                        "Sin operaciones registradas todavía.",
                        color = Slate,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                items(state.recentLogs) { log ->
                    LogRow(log = log, surface = surfaceHigh, slate = Slate, primary = Primary)
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = surfaceHigh,
            title = { Text("Borrar todos los registros", fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro? Se eliminarán todos los logs locales de monitoreo.", color = Slate) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearLogs(); showClearDialog = false }) {
                    Text("Sí, borrar", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar", color = Primary)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String, slate: Color) {
    Text(
        text,
        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Black, color = slate, letterSpacing = 2.sp)
    )
}

@Composable
private fun QuotaCard(label: String, emoji: String, used: Int, limit: Int, color: Color, surface: Color) {
    val pct = (used.toFloat() / limit).coerceIn(0f, 1f)
    val danger = pct > 0.80f
    val barColor = if (danger) Color(0xFFFF5252) else color

    Surface(shape = RoundedCornerShape(18.dp), color = surface, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 20.sp)
                    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$used / ${formatNum(limit)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = barColor
                    )
                    Text(
                        "${(pct * 100).toInt()}% del límite diario",
                        fontSize = 10.sp,
                        color = if (danger) Color(0xFFFF5252) else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(barColor.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(pct)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(barColor, barColor.copy(alpha = 0.7f))))
                )
            }
        }
    }
}

@Composable
private fun PeriodStatsTable(
    state: MonitoringUiState,
    primary: Color, secondary: Color, tertiary: Color, slate: Color, surface: Color
) {
    Surface(shape = RoundedCornerShape(18.dp), color = surface, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("", modifier = Modifier.weight(1.5f))
                listOf("HOY", "SEMANA", "MES", "TOTAL").forEach {
                    Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Black, color = slate, letterSpacing = 1.sp))
                }
            }
            HorizontalDivider(color = slate.copy(alpha = 0.1f))
            // Rows
            StatRow("✍️  Escrituras", primary, state.todayWrites, state.weekWrites, state.monthWrites, state.totalWrites)
            StatRow("📖  Lecturas",   secondary, state.todayReads, state.weekReads, state.monthReads, state.totalReads)
            StatRow("🗑️  Eliminaciones", tertiary, state.todayDeletes, state.weekDeletes, state.monthDeletes, state.totalDeletes)
        }
    }
}

@Composable
private fun StatRow(label: String, color: Color, today: Int, week: Int, month: Int, total: Int) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1.5f), fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold)
        listOf(today, week, month, total).forEach { value ->
            Text(
                value.toString(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LogRow(log: OpLog, surface: Color, slate: Color, primary: Color) {
    val fmt = SimpleDateFormat("MMM dd  HH:mm:ss", Locale.getDefault())
    val dotColor = when (log.type) {
        OpType.WRITE  -> primary
        OpType.READ   -> Color(0xFF00ACC1)
        OpType.DELETE -> Color(0xFFFF5252)
    }

    Surface(shape = RoundedCornerShape(12.dp), color = surface, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.size(8.dp).background(dotColor, CircleShape))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${log.type.emoji}  ${log.type.label}  ·  ${log.source}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    fmt.format(Date(log.timestamp)),
                    fontSize = 11.sp,
                    color = slate
                )
            }
            if (log.count > 1) {
                Text(
                    "×${log.count}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = dotColor
                )
            }
        }
    }
}

private fun formatNum(n: Int): String = when {
    n >= 1_000 -> "${n / 1_000}k"
    else -> n.toString()
}
