package com.lahsuak.apps.tasks.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.lahsuak.apps.tasks.ui.screens.NotificationScreen
import com.lahsuak.apps.tasks.ui.screens.OverviewScreen
import com.lahsuak.apps.tasks.ui.screens.SubTaskScreen
import com.lahsuak.apps.tasks.ui.screens.TaskScreen
import com.lahsuak.apps.tasks.ui.screens.settings.SettingScreen
import com.lahsuak.apps.tasks.ui.viewmodel.NotificationViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.SettingsViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.ADD_UPDATE_TASK_DEEP_LINK
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.HAS_NOTIFICATION
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.SUBTASK_DEEP_LINK
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.TASK_ID
import com.lahsuak.apps.tasks.util.WindowSize
import com.lahsuak.apps.tasks.util.preference.SettingPreferences

@Composable
fun TaskNavHost(
    taskViewModel: TaskViewModel,
    subTaskViewModel: SubTaskViewModel,
    notificationViewModel: NotificationViewModel,
    settingViewModel: SettingsViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    settingPreferences: SettingPreferences,
    windowSize: WindowSize,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavigationItem.Task.route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(500)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(500)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                animationSpec = tween(500)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                animationSpec = tween(500)
            )
        }
    ) {
        composable(
            NavigationItem.Task.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = ADD_UPDATE_TASK_DEEP_LINK
            })
        ) {
            TaskScreen(
                navController,
                taskViewModel,
                settingPreferences,
                windowSize
            )
        }
        composable("${NavigationItem.SubTask.route}/{$TASK_ID}/{$HAS_NOTIFICATION}",
            arguments = listOf(
                navArgument(TASK_ID) {
                    type = NavType.IntType
                },
                navArgument(HAS_NOTIFICATION) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = SUBTASK_DEEP_LINK
            })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt(TASK_ID)
            val hasNotification = backStackEntry.arguments?.getBoolean(HAS_NOTIFICATION) ?: false
            if (taskId != null) {
                SubTaskScreen(
                    taskId,
                    navController,
                    subTaskViewModel,
                    taskViewModel,
                    notificationViewModel,
                    settingPreferences,
                    windowSize,
                    hasNotification
                )
            }
        }
        composable(NavigationItem.Setting.route) {
            SettingScreen(navController, settingViewModel)
        }
        composable(NavigationItem.Overview.route) {
            OverviewScreen(navController, taskViewModel, windowSize)
        }
        composable(NavigationItem.Notification.route) {
            NotificationScreen(notificationViewModel, navController)
        }
    }
}