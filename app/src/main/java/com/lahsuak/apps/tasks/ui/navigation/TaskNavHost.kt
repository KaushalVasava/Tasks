package com.lahsuak.apps.tasks.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            Log.d("TAG", "TaskNavHost:")
            TaskScreen(
                navController,
                taskViewModel
            )
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
                SubTaskScreen(
                    taskId,
                    navController,
                    subTaskViewModel,
                    taskViewModel
                )
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
                    nullable = true
                },
                navArgument("isNewTask") {
                    type = NavType.BoolType
                }
            )
        ) { navBackStackEntry ->
            val taskId = navBackStackEntry.arguments?.getString("taskId")
            val isNewTask = navBackStackEntry.arguments?.getBoolean("isNewTask") ?: true
            AddUpdateTaskScreen(
                navController,
                taskViewModel,
                isNewTask,
                taskId
            )
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
            AddUpdateSubTaskScreen(
                taskId!!.toInt(),
                subTaskId,
                isNewTask,
                navController,
                subTaskViewModel
            )
        }
        composable(NavigationItem.Notification.route) {
            val notifications by notificationViewModel.notifications.collectAsState(
                initial = emptyList()
            )
            NotificationScreen(notifications)
        }
    }
}