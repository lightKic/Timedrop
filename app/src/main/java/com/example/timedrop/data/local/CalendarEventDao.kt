package com.example.timedrop.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY date ASC, time ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE date = :date ORDER BY time ASC")
    fun getEventsForDate(date: String): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE date LIKE :monthPrefix || '%'")
    fun getEventsForMonth(monthPrefix: String): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE isTask = 1 AND isCompleted = 0 ORDER BY date ASC, time ASC")
    fun getUncompletedTasks(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Update
    suspend fun updateEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Query("DELETE FROM calendar_events")
    suspend fun deleteAllEvents()
}
