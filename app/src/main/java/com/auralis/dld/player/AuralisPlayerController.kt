package com.auralis.dld.player

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.auralis.dld.service.PlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class AuralisPlayerController private constructor(
    private val context: Context
) : PlayerController {

    companion object {
        @Volatile
        private var instance: AuralisPlayerController? = null

        fun getInstance(context: Context): AuralisPlayerController {
            return instance ?: synchronized(this) {
                instance ?: AuralisPlayerController(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @OptIn(UnstableApi::class)
    val exoPlayer: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000)
            .build()
            .apply {
                addListener(playerListener)
            }
    }

    private val _playerState = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var progressJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val status = when (playbackState) {
                Player.STATE_IDLE -> PlaybackStatus.IDLE
                Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
                Player.STATE_READY -> if (exoPlayer.playWhenReady) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
                Player.STATE_ENDED -> PlaybackStatus.ENDED
                else -> PlaybackStatus.IDLE
            }
            _playerState.update {
                it.copy(
                    status = status,
                    durationMs = exoPlayer.duration.coerceAtLeast(0L),
                    currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                )
            }
            if (status == PlaybackStatus.PLAYING) {
                startProgressTracker()
            } else {
                stopProgressTracker()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val status = if (isPlaying) {
                PlaybackStatus.PLAYING
            } else {
                if (exoPlayer.playbackState == Player.STATE_BUFFERING) PlaybackStatus.BUFFERING else PlaybackStatus.PAUSED
            }
            _playerState.update { it.copy(status = status) }
            if (isPlaying) {
                startProgressTracker()
            } else {
                stopProgressTracker()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = exoPlayer.currentMediaItemIndex
            val queue = _playerState.value.queue
            val currentMedia = if (index in queue.indices) queue[index] else null
            _playerState.update {
                it.copy(
                    currentMedia = currentMedia,
                    currentIndex = index,
                    currentPositionMs = 0L,
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                )
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            _playerState.update {
                it.copy(
                    currentPositionMs = newPosition.positionMs.coerceAtLeast(0L),
                    currentIndex = newPosition.mediaItemIndex
                )
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _playerState.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            val mode = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                else -> RepeatMode.OFF
            }
            _playerState.update { it.copy(repeatMode = mode) }
        }

        override fun onPlayerError(error: PlaybackException) {
            _playerState.update {
                it.copy(
                    status = PlaybackStatus.ERROR,
                    errorMessage = error.localizedMessage ?: "Playback error"
                )
            }
            stopProgressTracker()
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    val currentPos = exoPlayer.currentPosition.coerceAtLeast(0L)
                    val duration = exoPlayer.duration.coerceAtLeast(0L)
                    _playerState.update {
                        it.copy(
                            currentPositionMs = currentPos,
                            durationMs = duration
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun buildMediaItem(media: PlayableMedia): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(media.title)
            .setArtist(media.artist)
            .setIsPlayable(true)
            .setMediaType(if (media.isVideo) MediaMetadata.MEDIA_TYPE_VIDEO else MediaMetadata.MEDIA_TYPE_MUSIC)
            .apply {
                if (media.albumArtUri != null) {
                    setArtworkUri(media.albumArtUri)
                } else if (media.isVideo) {
                    setArtworkUri(media.contentUri)
                }
            }
            .build()

        return MediaItem.Builder()
            .setMediaId(media.id)
            .setUri(media.contentUri)
            .setMediaMetadata(metadata)
            .build()
    }

    fun startService() {
        try {
            val intent = Intent(context, PlaybackService::class.java)
            context.startService(intent)
        } catch (_: Exception) {
            // Service might already be active
        }
    }

    override fun play(media: PlayableMedia, queue: List<PlayableMedia>, startIndex: Int) {
        val targetQueue = if (queue.isEmpty()) listOf(media) else queue
        val idx = if (startIndex in targetQueue.indices) startIndex else targetQueue.indexOfFirst { it.id == media.id }.coerceAtLeast(0)

        _playerState.update {
            it.copy(
                queue = targetQueue,
                currentMedia = media,
                currentIndex = idx,
                status = PlaybackStatus.BUFFERING,
                errorMessage = null
            )
        }

        val mediaItems = targetQueue.map { buildMediaItem(it) }
        exoPlayer.setMediaItems(mediaItems, idx, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
        startService()
    }

    override fun playQueue(queue: List<PlayableMedia>, startIndex: Int) {
        if (queue.isEmpty()) return
        val idx = startIndex.coerceIn(0, queue.lastIndex)
        val media = queue[idx]
        play(media, queue, idx)
    }

    override fun resume() {
        exoPlayer.play()
        startService()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    override fun next() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_playerState.value.repeatMode == RepeatMode.ALL && _playerState.value.queue.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
        }
    }

    override fun previous() {
        if (exoPlayer.currentPosition > 3000L || !exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekTo(0L)
        } else {
            exoPlayer.seekToPreviousMediaItem()
        }
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs.coerceAtLeast(0L))
        _playerState.update { it.copy(currentPositionMs = positionMs) }
    }

    override fun toggleShuffle() {
        val newMode = !_playerState.value.isShuffleEnabled
        exoPlayer.shuffleModeEnabled = newMode
    }

    override fun toggleRepeat() {
        val nextMode = when (_playerState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        val exoMode = when (nextMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        exoPlayer.repeatMode = exoMode
    }

    override fun release() {
        stopProgressTracker()
        exoPlayer.release()
    }

    override fun resetPlayback() {
        stopProgressTracker()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _playerState.value = PlayerState()
    }
}
