package com.lahsuak.apps.tasks.ui.screens.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.util.DateUtil
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTaskItem(
    subTask: SubTask,
    onImpSwipe: (Boolean) -> Unit,
    onItemClick: () -> Unit,
    onCompletedTask: (Boolean) -> Unit,
    onEditIconClick: (Boolean) -> Unit,
) {
//    Log.d("TAG", "SubTaskItem: ${subTask.isDone} and ${subTask.subTitle}")

    var isChecked by rememberSaveable {
        mutableStateOf(subTask.isDone)
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
    val isImp by rememberSaveable {
        mutableStateOf(subTask.isImportant)
    }
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
                        .fillMaxWidth()
                        .clickable { onItemClick() }
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box {
                        if (isImp) {
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
                            Column(Modifier.padding(bottom = 8.dp)) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        subTask.subTitle,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
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
                                        IconButton(onClick = {
                                            onEditIconClick(isChecked)
                                        }) {
                                            Icon(
                                                painter = if (isChecked) {
                                                    painterResource(id = R.drawable.ic_delete)
                                                } else {
                                                    painterResource(id = R.drawable.ic_edit)
                                                },
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                }
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    LinearProgressIndicator(
                                        trackColor = MaterialTheme.colorScheme.surface,
                                        progress = 0.60f, modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .height(16.dp)
                                            .weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
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
                                        Text(DateUtil.getDate(subTask.dateTime!!), fontSize = 10.sp)
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
                    onImpSwipe(!subTask.isImportant)
                }

                else -> {
                    // no-op
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SubTaskItemPreview() {
    val task = SubTask(
        id = 1,
        subTitle = "Hello sfdjkdjksdsjkdsjkdsjkdjsdkdjksd",
        sId = 1
    )
    SubTaskItem(subTask = task, onImpSwipe = {}, onItemClick = {}, {}) {}
}