package com.example.timedrop.data.local

import androidx.room.*
import java.time.LocalDate

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val date: String, // Stored as ISO-8601 string (YYYY-MM-DD)
    val time: String, // e.g., "09:30"
    val colorArgb: Int,
    val description: String = "",
    val isTask: Boolean = false,
    val isCompleted: Boolean = false,
    val repeatInterval: String = "none", // none, daily, weekly
    val completedAt: Long = 0L // Timestamp when task was completed
) {
    @Ignore
    val localDate: LocalDate = LocalDate.parse(date)
}
