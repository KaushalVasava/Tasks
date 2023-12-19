package com.lahsuak.apps.tasks.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChipGroup(
    items: List<String>,
    selectedIndex: Int,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    onSelectedChanged: (Int) -> Unit = {},
) {
    Column {
        LazyRow(
            horizontalArrangement = Arrangement.End
        ) {
            itemsIndexed(items) { index, item ->
                FilterChip(
                    label = {
                        Text(item)
                    },
                    colors = SelectableChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = selectedContainerColor,
                        selectedLabelColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                        disabledSelectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                        leadingIconColor = MaterialTheme.colorScheme.onSurface,
                        trailingIconColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = item == items[selectedIndex],
                        borderColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                    selected = items[selectedIndex] == item,
                    onClick = {
                        onSelectedChanged(index)
                    }
                )
            }
        }
    }
}