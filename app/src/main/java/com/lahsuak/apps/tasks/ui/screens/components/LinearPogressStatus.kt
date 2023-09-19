package com.lahsuak.apps.tasks.ui.screens.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@Preview
fun LinearProgressStatus(
    completedTask: Int = 2,
    totalTask: Int = 5,
    text: String? = "Task completed",
    color: Color = ProgressIndicatorDefaults.linearColor,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    width: Dp = 240.dp,
    height: Dp = 16.dp,
) {
    val progress by animateFloatAsState(
        targetValue = completedTask.toFloat() / totalTask.toFloat(),
        label = "animate progress", animationSpec = spring(
            stiffness = Spring.StiffnessVeryLow
        )
    )
    Box(contentAlignment = Alignment.Center) {
        LinearProgressIndicator(
            progress = progress,
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .size(width, height)
                .clip(RoundedCornerShape(16.dp))
        )
        if (text != null)
            Text(
                "$completedTask / $totalTask $text",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
    }
}