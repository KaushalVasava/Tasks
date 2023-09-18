package com.lahsuak.apps.tasks.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.ui.navigation.NavigationItem
import com.lahsuak.apps.tasks.ui.screens.components.SubTaskItem
import com.lahsuak.apps.tasks.util.demo.getDemoSubTaskData
import kotlin.random.Random

private const val FIRST = "1. "

private fun getSubText(list: List<SubTask>): String? {
    var sendtxt: String?
    sendtxt = FIRST
    if (list.isNotEmpty()) {
        sendtxt += list.first().subTitle
    }
    for (i in 1 until list.size) {
        sendtxt += "\n${i + 1}. " + list[i].subTitle
    }
    if (sendtxt == FIRST) {
        sendtxt = null
    }
    return sendtxt
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTaskScreen(
    task: Task,
    subtasks: List<SubTask>,
    navController: NavController,
    onSearchChange: (String) -> Unit,
    onItemImpSwipe: (SubTask) -> Unit,
    onCheckedChange: (SubTask, Float) -> Unit,
    onDeleteAllCompletedTask: () -> Unit,
    onBackClick: (Task) -> Unit,
    onTaskClick: (SubTask, Boolean) -> Unit,
) {
    var searchQuery by rememberSaveable {
        mutableStateOf("")
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
                        onBackClick(
                            task.copy(
                                subTaskList = getSubText(subtasks)
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
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FloatingActionButton(onClick = {
                    navController.navigate(NavigationItem.AddUpdateTask.route)
                }) {
                    Icon(
                        painterResource(id = R.drawable.ic_mic),
                        contentDescription = stringResource(id = R.string.add_task)
                    )
                }
                FloatingActionButton(onClick = {
                    onDeleteAllCompletedTask()
                }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete_task)
                    )
                }
                FloatingActionButton(onClick = {
                    navController.navigate("${NavigationItem.AddUpdateSubTask.route}?subTaskId=null/${task.id}/true")
                }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.add_task)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            Modifier.padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            item {
                Column {
                    LinearProgressIndicator(
                        progress = 0.40f,
                        trackColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            onSearchChange(it)
                        },
                        onSearch = {},
                        active = false,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    searchQuery = ""
                                    onSearchChange("")
                                })
                        },
                        placeholder = {
                            Text("Search task", color = Color.Gray)
                        },
                        onActiveChange = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {}
                }
            }
            items(subtasks, key = {
                it.id + Random.nextInt()
            }) { subtask ->
                Spacer(modifier = Modifier.height(8.dp))
                SubTaskItem(
                    subtask,
                    onImpSwipe = {
                        onItemImpSwipe(subtask.copy(isImportant = it))
                    },
                    onItemClick = {
                        onTaskClick(subtask, false)
                        navController.navigate(
                            "${NavigationItem.AddUpdateSubTask.route}?subTaskId=${subtask.id}/${task.id}/false"
                        )
                    },
                    onCompletedTask = { isCompleted ->
                        val progress =
                            subtasks.filter { it.isDone }.size.toFloat() / subtasks.size.toFloat()
                        onCheckedChange(subtask.copy(isDone = isCompleted), progress)
                    }
                ) { isDone ->
                    if (isDone) {
                        onTaskClick(subtask, true)
                    } else {
                        onTaskClick(subtask, false)
                        navController.navigate("${NavigationItem.AddUpdateSubTask.route}?subTaskId=${subtask.id}/${task.id}/false")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewSubTaskScreen() {
    MaterialTheme {
        Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
            SubTaskScreen(
                Task(0, "0", false),
                subtasks = getDemoSubTaskData(),
                navController = rememberNavController(),
                onSearchChange = {},
                onItemImpSwipe = {},
                onCheckedChange = {_,_->},
                onDeleteAllCompletedTask = {},
                onBackClick = {}
            ) { _, _ ->
            }
        }
    }
}
