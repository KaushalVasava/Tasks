package com.lahsuak.apps.tasks.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipGroup(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChanged: (Int) -> Unit = {},
) {
    Column(modifier = Modifier) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            itemsIndexed(items) { index, item ->
                FilterChip(
                    label = {
                        Text(item)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    selected = items[selectedIndex] == item,
                    onClick = {
                        onSelectedChanged(index)
                    }
                )
            }
        }
    }
}