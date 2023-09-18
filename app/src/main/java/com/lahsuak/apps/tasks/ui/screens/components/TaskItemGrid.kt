package com.lahsuak.apps.tasks.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.util.DateUtil
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItemGrid(
    task: Task,
    onImpSwipe: (Boolean) -> Unit,
    onItemClick: () -> Unit,
    onCompletedTask: (Boolean) -> Unit,
    onEditIconClick: (Boolean) -> Unit,
) {
    var isExpanded by rememberSaveable {
        mutableStateOf(true)
    }
    var isChecked by rememberSaveable {
        mutableStateOf(task.isDone)
    }

    var show by rememberSaveable { mutableStateOf(true) }
    val dismissState = rememberDismissState(
        confirmValueChange = {
            if (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd) {
                show = false
                true
            } else
                false
        }
    )
    AnimatedVisibility(
        show, exit = fadeOut(spring())
    ) {
        SwipeToDismiss(
            state = dismissState,
            background = {
                DismissBackground(dismissState)
            },
            dismissContent = {
                Column(
                    Modifier
                        .clickable { onItemClick() }
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .drawBehind {
                            drawLine(
                                color = Color(TaskApp.categoryTypes[task.color].color),
                                start = Offset(0f, 0f),
                                end = Offset(this.size.width, 0f),
                                strokeWidth = 16.dp.toPx(),
                            )
                        }
                ) {
                    Box {
                        if (task.isImp) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_pin),
                                contentDescription = "Important",
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 8.dp, start = 4.dp)
                            )
                        }
                        Row(Modifier.fillMaxWidth()) {
                            CircleCheckbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    onCompletedTask(it)
                                    isChecked = it
                                },
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(
                                        modifier = Modifier.weight(2f)
                                    ) {
                                        Text(
                                            task.title,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textDecoration = if (isChecked)
                                                TextDecoration.LineThrough
                                            else
                                                TextDecoration.None
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        task.subTaskList?.let {
                                            AnimatedVisibility(visible = isExpanded) {
                                                Text(
                                                    it, fontSize = 14.sp,
                                                    fontWeight = FontWeight.Light,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = if (isExpanded)
                                                        10 else
                                                        1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.clickable {
                                                // set reminder
                                            }
                                        ) {
                                            Icon(
                                                painterResource(id = R.drawable.ic_reminder_small),
                                                contentDescription = null
                                            )
                                            Spacer(
                                                Modifier
                                                    .width(4.dp)
                                                    .align(Alignment.Bottom)
                                            )
                                            Text("Reminder", fontSize = 12.sp)
                                        }
                                        Row {
                                            IconButton(onClick = {
                                                // expand list
                                                isExpanded = !isExpanded
                                            }) {
                                                Icon(
                                                    painterResource(id = R.drawable.ic_expand_more),
                                                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                                                    contentDescription = null
                                                )
                                            }
                                            IconButton(onClick = {
                                                onEditIconClick(isChecked)
                                            }) {
                                                Icon(
                                                    painter = if (isChecked) {
                                                        painterResource(id = R.drawable.ic_delete)
                                                    } else {
                                                        painterResource(id = R.drawable.ic_edit)
                                                    },
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            trackColor = MaterialTheme.colorScheme.surface,
                                            progress = 0.60f, modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .height(16.dp)
                                                .weight(1f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier
                                                .clip(
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .background(Color(TaskApp.categoryTypes[task.color].color))
                                                .padding(4.dp)
                                        ) {
                                            Icon(
                                                painterResource(id = R.drawable.ic_calendar_small),
                                                contentDescription = null
                                            )
                                            Spacer(
                                                Modifier
                                                    .width(4.dp)
                                                    .align(Alignment.Bottom)
                                            )
                                            Text(
                                                DateUtil.getDateRange(
                                                    task.startDate!!,
                                                    task.endDate
                                                ),
                                                fontSize = 10.sp,
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
    LaunchedEffect(show) {
        if (!show) {
            delay(800)
            when (dismissState.dismissDirection) {
                DismissDirection.EndToStart -> {
                    onEditIconClick(true)
                }

                DismissDirection.StartToEnd -> {
                    onImpSwipe(!task.isImp)
                }

                else -> {
                    // no-op
                }
            }
        }
    }
}

@Preview
@Composable
fun TaskItemGridPreview() {
    val task = Task(
        0,
        "Hello sfddjkssdkjsdjsdksdsjkdjksdsdjsjdskdksdjsjkdsjkdsjkdjksdsjkdsjkdsjkdjsdkdjksd",
        subTaskList = "Jellp"
    )
    TaskItem(task = task,isListViewEnable = false, onImpSwipe = {}, onItemClick = {}, onCompletedTask = {}) {
    }
}