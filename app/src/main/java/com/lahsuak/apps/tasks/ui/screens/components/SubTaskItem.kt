package com.lahsuak.apps.tasks.ui.screens.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissDirection
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.FixedThreshold
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.SwipeToDismiss
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.preference.SettingPreferences
import kotlinx.coroutines.delay

@OptIn(
    ExperimentalMaterialApi::class
)
@Composable
fun SubTaskItem(
    modifier: Modifier = Modifier,
    subTask: SubTask,
    settingPreferences: SettingPreferences,
    color: Color,
    isListViewEnable: Boolean,
    onImpSwipe: (Boolean) -> Unit,
    onCancelReminder: () -> Unit,
    onCompletedTask: (Boolean) -> Unit,
    onEditIconClick: (Boolean) -> Unit,
) {
    var isChecked by rememberSaveable {
        mutableStateOf(subTask.isDone)
    }
    val context = LocalContext.current

    val showCopyIcon by rememberSaveable {
        mutableStateOf(settingPreferences.showCopyIcon)
    }
    val titleSize by rememberSaveable {
        mutableFloatStateOf(settingPreferences.fontSize.toFloat())
    }
    val isSwipeGestureEnable by rememberSaveable {
        mutableStateOf(settingPreferences.swipeGestureEnable)
    }
    val showReminder by rememberSaveable {
        mutableStateOf(settingPreferences.showReminder)
    }

    var show by rememberSaveable { mutableStateOf(true) }
    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (isSwipeGestureEnable && (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd)) {
                show = false
                true
            } else
                false
        }
    )
    if (isSwipeGestureEnable) {
        AnimatedVisibility(
            show, exit = fadeOut(spring())
        ) {
            SwipeToDismiss(
                state = dismissState,
                background = {
                    DismissBackground(dismissState)
                },
                dismissContent = {
                    SwipeItem(
                        modifier,
                        subTask,
                        color,
                        titleSize,
                        isListViewEnable,
                        isChecked,
                        showCopyIcon,
                        showReminder,
                        onCheckedChange = {
                            isChecked = it
                        },
                        onCompletedTask = {
                            onCompletedTask(it)
                        },
                        onEditIconClick = {
                            onEditIconClick(it)
                        },
                        onCancelReminder = {
                            onCancelReminder()
                        }
                    )
                },
                dismissThresholds = {
                    FixedThreshold(120.dp)
                }
            )
        }
    } else {
        SwipeItem(
            modifier,
            subTask,
            color,
            titleSize,
            isListViewEnable,
            isChecked,
            showCopyIcon,
            showReminder,
            onCheckedChange = {
                isChecked = it
            },
            onCompletedTask = {
                onCompletedTask(it)
            },
            onEditIconClick = {
                onEditIconClick(it)
            },
            onCancelReminder = {
                onCancelReminder()
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwipeItem(
    modifier: Modifier,
    subTask: SubTask,
    color: Color,
    titleSize: Float,
    isListViewEnable: Boolean,
    isChecked: Boolean,
    showCopyIcon: Boolean,
    showReminder: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onCompletedTask: (Boolean) -> Unit,
    onEditIconClick: (Boolean) -> Unit,
    onCancelReminder: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.30f))
    ) {
        Box {
            if (subTask.isImportant) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pin),
                    contentDescription = stringResource(id = R.string.important_task),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(24.dp)
                        .padding(top = 8.dp, start = 4.dp)
                )
            }
            val tempModifier = if (isListViewEnable) Modifier.fillMaxWidth() else Modifier
            Row(tempModifier) {
                CircleCheckbox(
                    checked = isChecked,
                    onCheckedChange = {
                        onCompletedTask(it)
                        onCheckedChange(it)
                    },
                    activeColor = color,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                )
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LinkifyText(
                            subTask.subTitle,
                            titleSize,
                            MaterialTheme.colorScheme.onSurface,
                            textDecoration = isChecked,
                            modifier = Modifier
                                .fillMaxWidth(
                                    if (isListViewEnable) 0.8f else 0.9f
                                )
                                .padding(top = 8.dp),
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(8.dp)
                                )
                                .background(color)
                                .padding(2.dp)
                        ) {
                            Icon(
                                painterResource(id = R.drawable.ic_calendar_small),
                                stringResource(id = R.string.start_date),
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
                        if (showReminder && subTask.reminder != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .padding(2.dp)
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_reminder_small),
                                    null,
                                    tint = Color.Black
                                )
                                Spacer(
                                    Modifier
                                        .width(2.dp)
                                        .align(Alignment.Bottom)
                                )

                                val diff = DateUtil.getTimeDiff(subTask.reminder!!)

                                val (color1, text) = if (diff < 0) {
                                    Color.Red to stringResource(id = R.string.overdue)
                                } else
                                    Color.Black to DateUtil.getDate(subTask.reminder!!)
                                Text(text, fontSize = 10.sp, color = color1)
                                Icon(
                                    painterResource(
                                        R.drawable.ic_cancel
                                    ), stringResource(
                                        R.string.cancel_reminder
                                    ),
                                    tint = Color.Black,
                                    modifier = Modifier.clickable {
                                        onCancelReminder()
                                    }
                                )
                            }
                        }
                        AnimatedVisibility(visible = showCopyIcon) {
                            Icon(
                                painterResource(R.drawable.ic_copy),
                                stringResource(id = R.string.copy_text),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        AppUtil.setClipboard(context, subTask.subTitle)
                                    }
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}