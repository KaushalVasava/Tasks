package com.lahsuak.apps.tasks.ui.navigation

import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.NOTIFICATION
import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.OVERVIEW
import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.SETTING
import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.SUBTASK
import com.lahsuak.apps.tasks.util.NavigationConstants.Screen.TASK

sealed class NavigationItem(val route: String) {
    object Task : NavigationItem(TASK)
    object SubTask : NavigationItem(SUBTASK)
    object Overview : NavigationItem(OVERVIEW)
    object Setting : NavigationItem(SETTING)
    object Notification : NavigationItem(NOTIFICATION)
}