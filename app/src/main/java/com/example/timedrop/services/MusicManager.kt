package com.example.timedrop.services

import android.graphics.Bitmap
import android.media.session.MediaController
import android.media.session.PlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveTrack(
    val title: String,
    val artist: String,
    val durationMs: Long,
    val albumArt: Bitmap? = null
)

object MusicManager {
    var applicationContext: android.content.Context? = null
    
    private val _currentTrack = MutableStateFlow<ActiveTrack?>(null)
    val currentTrack: StateFlow<ActiveTrack?> = _currentTrack.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackState?>(null)
    val playbackState: StateFlow<PlaybackState?> = _playbackState.asStateFlow()

    // Active controller attached to the system's current media player
    var mediaController: MediaController? = null

    fun updateTrack(track: ActiveTrack?) {
        _currentTrack.value = track
    }

    fun updatePlaybackState(state: PlaybackState?) {
        _playbackState.value = state
    }

    fun play() {
        mediaController?.transportControls?.play()
    }

    fun pause() {
        mediaController?.transportControls?.pause()
    }

    fun skipToNext() {
        mediaController?.transportControls?.skipToNext()
    }

    fun skipToPrevious() {
        mediaController?.transportControls?.skipToPrevious()
    }

    fun skipToQueueItem(id: Long) {
        mediaController?.transportControls?.skipToQueueItem(id)
    }
}
