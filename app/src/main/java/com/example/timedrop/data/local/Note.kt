package com.example.timedrop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val date: String, // String for simplicity in display, e.g. "Oct 24, 2023"
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "Ideas" // Default category
)
