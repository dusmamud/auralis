package com.auralis.dld.ui.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auralis.dld.data.repository.LocalTrack
import com.auralis.dld.data.repository.LocalVideo
import com.auralis.dld.ui.theme.AuralisTheme

data class SelectedMediaItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val durationSeconds: Int,
    val contentUri: Uri,
    val thumbnailUri: Uri?,
    val isVideo: Boolean,
    val sizeBytes: Long,
    val mimeType: String,
    val relativePath: String,
    val displayName: String,
    val resolution: String? = null
)

fun LocalTrack.toSelectedMediaItem() = SelectedMediaItem(
    id = id,
    title = title,
    subtitle = artist,
    durationSeconds = durationSeconds,
    contentUri = contentUri,
    thumbnailUri = albumArtUri,
    isVideo = false,
    sizeBytes = sizeBytes,
    mimeType = mimeType,
    relativePath = relativePath,
    displayName = displayName
)

fun LocalVideo.toSelectedMediaItem() = SelectedMediaItem(
    id = id,
    title = title,
    subtitle = resolution,
    durationSeconds = durationSeconds,
    contentUri = contentUri,
    thumbnailUri = contentUri,
    isVideo = true,
    sizeBytes = sizeBytes,
    mimeType = mimeType,
    relativePath = relativePath,
    displayName = displayName,
    resolution = resolution
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaOptionMenuBottomSheet(
    mediaItem: SelectedMediaItem,
    onDismiss: () -> Unit,
    onRenameConfirm: (newTitle: String) -> Unit,
    onDeleteConfirm: () -> Unit
) {
    val context = LocalContext.current
    var showFileInfoDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AuralisTheme.colors.canvas,
        contentColor = AuralisTheme.colors.ink,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp, start = 20.dp, end = 20.dp)
        ) {
            // Header: Media info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AuralisTheme.colors.surfaceTileSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    if (mediaItem.thumbnailUri != null) {
                        AsyncImage(
                            model = mediaItem.thumbnailUri,
                            contentDescription = mediaItem.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = if (mediaItem.isVideo) Icons.Default.Videocam else Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = AuralisTheme.colors.inkMuted,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mediaItem.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        color = AuralisTheme.colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${mediaItem.subtitle} • ${formatDuration(mediaItem.durationSeconds * 1000L)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = AuralisTheme.colors.inkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                color = AuralisTheme.colors.hairline,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Options List
            MenuActionRow(
                icon = Icons.Default.Info,
                title = "File Info",
                onClick = { showFileInfoDialog = true }
            )

            MenuActionRow(
                icon = Icons.Default.Edit,
                title = "Rename",
                onClick = { showRenameDialog = true }
            )

            MenuActionRow(
                icon = Icons.Default.Share,
                title = "Share",
                onClick = {
                    shareMediaFile(context, mediaItem)
                    onDismiss()
                }
            )

            MenuActionRow(
                icon = Icons.Default.Delete,
                title = "Delete",
                iconTint = Color(0xFFE53935),
                textColor = Color(0xFFE53935),
                onClick = { showDeleteDialog = true }
            )
        }
    }

    if (showFileInfoDialog) {
        FileInfoDialog(
            mediaItem = mediaItem,
            onDismiss = {
                showFileInfoDialog = false
                onDismiss()
            }
        )
    }

    if (showRenameDialog) {
        RenameMediaDialog(
            currentTitle = mediaItem.title,
            onDismiss = {
                showRenameDialog = false
            },
            onConfirm = { newTitle ->
                showRenameDialog = false
                onRenameConfirm(newTitle)
                onDismiss()
            }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            mediaTitle = mediaItem.title,
            onDismiss = {
                showDeleteDialog = false
            },
            onConfirm = {
                showDeleteDialog = false
                onDeleteConfirm()
                onDismiss()
            }
        )
    }
}

@Composable
private fun MenuActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color = AuralisTheme.colors.ink,
    textColor: Color = AuralisTheme.colors.ink
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FileInfoDialog(
    mediaItem: SelectedMediaItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "File Info",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AuralisTheme.colors.ink
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoField(label = "Title", value = mediaItem.title)
                InfoField(label = "File Name", value = mediaItem.displayName.ifEmpty { "${mediaItem.title}.${if (mediaItem.isVideo) "mp4" else "mp3"}" })
                InfoField(label = "Location", value = mediaItem.relativePath)
                InfoField(label = "Duration", value = formatDuration(mediaItem.durationSeconds * 1000L))
                InfoField(label = "File Size", value = formatFileSize(mediaItem.sizeBytes))
                InfoField(label = "Format", value = mediaItem.mimeType)
                if (mediaItem.isVideo && !mediaItem.resolution.isNullOrBlank()) {
                    InfoField(label = "Resolution", value = mediaItem.resolution)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AuralisTheme.colors.actionBlue, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = AuralisTheme.colors.canvas,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun InfoField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AuralisTheme.colors.inkMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = AuralisTheme.colors.ink,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun RenameMediaDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newTitle by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rename",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AuralisTheme.colors.ink
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newTitle.isNotBlank()) {
                        onConfirm(newTitle.trim())
                    }
                }
            ) {
                Text("Rename", color = AuralisTheme.colors.actionBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AuralisTheme.colors.inkMuted)
            }
        },
        containerColor = AuralisTheme.colors.canvas,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DeleteConfirmationDialog(
    mediaTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete File",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AuralisTheme.colors.ink
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$mediaTitle\"? This will permanently delete the file from your device storage.",
                fontSize = 14.sp,
                color = AuralisTheme.colors.inkMuted
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AuralisTheme.colors.inkMuted)
            }
        },
        containerColor = AuralisTheme.colors.canvas,
        shape = RoundedCornerShape(20.dp)
    )
}

private fun shareMediaFile(context: Context, item: SelectedMediaItem) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (item.isVideo) "video/*" else "audio/*"
            putExtra(Intent.EXTRA_STREAM, item.contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share \"${item.title}\""))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "Unknown"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        "%.2f MB".format(mb)
    } else {
        "%.1f KB".format(kb)
    }
}
