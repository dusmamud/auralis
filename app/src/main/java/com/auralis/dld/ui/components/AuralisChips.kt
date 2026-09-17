package com.auralis.dld.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auralis.dld.ui.theme.AuralisTheme

/**
 * Option Selector Chip (DESIGN.md configurator-option-chip).
 * Pill shape (9999px), 2dp FocusBlue border when selected.
 */
@Composable
fun AuralisOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        AuralisTheme.colors.focusBlue
    } else {
        AuralisTheme.colors.hairline
    }

    val borderWidth = if (isSelected) 2.dp else 1.dp
    val textColor = AuralisTheme.colors.ink
    val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(if (isSelected) AuralisTheme.colors.surfaceTile else AuralisTheme.colors.surfaceTileSecondary.copy(alpha = 0.5f))
            .border(
                width = borderWidth,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = fontWeight,
            letterSpacing = (-0.224).sp
        )
    }
}
