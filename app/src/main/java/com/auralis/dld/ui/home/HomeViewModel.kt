package com.auralis.dld.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.dld.data.repository.DownloadRepository
import com.auralis.dld.data.repository.PythonEngineRepository
import com.auralis.dld.domain.model.AudioBitrate
import com.auralis.dld.domain.model.AudioFormat
import com.auralis.dld.domain.model.DownloadItem
import com.auralis.dld.domain.model.MediaType
import com.auralis.dld.domain.model.TrackMetadata
import com.auralis.dld.domain.model.VideoResolution
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

@Immutable
sealed interface HomeUiState {
    val url: String
    val mediaType: MediaType
    val audioFormat: AudioFormat
    val audioBitrate: AudioBitrate
    val videoResolution: VideoResolution

    data class Idle(
        override val url: String = "",
        override val mediaType: MediaType = MediaType.AUDIO,
        override val audioFormat: AudioFormat = AudioFormat.MP3,
        override val audioBitrate: AudioBitrate = AudioBitrate.CBR_320,
        override val videoResolution: VideoResolution = VideoResolution.RES_1080P
    ) : HomeUiState

    data class Loading(
        override val url: String,
        override val mediaType: MediaType,
        override val audioFormat: AudioFormat,
        override val audioBitrate: AudioBitrate,
        override val videoResolution: VideoResolution
    ) : HomeUiState

    data class Preview(
        override val url: String,
        val metadata: TrackMetadata,
        override val mediaType: MediaType,
        override val audioFormat: AudioFormat,
        override val audioBitrate: AudioBitrate,
        override val videoResolution: VideoResolution
    ) : HomeUiState

    data class Error(
        override val url: String,
        val message: String,
        override val mediaType: MediaType,
        override val audioFormat: AudioFormat,
        override val audioBitrate: AudioBitrate,
        override val videoResolution: VideoResolution
    ) : HomeUiState
}

sealed interface HomeUiEffect {
    data class ShowSnackbar(val message: String) : HomeUiEffect
    data object NavigateToQueue : HomeUiEffect
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val pythonRepository = PythonEngineRepository(application)
    private val downloadRepository = DownloadRepository.getInstance(application)

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<HomeUiEffect>()
    val effect: SharedFlow<HomeUiEffect> = _effect.asSharedFlow()

    fun onUrlChange(newUrl: String) {
        _uiState.update { current ->
            when (current) {
                is HomeUiState.Idle -> current.copy(url = newUrl)
                is HomeUiState.Loading -> current.copy(url = newUrl)
                is HomeUiState.Preview -> current.copy(url = newUrl)
                is HomeUiState.Error -> current.copy(url = newUrl)
            }
        }
    }

    fun onSelectMediaType(mediaType: MediaType) {
        _uiState.update { current ->
            when (current) {
                is HomeUiState.Idle -> current.copy(mediaType = mediaType)
                is HomeUiState.Loading -> current.copy(mediaType = mediaType)
                is HomeUiState.Preview -> current.copy(mediaType = mediaType)
                is HomeUiState.Error -> current.copy(mediaType = mediaType)
            }
        }
    }

    fun onSelectAudioFormat(format: AudioFormat) {
        _uiState.update { current ->
            when (current) {
                is HomeUiState.Idle -> current.copy(audioFormat = format)
                is HomeUiState.Loading -> current.copy(audioFormat = format)
                is HomeUiState.Preview -> current.copy(audioFormat = format)
                is HomeUiState.Error -> current.copy(audioFormat = format)
            }
        }
    }

    fun onSelectAudioBitrate(bitrate: AudioBitrate) {
        _uiState.update { current ->
            when (current) {
                is HomeUiState.Idle -> current.copy(audioBitrate = bitrate)
                is HomeUiState.Loading -> current.copy(audioBitrate = bitrate)
                is HomeUiState.Preview -> current.copy(audioBitrate = bitrate)
                is HomeUiState.Error -> current.copy(audioBitrate = bitrate)
            }
        }
    }

    fun onSelectVideoResolution(res: VideoResolution) {
        _uiState.update { current ->
            when (current) {
                is HomeUiState.Idle -> current.copy(videoResolution = res)
                is HomeUiState.Loading -> current.copy(videoResolution = res)
                is HomeUiState.Preview -> current.copy(videoResolution = res)
                is HomeUiState.Error -> current.copy(videoResolution = res)
            }
        }
    }

    fun fetchMetadata() {
        val currentUrl = _uiState.value.url.trim()
        if (currentUrl.isBlank()) {
            viewModelScope.launch {
                _effect.emit(HomeUiEffect.ShowSnackbar("Please paste or enter a valid URL"))
            }
            return
        }

        viewModelScope.launch {
            val prev = _uiState.value
            _uiState.value = HomeUiState.Loading(
                url = currentUrl,
                mediaType = prev.mediaType,
                audioFormat = prev.audioFormat,
                audioBitrate = prev.audioBitrate,
                videoResolution = prev.videoResolution
            )

            val result = pythonRepository.fetchMetadata(currentUrl)
            result.onSuccess { meta ->
                _uiState.update { current ->
                    HomeUiState.Preview(
                        url = currentUrl,
                        metadata = meta,
                        mediaType = current.mediaType,
                        audioFormat = current.audioFormat,
                        audioBitrate = current.audioBitrate,
                        videoResolution = current.videoResolution
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    HomeUiState.Error(
                        url = currentUrl,
                        message = error.message ?: "Failed to load media info",
                        mediaType = current.mediaType,
                        audioFormat = current.audioFormat,
                        audioBitrate = current.audioBitrate,
                        videoResolution = current.videoResolution
                    )
                }
            }
        }
    }

    fun startDownload() {
        val current = _uiState.value
        val (url, title, artist, thumb) = when (current) {
            is HomeUiState.Preview -> {
                listOf(
                    current.metadata.webpageUrl,
                    current.metadata.title,
                    current.metadata.artist,
                    current.metadata.thumbnailUrl
                )
            }
            else -> {
                val inputUrl = current.url.trim()
                if (inputUrl.isBlank()) return
                listOf(inputUrl, "Media Download", "Auralis", "")
            }
        }

        val item = DownloadItem(
            id = UUID.randomUUID().toString(),
            url = url,
            title = title,
            artist = artist,
            thumbnailUrl = thumb,
            mediaType = current.mediaType,
            audioFormat = current.audioFormat,
            audioBitrate = current.audioBitrate,
            videoResolution = current.videoResolution
        )

        downloadRepository.enqueue(item)
        viewModelScope.launch {
            _effect.emit(HomeUiEffect.ShowSnackbar("Added to download queue"))
            _effect.emit(HomeUiEffect.NavigateToQueue)
        }
    }
}
