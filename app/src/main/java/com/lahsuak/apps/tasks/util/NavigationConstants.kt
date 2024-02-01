package com.lahsuak.apps.tasks.util

object NavigationConstants {
    object Screen {
        const val TASK = "task"
        const val SUBTASK = "subtask"
        const val OVERVIEW = "overview"
        const val SETTING = "setting"
        const val NOTIFICATION = "notification"
    }

    object Key {
        const val TASK_ID = "task_id"
        const val HAS_NOTIFICATION = "has_notification"

        const val SUBTASK_DEEP_LINK = "tasks://com.lahsuak.apps.tasks/subtaskscreen/{task_id}/{has_notification}"

        const val ADD_UPDATE_TASK_DEEP_LINK = "tasks://com.lahsuak.apps.tasks/edittask/true"
    }

}