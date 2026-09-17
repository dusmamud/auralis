package com.auralis.dld.ui.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.dld.domain.model.DownloadItem
import com.auralis.dld.domain.model.DownloadStatus
import com.auralis.dld.ui.components.AuralisAlbumArt
import com.auralis.dld.ui.components.AuralisResponsiveContainer
import com.auralis.dld.ui.theme.AuralisTheme

@Composable
fun QueueRoute(
    viewModel: QueueViewModel,
    modifier: Modifier = Modifier
) {
    val queue by viewModel.queue.collectAsStateWithLifecycle()

    QueueContent(
        queue = queue,
        onCancelClick = viewModel::cancelDownload,
        onRetryClick = viewModel::retryDownload,
        onClearCompleted = viewModel::clearCompleted,
        modifier = modifier
    )
}

@Composable
fun QueueContent(
    queue: List<DownloadItem>,
    onCancelClick: (String) -> Unit,
    onRetryClick: (String) -> Unit,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuralisTheme.colors.canvas)
    ) {
        AuralisResponsiveContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                val screenWidth = LocalConfiguration.current.screenWidthDp
                val titleSp = when {
                    screenWidth < 420 -> 28.sp
                    screenWidth < 640 -> 34.sp
                    else -> 40.sp
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Download Queue",
                            color = AuralisTheme.colors.ink,
                            fontSize = titleSp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${queue.size} total tasks",
                            color = AuralisTheme.colors.inkMuted,
                            fontSize = 14.sp,
                            letterSpacing = (-0.224).sp
                        )
                    }

                    if (queue.any { it.status == DownloadStatus.COMPLETED || it.status == DownloadStatus.CANCELLED }) {
                        IconButton(onClick = onClearCompleted) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear completed",
                                tint = AuralisTheme.colors.inkMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

        if (queue.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        tint = AuralisTheme.colors.inkMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No active downloads",
                        color = AuralisTheme.colors.ink,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Paste a YouTube link on Home to start",
                        color = AuralisTheme.colors.inkMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = queue,
                    key = { it.id },
                    contentType = { "download_item" }
                ) { item ->
                    QueueItemCard(
                        item = item,
                        onCancelClick = { onCancelClick(item.id) },
                        onRetryClick = { onRetryClick(item.id) }
                    )
                }
            }
        }
    }
}
}
}

@Composable
fun QueueItemCard(
    item: DownloadItem,
    onCancelClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AuralisTheme.colors.surfaceTile)
            .border(1.dp, AuralisTheme.colors.hairline, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        AuralisAlbumArt(
            imageUrl = item.thumbnailUrl,
            size = 64.dp,
            cornerRadius = 10.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = AuralisTheme.colors.ink,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.374).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.artist,
                color = AuralisTheme.colors.inkMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.224).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (item.status) {
                DownloadStatus.DOWNLOADING -> {
                    LinearProgressIndicator(
                        progress = { item.progress / 100f },
                        color = AuralisTheme.colors.actionBlue,
                        trackColor = AuralisTheme.colors.hairline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val speedStr = "%.1f MB/s".format(item.speedMb)
                        Text(
                            text = "${item.progress.toInt()}% • $speedStr",
                            color = AuralisTheme.colors.ink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.12).sp
                        )
                        Text(
                            text = "ETA: ${item.etaSeconds}s",
                            color = AuralisTheme.colors.inkMuted,
                            fontSize = 12.sp,
                            letterSpacing = (-0.12).sp
                        )
                    }
                }

                DownloadStatus.PROCESSING -> {
                    LinearProgressIndicator(
                        color = AuralisTheme.colors.focusBlue,
                        trackColor = AuralisTheme.colors.hairline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Transcoding & Embedding Art...",
                        color = AuralisTheme.colors.inkMuted,
                        fontSize = 12.sp,
                        letterSpacing = (-0.12).sp
                    )
                }

                DownloadStatus.COMPLETED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Saved to Music/Auralis",
                            color = Color(0xFF34C759),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.12).sp
                        )
                    }
                }

                DownloadStatus.FAILED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Error,
                            contentDescription = null,
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.errorMessage ?: "Download failed",
                            color = Color(0xFFFF3B30),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                DownloadStatus.QUEUED -> {
                    Text(
                        text = "Queued in background...",
                        color = AuralisTheme.colors.inkMuted,
                        fontSize = 12.sp,
                        letterSpacing = (-0.12).sp
                    )
                }

                DownloadStatus.CANCELLED, DownloadStatus.IDLE -> {
                    Text(
                        text = "Cancelled",
                        color = AuralisTheme.colors.inkMuted,
                        fontSize = 12.sp,
                        letterSpacing = (-0.12).sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Action Button (Cancel or Retry)
        if (item.status == DownloadStatus.DOWNLOADING || item.status == DownloadStatus.QUEUED) {
            IconButton(
                onClick = onCancelClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Cancel",
                    tint = AuralisTheme.colors.inkMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else if (item.status == DownloadStatus.FAILED || item.status == DownloadStatus.CANCELLED) {
            IconButton(
                onClick = onRetryClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Retry",
                    tint = AuralisTheme.colors.inkMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

