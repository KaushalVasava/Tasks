package com.lahsuak.apps.tasks.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CircleCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val color = MaterialTheme.colorScheme
    val imageVector = if (checked) Icons.Filled.CheckCircle else Icons.Default.FavoriteBorder
    val tint = if (checked) color.primary.copy(alpha = 0.8f) else color.onSurface.copy(alpha = 0.8f)
    val background = if (checked) color.surface else Color.Transparent

    IconButton(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier.offset(x = 4.dp, y = 4.dp),
        enabled = enabled
    ) {
        Icon(
            imageVector = imageVector, tint = tint,
            modifier = Modifier.background(background, shape = CircleShape),
            contentDescription = "checkbox"
        )
    }
}
