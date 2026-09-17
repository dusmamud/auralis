package com.auralis.dld.ui.queue

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.auralis.dld.data.repository.DownloadRepository
import com.auralis.dld.domain.model.DownloadItem
import kotlinx.coroutines.flow.StateFlow

class QueueViewModel(application: Application) : AndroidViewModel(application) {

    private val downloadRepository = DownloadRepository.getInstance(application)
    val queue: StateFlow<List<DownloadItem>> = downloadRepository.queue

    fun cancelDownload(id: String) {
        downloadRepository.cancel(id)
    }

    fun retryDownload(id: String) {
        downloadRepository.retry(id)
    }

    fun clearCompleted() {
        downloadRepository.clearCompleted()
    }
}
