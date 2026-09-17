package com.auralis.dld.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.auralis.dld.domain.model.DownloadItem
import com.auralis.dld.domain.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Immutable
import java.io.File
import java.io.FileInputStream

@Immutable
data class LocalTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val contentUri: Uri,
    val albumArtUri: Uri?,
    val sizeBytes: Long = 0L,
    val mimeType: String = "audio/*",
    val relativePath: String = "Music/Auralis",
    val displayName: String = ""
)

@Immutable
data class LocalVideo(
    val id: Long,
    val title: String,
    val durationSeconds: Int,
    val contentUri: Uri,
    val resolution: String = "1080p",
    val sizeBytes: Long = 0L,
    val mimeType: String = "video/*",
    val relativePath: String = "Movies/Auralis",
    val displayName: String = ""
)

class MediaStoreRepository(private val context: Context) {

    suspend fun saveToMediaStore(
        sourceFile: File,
        downloadItem: DownloadItem
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val isAudio = downloadItem.mediaType == MediaType.AUDIO
            val collectionUri = if (isAudio) {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val relativePath = if (isAudio) {
                "${Environment.DIRECTORY_MUSIC}/Auralis"
            } else {
                "${Environment.DIRECTORY_MOVIES}/Auralis"
            }

            val targetExt = if (isAudio) {
                downloadItem.audioFormat.extension.lowercase().trim()
            } else {
                "mp4"
            }

            // Strip any previously attached extensions from the base name
            val rawBaseName = sourceFile.nameWithoutExtension
                .replace(Regex("\\.(mp3|m4a|mp4|webm|opus|flac|wav|ogg)$", RegexOption.IGNORE_CASE), "")
                .trim()
            val cleanDisplayName = "$rawBaseName.$targetExt"

            val mimeType = when {
                !isAudio -> "video/mp4"
                targetExt == "m4a" -> "audio/mp4"
                targetExt == "opus" -> "audio/opus"
                targetExt == "flac" -> "audio/flac"
                targetExt == "ogg" -> "audio/ogg"
                targetExt == "wav" -> "audio/wav"
                else -> "audio/mpeg"
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, cleanDisplayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                if (isAudio) {
                    put(MediaStore.Audio.Media.TITLE, downloadItem.title)
                    put(MediaStore.Audio.Media.ARTIST, downloadItem.artist)
                } else {
                    put(MediaStore.Video.Media.TITLE, downloadItem.title)
                }
            }

            val uri = context.contentResolver.insert(collectionUri, contentValues)
                ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

            // Stream file contents into content URI
            context.contentResolver.openOutputStream(uri)?.use { outStream ->
                FileInputStream(sourceFile).use { inStream ->
                    inStream.copyTo(outStream)
                }
            }

            // Mark file as complete
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }

            // Trigger MediaScanner indexing
            MediaScannerConnection.scanFile(
                context,
                arrayOf(sourceFile.absolutePath),
                arrayOf(mimeType)
            ) { _, _ -> }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchLocalTracks(): List<LocalTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<LocalTrack>()
        val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.RELATIVE_PATH
        } else {
            MediaStore.Audio.Media.DATA
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            pathColumn
        )

        val selection = "$pathColumn LIKE ?"
        val selectionArgs = arrayOf("%Auralis%")
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dispCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val pathCol = cursor.getColumnIndexOrThrow(pathColumn)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown"
                    val artist = cursor.getString(artistCol) ?: "Unknown"
                    val durationMs = cursor.getInt(durationCol)
                    val albumId = cursor.getLong(albumIdCol)
                    val size = cursor.getLong(sizeCol)
                    val mime = cursor.getString(mimeCol) ?: "audio/*"
                    val displayName = cursor.getString(dispCol) ?: "$title.mp3"
                    val relativePath = cursor.getString(pathCol) ?: "Music/Auralis"

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )

                    tracks.add(
                        LocalTrack(
                            id = id,
                            title = title,
                            artist = artist,
                            durationSeconds = durationMs / 1000,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri,
                            sizeBytes = size,
                            mimeType = mime,
                            relativePath = relativePath,
                            displayName = displayName
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        tracks
    }

    suspend fun fetchLocalVideos(): List<LocalVideo> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<LocalVideo>()
        val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.RELATIVE_PATH
        } else {
            MediaStore.Video.Media.DATA
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            pathColumn
        )

        val selection = "$pathColumn LIKE ?"
        val selectionArgs = arrayOf("%Auralis%")
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val dispCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val pathCol = cursor.getColumnIndexOrThrow(pathColumn)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Video"
                    val durationMs = cursor.getInt(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val mime = cursor.getString(mimeCol) ?: "video/*"
                    val displayName = cursor.getString(dispCol) ?: "$title.mp4"
                    val width = cursor.getInt(widthCol)
                    val height = cursor.getInt(heightCol)
                    val relativePath = cursor.getString(pathCol) ?: "Movies/Auralis"

                    val resolution = if (width > 0 && height > 0) {
                        "${height}p (${width}x${height})"
                    } else {
                        "1080p"
                    }

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    videos.add(
                        LocalVideo(
                            id = id,
                            title = title,
                            durationSeconds = durationMs / 1000,
                            contentUri = contentUri,
                            resolution = resolution,
                            sizeBytes = size,
                            mimeType = mime,
                            relativePath = relativePath,
                            displayName = displayName
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        videos
    }

    suspend fun deleteMedia(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val deletedRows = context.contentResolver.delete(uri, null, null)
            if (deletedRows > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Could not delete item from device storage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameMedia(uri: Uri, newTitle: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.TITLE, newTitle)
            }
            val updatedRows = context.contentResolver.update(uri, values, null, null)
            if (updatedRows > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Could not rename item in storage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
