package com.auralis.dld.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.auralis.dld.ui.theme.AuralisTheme

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.remember

private var cachedShadowBlurPx: Float = -1f
private var cachedShadowPaint: Paint? = null

/**
 * Signature Single Product Drop Shadow (DESIGN.md elevation token).
 * rgba(0, 0, 0, 0.22) 3px 5px 30px applied exclusively to product/media artwork.
 */
fun Modifier.auralisArtworkShadow(
    borderRadius: Dp = 12.dp
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val blurRadius = 30.dp.toPx()
        val paint = if (cachedShadowPaint != null && cachedShadowBlurPx == blurRadius) {
            cachedShadowPaint!!
        } else {
            val newPaint = Paint().apply {
                color = Color(0x38000000) // rgba(0, 0, 0, 0.22)
                asFrameworkPaint().apply {
                    maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
                }
            }
            cachedShadowBlurPx = blurRadius
            cachedShadowPaint = newPaint
            newPaint
        }
        canvas.drawRoundRect(
            left = 3.dp.toPx(),
            top = 5.dp.toPx(),
            right = size.width + 3.dp.toPx(),
            bottom = size.height + 5.dp.toPx(),
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}

/**
 * 1:1 Square Album Cover Art with soft rounded corners and signature shadow.
 */
@Composable
fun AuralisAlbumArt(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    cornerRadius: Dp = 12.dp
) {
    val density = LocalDensity.current
    val sizePx = remember(density, size) {
        with(density) { size.roundToPx() }
    }
    val context = LocalContext.current
    val imageRequest = remember(context, imageUrl, sizePx) {
        if (imageUrl.isNullOrBlank()) null else {
            ImageRequest.Builder(context)
                .data(imageUrl)
                .size(sizePx, sizePx)
                .crossfade(true)
                .build()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .auralisArtworkShadow(borderRadius = cornerRadius)
            .clip(RoundedCornerShape(cornerRadius))
            .background(AuralisTheme.colors.surfaceTileSecondary)
    ) {
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "Album Cover Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = AuralisTheme.colors.inkMuted,
                modifier = Modifier.size(size / 2)
            )
        }
    }
}
