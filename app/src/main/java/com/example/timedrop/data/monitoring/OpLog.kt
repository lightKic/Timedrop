package com.example.timedrop.data.monitoring

data class OpLog(
    val timestamp: Long = System.currentTimeMillis(),
    val type: OpType,
    val source: String,   // "event", "note", "settings", "batch_upload", "batch_download"
    val count: Int = 1
)

enum class OpType(val label: String, val emoji: String) {
    WRITE("Escritura", "✍️"),
    READ("Lectura", "📖"),
    DELETE("Eliminación", "🗑️")
}
