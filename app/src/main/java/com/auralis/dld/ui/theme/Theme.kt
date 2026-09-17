package com.auralis.dld.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

@Immutable
data class AuralisColorScheme(
    val actionBlue: Color,
    val focusBlue: Color,
    val skyLinkBlue: Color,
    val canvas: Color,
    val canvasParchment: Color,
    val surfaceTile: Color,
    val surfaceTileSecondary: Color,
    val surfacePearl: Color,
    val ink: Color,
    val inkMuted: Color,
    val hairline: Color,
    val divider: Color,
    val isDark: Boolean
)

val LightColorScheme = AuralisColorScheme(
    actionBlue = AuralisColors.ActionBlue,
    focusBlue = AuralisColors.FocusBlue,
    skyLinkBlue = AuralisColors.SkyLinkBlue,
    canvas = AuralisColors.CanvasWhite,
    canvasParchment = AuralisColors.CanvasParchment,
    surfaceTile = AuralisColors.CanvasWhite,
    surfaceTileSecondary = AuralisColors.CanvasParchment,
    surfacePearl = AuralisColors.SurfacePearl,
    ink = AuralisColors.Ink,
    inkMuted = AuralisColors.InkMuted48,
    hairline = AuralisColors.Hairline,
    divider = AuralisColors.DividerSoft,
    isDark = false
)

val DarkColorScheme = AuralisColorScheme(
    actionBlue = AuralisColors.SkyLinkBlue, // SkyLinkBlue on dark for high contrast
    focusBlue = AuralisColors.FocusBlue,
    skyLinkBlue = AuralisColors.SkyLinkBlue,
    canvas = AuralisColors.SurfaceBlack,   // OLED pure black
    canvasParchment = AuralisColors.SurfaceTile3,
    surfaceTile = AuralisColors.SurfaceTile1,
    surfaceTileSecondary = AuralisColors.SurfaceTile2,
    surfacePearl = AuralisColors.SurfaceTile2,
    ink = AuralisColors.BodyOnDark,
    inkMuted = AuralisColors.BodyMuted,
    hairline = Color(0xFF38383A),
    divider = Color(0xFF2A2A2C),
    isDark = true
)

val LocalAuralisColors = staticCompositionLocalOf { LightColorScheme }

@Composable
fun AuralisTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val customColors = if (isDark) DarkColorScheme else LightColorScheme

    val materialColors = if (isDark) {
        darkColorScheme(
            primary = customColors.actionBlue,
            background = customColors.canvas,
            surface = customColors.surfaceTile,
            onPrimary = Color.White,
            onBackground = customColors.ink,
            onSurface = customColors.ink
        )
    } else {
        lightColorScheme(
            primary = customColors.actionBlue,
            background = customColors.canvas,
            surface = customColors.surfaceTile,
            onPrimary = Color.White,
            onBackground = customColors.ink,
            onSurface = customColors.ink
        )
    }

    CompositionLocalProvider(
        LocalAuralisColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = AuralisTypography,
            content = content
        )
    }
}

object AuralisTheme {
    val colors: AuralisColorScheme
        @Composable
        get() = LocalAuralisColors.current

    val typography = AuralisTypography
}
