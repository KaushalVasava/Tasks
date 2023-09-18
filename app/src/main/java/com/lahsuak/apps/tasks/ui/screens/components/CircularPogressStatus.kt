package com.lahsuak.apps.tasks.ui.screens.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@Preview
fun CircularProgressStatus(
    progress: Float = 0.5f,
    color: Color = ProgressIndicatorDefaults.circularColor,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    trackColor: Color = ProgressIndicatorDefaults.circularTrackColor,
    size:Dp = 44.dp
) {
    Box(contentAlignment = Alignment.Center) {
        Text((progress*100).toInt().toString(), fontSize = 12.sp)
        CircularProgressIndicator(
            progress = progress ,
            color = color,
            strokeWidth = strokeWidth,
            trackColor = trackColor,
            modifier = Modifier.size(size)
        )
    }
}