package com.auralis.dld.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive Content Container (DESIGN.md Layout & Grid principles).
 * Provides a clean, centered container with max-width 760dp on wide screens / landscape,
 * preventing UI elements from stretching endlessly across large viewports while
 * giving museum-gallery breathing room on phones and tablets.
 */
@Composable
fun AuralisResponsiveContainer(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp,
    maxWidth: Dp = 760.dp,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth)
                .padding(horizontal = horizontalPadding)
        ) {
            content()
        }
    }
}
