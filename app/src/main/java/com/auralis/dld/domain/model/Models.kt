package com.auralis.dld.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class TrackMetadata(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val thumbnailUrl: String,
    val webpageUrl: String,
    val isLive: Boolean = false
) {
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

enum class MediaType {
    AUDIO,
    VIDEO
}

enum class AudioFormat(val extension: String, val label: String) {
    MP3("mp3", "MP3"),
    M4A("m4a", "M4A"),
    FLAC("flac", "FLAC"),
    OPUS("opus", "OPUS")
}

enum class AudioBitrate(val value: String, val label: String) {
    CBR_320("320", "320 kbps (High)"),
    CBR_256("256", "256 kbps"),
    CBR_192("192", "192 kbps (Standard)"),
    CBR_128("128", "128 kbps (Compact)")
}

enum class VideoResolution(val tag: String, val label: String) {
    RES_4K("4k", "4K Ultra HD"),
    RES_2K("2k", "2K QHD"),
    RES_1080P("1080p", "1080p Full HD"),
    RES_720P("720p", "720p HD")
}

enum class DownloadStatus {
    IDLE,
    QUEUED,
    DOWNLOADING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Immutable
data class DownloadItem(
    val id: String,
    val url: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val mediaType: MediaType = MediaType.AUDIO,
    val audioFormat: AudioFormat = AudioFormat.MP3,
    val audioBitrate: AudioBitrate = AudioBitrate.CBR_320,
    val videoResolution: VideoResolution = VideoResolution.RES_1080P,
    val progress: Float = 0f,
    val speedMb: Float = 0f,
    val etaSeconds: Int = 0,
    val status: DownloadStatus = DownloadStatus.IDLE,
    val filePath: String? = null,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class EngineStatus(
    val currentVersion: String = "2024.08.06+",
    val isUpdating: Boolean = false,
    val updateError: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
