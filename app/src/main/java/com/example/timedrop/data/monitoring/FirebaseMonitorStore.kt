package com.example.timedrop.data.monitoring

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Singleton monitor — initialized once from Application.onCreate.
 * Stores logs in-memory (StateFlow) and persists to a JSON file.
 */
object FirebaseMonitorStore {

    private const val FILE_NAME = "firebase_monitor_logs.json"
    private const val MAX_LOGS  = 500

    private var logFile: File? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _logs = MutableStateFlow<List<OpLog>>(emptyList())
    val logsFlow: StateFlow<List<OpLog>> = _logs.asStateFlow()

    fun init(context: Context) {
        logFile = File(context.filesDir, FILE_NAME)
        // Load persisted logs on startup
        scope.launch { _logs.value = loadFromFile() }
    }

    /** Fire-and-forget — safe to call from any thread */
    fun record(log: OpLog) {
        scope.launch {
            val updated = listOf(log) + _logs.value
            val trimmed = if (updated.size > MAX_LOGS) updated.take(MAX_LOGS) else updated
            _logs.value = trimmed
            saveToFile(trimmed)
        }
    }

    fun clearAll() {
        scope.launch {
            _logs.value = emptyList()
            logFile?.writeText("[]")
        }
    }

    // ── Persistence ──────────────────────────────────────────────

    private fun loadFromFile(): List<OpLog> {
        val file = logFile ?: return emptyList()
        if (!file.exists()) return emptyList()
        return try {
            val array = JSONArray(file.readText())
            (0 until array.length()).mapNotNull { i ->
                runCatching {
                    val obj = array.getJSONObject(i)
                    OpLog(
                        timestamp = obj.getLong("ts"),
                        type      = OpType.valueOf(obj.getString("type")),
                        source    = obj.getString("src"),
                        count     = obj.getInt("cnt")
                    )
                }.getOrNull()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveToFile(logs: List<OpLog>) {
        val file = logFile ?: return
        runCatching {
            val array = JSONArray()
            logs.forEach { log ->
                array.put(JSONObject().apply {
                    put("ts",   log.timestamp)
                    put("type", log.type.name)
                    put("src",  log.source)
                    put("cnt",  log.count)
                })
            }
            file.writeText(array.toString())
        }
    }
}
