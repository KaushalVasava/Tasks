package com.lahsuak.apps.tasks.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.Task

@Composable
fun ShareDialog(
    tasks: List<Task>,
    openDialog:Boolean,
    onDialogStatusChange:(Boolean)->Unit,
    onTaskAddButtonClick: () -> Unit,
    onSaveButtonClick: (Task) -> Unit,
    onCancelButtonClick: () -> Unit,
) {
    var isDropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var selectedTaskIndex by remember {
        mutableIntStateOf(0)
    }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = {
                onDialogStatusChange(false)
            },
            title = {
                Text(stringResource(id = R.string.add_task_or_subtask))
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        onTaskAddButtonClick()
                    }) {
                        Text(stringResource(id = R.string.add_task))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(id = R.string.or))
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(id = R.string.select_task),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Row(
                            Modifier
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                                .semantics(mergeDescendants = true) {}
                                .toggleable(isDropDownExpanded, onValueChange = {
                                    isDropDownExpanded = it
                                })
                                .onGloballyPositioned { coordinates ->
                                    // This value is used to assign to
                                    // the DropDown the same width
                                    textFieldSize = coordinates.size.toSize()
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                tasks[selectedTaskIndex].title,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                            Icon(
                                if (isDropDownExpanded)
                                    Icons.Filled.KeyboardArrowUp
                                else
                                    Icons.Filled.KeyboardArrowDown,
                                contentDescription = "sort expand/collapse button",
                                Modifier
                                    .padding(end = 4.dp)
                                    .clickable {
                                        isDropDownExpanded = !isDropDownExpanded
                                    }
                            )
                        }
                        DropdownMenu(
                            expanded = isDropDownExpanded,
                            onDismissRequest = { isDropDownExpanded = false },
                            modifier = Modifier
                                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                        ) {
                            tasks.forEachIndexed { index, task ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = task.title)
                                    },
                                    onClick = {
                                        selectedTaskIndex = index
                                        isDropDownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSaveButtonClick(tasks[selectedTaskIndex])
                }) {
                    Text(stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                ElevatedButton(onClick = { onCancelButtonClick() }) {
                    Text(stringResource(id = R.string.cancel))
                }
            },
        )
    }
}