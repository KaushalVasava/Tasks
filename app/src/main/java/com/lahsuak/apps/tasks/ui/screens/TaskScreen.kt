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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.util.preference.FilterPreferences
import com.lahsuak.apps.tasks.data.model.SortOrder
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.model.TaskEvent
import com.lahsuak.apps.tasks.ui.navigation.NavigationItem
import com.lahsuak.apps.tasks.ui.screens.components.ChipGroup
import com.lahsuak.apps.tasks.ui.screens.components.LinearProgressStatus
import com.lahsuak.apps.tasks.ui.screens.components.TaskItem
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.WindowSize
import com.lahsuak.apps.tasks.util.WindowType
import com.lahsuak.apps.tasks.util.rememberWindowSize
import com.lahsuak.apps.tasks.util.toSortForm
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskScreen(
    navController: NavController,
    taskViewModel: TaskViewModel,
    windowSize: WindowSize,
) {
    val prefManager = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
    val showVoiceTask =
        prefManager.getBoolean(AppConstants.SharedPreference.SHOW_VOICE_TASK_KEY, true)

    val tasks by taskViewModel.tasksFlow.collectAsState(initial = emptyList())
    val taskEvents by taskViewModel.tasksEvent.collectAsState(TaskEvent.Initial)
    val preference by taskViewModel.preferencesFlow.collectAsState(
        initial = FilterPreferences(
            sortOrder = SortOrder.BY_NAME, hideCompleted = false, viewType = false
        )
    )
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }
    val active = stringResource(id = R.string.active)
    val done = stringResource(id = R.string.done)
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
                title = result1!![0]
            )
            taskViewModel.insert(task)
        }
    }

    val lazyListState = rememberLazyListState()

    val isFabExtended by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex != 0
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
        stringResource(R.string.date),
        stringResource(R.string.name_desc),
        stringResource(R.string.date_desc),
        stringResource(R.string.category),
        stringResource(R.string.category_desc)
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

    if (isSnackBarShow) {
        when (val event = taskEvents) {
            is TaskEvent.ShowUndoDeleteTaskMessage -> {
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
                            taskViewModel.onUndoDeleteClick(event.task)
                        }
                    }
                    isSnackBarShow = false
                }
            }

            TaskEvent.NavigateToAllCompletedScreen -> {
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
                                    taskViewModel.deleteCompletedTask()
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

            TaskEvent.Initial -> {}
        }
    }

    Scaffold(
        topBar = {
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
                if (isTaskDone)
                    Arrangement.Center
                else
                    Arrangement.SpaceBetween,
            ) {
                AnimatedVisibility(visible = showVoiceTask && !isTaskDone) {
                    FloatingActionButton(onClick = {
                        AppUtil.speakToAddTaskCompose(context, speakLauncher)
                    }) {
                        Icon(
                            painterResource(id = R.drawable.ic_mic),
                            contentDescription = stringResource(id = R.string.add_task)
                        )
                    }
                }
                AnimatedVisibility(visible = isTaskDone) {
                    FloatingActionButton(
                        onClick = {
                            taskViewModel.onDeleteAllCompletedClick()
                            openDialog = true
                        },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete_task)
                        )
                    }
                }
                AnimatedVisibility(visible = !isTaskDone) {
                    if (isFabExtended) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                navController.navigate("${NavigationItem.AddUpdateTask.route}?taskId=null/true")
                            }, text = {
                                Text("Add task")
                            },
                            icon = {
                                Icon(
                                    painterResource(id = R.drawable.ic_create),
                                    contentDescription = stringResource(id = R.string.add_task)
                                )
                            }
                        )
                    } else {
                        FloatingActionButton(onClick = {
                            navController.navigate("${NavigationItem.AddUpdateTask.route}?taskId=null/true")
                        }) {
                            Icon(
                                painterResource(id = R.drawable.ic_create),
                                contentDescription = stringResource(id = R.string.add_task)
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
    ) { paddingValue ->
        if (!isListViewEnable) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.padding(paddingValue),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                item {
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
                            if (tasks.size > 1)
                                isListViewEnable = it
                        },
                        onStatusChange = {
                            isTaskDone = it
                        },
                        status = status.toList(),
                        selectedStatusIndex = if (isTaskDone) 1 else 0,
                        sortTypes = sortTypes,
                        selectedSortIndex = selectedSortIndex,
                        onSortChange = { index ->
                            selectedSortIndex = index
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
                    Spacer(Modifier.height(8.dp))
                }
                items(
                    tasks.filter { t -> isTaskDone == t.isDone }
                        .sortedByDescending { t -> t.isImp },
                    key = { t ->
                        t.id + Random.nextInt()
                    }
                ) { task ->
                    Row(Modifier.animateItemPlacement()) {
                        TaskItem(
                            task = task,
                            isListViewEnable,
                            onImpSwipe = { isImp ->
                                taskViewModel.update(task.copy(isImp = isImp))
                            },
                            onItemClick = {
                                taskViewModel.setTask(task)
                                navController.navigate("${NavigationItem.SubTask.route}/${task.id}")
                            },
                            onCompletedTask = { isCompleted ->
                                taskViewModel.update(task.copy(isDone = isCompleted))
                            }
                        ) { isDone ->
                            if (isDone) {
                                taskViewModel.onTaskSwiped(task)
                                isSnackBarShow = true
                            } else {
                                taskViewModel.setTask(task)
                                navController.navigate("${NavigationItem.AddUpdateTask.route}?taskId=${task.id}/false")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                modifier = Modifier.padding(paddingValue),
                contentPadding = PaddingValues(horizontal = 8.dp),
                columns = StaggeredGridCells.Fixed(
                    when (windowSize.width) {
                        WindowType.Expanded -> 4
                        else -> 2
                    }
                ),
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
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
                        },
                        onStatusChange = {
                            isTaskDone = it
                        },
                        status = status,
                        selectedStatusIndex = if (isTaskDone) 1 else 0,
                        sortTypes = sortTypes,
                        selectedSortIndex = selectedSortIndex,
                        onSortChange = { index ->
                            selectedSortIndex = index
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
                    Spacer(Modifier.height(8.dp))
                }
                items(
                    tasks.filter { t -> isTaskDone == t.isDone }
                        .sortedByDescending { t -> t.isImp },
                    key = { t ->
                        t.id + Random.nextInt()
                    }
                ) { task ->
                    Row(
                        Modifier
                            .animateItemPlacement()
                            .padding(4.dp)
                    ) {
                        TaskItem(
                            task = task,
                            isListViewEnable,
                            onImpSwipe = { isImp ->
                                taskViewModel.update(task.copy(isImp = isImp))
                            },
                            onItemClick = {
                                taskViewModel.setTask(task)
                                navController.navigate("${NavigationItem.SubTask.route}/${task.id}")
                            },
                            onCompletedTask = { isCompleted ->
                                taskViewModel.update(
                                    task.copy(
                                        isDone = isCompleted,
                                        startDate = System.currentTimeMillis()
                                    )
                                )
                            }
                        ) { isDone ->
                            if (isDone) {
                                taskViewModel.onTaskSwiped(task)
                                isSnackBarShow = true
                            } else {
                                taskViewModel.setTask(task)
                                navController.navigate("${NavigationItem.AddUpdateTask.route}?taskId=${task.id}/false")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    selectedSortIndex: Int,
    onSortChange: (Int) -> Unit,
    onProgressBarClick: () -> Unit,
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
            modifier = Modifier.clickable {
                onProgressBarClick()
            },
            progress = completedTask.toFloat() / totalTask.toFloat(),
            text = stringResource(id = R.string.task_progress, completedTask, totalTask),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            width = width,
            height = 24.dp,
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
                    Text(stringResource(id = R.string.search_task))
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
                Spacer(Modifier.height(4.dp))
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
            ChipGroup(
                items = status,
                selectedIndex = selectedStatusIndex
            ) { index ->
                onStatusChange(index != 0)
            }
        }
    }
}

@Preview
@Composable
fun PreviewTaskScreen() {
    val taskViewModel: TaskViewModel = viewModel()

    MaterialTheme {
        Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
            TaskScreen(
                navController = rememberNavController(),
                taskViewModel = taskViewModel,
                rememberWindowSize()
            )
        }
    }
}
