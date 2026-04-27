package com.example.timedrop.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AmbientSoundType(val displayName: String, val rawRes: String) {
    RAIN("Rain", "rain"),
    WAVES("Waves", "waves"),
    FOREST("Forest", "forest")
}

data class AmbientSoundState(
    val type: AmbientSoundType,
    val isPlaying: Boolean = false,
    val volume: Float = 0.5f
)

object AmbientSoundManager {
    private val _sounds = MutableStateFlow(
        AmbientSoundType.values().associateWith { AmbientSoundState(it) }
    )
    val sounds = _sounds.asStateFlow()

    private val players = mutableMapOf<AmbientSoundType, MediaPlayer>()

    fun toggleSound(context: Context, type: AmbientSoundType) {
        val currentState = _sounds.value[type] ?: return
        if (currentState.isPlaying) {
            stopSound(type)
        } else {
            playSound(context, type)
        }
    }

    fun setVolume(type: AmbientSoundType, volume: Float) {
        players[type]?.setVolume(volume, volume)
        _sounds.update { it.toMutableMap().apply { 
            this[type] = this[type]?.copy(volume = volume) ?: AmbientSoundState(type, volume = volume)
        } }
    }

    private fun playSound(context: Context, type: AmbientSoundType) {
        try {
            // Cancel existing player for this type if any
            players[type]?.release()
            
            val resId = context.resources.getIdentifier(type.rawRes, "raw", context.packageName)
            if (resId == 0) return // Resource not found yet
            
            val player = MediaPlayer.create(context, resId).apply {
                isLooping = true
                setVolume(_sounds.value[type]?.volume ?: 0.5f, _sounds.value[type]?.volume ?: 0.5f)
            }
            
            players[type] = player
            player.start()
            
            _sounds.update { it.toMutableMap().apply { 
                this[type] = this[type]?.copy(isPlaying = true) ?: AmbientSoundState(type, isPlaying = true)
            } }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopSound(type: AmbientSoundType) {
        players[type]?.stop()
        players[type]?.release()
        players.remove(type)
        _sounds.update { it.toMutableMap().apply { 
            this[type] = this[type]?.copy(isPlaying = false) ?: AmbientSoundState(type, isPlaying = false)
        } }
    }
}
