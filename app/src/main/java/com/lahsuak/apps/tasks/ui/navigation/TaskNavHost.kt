package com.lahsuak.apps.tasks.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
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
import com.lahsuak.apps.tasks.util.WindowSize

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
                windowSize
            )
        }
        composable("${NavigationItem.SubTask.route}/{task_id}/{has_notification}?sharedText={sharedText}",
            arguments = listOf(
                navArgument("task_id") {
                    type = NavType.IntType
                },
                navArgument("has_notification") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("sharedText") {
                    type = NavType.StringType
                    defaultValue = null
                    nullable = true
                }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern =
                    "myapp://kmv.com/subtaskscreen/{task_id}/{has_notification}"
            })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("task_id")
            val hasNotification = backStackEntry.arguments?.getBoolean("has_notification") ?: false
            val sharedText = backStackEntry.arguments?.getString("sharedText")

            if (taskId != null) {
                SubTaskScreen(
                    taskId,
                    navController,
                    subTaskViewModel,
                    taskViewModel,
                    notificationViewModel,
                    fragmentManager,
                    windowSize,
                    hasNotification,
                    sharedText
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
                navController,
                taskViewModel,
                isNewTask,
                taskId,
                fragmentManager,
                sharedText
            )
        }
        composable(NavigationItem.Notification.route) {
            val notifications by notificationViewModel.notifications.collectAsState(
                initial = emptyList()
            )
            NotificationScreen(notifications, navController)
        }
//                                navController.navigate("${NavigationItem.AddUpdateSubTask.route}?subTaskId=${subTask.id}/${task.id}/false")

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
                    taskId.toInt(),
                    subTaskId,
                    isNewTask,
                    navController,
                    subTaskViewModel,
                    fragmentManager,
                    sharedText
                )
            }
        }
    }
}