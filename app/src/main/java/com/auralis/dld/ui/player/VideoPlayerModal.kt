package com.auralis.dld.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.util.Rational
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.auralis.dld.player.AuralisPlayerController
import com.auralis.dld.player.PlayerState
import com.auralis.dld.ui.theme.AuralisTheme
import kotlinx.coroutines.delay

enum class VideoScaleMode(val label: String, val mode: Int) {
    FIT("Fit", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    ZOOM("Zoom (Fill)", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    STRETCH("Stretch", AspectRatioFrameLayout.RESIZE_MODE_FILL)
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerModal(
    isVisible: Boolean,
    playerState: PlayerState,
    playerController: AuralisPlayerController,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    if (isVisible) {
        BackHandler(onBack = onDismiss)
    }

    // Video aspect ratio mode
    var scaleModeIndex by remember { mutableIntStateOf(0) }
    val scaleModes = VideoScaleMode.entries
    val currentScaleMode = scaleModes[scaleModeIndex]

    // Overlay controls state & auto-hide
    var showControls by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var controlsLastInteracted by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Gestures HUD
    var hudText by remember { mutableStateOf<String?>(null) }
    var hudIcon by remember { mutableStateOf<androidx.compose.ui.graphics.vector.ImageVector?>(null) }
    var doubleTapSeekLeft by remember { mutableStateOf(false) }
    var doubleTapSeekRight by remember { mutableStateOf(false) }

    // Screen Brightness (0.01f to 1.0f)
    var currentBrightness by remember {
        val initial = activity?.window?.attributes?.screenBrightness ?: -1f
        mutableFloatStateOf(if (initial < 0) 0.5f else initial)
    }

    // Media Volume
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }

    // Speed options
    var showSpeedMenu by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableFloatStateOf(1.0f) }
    val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    // Orientation state
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Auto-hide controls timer (3.5 seconds)
    LaunchedEffect(showControls, controlsLastInteracted, isLocked) {
        if (showControls && !isLocked) {
            delay(3500L)
            showControls = false
        }
    }

    // Clear HUD message after 1.2s
    LaunchedEffect(hudText) {
        if (hudText != null) {
            delay(1200L)
            hudText = null
            hudIcon = null
        }
    }

    // Reset double-tap ripples
    LaunchedEffect(doubleTapSeekLeft) {
        if (doubleTapSeekLeft) {
            delay(700L)
            doubleTapSeekLeft = false
        }
    }
    LaunchedEffect(doubleTapSeekRight) {
        if (doubleTapSeekRight) {
            delay(700L)
            doubleTapSeekRight = false
        }
    }

    // Restore orientation and brightness on exit
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.let { act ->
                val lp = act.window.attributes
                lp.screenBrightness = -1f // System default
                act.window.attributes = lp
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible && playerState.hasCurrentMedia && playerState.currentMedia?.isVideo == true,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(200)),
        modifier = modifier
    ) {
        val media = playerState.currentMedia ?: return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 1. ExoPlayer PlayerView
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        player = playerController.exoPlayer
                        useController = false
                        resizeMode = currentScaleMode.mode
                        setKeepContentOnPlayerReset(true)
                    }
                },
                update = { view ->
                    view.player = playerController.exoPlayer
                    view.resizeMode = currentScaleMode.mode
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. Gesture Surface Detector
            var isBrightnessDrag by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isLocked) {
                        if (isLocked) {
                            detectTapGestures(
                                onTap = {
                                    // Show unlock button momentarily
                                    showControls = true
                                    controlsLastInteracted = System.currentTimeMillis()
                                }
                            )
                        } else {
                            detectTapGestures(
                                onTap = {
                                    showControls = !showControls
                                    controlsLastInteracted = System.currentTimeMillis()
                                },
                                onDoubleTap = { offset ->
                                    val screenWidth = size.width
                                    if (offset.x < screenWidth * 0.45f) {
                                        // Seek backward 10s
                                        val newPos = (playerState.currentPositionMs - 10000L).coerceAtLeast(0L)
                                        playerController.seekTo(newPos)
                                        doubleTapSeekLeft = true
                                    } else if (offset.x > screenWidth * 0.55f) {
                                        // Seek forward 10s
                                        val duration = playerState.durationMs.coerceAtLeast(1L)
                                        val newPos = (playerState.currentPositionMs + 10000L).coerceAtMost(duration)
                                        playerController.seekTo(newPos)
                                        doubleTapSeekRight = true
                                    }
                                }
                            )
                        }
                    }
                    .pointerInput(isLocked) {
                        if (!isLocked) {
                            detectVerticalDragGestures(
                                onDragStart = { offset ->
                                    isBrightnessDrag = offset.x < size.width * 0.5f
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    controlsLastInteracted = System.currentTimeMillis()

                                    if (isBrightnessDrag) {
                                        // Brightness control
                                        val delta = -dragAmount / 600f
                                        val newBrightness = (currentBrightness + delta).coerceIn(0.01f, 1.0f)
                                        currentBrightness = newBrightness
                                        activity?.let { act ->
                                            val lp = act.window.attributes
                                            lp.screenBrightness = newBrightness
                                            act.window.attributes = lp
                                        }
                                        hudIcon = Icons.Default.BrightnessHigh
                                        hudText = "Brightness ${(newBrightness * 100).toInt()}%"
                                    } else {
                                        // Volume control
                                        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                        val delta = if (dragAmount < -10f) 1 else if (dragAmount > 10f) -1 else 0
                                        if (delta != 0) {
                                            val newVol = (currentVol + delta).coerceIn(0, maxVolume)
                                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                            val percent = (newVol.toFloat() / maxVolume * 100).toInt()
                                            hudIcon = Icons.AutoMirrored.Filled.VolumeUp
                                            hudText = "Volume $percent%"
                                        }
                                    }
                                }
                            )
                        }
                    }
            )

            // 3. Double Tap Seek Ripple Overlays
            if (doubleTapSeekLeft) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 40.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    SeekIndicator(icon = Icons.Default.Replay10, text = "-10s")
                }
            }
            if (doubleTapSeekRight) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 40.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    SeekIndicator(icon = Icons.Default.Forward10, text = "+10s")
                }
            }

            // 4. Floating Gesture HUD Indicator (Volume / Brightness)
            if (hudText != null && hudIcon != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = hudIcon!!,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = hudText!!,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 5. Screen Lock Floating Toggle (when locked)
            if (isLocked) {
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 24.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(54.dp)
                            .clickable {
                                isLocked = false
                                showControls = true
                                controlsLastInteracted = System.currentTimeMillis()
                                hudIcon = Icons.Default.LockOpen
                                hudText = "Controls Unlocked"
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Unlock",
                                tint = AuralisTheme.colors.actionBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // 6. Interactive Player Overlays (Top Bar, Center, Bottom)
            AnimatedVisibility(
                visible = showControls && !isLocked,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.40f))
                ) {
                    // Top Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = media.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee()
                            )
                        }

                        // Top Action Buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Aspect Ratio Mode Button
                            IconButton(
                                onClick = {
                                    controlsLastInteracted = System.currentTimeMillis()
                                    scaleModeIndex = (scaleModeIndex + 1) % scaleModes.size
                                    hudIcon = Icons.Default.AspectRatio
                                    hudText = currentScaleMode.label
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = "Aspect Ratio",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Playback Speed Selector
                            Box {
                                IconButton(
                                    onClick = {
                                        controlsLastInteracted = System.currentTimeMillis()
                                        showSpeedMenu = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Speed",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSpeedMenu,
                                    onDismissRequest = { showSpeedMenu = false },
                                    modifier = Modifier.background(AuralisTheme.colors.canvas)
                                ) {
                                    speedOptions.forEach { spd ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "${spd}x",
                                                    fontWeight = if (currentSpeed == spd) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (currentSpeed == spd) AuralisTheme.colors.actionBlue else AuralisTheme.colors.ink
                                                )
                                            },
                                            onClick = {
                                                currentSpeed = spd
                                                playerController.exoPlayer.playbackParameters = PlaybackParameters(spd)
                                                showSpeedMenu = false
                                                hudIcon = Icons.Default.Speed
                                                hudText = "Speed ${spd}x"
                                            }
                                        )
                                    }
                                }
                            }

                            // Picture-in-Picture Button
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                IconButton(
                                    onClick = {
                                        controlsLastInteracted = System.currentTimeMillis()
                                        showControls = false
                                        try {
                                            val params = PictureInPictureParams.Builder()
                                                .setAspectRatio(Rational(16, 9))
                                                .build()
                                            activity?.enterPictureInPictureMode(params)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureInPictureAlt,
                                        contentDescription = "PiP",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Lock Screen Button
                            IconButton(
                                onClick = {
                                    isLocked = true
                                    showControls = false
                                    hudIcon = Icons.Default.Lock
                                    hudText = "Screen Locked"
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = "Lock",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Center Playback Controls (10s back, Play/Pause, 10s forward)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                controlsLastInteracted = System.currentTimeMillis()
                                val newPos = (playerState.currentPositionMs - 10000L).coerceAtLeast(0L)
                                playerController.seekTo(newPos)
                                doubleTapSeekLeft = true
                            },
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(36.dp))

                        Surface(
                            color = AuralisTheme.colors.actionBlue,
                            shape = CircleShape,
                            modifier = Modifier.size(68.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    controlsLastInteracted = System.currentTimeMillis()
                                    playerController.togglePlayPause()
                                },
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
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(36.dp))

                        IconButton(
                            onClick = {
                                controlsLastInteracted = System.currentTimeMillis()
                                val duration = playerState.durationMs.coerceAtLeast(1L)
                                val newPos = (playerState.currentPositionMs + 10000L).coerceAtMost(duration)
                                playerController.seekTo(newPos)
                                doubleTapSeekRight = true
                            },
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    // Bottom Bar with Timeline Scrubber & Orientation Toggle
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        var isUserSeeking by remember { mutableStateOf(false) }
                        var seekFraction by remember { mutableFloatStateOf(0f) }

                        val displayProgress = if (isUserSeeking) seekFraction else playerState.progressFraction
                        val durationMs = playerState.durationMs.coerceAtLeast(1L)
                        val currentPosMs = if (isUserSeeking) (seekFraction * durationMs).toLong() else playerState.currentPositionMs

                        // Scrubber Slider
                        Slider(
                            value = displayProgress,
                            onValueChange = { frac ->
                                isUserSeeking = true
                                seekFraction = frac
                                controlsLastInteracted = System.currentTimeMillis()
                            },
                            onValueChangeFinished = {
                                val targetMs = (seekFraction * durationMs).toLong()
                                playerController.seekTo(targetMs)
                                isUserSeeking = false
                                controlsLastInteracted = System.currentTimeMillis()
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = AuralisTheme.colors.actionBlue,
                                activeTrackColor = AuralisTheme.colors.actionBlue,
                                inactiveTrackColor = Color.White.copy(alpha = 0.30f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Duration labels and Orientation Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${formatDuration(currentPosMs)} / ${formatDuration(durationMs)}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            // Rotate Screen Toggle Button
                            IconButton(
                                onClick = {
                                    controlsLastInteracted = System.currentTimeMillis()
                                    activity?.let { act ->
                                        act.requestedOrientation = if (isLandscape) {
                                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                        } else {
                                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isLandscape) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Rotate",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeekIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        color = Color.Black.copy(alpha = 0.65f),
        shape = CircleShape,
        modifier = Modifier.size(80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
