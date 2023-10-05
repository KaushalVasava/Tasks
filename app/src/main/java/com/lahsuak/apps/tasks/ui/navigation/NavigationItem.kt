package com.lahsuak.apps.tasks.ui.navigation

import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.TASK
import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.SUBTASK
import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.SETTING
import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.ADD_UPDATE_TASK
import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.ADD_UPDATE_SUBTASK
import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.NOTIFICATION
import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.OVERVIEW

sealed class NavigationItem(val route: String) {
    object Task : NavigationItem(TASK)
    object SubTask : NavigationItem(SUBTASK)
    object AddUpdateTask : NavigationItem(ADD_UPDATE_TASK)
    object AddUpdateSubTask : NavigationItem(ADD_UPDATE_SUBTASK)
    object Overview : NavigationItem(OVERVIEW)
    object Setting : NavigationItem(SETTING)
    object Notification : NavigationItem(NOTIFICATION)
}