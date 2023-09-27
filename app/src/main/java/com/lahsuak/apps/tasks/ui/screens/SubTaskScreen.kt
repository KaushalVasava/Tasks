package com.lahsuak.apps.tasks.ui.screens

import android.app.Activity
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.model.SortOrder
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.model.SubTaskEvent
import com.lahsuak.apps.tasks.ui.navigation.NavigationItem
import com.lahsuak.apps.tasks.ui.screens.components.ChipGroup
import com.lahsuak.apps.tasks.ui.screens.components.LinearProgressStatus
import com.lahsuak.apps.tasks.ui.screens.components.RoundedOutlinedTextField
import com.lahsuak.apps.tasks.ui.screens.components.SubTaskItem
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.preference.FilterPreferences
import com.lahsuak.apps.tasks.util.toSortForm
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubTaskScreen(
    taskId: Int,
    navController: NavController,
    subTaskViewModel: SubTaskViewModel,
    taskViewModel: TaskViewModel,
    fragmentManager: FragmentManager,
) {
    val prefManager = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
    val showVoiceTask =
        prefManager.getBoolean(AppConstants.SharedPreference.SHOW_VOICE_TASK_KEY, true)


    val taskState by taskViewModel.taskFlow.collectAsState()
    val subTaskEvents by subTaskViewModel.subTasksEvent.collectAsState(SubTaskEvent.Initial)

    if (taskState != null) {
        var task = taskState!!
        subTaskViewModel.taskId.value = taskId
        val subTasks by subTaskViewModel.subTasks.collectAsState(initial = emptyList())
        val preference by subTaskViewModel.preferencesFlow.collectAsState(
            initial = FilterPreferences(
                sortOrder = SortOrder.BY_NAME,
                hideCompleted = false,
                viewType = false
            )
        )
        val context = LocalContext.current
        var isListViewEnable by rememberSaveable {
            mutableStateOf(preference.viewType)
        }
        var searchQuery by rememberSaveable {
            mutableStateOf("")
        }
        val active = stringResource(id = R.string.active)
        val done = stringResource(id = R.string.done)
        val status = remember {
            mutableStateListOf(active, done)
        }
        var isSubTaskDone by rememberSaveable {
            mutableStateOf(false)
        }
        var subTaskId: Int? by rememberSaveable {
            mutableStateOf(null)
        }
        val bottomSheet = rememberModalBottomSheetState()
        var isBottomSheetOpened by rememberSaveable {
            mutableStateOf(false)
        }

        val speakLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val result1 = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val subTask = SubTask(
                    id = taskId,
                    sId = 0,
                    subTitle = result1!![0]
                )
                subTaskViewModel.insertSubTask(subTask)
            }
        }

        var startDate by rememberSaveable {
            mutableStateOf(task.startDate?.let { DateUtil.getDate(it) } ?: "")
        }
        var endDate by rememberSaveable {
            mutableStateOf(task.endDate?.let { DateUtil.getDate(it) } ?: "")
        }
        var reminder by rememberSaveable {
            mutableStateOf(task.reminder)
        }

        if (isBottomSheetOpened) {
            ModalBottomSheet(
                sheetState = bottomSheet,
                onDismissRequest = {
                    isBottomSheetOpened = false
                }
            ) {
                AddUpdateSubTaskScreen(
                    taskId = taskId,
                    subTaskId = subTaskId?.toString(),
                    isNewTask = isSubTaskDone,
                    subTaskViewModel = subTaskViewModel,
                    fragmentManager = fragmentManager
                ) {
                    isBottomSheetOpened = false
                }
            }
        }

        val sorts = listOf(
            stringResource(R.string.name),
            stringResource(R.string.date),
            stringResource(R.string.name_desc),
            stringResource(R.string.date_desc)
        )
        val sortTypes by remember {
            mutableStateOf(sorts)
        }
        var selectedSortIndex by rememberSaveable {
            mutableIntStateOf(
                sorts.indexOfFirst {
                    it.contains(preference.sortOrder.name.toSortForm())
                }
            )
        }

        var isSnackBarShow by rememberSaveable {
            mutableStateOf(false)
        }
        var openDialog by remember { mutableStateOf(false) }
        val undoMsg = stringResource(R.string.undo)
        val snackBarMsg = stringResource(R.string.task_deleted)
        val snackBarHostState = remember {
            SnackbarHostState()
        }
        // below line is to check if the
        // dialog box is open or not.
        if (isSnackBarShow) {
            when (val event = subTaskEvents) {
                is SubTaskEvent.ShowUndoDeleteTaskMessage -> {
                    LaunchedEffect(Unit) {
                        val snackBarResult = snackBarHostState.showSnackbar(
                            message = snackBarMsg,
                            actionLabel = undoMsg,
                            duration = SnackbarDuration.Short
                        )
                        when (snackBarResult) {
                            SnackbarResult.Dismissed -> {
                                Log.d("TAG", "TaskScreen: dismissed")
                            }

                            SnackbarResult.ActionPerformed -> {
                                subTaskViewModel.onUndoDeleteClick(event.subTask)
                            }
                        }
                        isSnackBarShow = false
                    }
                }

                SubTaskEvent.NavigateToAllCompletedScreen -> {
                    if (openDialog) {
                        // below line is use to
                        // display a alert dialog.
                        AlertDialog(
                            // on dialog dismiss we are setting
                            // our dialog value to false.
                            onDismissRequest = { openDialog = false },

                            // below line is use to display title of our dialog
                            // box and we are setting text color to white.
                            title = { Text(text = stringResource(id = R.string.confirm_deletion)) },

                            // below line is use to display
                            // description to our alert dialog.
                            text = { Text(stringResource(id = R.string.delete_completed_task)) },

                            // in below line we are displaying
                            // our confirm button.
                            confirmButton = {
                                // below line we are adding on click
                                // listener for our confirm button.
                                TextButton(
                                    onClick = {
                                        openDialog = false
                                        subTaskViewModel.deleteAllCompletedSubTasks(task.id)
                                    }
                                ) {
                                    // in this line we are adding
                                    // text for our confirm button.
                                    Text(stringResource(id = R.string.delete))
                                }
                            },
                            // in below line we are displaying
                            // our dismiss button.
                            dismissButton = {
                                // in below line we are displaying
                                // our text button
                                TextButton(
                                    // adding on click listener for this button
                                    onClick = {
                                        openDialog = false
                                    }
                                ) {
                                    // adding text to our button.
                                    Text(stringResource(id = R.string.cancel))
                                }
                            },
                        )
                    }
                }

                SubTaskEvent.Initial -> {}
            }
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            task.title,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            val progress = if (subTasks.isNotEmpty()) {
                                subTasks.filter { it.isDone }.size.toFloat() / subTasks.size.toFloat()
                            } else -1f
                            taskViewModel.update(
                                task.copy(
                                    progress = progress,
                                    subTaskList = AppUtil.getSubText(subTasks.map { it.subTitle })
                                )
                            )
                            navController.popBackStack()
                        }) {
                            Icon(
                                painterResource(id = R.drawable.ic_back),
                                stringResource(id = R.string.back)
                            )
                        }
                    },
                    actions = {
                        Row {
                            IconButton(onClick = {
                                navController.navigate(NavigationItem.Notification.route)
                            }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "notifications"
                                )
                            }
                            IconButton(onClick = {
                                navController.navigate(NavigationItem.Setting.route)
                            }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "settings"
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement =
                    if (isSubTaskDone)
                        Arrangement.Center
                    else
                        Arrangement.SpaceBetween,
                ) {
                    AnimatedVisibility(visible = showVoiceTask && !isSubTaskDone) {
                        FloatingActionButton(onClick = {
                            AppUtil.speakToAddTaskCompose(context, speakLauncher)
                        }) {
                            Icon(
                                painterResource(R.drawable.ic_mic),
                                stringResource(R.string.add_task)
                            )
                        }
                    }
                    AnimatedVisibility(visible = isSubTaskDone) {
                        FloatingActionButton(onClick = {
                            subTaskViewModel.onDeleteAllCompletedClick()
                            openDialog = true
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                stringResource(R.string.delete_task)
                            )
                        }
                    }
                    AnimatedVisibility(visible = !isSubTaskDone) {
                        FloatingActionButton(onClick = {
                            subTaskId = -1
                            isBottomSheetOpened = !isBottomSheetOpened
//                            navController.navigate("${NavigationItem.AddUpdateSubTask.route}?subTaskId=null/${task.name}/true")
                        }) {
                            Icon(
                                painterResource(R.drawable.ic_edit),
                                stringResource(R.string.add_task)
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            if (!isListViewEnable) {
                LazyColumn(
                    Modifier.padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    item {
                        SubtaskHeaderContent(
                            startDate,
                            onStartDateChange = {
                                startDate = it
                            },
                            endDate,
                            onEndDateChange = {
                                endDate = it
                            },
                            setStartDate = {
                                AppUtil.setDateTimeCompose(
                                    context,
                                    fragmentManager
                                ) { calendar, time ->
                                    startDate = time
                                    task = task.copy(
                                        startDate = calendar.timeInMillis
                                    )
                                }
                            },
                            setEndDate = {
                                AppUtil.setDateTimeCompose(
                                    context,
                                    fragmentManager
                                ) { calendar, time ->
                                    endDate = time
                                    task = task.copy(
                                        endDate = calendar.timeInMillis
                                    )
                                    taskViewModel.update(task)
                                }
                            },
                            shareTask = {
                                subTaskViewModel.shareTask(
                                    context,
                                    AppUtil.getSubText(subTasks.map { it.subTitle })
                                )
                            },
                            reminder = reminder,
                            onReminderChange = {
                                AppUtil.setDateTimeCompose(
                                    context,
                                    fragmentManager
                                ) { calendar, _ ->
                                    AppUtil.setReminderWorkRequest(
                                        context,
                                        task.title,
                                        task,
                                        calendar
                                    )
                                    task.reminder = calendar.timeInMillis
                                    reminder = calendar.timeInMillis
                                }
                            },
                            completedSubTask = subTasks.filter { it.isDone }.size,
                            totalSubTask = subTasks.size,
                            searchQuery = searchQuery,
                            color = Color(TaskApp.categoryTypes[task.color].color),
                            onQueryChange = {
                                searchQuery = it
                                subTaskViewModel.searchQuery.value = it
                            },
                            isListViewEnable = isListViewEnable,
                            onViewChange = {
                                if (subTasks.size > 1)
                                    isListViewEnable = it
                            },
                            onStatusChange = {
                                isSubTaskDone = it
                            },
                            status = status,
                            selectedStatusIndex = if (isSubTaskDone) 1 else 0,
                            sortTypes = sortTypes,
                            selectedSortIndex = selectedSortIndex,
                            onSortChange = {
                                selectedSortIndex = it
                                subTaskViewModel.onSortOrderSelected(
                                    SortOrder.getOrder(it),
                                    context
                                )
                            }
                        )
                    }
                    items(subTasks.filter { t -> isSubTaskDone == t.isDone }
                        .sortedByDescending { t -> t.isImportant }, key = {
                        it.id + Random.nextInt()
                    }) { subtask ->
                        Row(Modifier.animateItemPlacement()) {
                            SubTaskItem(
                                subtask,
                                color = Color(TaskApp.categoryTypes[task.color].color),
                                onImpSwipe = {
                                    subTaskViewModel.updateSubTask(subtask.copy(isImportant = it))
                                },
                                isListViewEnable = isListViewEnable,
                                onItemClick = {
                                    subTaskViewModel.setSubTask(subtask)
                                    isBottomSheetOpened = !isBottomSheetOpened
                                },
                                onCompletedTask = { isCompleted ->
                                    subTaskViewModel.updateSubTask(
                                        subtask.copy(
                                            isDone = isCompleted,
                                            dateTime = System.currentTimeMillis()
                                        )
                                    )
                                }
                            ) { isDone ->
                                if (isDone) {
                                    subTaskViewModel.onSubTaskSwiped(subtask)
                                    isSnackBarShow = false
                                } else {
                                    subTaskViewModel.setSubTask(subtask)
                                    isBottomSheetOpened = !isBottomSheetOpened
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    modifier = Modifier.padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    columns = StaggeredGridCells.Fixed(2),
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        SubtaskHeaderContent(
                            startDate,
                            onStartDateChange = {
                                startDate = it
                            },
                            endDate,
                            onEndDateChange = {
                                endDate = it
                            },
                            setStartDate = {
                                AppUtil.setDateTimeCompose(
                                    context,
                                    fragmentManager
                                ) { calendar, time ->
                                    startDate = time
                                    task = task.copy(
                                        startDate = calendar.timeInMillis
                                    )
                                }
                            },
                            setEndDate = {
                                AppUtil.setDateTimeCompose(
                                    context,
                                    fragmentManager
                                ) { calendar, time ->
                                    endDate = time
                                    task = task.copy(
                                        endDate = calendar.timeInMillis
                                    )
                                    taskViewModel.update(task)
                                }
                            },
                            shareTask = {
                                subTaskViewModel.shareTask(
                                    context,
                                    AppUtil.getSubText(subTasks.map { it.subTitle })
                                )
                            },
                            reminder = reminder,
                            onReminderChange = {
                                AppUtil.setDateTimeCompose(
                                    context,
                                    fragmentManager
                                ) { calendar, _ ->
                                    AppUtil.setReminderWorkRequest(
                                        context,
                                        task.title,
                                        task,
                                        calendar
                                    )
                                    task.reminder = calendar.timeInMillis
                                    reminder = calendar.timeInMillis
                                }
                            },
                            subTasks.filter { it.isDone }.size,
                            subTasks.size,
                            searchQuery = searchQuery,
                            color = Color(TaskApp.categoryTypes[task.color].color),
                            onQueryChange = {
                                searchQuery = it
                                subTaskViewModel.searchQuery.value = it
                            },
                            isListViewEnable = isListViewEnable,
                            onViewChange = {
                                if (subTasks.size > 1)
                                    isListViewEnable = it
                            },
                            onStatusChange = {
                                isSubTaskDone = it
                            },
                            status = status,
                            selectedStatusIndex = if (isSubTaskDone) 1 else 0,
                            sortTypes = sortTypes,
                            selectedSortIndex = selectedSortIndex,
                            onSortChange = {
                                selectedSortIndex = it
                                subTaskViewModel.onSortOrderSelected(
                                    SortOrder.getOrder(it),
                                    context
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    items(
                        subTasks.filter { t -> isSubTaskDone == t.isDone }
                            .sortedByDescending { t -> t.isImportant },
                        key = { t ->
                            t.id + Random.nextInt()
                        }
                    ) { subTask ->
                        Row(
                            Modifier
                                .animateItemPlacement()
                                .padding(4.dp)
                        ) {
                            SubTaskItem(
                                subTask,
                                color = Color(TaskApp.categoryTypes[task.color].color),
                                onImpSwipe = {
                                    subTaskViewModel.updateSubTask(subTask.copy(isImportant = it))
                                },
                                isListViewEnable = isListViewEnable,
                                onItemClick = {
                                    subTaskViewModel.setSubTask(subTask)
                                    navController.navigate(
                                        "${NavigationItem.AddUpdateSubTask.route}?subTaskId=${subTask.id}/${task.id}/false"
                                    )
                                },
                                onCompletedTask = { isCompleted ->
                                    subTaskViewModel.updateSubTask(
                                        subTask.copy(
                                            isDone = isCompleted,
                                            dateTime = System.currentTimeMillis()
                                        )
                                    )
                                }
                            ) { isDone ->
                                if (isDone) {
                                    subTaskViewModel.onSubTaskSwiped(subTask)
                                    isSnackBarShow = false
                                } else {
                                    subTaskViewModel.setSubTask(subTask)
                                    navController.navigate("${NavigationItem.AddUpdateSubTask.route}?subTaskId=${subTask.id}/${task.id}/false")
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    } else {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtaskHeaderContent(
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit,
    setStartDate: () -> Unit,
    setEndDate: () -> Unit,
    shareTask: () -> Unit,
    reminder: Long?,
    onReminderChange: () -> Unit,
    completedSubTask: Int,
    totalSubTask: Int,
    searchQuery: String,
    color: Color,
    onQueryChange: (String) -> Unit,
    isListViewEnable: Boolean,
    onViewChange: (Boolean) -> Unit,
    onStatusChange: (Boolean) -> Unit,
    status: List<String>,
    selectedStatusIndex: Int,
    sortTypes: List<String>,
    selectedSortIndex: Int,
    onSortChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var mSelectedText by remember {
        mutableStateOf(sortTypes[selectedSortIndex])
    }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }
    val width = LocalConfiguration.current.screenWidthDp.dp

    Column(modifier) {
        LinearProgressStatus(
            progress = completedSubTask.toFloat() / totalSubTask.toFloat(),
            text = stringResource(id = R.string.subtask_progress, completedSubTask, totalSubTask),
            color = color,
            width = width,
            height = 24.dp
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { q ->
                    onQueryChange(q)
                },
                onSearch = {},
                active = false,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    AnimatedVisibility(searchQuery.isNotEmpty()) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                onQueryChange("")
                            }
                        )
                    }
                },
                placeholder = {
                    Text(stringResource(id = R.string.search_subtask))
                },
                onActiveChange = {},
                modifier = Modifier.weight(0.9f)
            ) {}
            Spacer(modifier = Modifier.width(8.dp))
            AnimatedVisibility(searchQuery.isEmpty()) {
                IconButton(onClick = {
                    onViewChange(!isListViewEnable)
                }) {
                    Icon(
                        if (isListViewEnable)
                            painterResource(id = R.drawable.ic_list_view)
                        else
                            painterResource(id = R.drawable.ic_grid_view),
                        contentDescription = "layout view changer"
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(stringResource(id = R.string.sorting_option))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    Modifier
                        .fillMaxWidth(0.5f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(4.dp)
                        .onGloballyPositioned { coordinates ->
                            //This value is used to assign to the DropDown the same width
                            mTextFieldSize = coordinates.size.toSize()
                        }
                        .toggleable(value = isDropDownExpanded) {
                            isDropDownExpanded = !isDropDownExpanded
                        }
                        .semantics(mergeDescendants = true) {},
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        mSelectedText, fontWeight = FontWeight.SemiBold,
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
                    modifier = Modifier.width(with(LocalDensity.current)
                    { mTextFieldSize.width.toDp() })
                ) {
                    sortTypes.forEachIndexed { index, type ->
                        DropdownMenuItem(
                            text = {
                                Text(text = type)
                            },
                            onClick = {
                                onSortChange(index)
                                mSelectedText = type
                                isDropDownExpanded = false
                            }
                        )
                    }
                }
            }
            TextButton(onClick = { shareTask() }) {
                Icon(painterResource(R.drawable.ic_share), stringResource(R.string.share))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.share))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            RoundedOutlinedTextField(
                value = startDate,
                onValueChange = {
                    onStartDateChange(it)
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
                            setStartDate()
                        }
                    },
                textStyle = TextStyle(fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            RoundedOutlinedTextField(
                value = endDate,
                onValueChange = {
                    onEndDateChange(it)
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
                            setEndDate()
                        }
                    },
                textStyle = TextStyle(fontSize = 12.sp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                onReminderChange()
            }) {
                Icon(painterResource(R.drawable.ic_reminder_small), null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (reminder != null) DateUtil.getDate(reminder)
                    else stringResource(R.string.add_date_time)
                )
            }
            ChipGroup(
                items = status,
                selectedIndex = selectedStatusIndex
            ) { index ->
                onStatusChange(index != 0)
            }
        }
    }
}