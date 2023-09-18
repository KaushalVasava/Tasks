package com.lahsuak.apps.tasks.ui.navigation

enum class Screen {
    TASK,
    SUBTASK,
    ADD_UPDATE_TASK,
    ADD_UPDATE_SUBTASK,
    OVERVIEW,
    SETTING,
    NOTIFICATION
}

sealed class NavigationItem(val route: String) {
    object Task : NavigationItem(Screen.TASK.name)
    object SubTask : NavigationItem(Screen.SUBTASK.name)
    object AddUpdateTask : NavigationItem(Screen.ADD_UPDATE_TASK.name)
    object AddUpdateSubTask : NavigationItem(Screen.ADD_UPDATE_SUBTASK.name)
    object Overview : NavigationItem(Screen.OVERVIEW.name)
    object Setting : NavigationItem(Screen.SETTING.name)
    object Notification : NavigationItem(Screen.NOTIFICATION.name)
}