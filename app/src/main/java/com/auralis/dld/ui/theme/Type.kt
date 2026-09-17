package com.auralis.dld.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Typography hierarchy matching DESIGN.md:
// - Display headlines with tight negative letter-spacing
// - Body copy at 17sp (line-height 1.47em = ~25sp, letter-spacing -0.374sp)
// - Deliberate weights: 300 (Light), 400 (Regular), 600 (SemiBold), 700 (Bold). Weight 500 is deliberately absent.

object AuralisTextTokens {
    val HeroDisplay = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 56.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.28).sp
    )
    val DisplayLg = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    )
    val DisplayMd = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.374).sp
    )
    val Lead = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.196.sp
    )
    val Tagline = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.231.sp
    )
    val BodyStrong = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.374).sp
    )
    val Body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.374).sp
    )
    val CaptionStrong = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.224).sp
    )
    val Caption = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.224).sp
    )
    val FinePrint = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = (-0.12).sp
    )
    val MicroLegal = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = (-0.08).sp
    )
    val NavLink = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = (-0.12).sp
    )
}

val AuralisTypography = Typography(
    displayLarge = AuralisTextTokens.DisplayLg,
    displayMedium = AuralisTextTokens.DisplayMd,
    headlineLarge = AuralisTextTokens.Lead,
    headlineMedium = AuralisTextTokens.Tagline,
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = AuralisTextTokens.Body,
    bodyMedium = AuralisTextTokens.BodyStrong,
    bodySmall = AuralisTextTokens.Caption,
    labelLarge = AuralisTextTokens.CaptionStrong,
    labelSmall = AuralisTextTokens.FinePrint
)
