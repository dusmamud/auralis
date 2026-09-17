package com.auralis.dld.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.auralis.dld.AuralisApplication
import com.auralis.dld.MainActivity
import com.auralis.dld.R
import com.auralis.dld.player.AuralisPlayerController
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private lateinit var playerController: AuralisPlayerController
    private var mediaSession: MediaSession? = null

    companion object {
        const val TAG = "PlaybackService"
        const val NOTIFICATION_ID = 3001
        const val ACTION_STOP_PLAYBACK = "com.auralis.dld.ACTION_STOP_PLAYBACK"
    }

    private val callback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            Log.d(TAG, "MediaSession.Callback onConnect from: ${controller.packageName}")
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().build()
            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon().build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .build()
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            Log.d(TAG, "MediaSession.Callback onPlaybackResumption")
            val player = mediaSession.player
            val currentItemIndex = player.currentMediaItemIndex
            val currentPosition = player.currentPosition
            val mediaItems = mutableListOf<MediaItem>()
            for (i in 0 until player.mediaItemCount) {
                mediaItems.add(player.getMediaItemAt(i))
            }
            return Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(
                    mediaItems,
                    currentItemIndex.coerceAtLeast(0),
                    currentPosition.coerceAtLeast(0L)
                )
            )
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PlaybackService onCreate")
        playerController = AuralisPlayerController.getInstance(this)
        createNotificationChannel()

        setListener(object : MediaSessionService.Listener {
            override fun onForegroundServiceStartNotAllowedException() {
                Log.e(TAG, "onForegroundServiceStartNotAllowedException triggered")
            }
        })

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Native Media3 Notification Provider - Single authoritative media notification
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(AuralisApplication.MUSIC_PLAYBACK_CHANNEL_ID)
            .setChannelName(R.string.playback_channel_name)
            .setNotificationId(NOTIFICATION_ID)
            .build().apply {
                setSmallIcon(R.drawable.ic_stat_auralis_dld)
            }
        setMediaNotificationProvider(notificationProvider)

        val builder = MediaSession.Builder(this, playerController.exoPlayer)
            .setSessionActivity(pendingIntent)
            .setCallback(callback)
            .setId("AuralisMediaSession")

        mediaSession = builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                AuralisApplication.MUSIC_PLAYBACK_CHANNEL_ID,
                getString(R.string.playback_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Auralis Media Playback Controls"
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_PLAYBACK) {
            Log.d(TAG, "onStartCommand: Received ACTION_STOP_PLAYBACK")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping foreground: ${e.message}")
            }
            stopSelf()
            return START_NOT_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || player.mediaItemCount == 0 || player.playbackState == androidx.media3.common.Player.STATE_IDLE) {
            Log.d(TAG, "onTaskRemoved: Stopping service because player is idle/empty")
            stopSelf()
        } else {
            Log.d(TAG, "onTaskRemoved: Keeping service alive in background (playback active)")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "PlaybackService onDestroy")
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
