package com.lahsuak.apps.tasks.ui.screens.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@Preview
fun LinearProgressStatus(
    modifier: Modifier = Modifier,
    progress: Float = 0.0f,
    text: String? = "Task completed",
    color: Color = ProgressIndicatorDefaults.linearColor,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    width: Dp = 240.dp,
    height: Dp = 16.dp,
) {
    val animProgress by animateFloatAsState(
        targetValue = progress,
        label = "animate progress", animationSpec = spring(
            stiffness = Spring.StiffnessVeryLow
        )
    )
    Box(contentAlignment = Alignment.Center) {
        LinearProgressIndicator(
            progress = animProgress,
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Square,
            modifier = modifier.size(width, height)
                .clip(RoundedCornerShape(8.dp))
        )
        if (text != null)
            Text(text, fontSize = 14.sp)
    }
}