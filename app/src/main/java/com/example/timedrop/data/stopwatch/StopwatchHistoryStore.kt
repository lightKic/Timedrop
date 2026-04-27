package com.example.timedrop.data.stopwatch

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val Context.dataStore by preferencesDataStore(name = "stopwatch")

data class StopwatchHistoryItem(
    val id: String,
    val name: String,
    val elapsedMs: Long,
    val createdAtEpochMs: Long,
)

class StopwatchHistoryStore(private val context: Context) {
    private object Keys {
        val ITEMS = stringSetPreferencesKey("history_items_v1")
    }

    val historyFlow: Flow<List<StopwatchHistoryItem>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[Keys.ITEMS].orEmpty()
            raw.mapNotNull { decode(it) }
                .sortedByDescending { it.createdAtEpochMs }
        }

    suspend fun add(item: StopwatchHistoryItem) {
        context.dataStore.edit { prefs ->
            val set = prefs[Keys.ITEMS].orEmpty().toMutableSet()
            set.add(encode(item))
            prefs[Keys.ITEMS] = set
        }
    }

    suspend fun delete(id: String) {
        context.dataStore.edit { prefs ->
            val set = prefs[Keys.ITEMS].orEmpty().toMutableSet()
            val filtered = set.filterNot { entry -> decode(entry)?.id == id }.toSet()
            prefs[Keys.ITEMS] = filtered
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs -> prefs.remove(Keys.ITEMS) }
    }

    private fun encode(item: StopwatchHistoryItem): String {
        fun e(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.toString())
        return listOf(
            e(item.id),
            e(item.name),
            item.elapsedMs.toString(),
            item.createdAtEpochMs.toString(),
        ).joinToString("|")
    }

    private fun decode(s: String): StopwatchHistoryItem? {
        return runCatching {
            val parts = s.split("|")
            if (parts.size != 4) return null
            fun d(v: String) = URLDecoder.decode(v, StandardCharsets.UTF_8.toString())
            StopwatchHistoryItem(
                id = d(parts[0]),
                name = d(parts[1]),
                elapsedMs = parts[2].toLong(),
                createdAtEpochMs = parts[3].toLong(),
            )
        }.getOrNull()
    }
}

