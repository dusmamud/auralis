package com.auralis.dld

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.dld.player.AuralisPlayerController
import com.auralis.dld.ui.components.MiniPlayer
import com.auralis.dld.ui.home.HomeRoute
import com.auralis.dld.ui.home.HomeViewModel
import com.auralis.dld.ui.library.LibraryRoute
import com.auralis.dld.ui.library.LibraryViewModel
import com.auralis.dld.ui.player.FullPlayerModal
import com.auralis.dld.ui.player.VideoPlayerModal
import com.auralis.dld.ui.queue.QueueRoute
import com.auralis.dld.ui.queue.QueueViewModel
import com.auralis.dld.ui.settings.SettingsRoute
import com.auralis.dld.ui.settings.SettingsViewModel
import com.auralis.dld.ui.theme.AuralisTheme

enum class NavTab(val title: String, val icon: ImageVector) {
    HOME("Download", Icons.Rounded.Download),
    QUEUE("Queue", Icons.AutoMirrored.Rounded.QueueMusic),
    LIBRARY("Library", Icons.Rounded.LibraryMusic),
    SETTINGS("Settings", Icons.Rounded.Settings)
}

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val queueViewModel: QueueViewModel by viewModels()
    private val libraryViewModel: LibraryViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission granted or denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Handle shared URL from YouTube / YouTube Music app
        handleIncomingIntent(intent)

        setContent {
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            AuralisTheme(themeMode = settingsUiState.themeMode) {
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val playerController = remember { AuralisPlayerController.getInstance(applicationContext) }
                var isFullPlayerVisible by remember { mutableStateOf(false) }

                Scaffold(
                    bottomBar = {
                        if (!isFullPlayerVisible) {
                            Column {
                                MiniPlayer(
                                    playerController = playerController,
                                    onMiniPlayerClick = { isFullPlayerVisible = true }
                                )
                                AuralisBottomNav(
                                    selectedTabIndex = selectedTabIndex,
                                    onTabSelected = { selectedTabIndex = it }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (!isFullPlayerVisible) innerPadding else androidx.compose.foundation.layout.PaddingValues(0.dp))
                        ) {
                            when (NavTab.entries[selectedTabIndex]) {
                                NavTab.HOME -> HomeRoute(
                                    viewModel = homeViewModel,
                                    onNavigateToQueue = { selectedTabIndex = NavTab.QUEUE.ordinal }
                                )
                                NavTab.QUEUE -> QueueRoute(viewModel = queueViewModel)
                                NavTab.LIBRARY -> LibraryRoute(
                                    viewModel = libraryViewModel,
                                    onVideoSelected = { isFullPlayerVisible = true }
                                )
                                NavTab.SETTINGS -> SettingsRoute(viewModel = settingsViewModel)
                            }
                        }

                        // Decoupled full player modals host (only recomposes when active)
                        PlayerModalsHost(
                            playerController = playerController,
                            isFullPlayerVisible = isFullPlayerVisible,
                            onDismiss = { isFullPlayerVisible = false }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            if (sharedText.isNotBlank()) {
                homeViewModel.onUrlChange(sharedText)
                homeViewModel.fetchMetadata()
            }
        }
    }
}

@Composable
private fun PlayerModalsHost(
    playerController: AuralisPlayerController,
    isFullPlayerVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isFullPlayerVisible) return
    val playerState by playerController.playerState.collectAsStateWithLifecycle()

    if (playerState.currentMedia?.isVideo == true) {
        VideoPlayerModal(
            isVisible = true,
            playerState = playerState,
            playerController = playerController,
            onDismiss = onDismiss
        )
    } else {
        FullPlayerModal(
            isVisible = true,
            playerState = playerState,
            playerController = playerController,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun AuralisBottomNav(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = AuralisTheme.colors.surfaceTile,
        modifier = modifier.fillMaxWidth()
    ) {
        NavTab.entries.forEachIndexed { index, tab ->
            val isSelected = selectedTabIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = (-0.12).sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AuralisTheme.colors.ink,
                    selectedTextColor = AuralisTheme.colors.ink,
                    indicatorColor = AuralisTheme.colors.surfaceTileSecondary,
                    unselectedIconColor = AuralisTheme.colors.inkMuted,
                    unselectedTextColor = AuralisTheme.colors.inkMuted
                )
            )
        }
    }
}
