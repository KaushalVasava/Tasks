package com.lahsuak.apps.tasks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.ui.screens.components.RoundedOutlinedTextField
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.toast

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddUpdateSubTaskScreen(
    taskId: Int,
    subTaskId: String?,
    isNewTask: Boolean,
    subTaskViewModel: SubTaskViewModel,
    fragmentManager: FragmentManager,
    onDismiss: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }
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

    val context = LocalContext.current
    var title by rememberSaveable {
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
            .padding(vertical = 8.dp)
    ) {
        RoundedOutlinedTextField(
            value = title,
            onValueChange = {
                title = it
            }, placeholder = {
                Text(stringResource(R.string.enter_title))
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboard?.hide()
                    focusRequester.freeFocus()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
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
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            TextButton(onClick = {
                AppUtil.setDateTimeCompose(context, fragmentManager) { calendar, _ ->
                    if (subTask != null) {
                        AppUtil.setReminderWorkRequest(
                            context,
                            subTask.subTitle,
                            subTask,
                            calendar
                        )
                        subTask.reminder = calendar.timeInMillis
                    } else {
                        val newTask = SubTask(
                            id = taskId,
                            sId = 0,
                            subTitle = title,
                            isImportant = isImp,
                            reminder = reminder
                        )
                        AppUtil.setReminderWorkRequest(
                            context,
                            newTask.subTitle,
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
                Text(
                    reminder?.let {
                        DateUtil.getDate(it)
                    } ?: stringResource(R.string.add_date_time)
                )
            }
            Button(onClick = {
                if (title.isNotEmpty()) {
                    if (subTask != null) {
                        val newTask = subTask.copy(
                            subTitle = title,
                            isImportant = isImp,
                            dateTime = System.currentTimeMillis(),
                            reminder = reminder
                        )
                        subTaskViewModel.updateSubTask(newTask)
                        subTaskViewModel.resetSubTaskValue()
                    } else {
                        val newTask = SubTask(
                            id = taskId,
                            subTitle = title,
                            isImportant = isImp,
                            sId = 0,
                            dateTime = System.currentTimeMillis(),
                            reminder = reminder
                        )
                        subTaskViewModel.insertSubTask(newTask)
                        subTaskViewModel.resetSubTaskValue()
                    }
                    onDismiss()
                } else {
                    context.toast {
                        context.getString(R.string.empty_task)
                    }
                }
            }) {
                Icon(painterResource(R.drawable.ic_done), null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.save))
            }
        }
    }
}