package com.auralis.dld.data.repository

import android.content.Context
import android.util.Log
import com.auralis.dld.domain.model.AudioBitrate
import com.auralis.dld.domain.model.AudioFormat
import com.auralis.dld.domain.model.MediaType
import com.auralis.dld.domain.model.TrackMetadata
import com.auralis.dld.domain.model.VideoResolution
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

interface DownloadProgressListener {
    fun onProgress(percent: Float, speedMb: Float, etaSeconds: Int)
    fun onStatusChange(status: String)
    fun onFinished(filePath: String)
    fun onError(error: String)
}

private fun PyObject.getDictString(key: String, default: String = ""): String {
    return try {
        this.callAttr("get", key)?.toString() ?: default
    } catch (e: Exception) {
        default
    }
}

private fun PyObject.getDictBoolean(key: String, default: Boolean = false): Boolean {
    return try {
        this.callAttr("get", key)?.toBoolean() ?: default
    } catch (e: Exception) {
        default
    }
}

private fun PyObject.getDictInt(key: String, default: Int = 0): Int {
    return try {
        this.callAttr("get", key)?.toInt() ?: default
    } catch (e: Exception) {
        default
    }
}

class PythonEngineRepository(private val context: Context) {

    private val py: Python
        get() = Python.getInstance()

    suspend fun fetchMetadata(url: String): Result<TrackMetadata> = withContext(Dispatchers.IO) {
        try {
            Log.d("Auralis", "Calling auralis.extractor.fetch_metadata for URL: $url")
            val extractorModule = py.getModule("auralis.extractor")
            val pyResult = extractorModule.callAttr("fetch_metadata", url)

            val success = pyResult.getDictBoolean("success", false)
            if (!success) {
                val errorMsg = pyResult.getDictString("error", "Failed to extract metadata")
                Log.e("Auralis", "fetch_metadata returned failure: $errorMsg")
                return@withContext Result.failure(Exception(errorMsg))
            }

            val metadata = TrackMetadata(
                id = pyResult.getDictString("id"),
                title = pyResult.getDictString("title", "Unknown Title"),
                artist = pyResult.getDictString("artist", "Unknown Artist"),
                durationSeconds = pyResult.getDictInt("duration", 0),
                thumbnailUrl = pyResult.getDictString("thumbnail"),
                webpageUrl = pyResult.getDictString("webpage_url", url),
                isLive = pyResult.getDictBoolean("is_live", false)
            )
            Log.d("Auralis", "fetch_metadata success: '${metadata.title}' by '${metadata.artist}'")
            Result.success(metadata)
        } catch (e: Exception) {
            Log.e("Auralis", "Exception in fetchMetadata", e)
            Result.failure(e)
        }
    }

    suspend fun downloadMedia(
        url: String,
        outputDir: File,
        fileName: String,
        mediaType: MediaType,
        audioFormat: AudioFormat,
        audioBitrate: AudioBitrate,
        videoResolution: VideoResolution,
        thumbnailUrl: String,
        title: String,
        artist: String,
        listener: DownloadProgressListener
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("Auralis", "Starting download_media: $url -> $fileName, format: ${audioFormat.extension}")
            val downloaderModule = py.getModule("auralis.downloader")

            // Create Chaquopy callback bridge
            val callbackBridge = object {
                fun onProgress(percent: Float, speedMb: Float, etaSeconds: Int) {
                    listener.onProgress(percent, speedMb, etaSeconds)
                }

                fun onStatusChange(status: String) {
                    listener.onStatusChange(status)
                }

                fun onFinished(filePath: String) {
                    listener.onFinished(filePath)
                }

                fun onError(error: String) {
                    listener.onError(error)
                }
            }

            val result = downloaderModule.callAttr(
                "download_media",
                url,
                outputDir.absolutePath,
                fileName,
                mediaType.name.lowercase(),
                audioFormat.extension,
                audioBitrate.value,
                videoResolution.tag,
                thumbnailUrl,
                artist,
                title,
                callbackBridge
            )

            val success = result.getDictBoolean("success", false)
            if (success) {
                val filePath = result.getDictString("file_path")
                Log.d("Auralis", "download_media successful: $filePath")
                Result.success(filePath)
            } else {
                val error = result.getDictString("error", "Download failed")
                Log.e("Auralis", "download_media returned failure: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("Auralis", "Exception in downloadMedia", e)
            listener.onError(e.message ?: "Unknown download error")
            Result.failure(e)
        }
    }

    suspend fun updateEngine(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val updaterModule = py.getModule("auralis.updater")
            val result = updaterModule.callAttr("update_ytdlp")

            val success = result.getDictBoolean("success", false)
            if (success) {
                val newVersion = result.getDictString("version", "Updated")
                Result.success(newVersion)
            } else {
                val error = result.getDictString("error", "Failed to update engine")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("Auralis", "Exception in updateEngine", e)
            Result.failure(e)
        }
    }

    suspend fun getEngineVersion(): String = withContext(Dispatchers.IO) {
        try {
            val updaterModule = py.getModule("auralis.updater")
            val versionObj = updaterModule.callAttr("get_current_ytdlp_version")
            versionObj?.toString() ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
