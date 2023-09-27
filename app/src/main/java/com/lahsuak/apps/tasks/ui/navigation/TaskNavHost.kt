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
    windowSize: WindowSize
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
                    taskViewModel,
                    fragmentManager
                )
            }
        }
        composable(NavigationItem.Setting.route) {
            SettingScreen(navController, fragmentManager)
        }
        composable(NavigationItem.Overview.route) {
            OverviewScreen(navController, taskViewModel)
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
                taskId,
                fragmentManager
            )
        }
        composable(NavigationItem.Notification.route) {
            val notifications by notificationViewModel.notifications.collectAsState(
                initial = emptyList()
            )
            NotificationScreen(notifications, navController)
        }
    }
}