package com.lahsuak.apps.tasks.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lahsuak.apps.tasks.ui.screens.AddUpdateSubTaskScreen
import com.lahsuak.apps.tasks.ui.screens.AddUpdateTaskScreen
import com.lahsuak.apps.tasks.ui.screens.NotificationScreen
import com.lahsuak.apps.tasks.ui.screens.OverviewScreen
import com.lahsuak.apps.tasks.ui.screens.SettingScreen
import com.lahsuak.apps.tasks.ui.screens.SubTaskScreen
import com.lahsuak.apps.tasks.ui.screens.TaskScreen
import com.lahsuak.apps.tasks.ui.viewmodel.NotificationViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel

@Composable
fun TaskNavHost(
    taskViewModel: TaskViewModel,
    subTaskViewModel: SubTaskViewModel,
    notificationViewModel: NotificationViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavigationItem.Task.route
    ) {
        composable(NavigationItem.Task.route) {
            val tasks by taskViewModel.tasksFlow.collectAsState(initial = emptyList())
            TaskScreen(
                tasks = tasks,
                navController = navController,
                isListView = false,
                onSearchChange = { query ->
                    taskViewModel.searchQuery.value = query
                },
                onCheckedChange = {
                    taskViewModel.update(it)
                },
                onItemImpSwipe = { task ->
                    taskViewModel.update(task)
                },
                onDeleteAllCompletedTask = {
                    taskViewModel.onDeleteAllCompletedClick()
                },
            ) { task, isDone ->
                if (isDone) {
                    taskViewModel.delete(task)
                } else {
                    taskViewModel.setTask(task)
                }
            }
        }
        composable("${NavigationItem.SubTask.route}/{taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            if (taskId != null) {
                LaunchedEffect(key1 = { taskId }) {
                    taskViewModel.getById(taskId)
                }
                val task by taskViewModel.taskFlow.collectAsState()
                if (task != null) {
                    subTaskViewModel.taskId.value = task!!.id
                    val subTasks by subTaskViewModel.subTasks.collectAsState(initial = emptyList())
                    SubTaskScreen(
                        task!!,
                        subtasks = subTasks,
                        navController = navController,
                        onSearchChange = {
                            subTaskViewModel.searchQuery.value = it
                        },
                        onItemImpSwipe = {
                            subTaskViewModel.updateSubTask(it)
                        },
                        onCheckedChange = { isCompleted, taskProgress ->
                            subTaskViewModel.updateSubTask(isCompleted)
                            Log.d("TAG", "TaskNavHost: #progress $taskProgress")
                            taskViewModel.update(task!!.copy(progress = taskProgress))
                        },
                        onDeleteAllCompletedTask = {
                            subTaskViewModel.onDeleteAllCompletedClick()
                        },
                        onBackClick = {
                            taskViewModel.update(it)
                        }
                    ) { subtask, isDone ->
                        if (isDone)
                            subTaskViewModel.deleteSubTask(subtask)
                        else
                            subTaskViewModel.setSubTask(subtask)
//                            subTaskViewModel.updateSubTask(subtask)
                    }
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        composable(NavigationItem.Setting.route) {
            SettingScreen(navController)
        }
        composable(NavigationItem.Overview.route) {
            OverviewScreen()
        }
        composable("${NavigationItem.AddUpdateTask.route}?taskId={taskId}/{isNewTask}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    defaultValue = null
                    nullable = true
                },
                navArgument("isNewTask") {
                    type = NavType.BoolType
                }
            )
        ) { navBackStackEntry ->
            val taskId = navBackStackEntry.arguments?.getString("taskId")
            val isNewTask = navBackStackEntry.arguments?.getBoolean("isNewTask") ?: true
            if (!isNewTask && taskId != null) {
                LaunchedEffect(key1 = taskId) {
                    taskViewModel.getById(taskId.toInt())
                }
            }
            AddUpdateTaskScreen(
                if (isNewTask) {
                    null
                } else {
                    val task by taskViewModel.taskFlow.collectAsState()
                    task
                },
                navController,
                onAddTask = {
                    taskViewModel.insert(it)
                    taskViewModel.resetTaskValue()
                }
            ) {
                taskViewModel.update(it)
                taskViewModel.resetTaskValue()
            }
        }
        composable("${NavigationItem.AddUpdateSubTask.route}?subTaskId={subTaskId}/{taskId}/{isNewTask}",
            arguments = listOf(
                navArgument("subTaskId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("taskId") {
                    type = NavType.StringType
                },
                navArgument("isNewTask") {
                    type = NavType.BoolType
                }
            )
        ) { navBackStackEntry ->
            val subTaskId = navBackStackEntry.arguments?.getString("subTaskId")
            val taskId = navBackStackEntry.arguments?.getString("taskId")
            val isNewTask = navBackStackEntry.arguments?.getBoolean("isNewTask") ?: true


            if (!isNewTask && subTaskId != null) {
                LaunchedEffect(key1 = subTaskId) {
                    subTaskViewModel.getBySubTaskId(subTaskId.toInt())
                }
            }
            AddUpdateSubTaskScreen(
                taskId!!.toInt(),
                if (isNewTask) {
                    null
                } else {
                    val subTask by subTaskViewModel.subTaskFlow.collectAsState()
                    subTask
                },
                navController,
                onAddSubTask = {
                    subTaskViewModel.insertSubTask(it)
                    subTaskViewModel.resetSubTaskValue()
                }
            ) {
                subTaskViewModel.updateSubTask(it)
                subTaskViewModel.resetSubTaskValue()
            }
        }
        composable(NavigationItem.Notification.route) {
            val notifications by notificationViewModel.notifications.collectAsState(
                initial = emptyList()
            )
            NotificationScreen(notifications)
        }
    }
}