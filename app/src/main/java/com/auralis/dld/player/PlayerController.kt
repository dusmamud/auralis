package com.auralis.dld.player

import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val playerState: StateFlow<PlayerState>

    fun play(media: PlayableMedia, queue: List<PlayableMedia> = listOf(media), startIndex: Int = 0)
    fun playQueue(queue: List<PlayableMedia>, startIndex: Int = 0)
    fun resume()
    fun pause()
    fun togglePlayPause()
    fun next()
    fun previous()
    fun seekTo(positionMs: Long)
    fun toggleShuffle()
    fun toggleRepeat()
    fun release()
    fun resetPlayback()
}
