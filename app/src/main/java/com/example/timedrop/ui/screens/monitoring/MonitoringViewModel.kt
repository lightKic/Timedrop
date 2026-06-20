package com.example.timedrop.ui.screens.monitoring

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timedrop.data.monitoring.FirebaseMonitorStore
import com.example.timedrop.data.monitoring.OpLog
import com.example.timedrop.data.monitoring.OpType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

data class MonitoringUiState(
    val totalWrites: Int = 0,
    val totalReads: Int = 0,
    val totalDeletes: Int = 0,
    val todayWrites: Int = 0,
    val todayReads: Int = 0,
    val todayDeletes: Int = 0,
    val weekWrites: Int = 0,
    val weekReads: Int = 0,
    val weekDeletes: Int = 0,
    val monthWrites: Int = 0,
    val monthReads: Int = 0,
    val monthDeletes: Int = 0,
    val recentLogs: List<OpLog> = emptyList(),
    val dailyWriteLimit: Int = 20_000,
    val dailyReadLimit: Int = 50_000,
    val dailyDeleteLimit: Int = 20_000
)

class MonitoringViewModel(application: Application) : AndroidViewModel(application) {

    val uiState: StateFlow<MonitoringUiState> = FirebaseMonitorStore.logsFlow
        .map { logs -> buildState(logs) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,   // Eagerly so initial value emits immediately
            initialValue = MonitoringUiState()
        )

    fun clearLogs() = FirebaseMonitorStore.clearAll()

    private fun buildState(logs: List<OpLog>): MonitoringUiState {
        if (logs.isEmpty()) return MonitoringUiState()

        val now = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val weekFields = WeekFields.of(Locale.getDefault())

        fun OpLog.toDate(): LocalDate =
            Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()

        fun List<OpLog>.countType(type: OpType) = filter { it.type == type }.sumOf { it.count }

        val todayLogs = logs.filter { it.toDate() == now }
        val weekLogs  = logs.filter {
            val d = it.toDate()
            d.get(weekFields.weekOfWeekBasedYear()) == now.get(weekFields.weekOfWeekBasedYear()) &&
                d.year == now.year
        }
        val monthLogs = logs.filter {
            val d = it.toDate()
            d.monthValue == now.monthValue && d.year == now.year
        }

        return MonitoringUiState(
            totalWrites  = logs.countType(OpType.WRITE),
            totalReads   = logs.countType(OpType.READ),
            totalDeletes = logs.countType(OpType.DELETE),
            todayWrites  = todayLogs.countType(OpType.WRITE),
            todayReads   = todayLogs.countType(OpType.READ),
            todayDeletes = todayLogs.countType(OpType.DELETE),
            weekWrites   = weekLogs.countType(OpType.WRITE),
            weekReads    = weekLogs.countType(OpType.READ),
            weekDeletes  = weekLogs.countType(OpType.DELETE),
            monthWrites  = monthLogs.countType(OpType.WRITE),
            monthReads   = monthLogs.countType(OpType.READ),
            monthDeletes = monthLogs.countType(OpType.DELETE),
            recentLogs   = logs.take(50)
        )
    }
}
