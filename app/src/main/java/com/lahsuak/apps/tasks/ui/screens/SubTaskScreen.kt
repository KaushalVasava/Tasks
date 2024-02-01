package com.lahsuak.apps.tasks.ui.screens

import android.app.Activity
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ModalBottomSheetLayout
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.model.Notification
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.model.SortOrder
import com.lahsuak.apps.tasks.model.SubTaskEvent
import com.lahsuak.apps.tasks.ui.MainActivity
import com.lahsuak.apps.tasks.ui.navigation.NavigationItem
import com.lahsuak.apps.tasks.ui.screens.components.ChipGroup
import com.lahsuak.apps.tasks.ui.screens.components.LinearProgressStatus
import com.lahsuak.apps.tasks.ui.screens.components.RoundedOutlinedTextField
import com.lahsuak.apps.tasks.ui.screens.components.SubTaskItem
import com.lahsuak.apps.tasks.ui.screens.dialog.AddUpdateSubTaskScreen
import com.lahsuak.apps.tasks.ui.viewmodel.NotificationViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.BackPressHandler
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.WindowSize
import com.lahsuak.apps.tasks.util.WindowType
import com.lahsuak.apps.tasks.util.preference.FilterPreferences
import com.lahsuak.apps.tasks.util.preference.SettingPreferences
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun SubTaskScreen(
    taskId: Int,
    navController: NavController,
    subTaskViewModel: SubTaskViewModel,
    taskViewModel: TaskViewModel,
    notificationViewModel: NotificationViewModel,
    settingPreferences: SettingPreferences,
    windowSize: WindowSize,
    hasNotification: Boolean,
) {
    val showVoiceTask = settingPreferences.showVoiceIcon

    var sharedText by rememberSaveable {
        mutableStateOf(MainActivity.shareTxt)
    }

    val taskState by taskViewModel.taskFlow.collectAsState()
    val subTaskEvents by subTaskViewModel.subTasksEvent.collectAsState(SubTaskEvent.Initial)
    var subTaskId: Int? by rememberSaveable {
        mutableStateOf(null)
    }
    val lazyGridListState = rememberLazyStaggeredGridState()

    val isFabExtended by remember {
        derivedStateOf { lazyGridListState.firstVisibleItemIndex != 0 }
    }
    var isNewTask by rememberSaveable {
        mutableStateOf(true)
    }
    var isBottomSheetOpened by rememberSaveable {
        mutableStateOf(false)
    }
    val sheetState = androidx.compose.material.rememberModalBottomSheetState(
        skipHalfExpanded = true,
        initialValue = ModalBottomSheetValue.Hidden
    )
    val scope = rememberCoroutineScope()

    if (taskState != null) {
        var task = taskState!!

        subTaskViewModel.taskId.value = taskId
        val subTasks by subTaskViewModel.subTasks.collectAsState(initial = emptyList())
        val preference by subTaskViewModel.preferencesFlow.collectAsState(
            initial = FilterPreferences(
                sortOrder = SortOrder.BY_NAME,
                viewType = false
            )
        )

        var actionMode by rememberSaveable {
            mutableStateOf(false)
        }

        val selectedItems = remember {
            mutableStateListOf<SubTask>()
        }

        val resetSelectionMode = {
            actionMode = false
            selectedItems.clear()
        }

        BackHandler(
            enabled = actionMode,
        ) {
            resetSelectionMode()
        }

        LaunchedEffect(
            key1 = actionMode,
            key2 = selectedItems.size,
        ) {
            if (actionMode && selectedItems.isEmpty()) {
                actionMode = false
            }
        }

        BackPressHandler {
            val progress = if (subTasks.isNotEmpty()) {
                subTasks.filter { it.isDone }.size.toFloat() / subTasks.size.toFloat()
            } else -1f
            taskViewModel.update(
                task.copy(
                    progress = progress,
                    startDate = task.startDate ?: System.currentTimeMillis(),
                    subTaskList = AppUtil.getSubTasks(subTasks.map { it.subTitle })
                )
            )
            navController.popBackStack()
        }

        val context = LocalContext.current
        var isListViewEnable by rememberSaveable {
            mutableStateOf(preference.viewType)
        }
        var searchQuery by rememberSaveable {
            mutableStateOf("")
        }
        val active = stringResource(R.string.active)
        val done = stringResource(R.string.done)
        val status = remember {
            mutableStateListOf(active, done)
        }
        var isSubTaskDone by rememberSaveable {
            mutableStateOf(false)
        }

        val speakLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val result1 = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val subTask = SubTask(
                    id = task.id,
                    sId = 0,
                    subTitle = result1!![0],
                    dateTime = System.currentTimeMillis()
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
        val color by remember {
            mutableStateOf(Color(TaskApp.categoryTypes[task.color].color))
        }
        var isNotified by rememberSaveable {
            mutableStateOf(hasNotification)
        }
        LaunchedEffect(key1 = Unit) {
            if (isNotified) {
                notificationViewModel.insert(
                    Notification(0, taskId, task.title, System.currentTimeMillis())
                )
                isNotified = false
            }
        }

        val sorts = listOf(
            stringResource(R.string.name),
            stringResource(R.string.name_desc),
            stringResource(R.string.date),
            stringResource(R.string.date_desc)
        )
        val sortTypes by remember {
            mutableStateOf(sorts)
        }
        val index = sortTypes.indexOfFirst {
            it == preference.sortOrder.type
        }
        var selectedSortIndex by rememberSaveable {
            mutableIntStateOf(if (index < 0) 0 else index)
        }
        var selectedSort by rememberSaveable {
            mutableStateOf(sortTypes[selectedSortIndex])
        }

        var isSnackBarShow by rememberSaveable {
            mutableStateOf(false)
        }
        var openDeleteDialog by remember { mutableStateOf(false) }
        val undoMsg = stringResource(R.string.undo)
        val snackBarMsg = stringResource(R.string.task_deleted)
        val snackBarHostState = remember {
            SnackbarHostState()
        }

        when (val event = subTaskEvents) {
            is SubTaskEvent.ShowUndoDeleteTaskMessage -> {
                if (isSnackBarShow) {
                    LaunchedEffect(Unit) {
                        val snackBarResult = snackBarHostState.showSnackbar(
                            message = snackBarMsg,
                            actionLabel = undoMsg,
                            duration = SnackbarDuration.Short
                        )
                        when (snackBarResult) {
                            SnackbarResult.Dismissed -> {
                            }

                            SnackbarResult.ActionPerformed -> {
                                subTaskViewModel.onUndoDeleteClick(event.subTask)
                            }
                        }
                        isSnackBarShow = false
                    }
                }
            }

            SubTaskEvent.NavigateToAllCompletedScreen -> {
                if (openDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { openDeleteDialog = false },
                        title = { Text(text = stringResource(R.string.confirm_deletion)) },
                        text = { Text(stringResource(R.string.delete_completed_task)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    openDeleteDialog = false
                                    subTaskViewModel.deleteAllCompletedSubTasks(task.id)
                                }
                            ) {
                                Text(stringResource(R.string.delete))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    openDeleteDialog = false
                                }
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                        },
                    )
                }
            }

            SubTaskEvent.Initial -> {}
        }

        ModalBottomSheetLayout(
            sheetBackgroundColor = MaterialTheme.colorScheme.surface,
            sheetState = sheetState,
            sheetContent = {
                if (isBottomSheetOpened) {
                    AddUpdateSubTaskScreen(
                        sheetState = sheetState,
                        taskId = taskId,
                        subTaskId = subTaskId?.toString(),
                        isNewTask = isNewTask,
                        subTaskViewModel = subTaskViewModel,
                        sharedText = sharedText
                    ) {
                        scope.launch {
                            sheetState.hide()
                            isBottomSheetOpened = false
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    if (actionMode) {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Black,
                                navigationIconContentColor = Color.White,
                                titleContentColor = Color.White,
                                actionIconContentColor = Color.White
                            ),
                            title = {
                                Text(
                                    stringResource(
                                        R.string.task_selected,
                                        selectedItems.size,
                                        subTasks.size
                                    ),
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    resetSelectionMode()
                                }) {
                                    Icon(
                                        painterResource(R.drawable.ic_back),
                                        stringResource(R.string.back)
                                    )
                                }
                            },
                            actions = {
                                Row {
                                    IconButton(onClick = {
                                        if (selectedItems.size == subTasks.size) {
                                            selectedItems.clear()
                                        } else {
                                            selectedItems.clear()
                                            selectedItems.addAll(subTasks)
                                        }
                                    }) {
                                        Icon(
                                            painterResource(
                                                R.drawable.ic_select_all
                                            ),
                                            stringResource(R.string.select_all),
                                            tint = if (selectedItems.size == subTasks.size) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                Color.White
                                            }
                                        )
                                    }
                                    IconButton(onClick = {
                                        // delete selected items
                                        selectedItems.map {
                                            subTaskViewModel.deleteSubTask(it)
                                        }
                                        resetSelectionMode()
                                    }) {
                                        Icon(
                                            painterResource(R.drawable.ic_delete),
                                            stringResource(R.string.delete_task)
                                        )
                                    }
                                }
                            }
                        )
                    } else {
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
                                            startDate = task.startDate
                                                ?: System.currentTimeMillis(),
                                            subTaskList = AppUtil.getSubTasks(subTasks.map { it.subTitle })
                                        )
                                    )
                                    navController.popBackStack()
                                }) {
                                    Icon(
                                        painterResource(R.drawable.ic_back),
                                        stringResource(R.string.back)
                                    )
                                }
                            },
                            actions = {
                                Row {
                                    IconButton(onClick = {
                                        navController.navigate(NavigationItem.Notification.route)
                                    }) {
                                        Icon(
                                            painterResource(R.drawable.ic_reminder),
                                            stringResource(R.string.notifications)
                                        )
                                    }
                                    IconButton(onClick = {
                                        navController.navigate(NavigationItem.Setting.route)
                                    }) {
                                        Icon(
                                            painterResource(R.drawable.ic_settings),
                                            stringResource(R.string.settings)
                                        )
                                    }
                                }
                            }
                        )
                    }
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
                        AnimatedVisibility(visible = !actionMode && showVoiceTask && !isSubTaskDone) {
                            FloatingActionButton(
                                containerColor = color,
                                onClick = {
                                    AppUtil.speakToAddTask(context, speakLauncher)
                                }) {
                                Icon(
                                    painterResource(R.drawable.ic_mic),
                                    stringResource(R.string.add_task),
                                    tint = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                        AnimatedVisibility(visible = isSubTaskDone) {
                            FloatingActionButton(
                                containerColor = MaterialTheme.colorScheme.error,
                                onClick = {
                                    subTaskViewModel.onDeleteAllCompletedClick()
                                    openDeleteDialog = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    stringResource(R.string.delete_task)
                                )
                            }
                        }
                        AnimatedVisibility(visible = !actionMode && !isSubTaskDone) {
                            if (isFabExtended) {
                                ExtendedFloatingActionButton(
                                    containerColor = color,
                                    onClick = {
                                        subTaskId = null
                                        isNewTask = true
                                        sharedText = null
                                        scope.launch {
                                            isBottomSheetOpened = true
                                            sheetState.show()
                                        }
                                    }, text = {
                                        Text(
                                            stringResource(R.string.add_task),
                                            color = MaterialTheme.colorScheme.surface
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            painterResource(R.drawable.ic_create),
                                            stringResource(R.string.add_task),
                                            tint = MaterialTheme.colorScheme.surface
                                        )
                                    }
                                )
                            } else {
                                FloatingActionButton(
                                    containerColor = color,
                                    onClick = {
                                        subTaskId = null
                                        isNewTask = true
                                        sharedText = null
                                        scope.launch {
                                            isBottomSheetOpened = true
                                            sheetState.show()
                                        }
                                    }) {
                                    Icon(
                                        painterResource(R.drawable.ic_create),
                                        stringResource(R.string.add_task),
                                        tint = MaterialTheme.colorScheme.surface
                                    )
                                }
                            }
                        }
                    }
                },
                snackbarHost = {
                    androidx.compose.material3.SnackbarHost(snackBarHostState)
                }
            ) { paddingValues ->
                if (subTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.clickable {
                                subTaskId = null
                                isNewTask = true
                                scope.launch {
                                    isBottomSheetOpened = true
                                    sheetState.show()
                                }
                            }
                        ) {
                            Image(
                                painterResource(R.drawable.ic_add_task),
                                contentDescription = stringResource(
                                    R.string.add_task
                                ),
                                colorFilter = ColorFilter.tint(color)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.create_new_sub_task),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                LazyVerticalStaggeredGrid(
                    state = lazyGridListState,
                    modifier = Modifier.padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    columns = StaggeredGridCells.Fixed(
                        if (isListViewEnable) {
                            when (windowSize.width) {
                                WindowType.Expanded -> 4
                                WindowType.Medium -> 3
                                else -> 2
                            }
                        } else {
                            when (windowSize.width) {
                                WindowType.Expanded -> 3
                                WindowType.Medium -> 2
                                else -> 1
                            }
                        }
                    ),
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        AnimatedVisibility(!actionMode) {
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
                                    AppUtil.setDateTime(context) { calendar, time ->
                                        startDate = time
                                        task = task.copy(
                                            startDate = calendar.timeInMillis
                                        )
                                    }
                                },
                                setEndDate = {
                                    AppUtil.setDateTime(context) { calendar, time ->
                                        endDate = time
                                        task = task.copy(endDate = calendar.timeInMillis)
                                        taskViewModel.update(task)
                                    }
                                },
                                shareTask = {
                                    subTaskViewModel.shareTask(
                                        context,
                                        AppUtil.getSubTasks(subTasks.map { it.subTitle })
                                    )
                                },
                                onProgressBarClick = {
                                    navController.navigate(NavigationItem.Overview.route)
                                },
                                reminder = reminder,
                                onReminderChange = {
                                    AppUtil.setDateTime(context) { calendar, _ ->
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
                                onReminderCancel = {
                                    taskViewModel.cancelReminderCompose(
                                        context,
                                        task
                                    )
                                    reminder = null
                                },
                                subTasks.filter { it.isDone }.size,
                                subTasks.size,
                                searchQuery = searchQuery,
                                color = color,
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
                                selectedSort = selectedSort,
                                onSortChange = {
                                    selectedSortIndex = it
                                    selectedSort = sortTypes[it]
                                    subTaskViewModel.onSortOrderSelected(
                                        SortOrder.getOrder(it),
                                        context
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                windowSize = windowSize
                            )
                        }
                    }
                    items(
                        subTasks.filter { t -> isSubTaskDone == t.isDone }
                            .sortedByDescending { t -> t.isImportant },
                        key = { t ->
                            t.id + Random.nextInt()
                        }
                    ) { subTask ->
                        val isSelected =
                            selectedItems.contains(subTask)
                        Row(
                            Modifier
                                .animateItemPlacement()
                                .padding(4.dp)
                        ) {
                            SubTaskItem(
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            if (actionMode) {
                                                if (isSelected)
                                                    selectedItems.remove(subTask)
                                                else
                                                    selectedItems.add(subTask)
                                            } else {
                                                subTaskViewModel.setSubTask(subTask)
                                                isNewTask = false
                                                subTaskId = subTask.sId
                                                scope.launch {
                                                    isBottomSheetOpened = true
                                                    sheetState.show()
                                                }
                                            }
                                        },
                                        onLongClick = {
                                            if (actionMode) {
                                                if (isSelected)
                                                    selectedItems.remove(subTask)
                                                else
                                                    selectedItems.add(subTask)
                                            } else {
                                                actionMode = true
                                                selectedItems.add(subTask)
                                            }
                                        },
                                    )
                                    .border(
                                        if (isSelected) 2.dp else (-1).dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(8.dp)
                                    ),
                                subTask = subTask,
                                settingPreferences = settingPreferences,
                                color = color,
                                onImpSwipe = {
                                    subTaskViewModel.updateSubTask(subTask.copy(isImportant = it))
                                },
                                isListViewEnable = isListViewEnable,
                                onCancelReminder = {
                                    if (!actionMode) {
                                        subTaskViewModel.updateSubTask(
                                            subTask.copy(reminder = null)
                                        )
                                    }
                                },
                                onCompletedTask = { isCompleted ->
                                    if (!actionMode) {
                                        subTaskViewModel.onSubTaskCheckedChanged(
                                            subTask,
                                            isCompleted
                                        )
                                    }
                                }
                            ) { isDone ->
                                if (!actionMode) {
                                    if (isDone) {
                                        subTaskViewModel.onSubTaskSwiped(subTask)
                                        isSnackBarShow = true
                                    } else {
                                        subTaskViewModel.setSubTask(subTask)
                                        isNewTask = false
                                        subTaskId = subTask.sId
                                        scope.launch {
                                            isBottomSheetOpened = true
                                            sheetState.show()
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(Modifier.fillMaxWidth().height(60.dp)){}
                    }
                }
            }
        }
    } else {
        if (hasNotification || sharedText != null) {
            LaunchedEffect(key1 = Unit) {
                taskViewModel.getById(taskId)
                if (sharedText != null) {
                    subTaskId = null
                    isNewTask = true
                    scope.launch {
                        isBottomSheetOpened = true
                        sheetState.show()
                    }
                    MainActivity.shareTxt = null
                }
            }
        }

        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubtaskHeaderContent(
    startDate: String,
    onStartDateChange: (String) -> Unit,
    endDate: String,
    onEndDateChange: (String) -> Unit,
    setStartDate: () -> Unit,
    setEndDate: () -> Unit,
    shareTask: () -> Unit,
    onProgressBarClick: () -> Unit,
    reminder: Long?,
    onReminderChange: () -> Unit,
    onReminderCancel: () -> Unit,
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
    selectedSort: String,
    onSortChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    windowSize: WindowSize,
) {
    var isDropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }
    val width = LocalConfiguration.current.screenWidthDp.dp
    val isLandscape = windowSize.width > windowSize.height

    Column(modifier) {
        if (totalSubTask > 0) {
            LinearProgressStatus(
                progress = completedSubTask.toFloat() / totalSubTask.toFloat(),
                text = stringResource(R.string.subtask_progress, completedSubTask, totalSubTask),
                color = color,
                width = width,
                height = 24.dp,
                modifier = Modifier.clickable {
                    onProgressBarClick()
                }
            )
        }
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = if (isLandscape) 3 else 1
        ) {
            SearchBar(
                shape = RoundedCornerShape(16.dp),
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
                    Text(stringResource(R.string.search_subtask))
                },
                onActiveChange = {},
                modifier = Modifier.weight(1f)
            ) {}
            if (isLandscape) {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                        .onGloballyPositioned { coordinates ->
                            //This value is used to assign to the DropDown the same width
                            mTextFieldSize = coordinates.size.toSize()
                        }
                        .toggleable(value = isDropDownExpanded) {
                            isDropDownExpanded = !isDropDownExpanded
                        }
                        .semantics(mergeDescendants = true) {}
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.sorting_option), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        selectedSort,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                    Icon(
                        if (isDropDownExpanded)
                            Icons.Filled.KeyboardArrowUp
                        else
                            Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.sort_expand_collapse_button),
                        Modifier.padding(end = 4.dp)
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
                            text = { Text(text = type) },
                            onClick = {
                                onSortChange(index)
                                isDropDownExpanded = false
                            }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { shareTask() }) {
                    Icon(
                        painterResource(R.drawable.ic_share),
                        stringResource(R.string.share),
                        tint = color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.share), color = color)
                }
                IconButton(onClick = { onViewChange(!isListViewEnable) }) {
                    Icon(
                        if (isListViewEnable)
                            painterResource(R.drawable.ic_list_view)
                        else
                            painterResource(R.drawable.ic_grid_view),
                        contentDescription = stringResource(R.string.layout_view_change_button),
                        tint = color
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            RoundedOutlinedTextField(
                value = startDate,
                onValueChange = {
                    onStartDateChange(it)
                },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_calendar_small),
                        contentDescription = null
                    )
                },
                placeholder = {
                    Text(stringResource(R.string.start_date), fontSize = 12.sp)
                },
                modifier = Modifier
                    .weight(1f)
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
                        painterResource(R.drawable.ic_calendar_small),
                        contentDescription = null
                    )
                },
                placeholder = {
                    Text(stringResource(R.string.end_date), fontSize = 12.sp)
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged {
                        if (it.hasFocus) {
                            setEndDate()
                        }
                    },
                textStyle = TextStyle(fontSize = 12.sp)
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onReminderChange() }) {
                Icon(
                    painterResource(R.drawable.ic_reminder_small),
                    null,
                    tint = color
                )
                if (reminder != null) {
                    val diff = DateUtil.getTimeDiff(reminder)
                    val (timeColor, text) = if (diff < 0) {
                        Color.Red to stringResource(R.string.overdue)
                    } else
                        color to DateUtil.getDate(reminder)
                    Text(text, color = timeColor)
                } else {
                    Text(stringResource(R.string.add_date_time), color = color)
                }
            }
            if (reminder != null) {
                Spacer(Modifier.width(8.dp))
                Icon(painterResource(R.drawable.ic_cancel), null, Modifier.clickable {
                    onReminderCancel()
                })
            }
            ChipGroup(
                items = status,
                selectedIndex = selectedStatusIndex,
                selectedContainerColor = color
            ) { index ->
                onStatusChange(index != 0)
            }
        }
    }
}