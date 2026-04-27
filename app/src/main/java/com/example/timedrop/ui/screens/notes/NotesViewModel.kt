package com.example.timedrop.ui.screens.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timedrop.data.local.AppDatabase
import com.example.timedrop.data.local.Note
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao = AppDatabase.getDatabase(application).noteDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allNotes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                noteDao.getAllNotes()
            } else {
                noteDao.searchNotes(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

    fun saveNote(id: Int, title: String, content: String, category: String) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
            val note = Note(
                id = if (id == 0) 0 else id,
                title = title,
                content = content,
                date = dateStr,
                timestamp = System.currentTimeMillis(),
                category = category
            )
            if (id == 0) {
                noteDao.insertNote(note)
            } else {
                noteDao.updateNote(note)
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }
}
