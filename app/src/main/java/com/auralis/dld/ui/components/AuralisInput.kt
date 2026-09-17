package com.auralis.dld.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auralis.dld.ui.theme.AuralisTheme

/**
 * Signature Auralis Search & URL Input Field (DESIGN.md search-input).
 * Full Pill shape (9999px), 44dp height, trailing clipboard paste button.
 */
@Composable
fun AuralisUrlInput(
    url: String,
    onUrlChange: (String) -> Unit,
    onPasteClick: () -> Unit,
    onClearClick: () -> Unit,
    onSearchSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = url,
        onValueChange = onUrlChange,
        textStyle = TextStyle(
            color = AuralisTheme.colors.ink,
            fontSize = 17.sp,
            letterSpacing = (-0.37).sp
        ),
        cursorBrush = SolidColor(AuralisTheme.colors.actionBlue),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchSubmit() }
        ),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        color = AuralisTheme.colors.surfaceTile,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = AuralisTheme.colors.hairline,
                        shape = CircleShape
                    )
                    .padding(horizontal = 18.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (url.isEmpty()) {
                        Text(
                            text = "Paste YouTube or YT Music link...",
                            color = AuralisTheme.colors.inkMuted,
                            fontSize = 17.sp,
                            letterSpacing = (-0.374).sp
                        )
                    }
                    innerTextField()
                }

                if (url.isNotEmpty()) {
                    IconButton(
                        onClick = onClearClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear link",
                            tint = AuralisTheme.colors.inkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onPasteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentPaste,
                        contentDescription = "Paste from clipboard",
                        tint = AuralisTheme.colors.inkMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        modifier = modifier
    )
}
