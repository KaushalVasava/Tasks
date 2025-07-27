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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.URLEmbeddedTask
import com.lahsuak.apps.tasks.util.hasLink
import com.lahsuak.apps.tasks.util.hasPhoneNumber
import com.lahsuak.apps.tasks.util.preference.SettingPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (isSwipeGestureEnable && (it == SwipeToDismissBoxValue.StartToEnd || it == SwipeToDismissBoxValue.EndToStart)) {
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
            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    DismissBackground(dismissState)
                },
                content = {
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
//                dismissThresholds = {
//                    FixedThreshold(120.dp)
//                }
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
                SwipeToDismissBoxValue.EndToStart -> {
                    onEditIconClick(true)
                }
                SwipeToDismissBoxValue.StartToEnd -> {
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
    val hasLink by rememberSaveable {
        mutableStateOf(subTask.subTitle.hasLink())
    }
    val hasPhoneNumber by rememberSaveable {
        mutableStateOf(subTask.subTitle.hasPhoneNumber())
    }

    var url: String? by rememberSaveable {
        mutableStateOf(subTask.subTitle)
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        if (hasLink && url.isNullOrEmpty().not()) {
            coroutineScope.launch (Dispatchers.IO){
                url = URLEmbeddedTask().getResult(url!!).thumbnailURL
            }
        }
    }
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            AnimatedVisibility(visible = hasLink) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White)
                                        .size(24.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            AnimatedVisibility(visible = !hasLink && hasPhoneNumber) {
                                AsyncImage(
                                    model = R.drawable.ic_phone,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White)
                                        .size(24.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            LinkifyText(
                                subTask.subTitle,
                                titleSize,
                                MaterialTheme.colorScheme.onSurface,
                                textDecoration = isChecked,
                                modifier = Modifier.fillMaxWidth(if (isListViewEnable) 0.8f else 0.9f)
                            )
                        }
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