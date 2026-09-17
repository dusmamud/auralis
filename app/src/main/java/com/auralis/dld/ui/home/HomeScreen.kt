package com.auralis.dld.ui.home

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.dld.domain.model.AudioBitrate
import com.auralis.dld.domain.model.AudioFormat
import com.auralis.dld.domain.model.MediaType
import com.auralis.dld.domain.model.VideoResolution
import com.auralis.dld.ui.components.AuralisAlbumArt
import com.auralis.dld.ui.components.AuralisOptionChip
import com.auralis.dld.ui.components.AuralisPrimaryButton
import com.auralis.dld.ui.components.AuralisResponsiveContainer
import com.auralis.dld.ui.components.AuralisUrlInput
import com.auralis.dld.ui.theme.AuralisTheme

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onNavigateToQueue: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onUrlChange = viewModel::onUrlChange,
        onPasteClick = {
            val clipboard = viewModel.getApplication<android.app.Application>()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val pasted = clipData.getItemAt(0).coerceToText(viewModel.getApplication()).toString().trim()
                if (pasted.isNotBlank()) {
                    viewModel.onUrlChange(pasted)
                    viewModel.fetchMetadata()
                }
            }
        },
        onClearClick = { viewModel.onUrlChange("") },
        onSearchSubmit = viewModel::fetchMetadata,
        onSelectMediaType = viewModel::onSelectMediaType,
        onSelectAudioFormat = viewModel::onSelectAudioFormat,
        onSelectAudioBitrate = viewModel::onSelectAudioBitrate,
        onSelectVideoResolution = viewModel::onSelectVideoResolution,
        onDownloadClick = viewModel::startDownload,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onUrlChange: (String) -> Unit,
    onPasteClick: () -> Unit,
    onClearClick: () -> Unit,
    onSearchSubmit: () -> Unit,
    onSelectMediaType: (MediaType) -> Unit,
    onSelectAudioFormat: (AudioFormat) -> Unit,
    onSelectAudioBitrate: (AudioBitrate) -> Unit,
    onSelectVideoResolution: (VideoResolution) -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val responsiveTitleSp = when {
        screenWidth < 420 -> 28.sp
        screenWidth < 640 -> 34.sp
        else -> 40.sp
    }

    AuralisResponsiveContainer(
        modifier = modifier
            .fillMaxSize()
            .background(AuralisTheme.colors.canvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp)
        ) {
            // App Header Title
            Text(
                text = "Auralis",
                color = AuralisTheme.colors.ink,
                fontSize = responsiveTitleSp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.374).sp
            )
            Text(
                text = "Lossless YouTube Music & Video Downloader",
                color = AuralisTheme.colors.inkMuted,
                fontSize = 14.sp,
                letterSpacing = (-0.224).sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Search / URL Input Pill
            AuralisUrlInput(
                url = uiState.url,
                onUrlChange = onUrlChange,
                onPasteClick = onPasteClick,
                onClearClick = onClearClick,
                onSearchSubmit = onSearchSubmit
            )

            Spacer(modifier = Modifier.height(20.dp))

            // State Feedback Card
            when (uiState) {
                is HomeUiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(AuralisTheme.colors.surfaceTile)
                            .border(1.dp, AuralisTheme.colors.hairline, RoundedCornerShape(18.dp))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = AuralisTheme.colors.inkMuted,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Extracting stream metadata...",
                                color = AuralisTheme.colors.inkMuted,
                                fontSize = 14.sp,
                                letterSpacing = (-0.224).sp
                            )
                        }
                    }
                }

                is HomeUiState.Preview -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(AuralisTheme.colors.surfaceTile)
                            .border(1.dp, AuralisTheme.colors.hairline, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        AuralisAlbumArt(
                            imageUrl = uiState.metadata.thumbnailUrl,
                            size = 80.dp,
                            cornerRadius = 10.dp
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.metadata.title,
                                color = AuralisTheme.colors.ink,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.374).sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.metadata.artist,
                                color = AuralisTheme.colors.inkMuted,
                                fontSize = 14.sp,
                                letterSpacing = (-0.224).sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Duration: ${uiState.metadata.formattedDuration}",
                                color = AuralisTheme.colors.inkMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = (-0.224).sp
                            )
                        }
                    }
                }

                is HomeUiState.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(AuralisTheme.colors.surfaceTile)
                            .border(1.dp, AuralisTheme.colors.hairline, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.message,
                            color = AuralisTheme.colors.ink,
                            fontSize = 14.sp,
                            letterSpacing = (-0.224).sp
                        )
                    }
                }

                is HomeUiState.Idle -> {
                    // Keep empty or subtle hint
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Media Type Selector (Audio vs Video)
            Text(
                text = "MEDIA TYPE",
                color = AuralisTheme.colors.inkMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.224).sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AuralisOptionChip(
                    label = "Music (Audio)",
                    isSelected = uiState.mediaType == MediaType.AUDIO,
                    onClick = { onSelectMediaType(MediaType.AUDIO) }
                )
                AuralisOptionChip(
                    label = "Video (4K / 1080p)",
                    isSelected = uiState.mediaType == MediaType.VIDEO,
                    onClick = { onSelectMediaType(MediaType.VIDEO) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.mediaType == MediaType.AUDIO) {
                // Audio Format Selector
                Text(
                    text = "AUDIO FORMAT",
                    color = AuralisTheme.colors.inkMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.224).sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AudioFormat.entries.forEach { format ->
                        AuralisOptionChip(
                            label = format.label,
                            isSelected = uiState.audioFormat == format,
                            onClick = { onSelectAudioFormat(format) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Audio Bitrate Selector
                Text(
                    text = "AUDIO BITRATE (TRANSCODING)",
                    color = AuralisTheme.colors.inkMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.224).sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AudioBitrate.entries.forEach { bitrate ->
                        AuralisOptionChip(
                            label = bitrate.label,
                            isSelected = uiState.audioBitrate == bitrate,
                            onClick = { onSelectAudioBitrate(bitrate) }
                        )
                    }
                }
            } else {
                // Video Resolution Selector
                Text(
                    text = "VIDEO RESOLUTION (DASH MUXING)",
                    color = AuralisTheme.colors.inkMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.224).sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VideoResolution.entries.forEach { res ->
                        AuralisOptionChip(
                            label = res.label,
                            isSelected = uiState.videoResolution == res,
                            onClick = { onSelectVideoResolution(res) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Primary Pill Action Button
            AuralisPrimaryButton(
                text = if (uiState is HomeUiState.Preview) "Download Now" else "Extract & Download",
                leadingIcon = Icons.Rounded.Download,
                onClick = {
                    if (uiState is HomeUiState.Preview) {
                        onDownloadClick()
                    } else {
                        onSearchSubmit()
                    }
                },
                enabled = uiState.url.isNotBlank() && uiState !is HomeUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
