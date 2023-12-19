package com.lahsuak.apps.tasks.ui.screens.settings

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.ui.theme.TaskAppTheme

@Composable
fun DropDownPreference(
    @DrawableRes
    icon: Int? = null,
    title: String,
    initialValue: String,
    items: List<Pair<String, Any>>,
    onClick: (Any, Int) -> Unit,
) {
    var isDropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    val ind = items.indexOfFirst { initialValue == it.first }

    var selectedItem by rememberSaveable {
        mutableIntStateOf(
            if (ind == -1) {
                0
            } else ind
        )
    }
    Card {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .toggleable(isDropDownExpanded, onValueChange = {
                    isDropDownExpanded = it
                })
        ) {
            if (icon != null) {
                Icon(
                    painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp)
                )
            }
            Text(title)
            if (items.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        Modifier
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            items[selectedItem].first,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Icon(
                            if (isDropDownExpanded)
                                Icons.Filled.KeyboardArrowUp
                            else
                                Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            Modifier.padding(end = 4.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = isDropDownExpanded,
                        onDismissRequest = { isDropDownExpanded = false }
                    ) {
                        items.forEachIndexed { index, item ->
                            DropdownMenuItem(
                                text = {
                                    Text(text = item.first, fontSize = 14.sp)
                                },
                                onClick = {
                                    onClick(item.second, index)
                                    selectedItem = index
                                    isDropDownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClickPreference(
    @DrawableRes
    icon: Int? = null,
    title: String,
    placeHolder: String? = null,
    onClick: () -> Unit,
) {
    Card(onClick = { onClick() }) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (icon != null) {
                Image(
                    painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(title)
                if (placeHolder != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(placeHolder, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SwitchPreference(
    title: String,
    placeHolder: String? = null,
    @DrawableRes
    icon: Int? = null,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    Card {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .semantics(mergeDescendants = true) {}
                .toggleable(value, onValueChange = {
                    onValueChange(it)
                })
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    painterResource(id = icon), contentDescription = null,
                    modifier = Modifier.padding(4.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(title)
                if (placeHolder != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(placeHolder, fontSize = 14.sp)
                }
            }
            Switch(
                checked = value,
                onCheckedChange = null
            )
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewSwitch() {
    TaskAppTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ClickPreference(title = "Backup Data", icon = R.drawable.ic_backup) {
                }
                SwitchPreference(
                    title = "Show voice icon",
                    placeHolder = "enable",
                    icon = R.drawable.ic_mic,
                    value = true
                ) {}
                DropDownPreference(
                    icon = R.drawable.ic_theme,
                    initialValue = "",
                    title = "Theme",
                    items = emptyList()
                ) { _, _ ->
                }
            }
        }
    }
}