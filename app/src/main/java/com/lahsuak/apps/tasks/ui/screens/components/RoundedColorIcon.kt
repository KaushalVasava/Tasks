package com.lahsuak.apps.tasks.ui.screens.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RoundedColorIcon(modifier: Modifier = Modifier, color: Color, size: Dp = 20.dp) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    ) {
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RoundedIconPreview() {
    RoundedColorIcon(color = Color.Blue)
}