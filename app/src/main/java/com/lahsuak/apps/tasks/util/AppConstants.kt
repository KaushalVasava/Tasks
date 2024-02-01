package com.lahsuak.apps.tasks.util

object AppConstants {
    const val UPDATE_REQUEST_CODE = 101
    //notification
    const val NOTIFICATION_CHANNEL_ID = "com.lahsuak.apps.mytask.notificationID"
    const val NOTIFICATION_CHANNEL_NAME = "Reminder"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Task reminder"
    const val NOTIFICATION_DAILY_CHANNEL_ID = "com.lahsuak.apps.mytask.notification_daily_id"
    const val NOTIFICATION_DAILY_CHANNEL_NAME = "Daily notification"
    const val NOTIFICATION_DAILY_CHANNEL_DESCRIPTION = "Daily task notification"

    //tables
    const val TASK_TABLE = "task_table"
    const val SUB_TASK_TABLE = "sub_task_table"
    const val NOTIFICATION_TABLE = "notification_table"

    const val TIME_FORMAT = "hh:mm a"
    const val DATE_FORMAT = "dd MM, yy"
    const val REMINDER_DATE_TIME_FORMAT = "dd MMM, yy hh:mm a"
    const val TOOLBAR_DATE_TIME_FORMAT = "E, dd LLLL"
    const val MARKET_PLACE_HOLDER = "market://details?id="
    const val SHARE_FORMAT = "text/plain"
    const val SORT_ORDER = "sort_order"
    const val SORT_ORDER2 = "sort_order2"
    const val SETTING = "SETTING_FOR_TASK_APP"
    const val DATABASE_NAME = "task_database"
    const val VIEW_TYPE = "VIEW_TYPE"
    const val SEARCH_QUERY = "searchQuery"
    const val SEARCH_INITIAL_VALUE = ""
    const val TASK_ID = "taskId"
    const val SEPARATOR = " - "

    const val SPLASH_SCREEN_TIME = 500L
    const val BACKUP_FILE_NAME = "Tasks.zip"
    const val ANY_MIME_TYPE = "*/*"
    const val BACKUP = "backup"
    const val RESTORE = "restore"
    const val INSTAGRAM_URL = "https://www.instagram.com/kaushalvasava_apps/"
    const val DONATION = "https://www.buymeacoffee.com/kaushal.developer"

    const val DEEP_LINK_SUBTASK  = "tasks://com.lahsuak.apps.tasks/subtaskscreen/"
    // Preferences key
    object SharedPreference {
        //theme
        const val THEME_KEY = "theme_key"
        const val DEFAULT_THEME = "-1"

        const val DEFAULT_LANGUAGE_VALUE = "-1"

        //default preference keys
        const val FONT_SIZE_KEY = "font_size"
        const val TASK_PROGRESS_KEY = "task_progress"
        const val SHOW_COPY_KEY = "show_copy"
        const val SHOW_SUBTASK_KEY = "show_subtask"
        const val SHOW_REMINDER_KEY = "show_reminder"
        const val SWIPE_GESTURE_KEY = "swipe_gesture_key"
        const val SHOW_VOICE_TASK_KEY = "show_voice_task_button"
        const val DEFAULT_FONT_SIZE = "18"

        //Daily notification
        const val DAILY_NOTIFICATION = "daily_notification"
        const val DAILY_NOTIFICATION_KEY = "daily_notification_key"
        const val LANGUAGE_KEY = "language"
        const val FINGERPRINT_KEY = "fingerprint"
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
    object BackUpRepo {
        const val ID = "ID"
        const val TITLE = "Title"
        const val SID = "SID"
        const val DATE_TIME = "Date time"
        const val COMPLETED = "Completed"
        const val IMP = "Imp"
        const val REMINDER = "Reminder"
        const val PROGRESS = "Progress"
        const val SUBTASKS = "SubTasks"
        const val COLOR = "Color"
        const val START_DATE = "StartDate"
        const val END_DATE = "EndDate"
        const val TASK_DIR = "tasks_dir"
        const val CSV_TASK_FILE_NAME = "tasks.csv"
        const val CSV_SUBTASK_FILE_NAME = "subtasks.csv"
    }

    /** The character used for escaping quotes.  */
    const val DEFAULT_ESCAPE_CHARACTER = '"'

    /** The default separator to use if none is supplied to the constructor.  */
    const val DEFAULT_SEPARATOR = ';'

    /**
     * The default quote character to use if none is supplied to the
     * constructor.
     */
    const val DEFAULT_QUOTE_CHARACTER = '"'

    /** The quote constant to use when you wish to suppress all quoting.  */
    const val NO_QUOTE_CHARACTER = '\u0000'

    /** The escape constant to use when you wish to suppress all escaping.  */
    const val NO_ESCAPE_CHARACTER = '\u0000'

    /** Default line terminator uses platform encoding.  */
    const val DEFAULT_LINE_END = "\n"
}