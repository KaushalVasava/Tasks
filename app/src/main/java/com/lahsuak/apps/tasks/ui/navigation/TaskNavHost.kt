package com.lahsuak.apps.tasks.ui.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.lahsuak.apps.tasks.ui.screens.dialog.AddUpdateSubTaskScreen
import com.lahsuak.apps.tasks.ui.screens.dialog.AddUpdateTaskScreen
import com.lahsuak.apps.tasks.ui.screens.NotificationScreen
import com.lahsuak.apps.tasks.ui.screens.OverviewScreen
import com.lahsuak.apps.tasks.ui.screens.SettingScreen
import com.lahsuak.apps.tasks.ui.screens.SubTaskScreen
import com.lahsuak.apps.tasks.ui.screens.TaskScreen
import com.lahsuak.apps.tasks.ui.viewmodel.NotificationViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.WindowSize

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TaskNavHost(
    taskViewModel: TaskViewModel,
    subTaskViewModel: SubTaskViewModel,
    notificationViewModel: NotificationViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    fragmentManager: FragmentManager,
    windowSize: WindowSize,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavigationItem.Task.route
    ) {
        composable(NavigationItem.Task.route) {
            TaskScreen(
                navController,
                taskViewModel,
                windowSize,
                fragmentManager
            )
        }
        composable("${NavigationItem.SubTask.route}/{task_id}/{has_notification}",
            arguments = listOf(
                navArgument("task_id") {
                    type = NavType.IntType
                },
                navArgument("has_notification") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern =
                    "myapp://kmv.com/subtaskscreen/{task_id}/{has_notification}"
            })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("task_id")
            val hasNotification = backStackEntry.arguments?.getBoolean("has_notification") ?: false
            if (taskId != null) {
                SubTaskScreen(
                    taskId,
                    navController,
                    subTaskViewModel,
                    taskViewModel,
                    notificationViewModel,
                    fragmentManager,
                    windowSize,
                    hasNotification
                )
            }
        }
        composable(NavigationItem.Setting.route) {
            SettingScreen(navController, fragmentManager)
        }
        composable(NavigationItem.Overview.route) {
            OverviewScreen(navController, taskViewModel)
        }
        composable(
            "${NavigationItem.AddUpdateTask.route}?taskId={taskId}/{isNewTask}?sharedText={sharedText}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("isNewTask") {
                    type = NavType.BoolType
                    defaultValue = true
                },
                navArgument("sharedText") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = "myapp://kmv.com/shortcut"
            })
        ) { navBackStackEntry ->
            val taskId = navBackStackEntry.arguments?.getString("taskId")
            val isNewTask = navBackStackEntry.arguments?.getBoolean("isNewTask") ?: true
            val sharedText = navBackStackEntry.arguments?.getString("sharedText")
            AddUpdateTaskScreen(
                null,
                taskViewModel,
                isNewTask,
                taskId,
                fragmentManager,
                sharedText
            ){}
        }
        composable(NavigationItem.Notification.route) {
            NotificationScreen(notificationViewModel, navController)
        }

        composable(
            "${NavigationItem.AddUpdateSubTask.route}?subTaskId={subTaskId}/{taskId}/{isNewTask}?sharedText={sharedText}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("isNewTask") {
                    type = NavType.BoolType
                    defaultValue = true
                },
                navArgument("sharedText") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            )
        ) { navBackStackEntry ->
            val subTaskId = navBackStackEntry.arguments?.getString("subTaskId")
            val taskId = navBackStackEntry.arguments?.getString("taskId")
            val isNewTask = navBackStackEntry.arguments?.getBoolean("isNewTask") ?: true
            val sharedText = navBackStackEntry.arguments?.getString("sharedText")
            if (taskId != null) {
                AddUpdateSubTaskScreen(
                    null,
                    taskId.toInt(),
                    subTaskId,
                    isNewTask,
                    subTaskViewModel,
                    fragmentManager,
                    sharedText,
                ){}
            }
        }
    }
}