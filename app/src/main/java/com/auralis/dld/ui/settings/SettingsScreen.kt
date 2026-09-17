package com.auralis.dld.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.dld.ui.components.AuralisOptionChip
import com.auralis.dld.ui.components.AuralisPrimaryButton
import com.auralis.dld.ui.components.AuralisResponsiveContainer
import com.auralis.dld.ui.theme.AuralisTheme
import com.auralis.dld.ui.theme.ThemeMode

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        onThemeSelect = viewModel::setThemeMode,
        onUpdateEngineClick = viewModel::updateEngine,
        modifier = modifier
    )
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onThemeSelect: (ThemeMode) -> Unit,
    onUpdateEngineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuralisTheme.colors.canvas)
    ) {
        AuralisResponsiveContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                val screenWidth = LocalConfiguration.current.screenWidthDp
                val titleSp = when {
                    screenWidth < 420 -> 28.sp
                    screenWidth < 640 -> 34.sp
                    else -> 40.sp
                }

                Text(
                    text = "Settings",
                    color = AuralisTheme.colors.ink,
                    fontSize = titleSp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "App preferences & extraction engine",
                    color = AuralisTheme.colors.inkMuted,
                    fontSize = 14.sp,
                    letterSpacing = (-0.224).sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Section 1: Appearance & Theme
                Text(
                    text = "APPEARANCE",
                    color = AuralisTheme.colors.inkMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.224).sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AuralisOptionChip(
                        label = "System Default",
                        isSelected = uiState.themeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeSelect(ThemeMode.SYSTEM) }
                    )
                    AuralisOptionChip(
                        label = "Dark",
                        isSelected = uiState.themeMode == ThemeMode.DARK,
                        onClick = { onThemeSelect(ThemeMode.DARK) }
                    )
                    AuralisOptionChip(
                        label = "Light",
                        isSelected = uiState.themeMode == ThemeMode.LIGHT,
                        onClick = { onThemeSelect(ThemeMode.LIGHT) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Section 2: In-App Dynamic Extraction Engine Updater (Anti-Break Protection)
                Text(
                    text = "EXTRACTION ENGINE",
                    color = AuralisTheme.colors.inkMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.224).sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(AuralisTheme.colors.surfaceTile)
                        .border(1.dp, AuralisTheme.colors.hairline, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = AuralisTheme.colors.inkMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "yt-dlp Core Engine",
                            color = AuralisTheme.colors.ink,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.374).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Current Version: ${uiState.engineStatus.currentVersion}",
                        color = AuralisTheme.colors.ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.224).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "YouTube changes cipher signatures frequently. Update the engine anytime in-app without waiting for a new app release.",
                        color = AuralisTheme.colors.inkMuted,
                        fontSize = 14.sp,
                        letterSpacing = (-0.224).sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.engineStatus.isUpdating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = AuralisTheme.colors.actionBlue,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Downloading latest yt-dlp wheel...",
                                color = AuralisTheme.colors.inkMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        AuralisPrimaryButton(
                            text = "Update Extraction Engine",
                            leadingIcon = Icons.Rounded.Sync,
                            onClick = onUpdateEngineClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (uiState.engineStatus.updateError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Error,
                                contentDescription = null,
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.engineStatus.updateError,
                                color = Color(0xFFFF3B30),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Section 3: About
                Text(
                    text = "ABOUT",
                    color = AuralisTheme.colors.inkMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.224).sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(AuralisTheme.colors.surfaceTile)
                        .border(1.dp, AuralisTheme.colors.hairline, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Auralis v1.0.0",
                        color = AuralisTheme.colors.ink,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.374).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "100% Client-Side • Embedded Chaquopy • Zero Backend Server Cost",
                        color = AuralisTheme.colors.inkMuted,
                        fontSize = 14.sp,
                        letterSpacing = (-0.224).sp
                    )
                }
            }
        }
    }
}
