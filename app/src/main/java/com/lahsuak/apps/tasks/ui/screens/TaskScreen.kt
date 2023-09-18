package com.lahsuak.apps.tasks.ui.screens

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.ui.navigation.NavigationItem
import com.lahsuak.apps.tasks.ui.screens.components.ChipGroup
import com.lahsuak.apps.tasks.ui.screens.components.TaskItem
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.demo.getDemoTaskData
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskScreen(
    tasks: List<Task>,
    navController: NavController,
    isListView: Boolean,
    onSearchChange: (String) -> Unit,
    onItemImpSwipe: (Task) -> Unit,
    onCheckedChange: (Task) -> Unit,
    onDeleteAllCompletedTask: () -> Unit,
    onTaskClick: (Task, Boolean) -> Unit,
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }
    val lazyColumnState = rememberLazyListState()

    val status = remember {
        mutableStateListOf("Active", "Done")
    }
    var isTaskDone by rememberSaveable {
        mutableStateOf(false)
    }
    var isListViewEnable by rememberSaveable {
        mutableStateOf(isListView)
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
                AnimatedVisibility(visible = !isTaskDone) {
                    FloatingActionButton(onClick = {
                        // voice task
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
                            onDeleteAllCompletedTask()
                        },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete_task)
                        )
                    }
                }
                AnimatedVisibility(visible = !isTaskDone) {
                    FloatingActionButton(onClick = {
                        navController.navigate("${NavigationItem.AddUpdateTask.route}?taskId=null/true")
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.add_task)
                        )
                    }
                }
            }
        }
    ) { paddingValue ->
        if (!isListViewEnable) {
            LazyColumn(
                state = lazyColumnState,
                modifier = Modifier.padding(paddingValue),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                item {
                    HeaderContent(
                        searchQuery = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            onSearchChange(it)
                        },
                        isListViewEnable = isListViewEnable,
                        onViewChange = {
                            isListViewEnable = it
                        },
                        onStatusChange = {
                            isTaskDone = it
                        },
                        status = status,
                        Modifier.fillMaxWidth()
                    )
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
                                onItemImpSwipe(task.copy(isImp = isImp))
                            },
                            onItemClick = {
                                onTaskClick(task, false)
                                navController.navigate("${NavigationItem.SubTask.route}/${task.id}")
                            },
                            onCompletedTask = { isCompleted ->
                                onCheckedChange(task.copy(isDone = isCompleted))
                            }
                        ) { isDone ->
                            if (isDone) {
                                onTaskClick(task, true)
                                navController.popBackStack()
                            } else {
                                onTaskClick(task, false)
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
                contentPadding = PaddingValues(8.dp),
                columns = StaggeredGridCells.Fixed(2),
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    HeaderContent(
                        searchQuery = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                        },
                        isListViewEnable = isListViewEnable,
                        onViewChange = {
                            isListViewEnable = it
                        },
                        onStatusChange = {
                            isTaskDone = it
                        },
                        status = status,
                        Modifier.fillMaxWidth()
                    )
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
                                onItemImpSwipe(task.copy(isImp = isImp))
                            },
                            onItemClick = {
                                onTaskClick(task, false)
                                navController.navigate("${NavigationItem.SubTask.route}/${task.id}")
                            },
                            onCompletedTask = { isCompleted ->
                                onCheckedChange(task.copy(isDone = isCompleted))
                            }
                        ) { isDone ->
                            if (isDone) {
                                onTaskClick(task, true)
                                navController.popBackStack()
                            } else {
                                onTaskClick(task, false)
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
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    isListViewEnable: Boolean,
    onViewChange: (Boolean) -> Unit,
    onStatusChange: (Boolean) -> Unit,
    status: List<String>,
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
    var selectedStatusIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    var isDropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var mSelectedText by remember {
        mutableStateOf(sortTypes[0])
    }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero) }
    val icon = if (isDropDownExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    Column(modifier) {
        LinearProgressIndicator(
            progress = 0.40f,
            trackColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(8.dp))
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
                    Text("Search task", color = Color.Gray)
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
                            painterResource(id = R.drawable.ic_grid_view)
                        else
                            painterResource(id = R.drawable.ic_list_view),
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
                    sortTypes.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(text = type)
                            },
                            onClick = {
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
fun PreviewTaskScreen() {
    MaterialTheme {
        Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
            TaskScreen(
                tasks = getDemoTaskData(),
                navController = rememberNavController(),
                isListView = false,
                onSearchChange = {},
                onItemImpSwipe = {},
                onCheckedChange = {},
                onDeleteAllCompletedTask = {}
            ) { _, _ ->
            }
        }
    }
}
