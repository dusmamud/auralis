package com.auralis.dld.player

import android.net.Uri
import androidx.compose.runtime.Immutable

enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ENDED,
    ERROR
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

@Immutable
data class PlayableMedia(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val contentUri: Uri,
    val albumArtUri: Uri? = null,
    val isVideo: Boolean = false
)

@Immutable
data class PlayerState(
    val currentMedia: PlayableMedia? = null,
    val queue: List<PlayableMedia> = emptyList(),
    val currentIndex: Int = -1,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val errorMessage: String? = null
) {
    val isPlaying: Boolean get() = status == PlaybackStatus.PLAYING
    val isBuffering: Boolean get() = status == PlaybackStatus.BUFFERING
    val hasCurrentMedia: Boolean get() = currentMedia != null
    val progressFraction: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
