package com.lahsuak.apps.tasks.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CheckBoxWithText(
    text: String,
    modifier: Modifier = Modifier,
    value: Boolean,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    onValueChange: (Boolean) -> Unit,
    fontSize:TextUnit = 14.sp
) {
    Row(
        modifier
            .toggleable(
                value,
                role = Role.Checkbox,
                onValueChange = {
                    onValueChange(it)
                }
            )
            .semantics(mergeDescendants = true) {}
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {
        Checkbox(checked = value, onCheckedChange = null)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = fontSize)
    }
}