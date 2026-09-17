package com.auralis.dld

import com.auralis.dld.domain.model.AudioBitrate
import com.auralis.dld.domain.model.AudioFormat
import com.auralis.dld.domain.model.DownloadItem
import com.auralis.dld.domain.model.DownloadStatus
import com.auralis.dld.domain.model.MediaType
import com.auralis.dld.domain.model.TrackMetadata
import com.auralis.dld.domain.model.VideoResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadItemTest {

    @Test
    fun trackMetadata_formattedDuration_calculatesCorrectly() {
        val meta1 = TrackMetadata(
            id = "1",
            title = "Test Song",
            artist = "Test Artist",
            durationSeconds = 215, // 3 mins 35 secs
            thumbnailUrl = "https://example.com/thumb.jpg",
            webpageUrl = "https://music.youtube.com/watch?v=1"
        )
        assertEquals("3:35", meta1.formattedDuration)

        val meta2 = TrackMetadata(
            id = "2",
            title = "Short Song",
            artist = "Artist",
            durationSeconds = 9, // 0 mins 9 secs
            thumbnailUrl = "",
            webpageUrl = ""
        )
        assertEquals("0:09", meta2.formattedDuration)
    }

    @Test
    fun downloadItem_defaultState_isIdle() {
        val item = DownloadItem(
            id = "test-id",
            url = "https://music.youtube.com/watch?v=test",
            title = "Test Title",
            artist = "Test Artist",
            thumbnailUrl = "https://example.com/thumb.jpg",
            mediaType = MediaType.AUDIO,
            audioFormat = AudioFormat.MP3,
            audioBitrate = AudioBitrate.CBR_320,
            videoResolution = VideoResolution.RES_1080P
        )

        assertEquals(DownloadStatus.IDLE, item.status)
        assertEquals(0f, item.progress, 0.001f)
        assertEquals(AudioBitrate.CBR_320, item.audioBitrate)
        assertEquals(AudioFormat.MP3, item.audioFormat)
    }

    @Test
    fun downloadItem_progressUpdates_accurately() {
        val item = DownloadItem(
            id = "test-id",
            url = "https://music.youtube.com/watch?v=test",
            title = "Test Title",
            artist = "Test Artist",
            thumbnailUrl = "",
            progress = 45.5f,
            speedMb = 3.2f,
            etaSeconds = 12,
            status = DownloadStatus.DOWNLOADING
        )

        assertEquals(45.5f, item.progress, 0.001f)
        assertEquals(3.2f, item.speedMb, 0.001f)
        assertEquals(12, item.etaSeconds)
        assertEquals(DownloadStatus.DOWNLOADING, item.status)
    }
}
