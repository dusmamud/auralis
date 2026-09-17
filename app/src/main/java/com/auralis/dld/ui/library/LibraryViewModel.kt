package com.auralis.dld.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auralis.dld.data.repository.LocalTrack
import com.auralis.dld.data.repository.LocalVideo
import com.auralis.dld.data.repository.MediaStoreRepository
import com.auralis.dld.player.AuralisPlayerController
import com.auralis.dld.player.PlayableMedia
import com.auralis.dld.player.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class LibraryUiState(
    val selectedTab: Int = 0, // 0 = Music, 1 = Videos
    val tracks: List<LocalTrack> = emptyList(),
    val videos: List<LocalVideo> = emptyList(),
    val isLoading: Boolean = false
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaStoreRepository = MediaStoreRepository(application)
    val playerController: AuralisPlayerController = AuralisPlayerController.getInstance(application)
    val playerState: StateFlow<PlayerState> = playerController.playerState

    private val _uiState = MutableStateFlow(LibraryUiState(isLoading = true))
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadMedia()
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun loadMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val localTracks = mediaStoreRepository.fetchLocalTracks()
            val localVideos = mediaStoreRepository.fetchLocalVideos()
            _uiState.update {
                it.copy(
                    tracks = localTracks,
                    videos = localVideos,
                    isLoading = false
                )
            }
        }
    }

    fun playTrack(track: LocalTrack) {
        val allTracks = _uiState.value.tracks.map {
            PlayableMedia(
                id = it.id.toString(),
                title = it.title,
                artist = it.artist,
                durationSeconds = it.durationSeconds,
                contentUri = it.contentUri,
                albumArtUri = it.albumArtUri,
                isVideo = false
            )
        }
        val target = PlayableMedia(
            id = track.id.toString(),
            title = track.title,
            artist = track.artist,
            durationSeconds = track.durationSeconds,
            contentUri = track.contentUri,
            albumArtUri = track.albumArtUri,
            isVideo = false
        )
        val startIndex = allTracks.indexOfFirst { it.id == target.id }.coerceAtLeast(0)
        playerController.play(target, allTracks, startIndex)
    }

    fun playVideo(video: LocalVideo) {
        val media = PlayableMedia(
            id = video.id.toString(),
            title = video.title,
            artist = "Local Video",
            durationSeconds = video.durationSeconds,
            contentUri = video.contentUri,
            albumArtUri = null,
            isVideo = true
        )
        playerController.play(media, listOf(media), 0)
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun deleteMedia(uri: android.net.Uri) {
        viewModelScope.launch {
            val result = mediaStoreRepository.deleteMedia(uri)
            if (result.isSuccess) {
                loadMedia()
            }
        }
    }

    fun renameMedia(uri: android.net.Uri, newTitle: String) {
        viewModelScope.launch {
            val result = mediaStoreRepository.renameMedia(uri, newTitle)
            if (result.isSuccess) {
                loadMedia()
            }
        }
    }
}
