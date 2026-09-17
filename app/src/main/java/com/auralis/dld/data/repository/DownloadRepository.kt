package com.auralis.dld.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import com.auralis.dld.domain.model.DownloadItem
import com.auralis.dld.domain.model.DownloadStatus
import com.auralis.dld.service.DownloadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class DownloadRepository(private val context: Context) {

    companion object {
        @Volatile
        private var instance: DownloadRepository? = null

        fun getInstance(context: Context): DownloadRepository {
            return instance ?: synchronized(this) {
                instance ?: DownloadRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val _queue = MutableStateFlow<List<DownloadItem>>(emptyList())
    val queue: StateFlow<List<DownloadItem>> = _queue.asStateFlow()

    fun enqueue(downloadItem: DownloadItem) {
        val itemWithId = if (downloadItem.id.isBlank()) {
            downloadItem.copy(id = UUID.randomUUID().toString(), status = DownloadStatus.QUEUED)
        } else {
            downloadItem.copy(status = DownloadStatus.QUEUED)
        }

        _queue.update { current ->
            current.filterNot { it.id == itemWithId.id } + itemWithId
        }

        startDownloadService(itemWithId.id)
    }

    fun updateProgress(id: String, progress: Float, speedMb: Float, etaSeconds: Int) {
        _queue.update { list ->
            list.map { item ->
                if (item.id == id) {
                    item.copy(
                        progress = progress,
                        speedMb = speedMb,
                        etaSeconds = etaSeconds,
                        status = DownloadStatus.DOWNLOADING
                    )
                } else item
            }
        }
    }

    fun updateStatus(id: String, status: DownloadStatus, filePath: String? = null, error: String? = null) {
        _queue.update { list ->
            list.map { item ->
                if (item.id == id) {
                    item.copy(
                        status = status,
                        filePath = filePath ?: item.filePath,
                        errorMessage = error,
                        progress = if (status == DownloadStatus.COMPLETED) 100f else item.progress
                    )
                } else item
            }
        }
    }

    fun cancel(id: String) {
        updateStatus(id, DownloadStatus.CANCELLED)
    }

    fun retry(id: String) {
        val item = _queue.value.find { it.id == id } ?: return
        enqueue(item.copy(status = DownloadStatus.QUEUED, progress = 0f, errorMessage = null))
    }

    fun clearCompleted() {
        _queue.update { list ->
            list.filter { it.status != DownloadStatus.COMPLETED && it.status != DownloadStatus.CANCELLED }
        }
    }

    private fun startDownloadService(downloadId: String) {
        val intent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_START_DOWNLOAD
            putExtra(DownloadService.EXTRA_DOWNLOAD_ID, downloadId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
