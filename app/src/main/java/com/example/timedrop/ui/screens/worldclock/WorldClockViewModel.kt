package com.example.timedrop.ui.screens.worldclock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timedrop.data.worldclock.WorldClockDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CityInfo(
    val name: String,
    val zoneIdStr: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class CityTimeState(
    val city: CityInfo,
    val timeStr: String,
    val amPm: String,
    val offsetStr: String,
    val dayStatus: String,
    val isDaytime: Boolean
)

data class WorldClockUiState(
    val currentLocalTime: ZonedDateTime = ZonedDateTime.now(),
    val savedCities: List<CityTimeState> = emptyList(),
    val availableCities: List<CityInfo> = emptyList(),
    val isAddCityDialogOpen: Boolean = false,
)

class WorldClockViewModel(app: Application) : AndroidViewModel(app) {

    private val dataStore = WorldClockDataStore(app.applicationContext)
    
    private val _uiState = MutableStateFlow(WorldClockUiState())
    val uiState: StateFlow<WorldClockUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm", Locale.getDefault())
    private val amPmFormatter = DateTimeFormatter.ofPattern("a", Locale.getDefault())

    // Hardcoded available cities
    private val allAvailableCities = listOf(
        CityInfo("New York", "America/New_York", 40.71, -74.00),
        CityInfo("Tokyo", "Asia/Tokyo", 35.67, 139.65),
        CityInfo("London", "Europe/London", 51.50, -0.12),
        CityInfo("Paris", "Europe/Paris", 48.85, 2.35),
        CityInfo("San Francisco", "America/Los_Angeles", 37.77, -122.41),
        CityInfo("Sydney", "Australia/Sydney", -33.86, 151.20),
        CityInfo("Dubai", "Asia/Dubai", 25.20, 55.27),
        CityInfo("Singapore", "Asia/Singapore", 1.35, 103.81),
        CityInfo("Berlin", "Europe/Berlin", 52.52, 13.40),
        CityInfo("São Paulo", "America/Sao_Paulo", -23.55, -46.63),
        CityInfo("Mexico City", "America/Mexico_City", 19.43, -99.13),
        CityInfo("Madrid", "Europe/Madrid", 40.41, -3.70)
    )
    
    // Internal state cache of persisted cities
    private var internalSavedCities: List<CityInfo> = emptyList()

    init {
        // Collect persisted cities
        viewModelScope.launch {
            dataStore.savedCitiesFlow.collect { persistedCities ->
                // Provide a default list if none was ever saved
                val targetCities = if (persistedCities.isEmpty()) {
                    listOf(
                        allAvailableCities.first { it.name == "Tokyo" },
                        allAvailableCities.first { it.name == "London" },
                        allAvailableCities.first { it.name == "San Francisco" }
                    )
                } else {
                    // Enrich persisted cities with coordinates from our master list
                    persistedCities.map { persisted ->
                        allAvailableCities.find { it.name == persisted.name } ?: persisted
                    }
                }
                
                internalSavedCities = targetCities
                updateUiState(internalSavedCities, ZonedDateTime.now())
            }
        }
        
        startClock()
    }

    private fun startClock() {
        viewModelScope.launch {
            while (isActive) {
                val now = ZonedDateTime.now()
                updateUiState(internalSavedCities, now)
                delay(1000)
            }
        }
    }
    
    private fun updateUiState(cities: List<CityInfo>, now: ZonedDateTime) {
        val available = allAvailableCities.filterNot { availableCity ->
            cities.any { it.name == availableCity.name }
        }
        
        _uiState.update { current ->
            current.copy(
                currentLocalTime = now,
                savedCities = cities.map { calculateCityState(it, now) },
                availableCities = available
            )
        }
    }

    private fun calculateCityState(city: CityInfo, localNow: ZonedDateTime): CityTimeState {
        val zoneId = ZoneId.of(city.zoneIdStr)
        val cityTime = ZonedDateTime.now(zoneId)

        // Calculate hours difference relative to localNow using totalSeconds (accurate for DST constraints occasionally)
        val localOffsetSec = localNow.offset.totalSeconds
        val cityOffsetSec = cityTime.offset.totalSeconds
        val diffHours = (cityOffsetSec - localOffsetSec) / 3600
        
        val sign = if (diffHours >= 0) "+" else ""
        val offsetStr = "${sign}${diffHours}HRS"

        // Calculate day status
        val localDayOfYear = localNow.dayOfYear
        val cityDayOfYear = cityTime.dayOfYear
        var dayStatus = "Today"
        if (localNow.year == cityTime.year) {
            when (cityDayOfYear - localDayOfYear) {
                1 -> dayStatus = "Tomorrow"
                -1 -> dayStatus = "Yesterday"
            }
        }

        val hour = cityTime.hour
        val isDaytime = hour in 6..18

        return CityTimeState(
            city = city,
            timeStr = cityTime.format(timeFormatter),
            amPm = cityTime.format(amPmFormatter),
            offsetStr = offsetStr,
            dayStatus = dayStatus,
            isDaytime = isDaytime
        )
    }

    fun openAddCityDialog() {
        _uiState.update { it.copy(isAddCityDialogOpen = true) }
    }

    fun closeAddCityDialog() {
        _uiState.update { it.copy(isAddCityDialogOpen = false) }
    }

    fun addCity(city: CityInfo) {
        // Enforce maximum of 5 cities limit
        if (internalSavedCities.size >= 5) return
        
        // Prevent duplicate city insertion
        if (internalSavedCities.any { it.name == city.name }) return
        
        val newCities = internalSavedCities + city
        viewModelScope.launch {
            dataStore.saveCities(newCities) // triggers flow collect which updates UI state
            closeAddCityDialog()
        }
    }
    
    fun removeCity(city: CityInfo) {
        val newCities = internalSavedCities.filterNot { it.name == city.name }
        viewModelScope.launch {
            dataStore.saveCities(newCities)
        }
    }
}
