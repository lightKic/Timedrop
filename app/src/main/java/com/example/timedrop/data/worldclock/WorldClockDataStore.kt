package com.example.timedrop.data.worldclock

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.timedrop.ui.screens.worldclock.CityInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.worldClockDataStore by preferencesDataStore(name = "worldclock")

class WorldClockDataStore(private val context: Context) {
    private object Keys {
        val SAVED_CITIES = stringPreferencesKey("saved_cities")
    }

    val savedCitiesFlow: Flow<List<CityInfo>> = context.worldClockDataStore.data.map { prefs ->
        val serialized = prefs[Keys.SAVED_CITIES] ?: ""
        if (serialized.isEmpty()) {
            emptyList()
        } else {
            serialized.split("|").mapNotNull {
                val parts = it.split(",")
                if (parts.size == 2) CityInfo(parts[0], parts[1]) else null
            }
        }
    }

    suspend fun saveCities(cities: List<CityInfo>) {
        val serialized = cities.joinToString("|") { "${it.name},${it.zoneIdStr}" }
        context.worldClockDataStore.edit { prefs ->
            prefs[Keys.SAVED_CITIES] = serialized
        }
    }
}
