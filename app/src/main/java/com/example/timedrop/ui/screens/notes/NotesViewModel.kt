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
import com.example.timedrop.data.SyncRepository
import com.example.timedrop.data.settings.SettingsDataStore


@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val noteDao = db.noteDao()
    private val syncRepository = SyncRepository(db)
    private val settingsStore = SettingsDataStore(application)

    init {
        viewModelScope.launch {
            syncRepository.signInAnonymously()
            settingsStore.settingsFlow.collect { settings ->
                if (settings.autoSyncEnabled) {
                    syncRepository.startRealtimeSync()
                } else {
                    syncRepository.stopRealtimeSync()
                }
            }
        }
    }


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
                val newId = noteDao.insertNote(note)
                if (settingsStore.settingsFlow.first().autoSyncEnabled) {
                    syncRepository.syncNote(note.copy(id = newId.toInt()))
                }
            } else {
                noteDao.updateNote(note)
                if (settingsStore.settingsFlow.first().autoSyncEnabled) {
                    syncRepository.syncNote(note)
                }
            }
        }

    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
            if (settingsStore.settingsFlow.first().autoSyncEnabled) {
                syncRepository.deleteNote(note.id)
            }
        }

    }
}
