package com.example.timedrop.services

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Handler
import android.os.Looper
import androidx.core.os.BundleCompat
import android.util.Log

class MusicNotificationListener : NotificationListenerService(), MediaSessionManager.OnActiveSessionsChangedListener {

    private var currentController: MediaController? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val mediaControllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            MusicManager.updatePlaybackState(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateMusicManagerMetadata(metadata)
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            refreshFromSessions()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        // We still check notifications as a trigger for some older apps
        refreshFromSessions()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        refreshFromSessions()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        MusicManager.applicationContext = applicationContext
        val mm = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
        try {
            val componentName = ComponentName(this, MusicNotificationListener::class.java)
            mm?.addOnActiveSessionsChangedListener(this, componentName)
        } catch (e: Exception) {
            Log.e("MusicListener", "Error adding listener", e)
        }
        refreshFromSessions()
    }

    override fun onActiveSessionsChanged(controllers: MutableList<MediaController>?) {
        refreshFromSessions()
    }

    private fun refreshFromSessions() {
        val mm = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
        try {
            val componentName = ComponentName(this, MusicNotificationListener::class.java)
            val controllers = mm.getActiveSessions(componentName)
            
            // Prioritize: 
            // 1. Existing controller if still playing
            // 2. Any other playing controller
            // 3. First available controller
            
            val playing = controllers.find { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            val best = if (currentController?.playbackState?.state == PlaybackState.STATE_PLAYING) {
                currentController
            } else {
                playing ?: controllers.firstOrNull()
            }

            if (best != null) {
                setupController(best)
            } else {
                // Check notifications as fallback before clearing
                val hasMediaNotification = activeNotifications?.any { 
                    it.notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION) 
                } ?: false
                
                if (!hasMediaNotification) {
                    clearController()
                }
            }
        } catch (e: Exception) {
            Log.e("MusicListener", "Error refreshing", e)
        }
    }

    private fun setupController(controller: MediaController) {
        if (currentController?.sessionToken == controller.sessionToken) {
            if (MusicManager.mediaController == null) {
                syncWithManager(controller)
            }
            return
        }

        currentController?.unregisterCallback(mediaControllerCallback)
        currentController = controller
        currentController?.registerCallback(mediaControllerCallback, mainHandler)
        syncWithManager(controller)
    }

    private fun syncWithManager(controller: MediaController) {
        MusicManager.mediaController = controller
        updateMusicManagerMetadata(controller.metadata)
        MusicManager.updatePlaybackState(controller.playbackState)
    }

    private fun clearController() {
        currentController?.unregisterCallback(mediaControllerCallback)
        currentController = null
        MusicManager.mediaController = null
        MusicManager.updateTrack(null)
        MusicManager.updatePlaybackState(null)
    }

    private fun updateMusicManagerMetadata(metadata: MediaMetadata?) {
        if (metadata == null) return

        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)

        if (title == null && artist == null) return

        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val albumArt = try {
            metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
        } catch (e: Exception) { null }

        MusicManager.updateTrack(ActiveTrack(
            title = title ?: "Unknown",
            artist = artist ?: "Unknown",
            durationMs = duration,
            albumArt = albumArt
        ))
    }
}
