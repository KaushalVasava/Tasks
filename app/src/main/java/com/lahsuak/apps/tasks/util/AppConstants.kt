package com.lahsuak.apps.tasks.util

object AppConstants {
    //notfiication
    const val NOTIFICATION_CHANNEL_ID = "com.lahsuak.apps.mytask.notificationID"
    const val NOTIFICATION_CHANNEL_NAME = "Reminder"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Task reminder"
    const val NOTIFICATION_DAILY_CHANNEL_ID = "com.lahsuak.apps.mytask.notification_daily_id"
    const val NOTIFICATION_DAILY_CHANNEL_NAME = "Daily notification"
    const val NOTIFICATION_DAILY_CHANNEL_DESCRIPTION = "Daily task notification"

    //website
    const val WEBSITE = "https://zaap.bio/KaushalVasava"

    const val TIME_FORMAT = "hh:mm a"
    const val DATE_FORMAT = "dd MM, yy"
    const val REMINDER_DATE_TIME_FORMAT = "dd MMM, yy hh:mm a"
    const val TOOLBAR_DATE_TIME_FORMAT = "E, dd LLLL"
    const val MARKET_PLACE_HOLDER = "market://details?name="
    const val SHARE_FORMAT = "text/plain"
    const val SORT_ORDER = "sort_order"
    const val SORT_ORDER2 = "sort_order2"
    const val HIDE_COMPLETED = "hide_completed"
    const val HIDE_COMPLETED2 = "hide_completed2"
    const val SETTING = "SETTING_FOR_TASK_APP"
    const val DATABASE_NAME = "task_database"
    const val VIEW_TYPE = "VIEW_TYPE"
    const val SEARCH_QUERY = "searchQuery"
    const val SEARCH_INITIAL_VALUE = ""
    const val TASK_ID = "taskId"
    const val UPDATE_REQUEST_CODE = 123
    const val SEPARATOR = " - "
    const val PACKAGE = "package"
    const val DEPRECATION = "deprecation"
    const val MAX_COUNTER_FOR_MORE_APPS = 5
    const val MORE_APPS_DELAY = 10000L
    const val INVALID_ID = -1

    // Preferences key
    object SharedPreference {
        //theme
        const val THEME_KEY = "theme_key"
        const val THEME_DEFAULT = "-1"

        //language
        const val LANGUAGE_SHARED_PREFERENCE = "LANGUAGE"
        const val LANGUAGE_SHARED_PREFERENCE_KEY = "selectedLang"
        const val LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY = "Language"
        const val LANGUAGE_DEFAULT_VALUE = "-1"

        //default preference keys
        const val FONT_SIZE_KEY = "font_size"
        const val TASK_PROGRESS_KEY = "task_progress"
        const val SHOW_SUBTASK_KEY = "show_subtask"
        const val SHOW_REMINDER_KEY = "show_reminder"
        const val SHOW_VOICE_TASK_KEY = "show_voice_task_button"
        const val REM_KEY = "rem_key"
        const val INITIAL_FONT_SIZE = "18"

        //Daily notification
        const val DAILY_NOTIFICATION = "daily_notification"
        const val DAILY_NOTIFICATION_KEY = "daily_notification_key"
    }

    object WorkManager {
        const val ID_KEY = "id"
        const val MESSAGE_KEY = "message"
        const val PARENT_TITLE_KEY = "parent_title_key"
        const val STATUS_KEY = "status"
        const val START_DATE_KEY = "start_date"
        const val END_DATE_KEY = "end_date"
        const val DAILY_TITLE_KEY = "daily_title_key"
        const val DAILY_MSG_KEY = "daily_msg_key"
    }
}