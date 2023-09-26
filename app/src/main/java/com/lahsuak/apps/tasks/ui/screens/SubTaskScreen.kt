package com.lahsuak.apps.tasks.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.FilterPreferences
import com.lahsuak.apps.tasks.data.SortOrder
import com.lahsuak.apps.tasks.ui.navigation.NavigationItem
import com.lahsuak.apps.tasks.ui.screens.components.ChipGroup
import com.lahsuak.apps.tasks.ui.screens.components.LinearProgressStatus
import com.lahsuak.apps.tasks.ui.screens.components.SubTaskItem
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.toSortForm
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SubTaskScreen(
    taskId: Int,
    navController: NavController,
    subTaskViewModel: SubTaskViewModel,
    taskViewModel: TaskViewModel,
) {
    val taskState by taskViewModel.taskFlow.collectAsState()
    if (taskState != null) {
        val task = taskState!!
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
        val status = remember {
            mutableStateListOf("Active", "Done")
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
                    navController = navController,
                    subTaskViewModel = subTaskViewModel,
                ) {
                    isBottomSheetOpened = false
                }
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
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "back"
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
                    AnimatedVisibility(visible = !isSubTaskDone) {
                        FloatingActionButton(onClick = {
                            navController.navigate(NavigationItem.AddUpdateTask.route)
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
                            onSortChange = {
                                taskViewModel.onSortOrderSelected(it, context)
                            },
                            sortOrder = preference.sortOrder
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
                                    navController.navigate(
                                        "${NavigationItem.AddUpdateSubTask.route}?subTaskId=${subtask.id}/${task.id}/false"
                                    )
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
                                if (isDone)
                                    subTaskViewModel.deleteSubTask(subtask)
                                else {
                                    subTaskViewModel.setSubTask(subtask)
                                    navController.navigate("${NavigationItem.AddUpdateSubTask.route}?subTaskId=${subtask.id}/${task.id}/false")
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
                            subTasks.filter { it.isDone }.size,
                            subTasks.size,
                            color = Color(TaskApp.categoryTypes[task.color].color),
                            searchQuery = searchQuery,
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
                            onSortChange = {
                                taskViewModel.onSortOrderSelected(it, context)
                            },
                            sortOrder = preference.sortOrder,
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
                                if (isDone)
                                    subTaskViewModel.deleteSubTask(subTask)
                                else {
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
    completedSubTask: Int,
    totalSubTask: Int,
    searchQuery: String,
    color: Color,
    onQueryChange: (String) -> Unit,
    isListViewEnable: Boolean,
    onViewChange: (Boolean) -> Unit,
    onStatusChange: (Boolean) -> Unit,
    status: List<String>,
    onSortChange: (SortOrder) -> Unit,
    sortOrder: SortOrder,
    modifier: Modifier = Modifier,
) {
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
    val sort = sorts.indexOfFirst {
        it.contains(sortOrder.name.toSortForm())
    }
    var selectedStatusIndex by rememberSaveable {
        mutableIntStateOf(sort)
    }
    var isDropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var mSelectedText by remember {
        mutableStateOf("Name")//sortTypes[0])
    }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }
    val icon = if (isDropDownExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
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
                    Text("Search task")
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
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Sort by")
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
                        },
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
                        icon,
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
                                onSortChange(SortOrder.getOrder(index))
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
                selectedStatusIndex = index
                onStatusChange(index != 0)
            }
        }
    }
}

@Preview
@Composable
fun PreviewSubTaskScreen() {
    val taskViewModel: TaskViewModel = viewModel()
    val subTaskViewModel: SubTaskViewModel = viewModel()

    MaterialTheme {
        Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
            SubTaskScreen(
                0,
                navController = rememberNavController(),
                taskViewModel = taskViewModel,
                subTaskViewModel = subTaskViewModel
            )
        }
    }
}
