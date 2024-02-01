package com.lahsuak.apps.tasks.ui.screens.dialog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.ui.screens.components.CheckBoxWithText
import com.lahsuak.apps.tasks.ui.screens.components.RoundedOutlinedTextField
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.toast

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddUpdateSubTaskScreen(
    sheetState: ModalBottomSheetState? = null,
    taskId: Int,
    subTaskId: String?,
    isNewTask: Boolean,
    subTaskViewModel: SubTaskViewModel,
    sharedText: String?,
    onSaveClick: () -> Unit,
) {
    val context = LocalContext.current
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

    var title by rememberSaveable {
        mutableStateOf(
            subTask?.subTitle ?: (sharedText ?: "")
        )
    }

    var isImp by rememberSaveable {
        mutableStateOf(subTask?.isImportant ?: false)
    }

    var reminder: Long? by rememberSaveable {
        mutableStateOf(subTask?.reminder)
    }

    if (sheetState?.isVisible == false) {
        keyboard?.hide()
        title = ""
        isImp = false
        reminder = null
        onSaveClick()
    }
    BackHandler(true) {
        keyboard?.hide()
        title = ""
        isImp = false
        reminder = null
        onSaveClick()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        RoundedOutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = {
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
                .padding(horizontal = 8.dp),
            trailingIcon = {
                Icon(painterResource( R.drawable.ic_paste), stringResource(
                     R.string.paste),
                Modifier.clickable {
                    val pastedText = AppUtil.pasteText(context)
                    title = pastedText
                })
            }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            CheckBoxWithText(
                text = stringResource( R.string.important_task),
                value = isImp,
                onValueChange = { isImp = it },
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
            TextButton(onClick = { AppUtil.setClipboard(context, title) }) {
                Icon(painterResource( R.drawable.ic_copy), stringResource(R.string.copy_text))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource( R.string.copy_text), fontSize = 12.sp)
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
        ) {
            TextButton(onClick = {
                if (title.isNotEmpty()) {
                    AppUtil.setDateTime(context) { calendar, _ ->
                        if (subTask != null) {
                            AppUtil.setReminderWorkRequest(
                                context,
                                title.trim(),
                                subTask.copy(subTitle = title.trim()),
                                calendar
                            )
                            subTask.reminder = calendar.timeInMillis
                        } else {
                            val newTask = SubTask(
                                id = taskId,
                                sId = 0,
                                subTitle = title.trim(),
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
                            subTitle = title.trim(),
                            isImportant = isImp,
                            dateTime = System.currentTimeMillis(),
                            reminder = reminder
                        )
                        subTaskViewModel.updateSubTask(newTask)
                        subTaskViewModel.resetSubTaskValue()
                    } else {
                        val newTask = SubTask(
                            id = taskId,
                            subTitle = title.trim(),
                            isImportant = isImp,
                            sId = 0,
                            dateTime = System.currentTimeMillis(),
                            reminder = reminder
                        )
                        subTaskViewModel.insertSubTask(newTask)
                        subTaskViewModel.resetSubTaskValue()
                    }
                    onSaveClick()
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