package com.lahsuak.apps.tasks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.ui.screens.components.RoundedColorIcon
import com.lahsuak.apps.tasks.ui.screens.components.RoundedOutlinedTextField
import com.lahsuak.apps.tasks.ui.screens.components.SubTaskItem
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.toast

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddUpdateTaskScreen(
    navController: NavController,
    taskViewModel: TaskViewModel,
    isNewTask: Boolean,
    taskId: String?,
    fragmentManager: FragmentManager,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
        if (!isNewTask && taskId != null) {
            taskViewModel.getById(taskId.toInt())
        }
    }

    var task = if (isNewTask) {
        null
    } else {
        val tempTask by taskViewModel.taskFlow.collectAsState()
        tempTask
    }
    val context = LocalContext.current
    var title by rememberSaveable {
        mutableStateOf(task?.title ?: "")
    }

    var isImp by rememberSaveable {
        mutableStateOf(task?.isImp ?: false)
    }

    var startDate by rememberSaveable {
        mutableStateOf(task?.startDate?.let { DateUtil.getDate(it) } ?: "")
    }
    var endDate by rememberSaveable {
        mutableStateOf(task?.endDate?.let { DateUtil.getDate(it) } ?: "")
    }
    var reminder by rememberSaveable {
        mutableStateOf(task?.reminder)
    }
    var isDropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    val categories = TaskApp.categoryTypes
    var selectedCategory by remember {
        mutableIntStateOf(0)
    }
//    var selectedCategory by remember {
//        mutableStateOf(TaskApp.categoryTypes[task?.color ?: 0].name)
//    }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val subtasks = remember {
        mutableStateListOf("")
    }
    var subtaskText by remember {
        mutableStateOf("")
    }
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        RoundedOutlinedTextField(
            value = title,
            onValueChange = {
                title = it
            }, placeholder = {
                Text(stringResource(R.string.enter_title))
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .padding(horizontal = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            trailingIcon = {
                Icon(painterResource(R.drawable.ic_paste), stringResource(R.string.paste))
            }
        )
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isImp, onCheckedChange = {
                    isImp = it
                })
                Text(stringResource(id = R.string.important_task))
            }
            TextButton(onClick = { }) {
                Icon(painterResource(R.drawable.ic_copy), stringResource(R.string.copy_text))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.copy_text))
            }
            Column {
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
                    RoundedColorIcon(color = Color(categories[selectedCategory].color), size = 15.dp,
                        modifier = Modifier.padding(start = 8.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        categories[selectedCategory].name,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f),
                        textAlign = TextAlign.Center
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
                    TaskApp.categoryTypes.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Text(text = category.name)
                            },
                            onClick = {
                                selectedCategory = category.order
                                isDropDownExpanded = false
                            },
                            leadingIcon = {
                                RoundedColorIcon(color = Color(category.color))
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            RoundedOutlinedTextField(
                value = startDate,
                onValueChange = {
                    startDate = it
                },
                leadingIcon = {
                    Icon(
                        painterResource(id = R.drawable.ic_calendar_small),
                        contentDescription = null
                    )
                },
                placeholder = {
                    Text(stringResource(R.string.start_date))
                },
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            AppUtil.setDateTimeCompose(context, fragmentManager) { calendar, time ->
                                startDate = time
                                task = Task(
                                    id = 0,
                                    title = title,
                                    startDate = calendar.timeInMillis
                                )
                            }
                        }
                    },
                textStyle = TextStyle(fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            RoundedOutlinedTextField(
                value = endDate,
                onValueChange = {
                    endDate = it
                },
                leadingIcon = {
                    Icon(
                        painterResource(id = R.drawable.ic_calendar_small),
                        contentDescription = null
                    )
                },
                placeholder = {
                    Text(stringResource(R.string.end_date))
                },
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            AppUtil.setDateTimeCompose(context, fragmentManager) { calendar, time ->
                                endDate = time
                                if (title.isNotEmpty()) {
                                    val newTask = Task(
                                        id = 0,
                                        title = title,
                                        isImp = isImp,
                                        reminder = reminder,
                                        endDate = calendar.timeInMillis
                                    )
                                    task = newTask
                                    taskViewModel.update(newTask)
                                }
                            }
                        }
                    },
                textStyle = TextStyle(fontSize = 12.sp)
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextButton(onClick = {
                AppUtil.setDateTimeCompose(context, fragmentManager) { calendar, _ ->
                    if (task != null) {
                        AppUtil.setReminderWorkRequest(
                            context,
                            task!!.title,
                            task,
                            calendar
                        )
                        task!!.reminder = calendar.timeInMillis
                    } else {
                        val newTask = Task(
                            id = 0,
                            title = title,
                            isImp = isImp,
                            reminder = reminder
                        )
                        AppUtil.setReminderWorkRequest(
                            context,
                            newTask.title,
                            newTask,
                            calendar
                        )
                        newTask.reminder = calendar.timeInMillis
                    }
                    reminder = calendar.timeInMillis
                }
            }) {
                Icon(painterResource(R.drawable.ic_reminder_small), null)
                Spacer(Modifier.width(8.dp))
                Text(if (reminder != null) DateUtil.getDate(reminder!!) else stringResource(R.string.add_date_time))
            }
            Button(onClick = {
                if (title.isNotEmpty()) {
                    if (task != null) {
                        val updateTask = task!!.copy(
                            title = title,
                            isImp = isImp,
                            startDate = System.currentTimeMillis(),
                            endDate = task!!.endDate,
                            reminder = reminder,
                            color = TaskApp.categoryTypes.indexOfFirst {
                                it.order == selectedCategory
                            },
                            subTaskList = AppUtil.getSubText(subtasks)
                        )
                        taskViewModel.update(updateTask)
                        taskViewModel.resetTaskValue()
                    } else {
                        val newTask = Task(
                            id = 0,
                            title = title,
                            isImp = isImp,
                            startDate = System.currentTimeMillis(),
                            reminder = reminder,
                            color = TaskApp.categoryTypes.indexOfFirst {
                                it.order == selectedCategory
                            },
                            subTaskList = AppUtil.getSubText(subtasks)
                        )
                        taskViewModel.insert(newTask)
                        taskViewModel.resetTaskValue()
                    }
                    navController.popBackStack()
                } else {
                    context.toast {
                        context.getString(R.string.empty_task)
                    }
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_done),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.save))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(text = "Subtasks", modifier = Modifier.padding(horizontal = 8.dp))
        LazyColumn(contentPadding = PaddingValues(8.dp)) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = subtaskText,
                        onValueChange = {
                            subtaskText = it
                        },
                        placeholder = {
                            Text("Enter subtask")
                        }
                    )
                    FloatingActionButton(onClick = {
                        if (subtaskText.isNotEmpty()) {
                            subtasks.add(subtaskText)
                            subtaskText = ""
                        }
                    }) {
                        Icon(
                            painter = if (subtaskText.isEmpty())
                                painterResource(id = R.drawable.ic_edit)
                            else
                                painterResource(id = R.drawable.ic_done),
                            contentDescription = "Add subtask"
                        )
                    }
                }
            }
            items(subtasks.filter {
                it.isNotEmpty()
            }) {
                Spacer(modifier = Modifier.height(8.dp))
                SubTaskItem(
                    subTask = SubTask(
                        id = task?.id ?: 0, subTitle = it, isDone = false,
                        isImportant = false, sId = 0
                    ),
                    color = Color(TaskApp.categoryTypes[task?.color ?: 0].color),
                    isListViewEnable = false,
                    onImpSwipe = {},
                    onItemClick = {},
                    onCompletedTask = {},
                    onEditIconClick = {}
                )
            }
        }
    }
}