package com.lahsuak.apps.tasks.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.ui.screens.components.SubTaskItem
import com.lahsuak.apps.tasks.ui.theme.TaskAppTheme
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.DateUtil

@Composable
fun AddUpdateTaskScreen(
    navController: NavController,
    taskViewModel: TaskViewModel,
    isNewTask: Boolean,
    taskId: String?,
) {
    if (!isNewTask && taskId != null) {
        LaunchedEffect(key1 = taskId) {
            taskViewModel.getById(taskId.toInt())
        }
    }
    val task = if (isNewTask) {
        null
    } else {
        val tempTask by taskViewModel.taskFlow.collectAsState()
        tempTask
    }
    val activity = LocalContext.current
    var text by rememberSaveable {
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
    var mSelectedText by remember {
        mutableStateOf(TaskApp.categoryTypes[task?.color ?: 0].name)
    }

    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }

    // Up Icon when expanded and down icon when collapsed
    val icon = if (isDropDownExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    val subtasks = remember {
        mutableStateListOf<String>("")
    }
    var subtaskText by remember {
        mutableStateOf("")
    }
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
            }, placeholder = {
                Text("Enter title")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            trailingIcon = {
                Icon(painter = painterResource(id = R.drawable.ic_paste), contentDescription = null)
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
                Icon(painterResource(id = R.drawable.ic_copy), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.copy_text))
            }
            Column {
                OutlinedTextField(
                    value = mSelectedText,
                    onValueChange = { mSelectedText = it },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .onGloballyPositioned { coordinates ->
                            // This value is used to assign to
                            // the DropDown the same width
                            mTextFieldSize = coordinates.size.toSize()
                        },
                    label = { Text("Category") },
                    trailingIcon = {
                        Icon(icon, "contentDescription",
                            Modifier.clickable { isDropDownExpanded = !isDropDownExpanded })
                    }
                )
                DropdownMenu(
                    expanded = isDropDownExpanded,
                    onDismissRequest = { isDropDownExpanded = false },
                    modifier = Modifier
                        .width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
                ) {
                    TaskApp.categoryTypes.forEach { label ->
                        DropdownMenuItem(
                            text = {
                                Text(text = label.name)
                            },
                            onClick = {
                                mSelectedText = label.name
                                isDropDownExpanded = false
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
                .padding(horizontal = 8.dp)
                .semantics(mergeDescendants = true) { },
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            OutlinedTextField(
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
                    Text("Start date")
                },
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                textStyle = TextStyle(fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
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
                    Text("End date")
                },
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                textStyle = TextStyle(fontSize = 12.sp)
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val context = LocalContext.current
            TextButton(onClick = {
                AppUtil.setDateTimeCompose(context) { calendar, _ ->
                    if (task != null) {
                        AppUtil.setReminderWorkRequest(
                            activity,
                            task.title,
                            task,
                            calendar
                        )

                        task.reminder = calendar.timeInMillis
                    }
                    else {
                        val newTask = Task(
                            id = 0,
                            title = text,
                            isImp = isImp,
                            reminder = reminder
                        )
                        AppUtil.setReminderWorkRequest(
                            activity,
                            newTask.title,
                            newTask,
                            calendar
                        )
                        newTask.reminder = calendar.timeInMillis
                    }
                    reminder = calendar.timeInMillis
                }
            }) {
                Icon(
                    painterResource(id = R.drawable.ic_reminder_small),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (reminder != null) DateUtil.getDate(reminder!!) else "Add date/time")
            }
            Button(onClick = {
                if (task != null) {
                    val updateTask = task.copy(
                        title = text,
                        isImp = isImp,
                        startDate = System.currentTimeMillis(),
                        endDate = task.endDate,
                        reminder = reminder,
                        color = TaskApp.categoryTypes.indexOfFirst {
                            it.name == mSelectedText
                        },
                        subTaskList = AppUtil.getSubText(subtasks)
                    )
                    taskViewModel.update(updateTask)
                    taskViewModel.resetTaskValue()
                } else {
                    val newTask = Task(
                        id = 0,
                        title = text,
                        isImp = isImp,
                        startDate = System.currentTimeMillis(),
                        reminder = reminder,
                        color = TaskApp.categoryTypes.indexOfFirst {
                            it.name == mSelectedText
                        },
                        subTaskList = AppUtil.getSubText(subtasks)
                    )
                    taskViewModel.insert(newTask)
                    taskViewModel.resetTaskValue()
                }
                navController.popBackStack()
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
                        } else {

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

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewAddUpdateTaskScreen() {
    val taskViewModel: TaskViewModel = viewModel()
    TaskAppTheme {
        Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
            AddUpdateTaskScreen(
                navController = rememberNavController(),
                taskViewModel,
                false,
                null
            )
        }
    }
}