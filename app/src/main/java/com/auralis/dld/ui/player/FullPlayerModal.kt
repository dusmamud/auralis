@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.auralis.dld.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auralis.dld.player.AuralisPlayerController
import com.auralis.dld.player.PlayerState
import com.auralis.dld.player.RepeatMode
import com.auralis.dld.ui.components.AuralisAlbumArt
import com.auralis.dld.ui.theme.AuralisTheme

@Composable
fun FullPlayerModal(
    isVisible: Boolean,
    playerState: PlayerState,
    playerController: AuralisPlayerController,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        BackHandler(onBack = onDismiss)
    }

    AnimatedVisibility(
        visible = isVisible && playerState.hasCurrentMedia,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        val media = playerState.currentMedia ?: return@AnimatedVisibility
        val density = LocalDensity.current
        val responsiveTitleSp = (24 * density.fontScale.coerceIn(0.85f, 1.15f)).sp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AuralisTheme.colors.canvas)
                .statusBarsPadding()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Top Action Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Collapse player",
                            tint = AuralisTheme.colors.ink,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "NOW PLAYING",
                        color = AuralisTheme.colors.inkMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Music Player Artwork (Large 1:1 Square)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    AuralisAlbumArt(
                        imageUrl = media.albumArtUri?.toString(),
                        size = 320.dp,
                        cornerRadius = 20.dp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Title & Artist
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = media.title,
                            color = AuralisTheme.colors.ink,
                            fontSize = responsiveTitleSp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = media.artist,
                            color = AuralisTheme.colors.inkMuted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee()
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Scrubber Timeline Slider
                    // Scrubber Timeline Slider with Apple Music Tactile Spring Physics
                    var isUserSeeking by remember { mutableStateOf(false) }
                    var seekFraction by remember { mutableFloatStateOf(0f) }

                    val displayProgress = if (isUserSeeking) seekFraction else playerState.progressFraction
                    val durationMs = playerState.durationMs.coerceAtLeast(1L)
                    val currentPosMs = if (isUserSeeking) (seekFraction * durationMs).toLong() else playerState.currentPositionMs

                    val sliderInteractionSource = remember { MutableInteractionSource() }
                    val isPressed by sliderInteractionSource.collectIsPressedAsState()
                    val isDragged by sliderInteractionSource.collectIsDraggedAsState()
                    val isInteracting = isPressed || isDragged

                    val animatedTrackHeight by animateDpAsState(
                        targetValue = if (isInteracting) 8.dp else 4.5.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "sliderTrackHeight"
                    )

                    val animatedThumbWidth by animateDpAsState(
                        targetValue = if (isInteracting) 12.dp else 7.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "sliderThumbWidth"
                    )

                    val animatedThumbHeight by animateDpAsState(
                        targetValue = if (isInteracting) 22.dp else 16.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "sliderThumbHeight"
                    )

                    val sliderColor = AuralisTheme.colors.actionBlue

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Buffer / Background track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                    LinearProgressIndicator(
                                        progress = { displayProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.5.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        color = sliderColor.copy(alpha = 0.35f),
                                        trackColor = AuralisTheme.colors.surfaceTileSecondary,
                                        strokeCap = StrokeCap.Round,
                                        drawStopIndicator = {}
                                    )
                                }
                            }

                            // Interactive Inflating Slider
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                Slider(
                                    value = displayProgress,
                                    onValueChange = { frac ->
                                        isUserSeeking = true
                                        seekFraction = frac
                                    },
                                    onValueChangeFinished = {
                                        val targetMs = (seekFraction * durationMs).toLong()
                                        playerController.seekTo(targetMs)
                                        isUserSeeking = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    interactionSource = sliderInteractionSource,
                                    track = { sliderState ->
                                        SliderDefaults.Track(
                                            modifier = Modifier.height(animatedTrackHeight),
                                            enabled = true,
                                            sliderState = sliderState,
                                            colors = SliderDefaults.colors(
                                                thumbColor = sliderColor,
                                                activeTrackColor = sliderColor,
                                                inactiveTrackColor = Color.Transparent
                                            ),
                                            thumbTrackGapSize = 0.dp,
                                            drawStopIndicator = null
                                        )
                                    },
                                    thumb = {
                                        SliderDefaults.Thumb(
                                            modifier = Modifier
                                                .height(animatedThumbHeight)
                                                .width(animatedThumbWidth)
                                                .padding(vertical = 2.dp),
                                            thumbSize = DpSize(animatedThumbWidth, animatedThumbHeight),
                                            interactionSource = sliderInteractionSource,
                                            colors = SliderDefaults.colors(
                                                thumbColor = sliderColor,
                                                activeTrackColor = sliderColor,
                                                inactiveTrackColor = Color.Transparent
                                            ),
                                            enabled = true
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp)
                        ) {
                            Text(
                                text = formatDuration(currentPosMs),
                                color = AuralisTheme.colors.inkMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatDuration(durationMs),
                                color = AuralisTheme.colors.inkMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Playback Controls Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Shuffle
                        IconButton(onClick = playerController::toggleShuffle) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (playerState.isShuffleEnabled) AuralisTheme.colors.actionBlue else AuralisTheme.colors.inkMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Previous
                        IconButton(
                            onClick = playerController::previous,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous",
                                tint = AuralisTheme.colors.ink,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Play / Pause FAB
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(AuralisTheme.colors.actionBlue)
                        ) {
                            IconButton(
                                onClick = playerController::togglePlayPause,
                                modifier = Modifier.size(68.dp)
                            ) {
                                if (playerState.isBuffering) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }

                        // Next
                        IconButton(
                            onClick = playerController::next,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next",
                                tint = AuralisTheme.colors.ink,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Repeat
                        IconButton(onClick = playerController::toggleRepeat) {
                            val repeatIcon = when (playerState.repeatMode) {
                                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            }
                            Icon(
                                imageVector = repeatIcon,
                                contentDescription = "Repeat",
                                tint = if (playerState.repeatMode != RepeatMode.OFF) AuralisTheme.colors.actionBlue else AuralisTheme.colors.inkMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
