package com.auralis.dld.ui.library

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.auralis.dld.data.repository.LocalTrack
import com.auralis.dld.data.repository.LocalVideo
import com.auralis.dld.player.PlayerState
import com.auralis.dld.ui.components.AuralisAlbumArt
import com.auralis.dld.ui.components.AuralisOptionChip
import com.auralis.dld.ui.components.AuralisResponsiveContainer
import com.auralis.dld.ui.theme.AuralisTheme

@Composable
fun LibraryRoute(
    viewModel: LibraryViewModel,
    onVideoSelected: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()

    LibraryContent(
        uiState = uiState,
        playerState = playerState,
        onRefreshClick = viewModel::loadMedia,
        onSelectTab = viewModel::selectTab,
        onPlayTrack = viewModel::playTrack,
        onPlayVideo = { video ->
            viewModel.playVideo(video)
            onVideoSelected()
        },
        onDeleteMediaUri = viewModel::deleteMedia,
        onRenameMediaUri = viewModel::renameMedia,
        modifier = modifier
    )
}

@Composable
fun LibraryContent(
    uiState: LibraryUiState,
    playerState: PlayerState,
    onRefreshClick: () -> Unit,
    onSelectTab: (Int) -> Unit,
    onPlayTrack: (LocalTrack) -> Unit,
    onPlayVideo: (LocalVideo) -> Unit,
    onDeleteMediaUri: (Uri) -> Unit,
    onRenameMediaUri: (Uri, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMediaForOptions by remember { mutableStateOf<SelectedMediaItem?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuralisTheme.colors.canvas)
    ) {
        AuralisResponsiveContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
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
                            text = "Library",
                            color = AuralisTheme.colors.ink,
                            fontSize = titleSp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (uiState.selectedTab == 0) "${uiState.tracks.size} downloaded songs" else "${uiState.videos.size} downloaded videos",
                            color = AuralisTheme.colors.inkMuted,
                            fontSize = 14.sp,
                            letterSpacing = (-0.224).sp
                        )
                    }

                    IconButton(onClick = onRefreshClick) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh library",
                            tint = AuralisTheme.colors.ink
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dual Tabs (Music & Videos)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AuralisOptionChip(
                        label = "Music (${uiState.tracks.size})",
                        isSelected = uiState.selectedTab == 0,
                        onClick = { onSelectTab(0) }
                    )
                    AuralisOptionChip(
                        label = "Videos (${uiState.videos.size})",
                        isSelected = uiState.selectedTab == 1,
                        onClick = { onSelectTab(1) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (uiState.isLoading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            color = AuralisTheme.colors.ink,
                            strokeWidth = 3.dp
                        )
                    }
                } else if (uiState.selectedTab == 0) {
                    // Music Tab
                    if (uiState.tracks.isEmpty()) {
                        EmptyLibraryView(
                            icon = Icons.Rounded.LibraryMusic,
                            title = "No songs downloaded yet",
                            subtitle = "Downloaded songs will appear here in Music/Auralis"
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.tracks,
                                key = { "track_${it.id}" },
                                contentType = { "local_track" }
                            ) { track ->
                                val isPlayingThis = playerState.currentMedia?.id == track.id.toString() && playerState.isPlaying
                                TrackRow(
                                    track = track,
                                    isPlaying = isPlayingThis,
                                    onPlayClick = { onPlayTrack(track) },
                                    onThreeDotClick = { selectedMediaForOptions = track.toSelectedMediaItem() }
                                )
                            }
                        }
                    }
                } else {
                    // Videos Tab
                    if (uiState.videos.isEmpty()) {
                        EmptyLibraryView(
                            icon = Icons.Rounded.Movie,
                            title = "No videos downloaded yet",
                            subtitle = "Downloaded videos will appear here in Movies/Auralis"
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.videos,
                                key = { "video_${it.id}" },
                                contentType = { "local_video" }
                            ) { video ->
                                val isPlayingThis = playerState.currentMedia?.id == video.id.toString() && playerState.isPlaying
                                VideoRow(
                                    video = video,
                                    isPlaying = isPlayingThis,
                                    onPlayClick = { onPlayVideo(video) },
                                    onThreeDotClick = { selectedMediaForOptions = video.toSelectedMediaItem() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Three-Dot Options Bottom Sheet
    selectedMediaForOptions?.let { mediaItem ->
        MediaOptionMenuBottomSheet(
            mediaItem = mediaItem,
            onDismiss = { selectedMediaForOptions = null },
            onRenameConfirm = { newTitle ->
                onRenameMediaUri(mediaItem.contentUri, newTitle)
                selectedMediaForOptions = null
            },
            onDeleteConfirm = {
                onDeleteMediaUri(mediaItem.contentUri)
                selectedMediaForOptions = null
            }
        )
    }
}

@Composable
private fun EmptyLibraryView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuralisTheme.colors.inkMuted,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = AuralisTheme.colors.ink,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.374).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = AuralisTheme.colors.inkMuted,
                fontSize = 14.sp,
                letterSpacing = (-0.224).sp
            )
        }
    }
}

@Composable
fun TrackRow(
    track: LocalTrack,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onThreeDotClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCurrent = isPlaying
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (activeCurrent) AuralisTheme.colors.surfaceTileSecondary else Color.Transparent)
            .clickable(onClick = onPlayClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // Flat 48dp Album Artwork
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AuralisTheme.colors.surfaceTileSecondary),
            contentAlignment = Alignment.Center
        ) {
            AuralisAlbumArt(
                imageUrl = track.albumArtUri?.toString(),
                size = 48.dp,
                cornerRadius = 8.dp
            )

            if (activeCurrent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Playing",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = AuralisTheme.colors.ink,
                fontSize = 15.sp,
                fontWeight = if (activeCurrent) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = (-0.224).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            val dur = "${track.durationSeconds / 60}:${"%02d".format(track.durationSeconds % 60)}"
            Text(
                text = "${track.artist} • $dur",
                color = AuralisTheme.colors.inkMuted,
                fontSize = 12.sp,
                letterSpacing = (-0.12).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Three dots menu button
        IconButton(
            onClick = onThreeDotClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = AuralisTheme.colors.inkMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun VideoRow(
    video: LocalVideo,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onThreeDotClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCurrent = isPlaying
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { 48.dp.roundToPx() }
    val request = remember(video.contentUri, sizePx) {
        ImageRequest.Builder(context)
            .data(video.contentUri)
            .size(sizePx, sizePx)
            .crossfade(true)
            .build()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (activeCurrent) AuralisTheme.colors.surfaceTileSecondary else Color.Transparent)
            .clickable(onClick = onPlayClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // Real Video Frame Thumbnail (48dp x 48dp)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AuralisTheme.colors.surfaceTileSecondary),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = request,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (activeCurrent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Playing",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Resolution/Duration
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                color = AuralisTheme.colors.ink,
                fontSize = 15.sp,
                fontWeight = if (activeCurrent) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = (-0.224).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            val dur = if (video.durationSeconds > 0) "${video.durationSeconds / 60}:${"%02d".format(video.durationSeconds % 60)}" else "Video"
            Text(
                text = "${video.resolution} • $dur",
                color = AuralisTheme.colors.inkMuted,
                fontSize = 12.sp,
                letterSpacing = (-0.12).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Three dots menu button
        IconButton(
            onClick = onThreeDotClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = AuralisTheme.colors.inkMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
