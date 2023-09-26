package com.lahsuak.apps.tasks.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.ui.MainActivity
import com.lahsuak.apps.tasks.ui.theme.TaskAppTheme
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.DateUtil

@Composable
fun AddUpdateSubTaskScreen(
    taskId: Int,
    subTaskId: String?,
    isNewTask: Boolean,
    navController: NavController,
    subTaskViewModel: SubTaskViewModel,
    onDismiss: () -> Unit
) {
    if (!isNewTask && subTaskId != null) {
        LaunchedEffect(key1 = subTaskId) {
            subTaskViewModel.getBySubTaskId(subTaskId.toInt())
        }
    }
    val subTask = if (isNewTask) {
        null
    } else {
        val st by subTaskViewModel.subTaskFlow.collectAsState()
        st
    }
    val activity = LocalContext.current
    var text by rememberSaveable {
        mutableStateOf(
            subTask?.subTitle ?: ""
        )
    }

    var isImp by rememberSaveable {
        mutableStateOf(subTask?.isImportant ?: false)
    }

    var reminder: Long? by rememberSaveable {
        mutableStateOf(null)
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)) {
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
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isImp, onCheckedChange = {
                isImp = it
            })
            Text(stringResource(id = R.string.important_task))
            TextButton(onClick = { }) {
                Icon(painterResource(id = R.drawable.ic_copy), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.copy_text))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val mainActivity = MainActivity()
            TextButton(onClick = {
                AppUtil.setDateTime(mainActivity) { calendar, time ->
                    if (subTask != null) {
                        AppUtil.setReminderWorkRequest(
                            activity,
                            subTask.subTitle,
                            subTask,
                            calendar
                        )
                        subTask.reminder = calendar.timeInMillis
                    } else {
                        val newSubTask = SubTask(
                            id = taskId,
                            sId = 0,
                            subTitle = text,
                            isImportant = isImp,
                            reminder = reminder
                        )
                        AppUtil.setReminderWorkRequest(
                            activity,
                            newSubTask.subTitle,
                            newSubTask,
                            calendar
                        )
                        newSubTask.reminder = calendar.timeInMillis
                    }
                    reminder = calendar.timeInMillis
                }
            }) {
                Icon(
                    painterResource(id = R.drawable.ic_reminder_small),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    reminder?.let {
                        DateUtil.getDate(it)
                    } ?: "Add date/time"
                )
            }
            Button(onClick = {
                if (subTask != null) {
                    val newTask = subTask.copy(
                        subTitle = text,
                        isImportant = isImp,
                        dateTime = System.currentTimeMillis(),
                        reminder = reminder
                    )
                    subTaskViewModel.updateSubTask(newTask)
                    subTaskViewModel.resetSubTaskValue()
                } else {
                    val newTask = SubTask(
                        id = taskId,
                        subTitle = text,
                        isImportant = isImp,
                        sId = 0,
                        dateTime = System.currentTimeMillis(),
                        reminder = reminder
                    )
                    subTaskViewModel.insertSubTask(newTask)
                    subTaskViewModel.resetSubTaskValue()
                }
//                navController.popBackStack()
                onDismiss()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_done),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.save))
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewAddUpdateSubTaskScreen() {
    val subTaskViewModel: SubTaskViewModel = viewModel()

    TaskAppTheme {
        Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
            AddUpdateSubTaskScreen(
                navController = rememberNavController(),
                taskId = 0,
                subTaskId = null,
                isNewTask = false,
                subTaskViewModel = subTaskViewModel
            ){}
        }
    }
}