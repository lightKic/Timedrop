package com.example.timedrop.services

import android.app.Notification
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.os.Build
import androidx.core.os.BundleCompat
import com.example.timedrop.services.ActiveTrack
import com.example.timedrop.services.MusicManager

class MusicNotificationListener : NotificationListenerService() {

    private var currentController: MediaController? = null

    private val mediaControllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            MusicManager.updatePlaybackState(state)
            
            // If the state is stopped or none, we might want to eventually clear it
            if (state != null && (state.state == PlaybackState.STATE_STOPPED || state.state == PlaybackState.STATE_NONE)) {
                MusicManager.updateTrack(null)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            updateMusicManagerMetadata(metadata)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        handleNotification(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        val extras = sbn.notification.extras
        val token = BundleCompat.getParcelable(extras, Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
        
        // Check if there are ANY media notifications left at all
        val hasAnyMediaLeft = activeNotifications?.any { 
            BundleCompat.getParcelable(it.notification.extras, Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java) != null 
        } ?: false

        if (!hasAnyMediaLeft) {
            currentController?.unregisterCallback(mediaControllerCallback)
            currentController = null
            MusicManager.mediaController = null
            MusicManager.updateTrack(null)
            MusicManager.updatePlaybackState(null)
            return
        }

        // Only clear if the token being removed is the same as the current controller
        if (token != null && currentController?.sessionToken == token) {
            currentController?.unregisterCallback(mediaControllerCallback)
            currentController = null
            MusicManager.mediaController = null
            MusicManager.updateTrack(null)
            MusicManager.updatePlaybackState(null)
            
            // Try to pick up another active one if exists
            activeNotifications?.forEach { handleNotification(it) }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        MusicManager.applicationContext = applicationContext
        // Check existing notifications on startup
        activeNotifications?.forEach { handleNotification(it) }
    }

    private fun handleNotification(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val token = BundleCompat.getParcelable(extras, Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java) as? MediaSession.Token

        if (token != null) {
            setupController(token)
        } else {
            // Some apps like Spotify might post the notification slightly before the token is attached.
            // Aggressive retry: try every 200ms for 5 times (total 1s)
            scheduleRetry(sbn, 5)
        }
    }

    private fun scheduleRetry(sbn: StatusBarNotification, remainingRetries: Int) {
        if (remainingRetries <= 0) return
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val retryExtras = sbn.notification.extras
            val retryToken = BundleCompat.getParcelable(retryExtras, Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java) as? MediaSession.Token
            if (retryToken != null) {
                setupController(retryToken)
            } else {
                scheduleRetry(sbn, remainingRetries - 1)
            }
        }, 200)
    }

    private fun setupController(token: MediaSession.Token) {
        // Same session, ignore
        if (currentController?.sessionToken == token) return

        // Unregister old controller if present
        currentController?.unregisterCallback(mediaControllerCallback)

        // Register new controller
        currentController = MediaController(this, token)
        currentController?.registerCallback(mediaControllerCallback)
        
        // Link to manager
        MusicManager.mediaController = currentController
        
        // Initial payload
        updateMusicManagerMetadata(currentController?.metadata)
        MusicManager.updatePlaybackState(currentController?.playbackState)
    }

    private fun updateMusicManagerMetadata(metadata: MediaMetadata?) {
        if (metadata == null) return // Don't clear if we just get a null update (keep last)

        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
        
        // If we have no title/artist, don't update with "Unknown" yet, might be a partial update
        if (title == null && artist == null) return

        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val albumArt = try {
             metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) 
                ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
        } catch (e: Exception) { null }

        val track = ActiveTrack(
            title = title ?: "Unknown",
            artist = artist ?: "Unknown",
            durationMs = duration,
            albumArt = albumArt
        )
        MusicManager.updateTrack(track)
    }
}
