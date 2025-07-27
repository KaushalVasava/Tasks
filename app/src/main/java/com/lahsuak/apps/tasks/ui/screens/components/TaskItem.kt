package com.lahsuak.apps.tasks.ui.screens.components

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.model.Task
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
fun TaskItem(
    modifier: Modifier = Modifier,
    task: Task,
    settingPreferences: SettingPreferences,
    isListViewEnable: Boolean,
    onImpSwipe: (Boolean) -> Unit,
    onCancelReminder: () -> Unit,
    onCompletedTask: (Boolean) -> Unit,
    onEditIconClick: (Boolean) -> Unit,
) {
    val isLandScape =
        LocalConfiguration.current.screenHeightDp < LocalConfiguration.current.screenWidthDp
    val color = Color(TaskApp.categoryTypes[task.color].color)
    val showProgress by rememberSaveable {
        mutableStateOf(settingPreferences.showProgress)
    }
    val showReminder by rememberSaveable {
        mutableStateOf(settingPreferences.showReminder)
    }
    val showSubTask by rememberSaveable {
        mutableStateOf(settingPreferences.showSubTask)
    }
    val isSwipeGestureEnable by rememberSaveable {
        mutableStateOf(settingPreferences.swipeGestureEnable)
    }
    val showCopyIcon by rememberSaveable {
        mutableStateOf(settingPreferences.showCopyIcon)
    }
    val titleSize by rememberSaveable {
        mutableFloatStateOf(settingPreferences.fontSize.toFloat())
    }

    var isExpanded by rememberSaveable {
        mutableStateOf(true)
    }
    var isChecked by rememberSaveable {
        mutableStateOf(task.isDone)
    }
    var show by remember { mutableStateOf(true) }
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
                        task,
                        color,
                        isLandScape,
                        titleSize,
                        isListViewEnable,
                        showSubTask,
                        isChecked,
                        isExpanded,
                        showCopyIcon,
                        showReminder,
                        showProgress,
                        onExpandChange = {
                            isExpanded = it
                        },
                        onCheckedChange =
                        {
                            isChecked = it
                        },
                        onCompletedTask = {
                            onCompletedTask(it)
                        },
                        onEditIconClick = {
                            onEditIconClick(it)
                        },
                        onCancelReminder = { onCancelReminder() }
                    )
                }
            )
        }
    } else {
        SwipeItem(
            modifier,
            task,
            color,
            isLandScape,
            titleSize,
            isListViewEnable,
            showSubTask,
            isChecked,
            isExpanded,
            showCopyIcon,
            showReminder,
            showProgress,
            onExpandChange = {
                isExpanded = it
            },
            onCheckedChange =
            {
                isChecked = it
            },
            onCompletedTask = {
                onCompletedTask(it)
            },
            onEditIconClick = {
                onEditIconClick(it)
            },
            onCancelReminder = { onCancelReminder() }
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
                    onImpSwipe(!task.isImp)
                }

                else -> {
                    // no-op
                }
            }
        } else {
            dismissState.dismiss(SwipeToDismissBoxValue.Settled)
        }
    }
}

@Composable
fun SwipeItem(
    modifier: Modifier,
    task: Task,
    color: Color,
    isLandScape: Boolean,
    titleSize: Float,
    isListViewEnable: Boolean,
    showSubTask: Boolean,
    isChecked: Boolean,
    isExpanded: Boolean,
    showCopyIcon: Boolean,
    showReminder: Boolean,
    showProgress: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onCompletedTask: (Boolean) -> Unit,
    onEditIconClick: (Boolean) -> Unit,
    onCancelReminder: () -> Unit,
) {
    val hasLink by rememberSaveable {
        mutableStateOf(task.title.hasLink())
    }
    val hasPhoneNumber by rememberSaveable {
        mutableStateOf(task.title.hasPhoneNumber())
    }
    val context = LocalContext.current
    var url: String? by rememberSaveable {
        mutableStateOf(task.title)
    }
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
            .background(Color(TaskApp.categoryTypes[task.color].color).copy(alpha = 0.30f))
    ) {
        Box {
            if (task.isImp) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pin),
                    contentDescription = stringResource(id = R.string.important_task),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopStart)
                        .padding(top = 8.dp, start = 4.dp)
                )
            }
            val tempModifier =
                if (isListViewEnable) Modifier.fillMaxWidth() else Modifier
            Row(tempModifier) {
                CircleCheckbox(
                    checked = isChecked,
                    onCheckedChange = {
                        onCompletedTask(it)
                        onCheckedChange(it)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                )
                Column {
                    FlowRow(
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
                                task.title,
                                titleSize,
                                MaterialTheme.colorScheme.onSurface,
                                textDecoration = isChecked
                            )
                        }
                        Row {
                            if (showSubTask && task.subTaskList != null) {
                                IconButton(onClick = {
                                    // expand list
                                    onExpandChange(!isExpanded)
                                }) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_expand_more),
                                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                                        contentDescription = null
                                    )
                                }
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
                    }
                    if (showSubTask && task.subTaskList != null) {
                        AnimatedVisibility(visible = isExpanded) {
                            Text(
                                task.subTaskList!!, fontSize = 12.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = if (isExpanded)
                                    10 else
                                    1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp, bottom = 8.dp),
                        maxItemsInEachRow = if (isLandScape) 4 else 3,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .padding(2.dp)
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
                                DateUtil.getDateRange(task.startDate!!, task.endDate),
                                fontSize = 10.sp,
                                color = Color.Black
                            )
                        }
                        AnimatedVisibility(showProgress && task.progress != -1f) {
                            CircularProgressStatus(
                                progress = task.progress,
                                color = color,
                                trackColor = Color.LightGray,
                                size = if (isListViewEnable) 32.dp else 28.dp
                            )
                        }
                        AnimatedVisibility(visible = showCopyIcon) {
                            Icon(
                                painterResource(R.drawable.ic_copy),
                                stringResource(id = R.string.copy_text),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        AppUtil.setClipboard(context, task.title)
                                    }
                                    .padding(2.dp)
                            )
                        }
                        AnimatedVisibility(showReminder && task.reminder != null) {
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
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                                Spacer(
                                    Modifier
                                        .width(2.dp)
                                        .align(Alignment.Bottom)
                                )
                                val diff = DateUtil.getTimeDiff(task.reminder!!)

                                val (color, text) = if (diff < 0) {
                                    Color.Red to stringResource(id = R.string.overdue)
                                } else
                                    Color.Black to DateUtil.getDate(task.reminder!!)
                                Text(text, fontSize = 10.sp, color = color)
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
                    }
                }
            }
        }
    }
}

@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Color(0xFF039BE5)
        SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF1744)
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }
    val direction = dismissState.dismissDirection

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (direction == SwipeToDismissBoxValue.StartToEnd)
            Icon(
                // make sure add baseline_archive_24 resource to drawable folder
                painter = painterResource(R.drawable.ic_pin),
                contentDescription = stringResource(id = R.string.important_task)
            )
        Spacer(modifier = Modifier)
        if (direction == SwipeToDismissBoxValue.EndToStart)
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(id = R.string.delete)
            )
    }
}