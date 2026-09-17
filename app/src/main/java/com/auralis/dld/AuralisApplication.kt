package com.auralis.dld

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class AuralisApplication : Application() {

    companion object {
        const val DOWNLOAD_CHANNEL_ID = "auralis_downloads_channel"
        const val MUSIC_PLAYBACK_CHANNEL_ID = "auralis_music_playback_channel"
        const val VIDEO_PLAYBACK_CHANNEL_ID = "auralis_video_playback_channel"
        const val PLAYBACK_CHANNEL_ID = MUSIC_PLAYBACK_CHANNEL_ID
    }

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Chaquopy embedded Python engine
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        // 2. Configure Coil ImageLoader with VideoFrameDecoder for real video thumbnails
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
        Coil.setImageLoader(imageLoader)

        // 3. Create notification channels
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Downloads notification channel (Data Sync)
            val downloadChannel = NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                "Media Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of active media downloads and transcoding"
                setShowBadge(false)
            }

            // Audio/Music playback channel (Media Playback)
            val musicChannel = NotificationChannel(
                MUSIC_PLAYBACK_CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows media player controls during music playback"
                setShowBadge(false)
            }

            // Video playback channel (Media Playback)
            val videoChannel = NotificationChannel(
                VIDEO_PLAYBACK_CHANNEL_ID,
                "Video Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows media player controls during video playback"
                setShowBadge(false)
            }

            notificationManager?.createNotificationChannel(downloadChannel)
            notificationManager?.createNotificationChannel(musicChannel)
            notificationManager?.createNotificationChannel(videoChannel)
        }
    }
}
