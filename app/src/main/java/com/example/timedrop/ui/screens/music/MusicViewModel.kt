package com.example.timedrop.ui.screens.music

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import androidx.lifecycle.viewModelScope
import com.example.timedrop.services.MusicManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flow
import android.os.SystemClock
import android.media.session.PlaybackState
import android.graphics.Bitmap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings

data class Track(
    val title: String,
    val artist: String,
    val durationSec: Int,
    val albumArt: Bitmap? = null,
    val queueId: Long = -1L
)

data class MusicState(
    val currentTrack: Track? = null,
    val upNext: List<Track> = emptyList(),
    val isPlaying: Boolean = false,
    val progressFraction: Float = 0f,
    val musicVolume: Float = 0.5f // 0f to 1f
)

@OptIn(ExperimentalCoroutinesApi::class)
class MusicViewModel : ViewModel() {
    private fun tickerFlow(periodMillis: Long) = flow {
        emit(Unit) // Emit immediately on start
        while (true) {
            delay(periodMillis)
            emit(Unit)
        }
    }


    private val _volumeState = MutableStateFlow(0.5f)
    

    val uiState: StateFlow<MusicState> = combine(
        MusicManager.currentTrack,
        MusicManager.playbackState,
        _volumeState,
        // Ticker ONLY when playing, reduces CPU usage on pause
        MusicManager.playbackState.flatMapLatest { state ->
            if (state?.state == PlaybackState.STATE_PLAYING) {
                tickerFlow(1000) // Lower frequency, UI will smooth it out
            } else {
                flow { emit(Unit) }
            }
        }
    ) { track, state, volume, _ ->
        val controller = MusicManager.mediaController
        val queue = controller?.queue?.map { item ->
            Track(
                title = item.description.title?.toString() ?: "Unknown",
                artist = item.description.subtitle?.toString() ?: "UnknownArtist",
                durationSec = 0, // Queue items usually don't have duration in description
                albumArt = item.description.iconBitmap,
                queueId = item.queueId
            )
        }?.take(10) ?: emptyList()

        if (track != null && state != null) {
            val isPlaying = state.state == PlaybackState.STATE_PLAYING
            
            val currentPos = if (isPlaying) {
                val timeSinceUpdate = SystemClock.elapsedRealtime() - state.lastPositionUpdateTime
                state.position + (timeSinceUpdate * state.playbackSpeed).toLong()
            } else {
                state.position
            }

            val progress = if (track.durationMs > 0) currentPos.toFloat() / track.durationMs.toFloat() else 0f
            
            MusicState(
                currentTrack = Track(track.title, track.artist, (track.durationMs / 1000).toInt(), track.albumArt),
                upNext = queue,
                isPlaying = isPlaying,
                progressFraction = progress.coerceIn(0f, 1f),
                musicVolume = volume
            )
        } else {
            MusicState(
                currentTrack = null,
                upNext = queue,
                isPlaying = false,
                progressFraction = 0f,
                musicVolume = volume
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MusicState())

    fun togglePlayPause() {
        if (MusicManager.currentTrack.value != null) {
            val isPlaying = MusicManager.playbackState.value?.state == PlaybackState.STATE_PLAYING
            if (isPlaying) MusicManager.pause() else MusicManager.play()
        }
    }

    fun skipNext() {
        MusicManager.skipToNext()
    }

    fun skipPrevious() {
        MusicManager.skipToPrevious()
    }

    fun playQueueItem(id: Long) {
        MusicManager.skipToQueueItem(id)
    }

    // ── Volume Control ──
    private val audioManager: android.media.AudioManager? by lazy {
        MusicManager.applicationContext?.getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager
    }

    private val volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            _volumeState.value = getMusicVolume()
        }
    }

    init {
        // Initial volume
        _volumeState.value = getMusicVolume()
        
        // Register observer for hardware volume buttons
        MusicManager.applicationContext?.contentResolver?.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            volumeObserver
        )
    }

    override fun onCleared() {
        super.onCleared()
        MusicManager.applicationContext?.contentResolver?.unregisterContentObserver(volumeObserver)
    }

    fun setMusicVolume(fraction: Float) {
        audioManager?.let { am ->
            val maxVolume = am.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
            val newVolume = (fraction * maxVolume).toInt()
            am.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVolume, 0)
            _volumeState.value = fraction // Immediate UI update
        }
    }

    fun getMusicVolume(): Float {
        val am = audioManager ?: return 0.5f
        val maxVolume = am.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        val currentVolume = am.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        return if (maxVolume > 0) currentVolume.toFloat() / maxVolume.toFloat() else 0.5f
    }
}
