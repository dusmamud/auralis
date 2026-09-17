package com.auralis.dld.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.dld.player.AuralisPlayerController
import com.auralis.dld.player.PlayerState
import com.auralis.dld.ui.theme.AuralisTheme

@Composable
fun MiniPlayer(
    playerController: AuralisPlayerController,
    onMiniPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by playerController.playerState.collectAsStateWithLifecycle()
    MiniPlayer(
        playerState = playerState,
        onPlayPauseClick = playerController::togglePlayPause,
        onMiniPlayerClick = onMiniPlayerClick,
        modifier = modifier
    )
}

@Composable
fun MiniPlayer(
    playerState: PlayerState,
    onPlayPauseClick: () -> Unit,
    onMiniPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val media = playerState.currentMedia ?: return

    AnimatedVisibility(
        visible = playerState.hasCurrentMedia,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        val shape = RoundedCornerShape(16.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(shape)
                .background(AuralisTheme.colors.surfaceTile)
                .border(1.dp, AuralisTheme.colors.hairline, shape)
                .clickable(onClick = onMiniPlayerClick)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                // Artwork / Video Icon
                if (media.isVideo) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AuralisTheme.colors.surfaceTileSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Movie,
                            contentDescription = null,
                            tint = AuralisTheme.colors.inkMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    AuralisAlbumArt(
                        imageUrl = media.albumArtUri?.toString(),
                        size = 44.dp,
                        cornerRadius = 8.dp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Artist with Marquee
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = media.title,
                        color = AuralisTheme.colors.ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.224).sp,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = media.artist,
                        color = AuralisTheme.colors.inkMuted,
                        fontSize = 12.sp,
                        letterSpacing = (-0.12).sp,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play / Pause Button
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    if (playerState.isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AuralisTheme.colors.focusBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = AuralisTheme.colors.ink,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Micro Progress Bar at bottom of card
            LinearProgressIndicator(
                progress = { playerState.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter),
                color = AuralisTheme.colors.focusBlue,
                trackColor = Color.Transparent,
                strokeCap = StrokeCap.Round
            )
        }
    }
}
