package com.auralis.dld.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.auralis.dld.AuralisApplication
import com.auralis.dld.MainActivity
import com.auralis.dld.R
import com.auralis.dld.data.repository.DownloadProgressListener
import com.auralis.dld.data.repository.DownloadRepository
import com.auralis.dld.data.repository.MediaStoreRepository
import com.auralis.dld.data.repository.PythonEngineRepository
import com.auralis.dld.domain.model.DownloadItem
import com.auralis.dld.domain.model.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class DownloadService : Service() {

    companion object {
        const val NOTIFICATION_ID = 2001
        const val ACTION_START_DOWNLOAD = "com.auralis.dld.START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.auralis.dld.CANCEL_DOWNLOAD"
        const val EXTRA_DOWNLOAD_ID = "extra_download_id"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeJob: Job? = null

    private lateinit var downloadRepository: DownloadRepository
    private lateinit var pythonRepository: PythonEngineRepository
    private lateinit var mediaStoreRepository: MediaStoreRepository

    override fun onCreate() {
        super.onCreate()
        downloadRepository = DownloadRepository.getInstance(this)
        pythonRepository = PythonEngineRepository(this)
        mediaStoreRepository = MediaStoreRepository(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val downloadId = intent?.getStringExtra(EXTRA_DOWNLOAD_ID) ?: return START_NOT_STICKY

        if (intent.action == ACTION_CANCEL_DOWNLOAD) {
            activeJob?.cancel()
            downloadRepository.updateStatus(downloadId, DownloadStatus.CANCELLED)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val item = downloadRepository.queue.value.find { it.id == downloadId } ?: return START_NOT_STICKY

        // Start foreground service with initial notification
        val notification = buildNotification(item, progress = 0, speedMb = 0f, etaSeconds = 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        executeDownload(item)
        return START_NOT_STICKY
    }

    private fun executeDownload(item: DownloadItem) {
        activeJob = serviceScope.launch {
            downloadRepository.updateStatus(item.id, DownloadStatus.DOWNLOADING)

            val cacheDir = File(cacheDir, "downloads")
            cacheDir.mkdirs()
            val cleanFileName = item.title.replace(Regex("[^a-zA-Z0-9\\s-_]"), "").trim().take(60)

            val listener = object : DownloadProgressListener {
                override fun onProgress(percent: Float, speedMb: Float, etaSeconds: Int) {
                    downloadRepository.updateProgress(item.id, percent, speedMb, etaSeconds)
                    val updatedNotification = buildNotification(item, percent.toInt(), speedMb, etaSeconds)
                    val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                    manager.notify(NOTIFICATION_ID, updatedNotification)
                }

                override fun onStatusChange(status: String) {
                    downloadRepository.updateStatus(item.id, DownloadStatus.PROCESSING)
                }

                override fun onFinished(filePath: String) {
                    // Export to MediaStore
                    serviceScope.launch {
                        val downloadedFile = File(filePath)
                        var finalMediaUri: String? = null
                        if (downloadedFile.exists()) {
                            val saveResult = mediaStoreRepository.saveToMediaStore(downloadedFile, item)
                            saveResult.onSuccess { uri ->
                                finalMediaUri = uri.toString()
                            }
                            // AUTO CLEAN CACHE FILE IMMEDIATELY
                            try {
                                downloadedFile.delete()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        cleanOrphanedCacheFiles(cacheDir)

                        downloadRepository.updateStatus(item.id, DownloadStatus.COMPLETED, filePath = finalMediaUri ?: filePath)
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }

                override fun onError(error: String) {
                    cleanOrphanedCacheFiles(cacheDir)
                    downloadRepository.updateStatus(item.id, DownloadStatus.FAILED, error = error)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }

            pythonRepository.downloadMedia(
                url = item.url,
                outputDir = cacheDir,
                fileName = cleanFileName,
                mediaType = item.mediaType,
                audioFormat = item.audioFormat,
                audioBitrate = item.audioBitrate,
                videoResolution = item.videoResolution,
                thumbnailUrl = item.thumbnailUrl,
                title = item.title,
                artist = item.artist,
                listener = listener
            )
        }
    }

    private fun buildNotification(
        item: DownloadItem,
        progress: Int,
        speedMb: Float,
        etaSeconds: Int
    ): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, DownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
                putExtra(EXTRA_DOWNLOAD_ID, item.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val speedText = if (speedMb > 0f) "%.1f MB/s".format(speedMb) else "Connecting..."
        val contentText = "$progress% • $speedText • ETA: ${etaSeconds}s"

        return NotificationCompat.Builder(this, AuralisApplication.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Downloading: ${item.title}")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_stat_auralis_dld)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setContentIntent(openAppIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelIntent)
            .build()
    }

    private fun cleanOrphanedCacheFiles(dir: File) {
        try {
            dir.listFiles()?.forEach { file ->
                if (file.isFile && (file.extension in listOf("part", "ytdl", "temp") || System.currentTimeMillis() - file.lastModified() > 10 * 60 * 1000)) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        try {
            cleanOrphanedCacheFiles(File(cacheDir, "downloads"))
        } catch (_: Exception) {}
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
