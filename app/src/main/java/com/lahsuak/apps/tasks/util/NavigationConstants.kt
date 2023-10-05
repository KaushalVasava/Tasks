package com.lahsuak.apps.tasks.util

object NavigationConstants {
    object Screen {
        const val TASK = "task"
        const val SUBTASK = "subtask"
        const val ADD_UPDATE_TASK = "add_update_task"
        const val ADD_UPDATE_SUBTASK = "add_update_subtask"
        const val OVERVIEW = "overview"
        const val SETTING = "setting"
        const val NOTIFICATION = "notification"
    }

    object Key {
        const val TASK_ID = "task_id"
        const val SUBTASK_ID = "subTaskId"
        const val HAS_NOTIFICATION = "has_notification"

        const val SUBTASK_DEEP_LINK = "myapp://kmv.com/subtaskscreen/{task_id}/{has_notification}"
        const val IS_NEW_TASK = "is_new_task"
        const val SHARED_TASK = "shared_text"

        const val ADD_UPDATE_TASK_DEEP_LINK = "myapp://kmv.com/shortcut/true"
    }

}