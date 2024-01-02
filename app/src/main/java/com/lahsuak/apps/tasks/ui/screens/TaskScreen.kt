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
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.model.SortOrder
import com.lahsuak.apps.tasks.model.TaskEvent
import com.lahsuak.apps.tasks.ui.MainActivity
import com.lahsuak.apps.tasks.ui.navigation.NavigationItem
import com.lahsuak.apps.tasks.ui.screens.components.ChipGroup
import com.lahsuak.apps.tasks.ui.screens.components.LinearProgressStatus
import com.lahsuak.apps.tasks.ui.screens.components.ShareDialog
import com.lahsuak.apps.tasks.ui.screens.components.TaskItem
import com.lahsuak.apps.tasks.ui.screens.dialog.AddUpdateTaskScreen
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppUtil
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
fun TaskScreen(
    navController: NavController,
    taskViewModel: TaskViewModel,
    settingsPreferences: SettingPreferences,
    windowSize: WindowSize,
) {
    var sharedText by rememberSaveable {
        mutableStateOf(MainActivity.shareTxt)
    }
    val tasks by taskViewModel.tasksFlow.collectAsState(initial = emptyList())
    val taskEvents by taskViewModel.tasksEvent.collectAsState(TaskEvent.Initial)
    val preference by taskViewModel.preferencesFlow.collectAsState(
        initial = FilterPreferences(
            sortOrder = SortOrder.BY_NAME, viewType = false
        )
    )
    val showVoiceTask = settingsPreferences.showVoiceIcon

    var taskId: String? by rememberSaveable {
        mutableStateOf(null)
    }
    var isNewTask by rememberSaveable {
        mutableStateOf(true)
    }

    var isBottomSheetOpened by rememberSaveable {
        mutableStateOf(false)
    }

    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }
    val active = stringResource(R.string.active)
    val done = stringResource(R.string.done)
    val status = remember {
        mutableStateListOf(active, done)
    }
    var isTaskDone by rememberSaveable {
        mutableStateOf(false)
    }
    val speakLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val result1 = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val task = Task(
                id = 0,
                title = result1!![0],
                startDate = System.currentTimeMillis()
            )
            taskViewModel.insert(task)
        }
    }

    var isListViewEnable by rememberSaveable {
        mutableStateOf(preference.viewType)
    }
    val context = LocalContext.current

    val snackBarHostState = remember {
        SnackbarHostState()
    }
    val sorts = listOf(
        stringResource(R.string.name),
        stringResource(R.string.name_desc),
        stringResource(R.string.date),
        stringResource(R.string.date_desc),
        stringResource(R.string.category),
        stringResource(R.string.category_desc)
    )
    val sortTypes by remember {
        mutableStateOf(sorts)
    }
    var selectedSortIndex by rememberSaveable {
        mutableIntStateOf(sortTypes.indexOfFirst {
            it == preference.sortOrder.type
        })
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

    var actionMode by rememberSaveable {
        mutableStateOf(false)
    }

    val selectedItems = remember {
        mutableStateListOf<Task>()
    }

    val resetSelectionMode = {
        actionMode = false
        selectedItems.clear()
    }

    when (val event = taskEvents) {
        is TaskEvent.ShowUndoDeleteTaskMessage -> {
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
                            taskViewModel.onUndoDeleteClick(event.task)
                        }
                    }
                    isSnackBarShow = false
                }
            }
        }

        TaskEvent.NavigateToAllCompletedScreen -> {
            if (openDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { openDeleteDialog = false },
                    title = { Text(text = stringResource(R.string.confirm_deletion)) },
                    text = { Text(stringResource(R.string.delete_completed_task)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                openDeleteDialog = false
                                taskViewModel.deleteCompletedTask()
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

        TaskEvent.Initial -> {}
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

    val sheetState = androidx.compose.material.rememberModalBottomSheetState(
        skipHalfExpanded = true,
        initialValue = ModalBottomSheetValue.Hidden
    )
    val scope = rememberCoroutineScope()

    if (sharedText != null && tasks.isNotEmpty()) {
        var openDialog by rememberSaveable {
            mutableStateOf(true)
        }

        ShareDialog(
            tasks,
            openDialog = openDialog,
            onDialogStatusChange = {
                openDialog = it
                MainActivity.shareTxt = null
                sharedText = null
            },
            onTaskAddButtonClick = {
                taskId = null
                isNewTask = true
                isBottomSheetOpened = true
                scope.launch {
                    sheetState.show()
                    MainActivity.shareTxt = null
                }
                openDialog = false
            },
            onSaveButtonClick = {
                navController.navigate("${NavigationItem.SubTask.route}/${it.id}/false")
                openDialog = false
                sharedText = null
            }
        ) {
            openDialog = false
            sharedText = null
            MainActivity.shareTxt = null
        }
    }

    ModalBottomSheetLayout(
        sheetBackgroundColor = MaterialTheme.colorScheme.surface,
        sheetState = sheetState,
        sheetContent = {
            if (isBottomSheetOpened) {
                AddUpdateTaskScreen(
                    sheetState,
                    taskViewModel = taskViewModel,
                    isNewTask = isNewTask,
                    taskId = taskId,
                    sharedText = sharedText
                ) {
                    scope.launch {
                        sheetState.hide()
                        isBottomSheetOpened = false
                    }
                }
            }
        }) {

        val lazyGridListState = rememberLazyStaggeredGridState()

        val isFabExtended by remember {
            derivedStateOf { lazyGridListState.firstVisibleItemIndex != 0 }
        }

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
                                    tasks.size
                                ),
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold
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
                                    if (selectedItems.size == tasks.size) {
                                        selectedItems.clear()
                                    } else {
                                        selectedItems.clear()
                                        selectedItems.addAll(tasks)
                                    }
                                }) {
                                    Icon(
                                        painterResource(R.drawable.ic_select_all),
                                        stringResource(R.string.select_all),
                                        tint = if (selectedItems.size == tasks.size) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.White
                                        }
                                    )
                                }
                                IconButton(onClick = {
                                    // delete selected items
                                    selectedItems.map {
                                        taskViewModel.delete(it)
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
                                DateUtil.getToolbarDateTime(System.currentTimeMillis()),
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold
                            )
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
                if (!actionMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement =
                        if (isTaskDone)
                            Arrangement.Center
                        else
                            Arrangement.SpaceBetween,
                    ) {
                        AnimatedVisibility(visible = showVoiceTask && !isTaskDone) {
                            FloatingActionButton(
                                containerColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    AppUtil.speakToAddTask(context, speakLauncher)
                                }) {
                                Icon(
                                    painterResource(R.drawable.ic_mic),
                                    stringResource(R.string.add_task)
                                )
                            }
                        }
                        AnimatedVisibility(visible = isTaskDone) {
                            FloatingActionButton(
                                containerColor = MaterialTheme.colorScheme.error,
                                onClick = {
                                    taskViewModel.onDeleteAllCompletedClick()
                                    openDeleteDialog = true
                                },
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    stringResource(R.string.delete_task)
                                )
                            }
                        }
                        AnimatedVisibility(visible = !isTaskDone) {
                            if (isFabExtended) {
                                ExtendedFloatingActionButton(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    onClick = {
                                        taskId = null
                                        isNewTask = true
                                        isBottomSheetOpened = true
                                        sharedText = null
                                        scope.launch {
                                            sheetState.show()
                                        }
                                    }, text = {
                                        Text(stringResource(R.string.add_task))
                                    },
                                    icon = {
                                        Icon(
                                            painterResource(R.drawable.ic_create),
                                            stringResource(R.string.add_task)
                                        )
                                    }
                                )
                            } else {
                                FloatingActionButton(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    onClick = {
                                        taskId = null
                                        isNewTask = true
                                        isBottomSheetOpened = true
                                        sharedText = null
                                        scope.launch {
                                            sheetState.show()
                                        }
                                    }) {
                                    Icon(
                                        painterResource(R.drawable.ic_create),
                                        stringResource(R.string.add_task)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(snackBarHostState)
            }
        ) { paddingValue ->
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValue),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable {
                            taskId = null
                            isNewTask = true
                            isBottomSheetOpened = true
                            scope.launch {
                                sheetState.show()
                            }
                        }
                    ) {
                        Image(
                            painterResource(R.drawable.ic_add_task2),
                            stringResource(
                                R.string.add_task
                            ),
                            modifier = Modifier,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stringResource(R.string.create_new_task), textAlign = TextAlign.Center)
                    }
                }
            }

            LazyVerticalStaggeredGrid(
                state = lazyGridListState,
                modifier = Modifier.padding(paddingValue),
                contentPadding = PaddingValues(horizontal = 8.dp),
                columns = StaggeredGridCells.Fixed(
                    if (isListViewEnable) {
                        when (windowSize.width) {
                            WindowType.Compact -> 2
                            WindowType.Medium -> 3
                            WindowType.Expanded -> 4
                        }
                    } else {
                        when (windowSize.width) {
                            WindowType.Compact -> 1
                            WindowType.Medium -> 2
                            WindowType.Expanded -> 3
                        }
                    }
                ),
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    AnimatedVisibility(!actionMode) {
                        HeaderContent(
                            tasks.filter { it.isDone }.size,
                            tasks.size,
                            searchQuery = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                taskViewModel.searchQuery.value = it
                            },
                            isListViewEnable = isListViewEnable,
                            onViewChange = {
                                isListViewEnable = it
                                taskViewModel.onViewTypeChanged(it, context)
                            },
                            onStatusChange = {
                                isTaskDone = it
                            },
                            status = status,
                            selectedStatusIndex = if (isTaskDone) 1 else 0,
                            sortTypes = sortTypes,
                            selectedSort = selectedSort,
                            onSortChange = { index ->
                                selectedSortIndex = index
                                selectedSort = sortTypes[selectedSortIndex]
                                taskViewModel.onSortOrderSelected(
                                    SortOrder.getOrder(index),
                                    context
                                )
                            },
                            onProgressBarClick = {
                                navController.navigate(NavigationItem.Overview.route)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                items(
                    tasks.filter { t -> isTaskDone == t.isDone }
                        .sortedByDescending { t -> t.isImp },
                    key = { t ->
                        t.id + Random.nextInt()
                    }
                ) { task ->
                    val isSelected = selectedItems.contains(task)
                    Row(
                        Modifier
                            .animateItemPlacement()
                            .padding(4.dp)
                    ) {
                        TaskItem(
                            Modifier
                                .combinedClickable(
                                    onClick = {
                                        if (actionMode) {
                                            if (isSelected)
                                                selectedItems.remove(task)
                                            else
                                                selectedItems.add(task)
                                        } else {
                                            taskViewModel.setTask(task)
                                            navController.navigate("${NavigationItem.SubTask.route}/${task.id}/false")
                                        }
                                    },
                                    onLongClick = {
                                        if (actionMode) {
                                            if (isSelected)
                                                selectedItems.remove(task)
                                            else
                                                selectedItems.add(task)
                                        } else {
                                            actionMode = true
                                            selectedItems.add(task)
                                        }
                                    },
                                )
                                .border(
                                    if (isSelected) 2.dp else (-1).dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                ),
                            task = task,
                            settingPreferences = settingsPreferences,
                            isListViewEnable = isListViewEnable,
                            onImpSwipe = { isImp ->
                                taskViewModel.update(task.copy(isImp = isImp))
                            },
                            onCancelReminder = {
                                if (!actionMode) {
                                    taskViewModel.update(task.copy(reminder = null))
                                }
                            },
                            onCompletedTask = { isCompleted ->
                                if (!actionMode) {
                                    taskViewModel.onTaskCheckedChanged(task, isCompleted)
                                }
                            }
                        ) { isDone ->
                            if (!actionMode) {
                                if (isDone) {
                                    taskViewModel.onTaskSwiped(task)
                                    isSnackBarShow = true
                                } else {
                                    taskViewModel.setTask(task)
                                    taskId = task.id.toString()
                                    isNewTask = false
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HeaderContent(
    completedTask: Int,
    totalTask: Int,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    isListViewEnable: Boolean,
    onViewChange: (Boolean) -> Unit,
    onStatusChange: (Boolean) -> Unit,
    status: List<String>,
    selectedStatusIndex: Int,
    sortTypes: List<String>,
    selectedSort: String,
    onSortChange: (Int) -> Unit,
    onProgressBarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }
    val width = LocalConfiguration.current.screenWidthDp.dp

    Column(modifier) {
        if (totalTask > 0) {
            LinearProgressStatus(
                modifier = Modifier.clickable {
                    onProgressBarClick()
                },
                progress = completedTask.toFloat() / totalTask.toFloat(),
                text = stringResource(R.string.task_progress, completedTask, totalTask),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                width = width,
                height = 24.dp,
            )
        }
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchBar(
                shape = RoundedCornerShape(16.dp),
                query = searchQuery,
                onQueryChange = { q -> onQueryChange(q) },
                onSearch = {},
                active = false,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    AnimatedVisibility(searchQuery.isNotEmpty()) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            modifier = Modifier.clickable {
                                onQueryChange("")
                            }
                        )
                    }
                },
                placeholder = { Text(stringResource(R.string.search_task)) },
                onActiveChange = {},
                modifier = Modifier.weight(1f)
            ) {}
            Column(
                Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .onGloballyPositioned { coordinates ->
                            //This value is used to assign to the DropDown the same width
                            mTextFieldSize = coordinates.size.toSize()
                        }
                        .toggleable(value = isDropDownExpanded) {
                            isDropDownExpanded = !isDropDownExpanded
                        }
                        .semantics(mergeDescendants = true) {}
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.sorting_option), fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
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
                        stringResource(R.string.sort_expand_collapse_button),
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
        }

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            ChipGroup(
                items = status,
                selectedIndex = selectedStatusIndex
            ) { index ->
                onStatusChange(index != 0)
            }
            IconButton(onClick = {
                onViewChange(!isListViewEnable)
            }) {
                Icon(
                    if (isListViewEnable)
                        painterResource(R.drawable.ic_list_view)
                    else
                        painterResource(R.drawable.ic_grid_view),
                    stringResource(R.string.layout_view_change_button),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}