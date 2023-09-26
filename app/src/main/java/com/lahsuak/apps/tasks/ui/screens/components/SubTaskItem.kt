package com.lahsuak.apps.tasks.ui.screens.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.DateUtil
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubTaskItem(
    subTask: SubTask,
    color: Color,
    isListViewEnable: Boolean,
    onImpSwipe: (Boolean) -> Unit,
    onItemClick: () -> Unit,
    onCompletedTask: (Boolean) -> Unit,
    onEditIconClick: (Boolean) -> Unit,
) {
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
    val context = LocalContext.current
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
                        .background(color.copy(alpha = 0.30f))
                ) {
                    Box {
                        if (isImp) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_pin),
                                contentDescription = stringResource(id = R.string.important_task),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 8.dp, start = 4.dp)
                            )
                        }
                        val modifier = if (isListViewEnable) Modifier.fillMaxWidth() else Modifier
                        Row(modifier) {
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
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        subTask.subTitle,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (isChecked)
                                            TextDecoration.LineThrough
                                        else
                                            TextDecoration.None,
                                        modifier = Modifier
                                            .fillMaxWidth(
                                                if (isListViewEnable) 0.8f else 0.9f
                                            )
                                            .padding(top = 8.dp)
                                    )
                                    IconButton(onClick = {
                                        onEditIconClick(isChecked)
                                    }, Modifier.widthIn(min = 48.dp)) {
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
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 8.dp, bottom = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = if (isListViewEnable || subTask.reminder == null)
                                        Arrangement.SpaceBetween
                                    else
                                        Arrangement.SpaceEvenly
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(16.dp)
                                            )
                                            .background(color)
                                            .padding(4.dp)
                                    ) {
                                        Icon(
                                            painterResource(id = R.drawable.ic_calendar_small),
                                            contentDescription = null,
                                            tint = Color.Black
                                        )
                                        Spacer(
                                            Modifier
                                                .width(4.dp)
                                                .align(Alignment.Bottom)
                                        )
                                        Text(
                                            DateUtil.getDate(subTask.dateTime!!),
                                            fontSize = 10.sp,
                                            color = Color.Black
                                        )
                                    }
                                    Icon(
                                        painterResource(R.drawable.ic_copy),
                                        stringResource(id = R.string.copy_text),
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable {
                                                AppUtil.setClipboard(context, subTask.subTitle)
                                            }
                                            .padding(4.dp)
                                    )
                                    if (subTask.reminder == null) {
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
    SubTaskItem(
        subTask = task,
        isListViewEnable = true,
        onImpSwipe = {},
        onItemClick = {},
        onCompletedTask = {},
        color = Color.White
    ) {}
}