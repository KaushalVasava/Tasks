package com.lahsuak.apps.tasks.ui.screens.dialog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.ui.screens.components.CheckBoxWithText
import com.lahsuak.apps.tasks.ui.screens.components.RoundedColorIcon
import com.lahsuak.apps.tasks.ui.screens.components.RoundedOutlinedTextField
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.rememberWindowSize
import com.lahsuak.apps.tasks.util.toast

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddUpdateTaskScreen(
    sheetState: ModalBottomSheetState? = null,
    taskViewModel: TaskViewModel,
    isNewTask: Boolean,
    taskId: String?,
    sharedText: String?,
    onBottomSheetClick: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val windowSize = rememberWindowSize()
    val isLandScape = windowSize.width > windowSize.height
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
        mutableStateOf(task?.title ?: (sharedText ?: ""))
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
        mutableIntStateOf(task?.color ?: 0)
    }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    if (sheetState?.isVisible == false) {
        keyboard?.hide()
        title = ""
        isImp = false
        startDate = ""
        endDate = ""
        reminder = null
        isDropDownExpanded = false
        selectedCategory = 0
        onBottomSheetClick()
    }
    BackHandler(true) {
        keyboard?.hide()
        title = ""
        isImp = false
        startDate = ""
        endDate = ""
        reminder = null
        isDropDownExpanded = false
        selectedCategory = 0
        onBottomSheetClick()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .systemBarsPadding()
    ) {
        Spacer(Modifier.height(8.dp))

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
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .padding(horizontal = 8.dp),
            trailingIcon = {
                Icon(painterResource(R.drawable.ic_paste), stringResource(R.string.paste),
                    Modifier.clickable {
                        val pastedText = AppUtil.pasteText(context)
                        title = pastedText
                    }
                )
            }
        )
        Row(
            Modifier.clip(RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CheckBoxWithText(
                text = stringResource(R.string.important_task),
                value = isImp,
                onValueChange = { isImp = it },
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
            TextButton(onClick = {
                AppUtil.setClipboard(context, title)
            }) {
                Icon(painterResource(R.drawable.ic_copy), stringResource(R.string.copy_text))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.copy_text), fontSize = 12.sp)
            }
            if (isLandScape) {
                Column(verticalArrangement = Arrangement.Center) {
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
                                textFieldSize = coordinates.size.toSize()
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RoundedColorIcon(
                            color = Color(categories[selectedCategory].color), size = 12.dp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            categories[selectedCategory].name,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Icon(
                            if (isDropDownExpanded)
                                Icons.Filled.KeyboardArrowUp
                            else
                                Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            Modifier.padding(end = 4.dp)
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
                                    Text(text = category.name, fontSize = 14.sp)
                                },
                                onClick = {
                                    selectedCategory = category.order
                                    isDropDownExpanded = false
                                },
                                leadingIcon = {
                                    RoundedColorIcon(color = Color(category.color), size = 14.dp)
                                }
                            )
                        }
                    }
                }
            }
        }
        if (!isLandScape) {
            Column(verticalArrangement = Arrangement.Center) {
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
                            textFieldSize = coordinates.size.toSize()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RoundedColorIcon(
                        color = Color(categories[selectedCategory].color), size = 12.dp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        categories[selectedCategory].name,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Icon(
                        if (isDropDownExpanded)
                            Icons.Filled.KeyboardArrowUp
                        else
                            Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        Modifier.padding(end = 4.dp)
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
                                Text(text = category.name, fontSize = 14.sp)
                            },
                            onClick = {
                                selectedCategory = category.order
                                isDropDownExpanded = false
                            },
                            leadingIcon = {
                                RoundedColorIcon(color = Color(category.color), size = 14.dp)
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
                        painterResource(R.drawable.ic_calendar_small),
                        null
                    )
                },
                placeholder = {
                    Text(stringResource(R.string.start_date), fontSize = 12.sp)
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            AppUtil.setDateTime(
                                context
                            ) { calendar, time ->
                                startDate = time
                                task = Task(
                                    id = 0,
                                    title = title.trim(),
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
                onValueChange = { endDate = it },
                leadingIcon = {
                    Icon(painterResource(R.drawable.ic_calendar_small), null)
                },
                placeholder = {
                    Text(stringResource(R.string.end_date), fontSize = 12.sp)
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            AppUtil.setDateTime(
                                context,
                            ) { calendar, time ->
                                endDate = time
                                if (title.isNotEmpty()) {
                                    val newTask = Task(
                                        id = 0,
                                        title = title.trim(),
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp, top = 8.dp, bottom = 8.dp)
        ) {
            TextButton(onClick = {
                if (title.isNotEmpty()) {
                    AppUtil.setDateTime(context) { calendar, _ ->
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
                                title = title.trim(),
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
                            title = title.trim(),
                            isImp = isImp,
                            startDate = task!!.startDate ?: System.currentTimeMillis(),
                            endDate = task!!.endDate,
                            reminder = reminder,
                            color = TaskApp.categoryTypes.indexOfFirst {
                                it.order == selectedCategory
                            }
                        )
                        taskViewModel.update(updateTask)
                        taskViewModel.resetTaskValue()
                    } else {
                        val newTask = Task(
                            id = 0,
                            title = title.trim(),
                            isImp = isImp,
                            startDate = System.currentTimeMillis(),
                            reminder = reminder,
                            color = TaskApp.categoryTypes.indexOfFirst {
                                it.order == selectedCategory
                            }
                        )
                        taskViewModel.insert(newTask)
                        taskViewModel.resetTaskValue()
                    }
                    onBottomSheetClick()
                } else {
                    context.toast {
                        context.getString(R.string.empty_task)
                    }
                }
            }) {
                Icon(
                    painterResource(id = R.drawable.ic_done),
                    null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.save))
            }
        }
    }
}