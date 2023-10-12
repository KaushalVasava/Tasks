package com.lahsuak.apps.tasks.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.lahsuak.apps.tasks.ui.screens.NotificationScreen
import com.lahsuak.apps.tasks.ui.screens.OverviewScreen
import com.lahsuak.apps.tasks.ui.screens.SettingScreen
import com.lahsuak.apps.tasks.ui.screens.SubTaskScreen
import com.lahsuak.apps.tasks.ui.screens.TaskScreen
import com.lahsuak.apps.tasks.ui.screens.dialog.AddUpdateSubTaskScreen
import com.lahsuak.apps.tasks.ui.screens.dialog.AddUpdateTaskScreen
import com.lahsuak.apps.tasks.ui.viewmodel.NotificationViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.ADD_UPDATE_TASK_DEEP_LINK
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.HAS_NOTIFICATION
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.IS_NEW_TASK
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.SHARED_TASK
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.SUBTASK_DEEP_LINK
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.SUBTASK_ID
import com.lahsuak.apps.tasks.util.NavigationConstants.Key.TASK_ID
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
        composable(NavigationItem.Task.route) {
            TaskScreen(
                navController,
                taskViewModel,
                windowSize,
                fragmentManager
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
            OverviewScreen(navController, taskViewModel, windowSize)
        }
        composable(
            "${NavigationItem.AddUpdateTask.route}?$TASK_ID={$TASK_ID}/{$IS_NEW_TASK}?$SHARED_TASK={$SHARED_TASK}",
            arguments = listOf(
                navArgument(TASK_ID) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(IS_NEW_TASK) {
                    type = NavType.BoolType
                    defaultValue = true
                },
                navArgument(SHARED_TASK) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
            deepLinks = listOf(navDeepLink {
                uriPattern = ADD_UPDATE_TASK_DEEP_LINK
            })
        ) { navBackStackEntry ->
            val taskId = navBackStackEntry.arguments?.getString(TASK_ID)
            val isNewTask = navBackStackEntry.arguments?.getBoolean(IS_NEW_TASK) ?: true
            val sharedText = navBackStackEntry.arguments?.getString(SHARED_TASK)
            val sheetState = androidx.compose.material.rememberModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden
            )
            AddUpdateTaskScreen(
                sheetState,
                taskViewModel,
                isNewTask,
                taskId,
                fragmentManager,
                sharedText
            ) {}
        }
        composable(NavigationItem.Notification.route) {
            NotificationScreen(notificationViewModel, navController)
        }

        composable(
            "${NavigationItem.AddUpdateSubTask.route}?$SUBTASK_ID={$SUBTASK_ID}/{$TASK_ID}/{$IS_NEW_TASK}?$SHARED_TASK={$SHARED_TASK}",
            arguments = listOf(
                navArgument(TASK_ID) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(IS_NEW_TASK) {
                    type = NavType.BoolType
                    defaultValue = true
                },
                navArgument(SHARED_TASK) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            )
        ) { navBackStackEntry ->
            val subTaskId = navBackStackEntry.arguments?.getString(SUBTASK_ID)
            val taskId = navBackStackEntry.arguments?.getString(TASK_ID)
            val isNewTask = navBackStackEntry.arguments?.getBoolean(IS_NEW_TASK) ?: true
            val sharedText = navBackStackEntry.arguments?.getString(SHARED_TASK)
            if (taskId != null) {
                AddUpdateSubTaskScreen(
                    null,
                    taskId.toInt(),
                    subTaskId,
                    isNewTask,
                    subTaskViewModel,
                    fragmentManager,
                    sharedText,
                ) {}
            }
        }
    }
}