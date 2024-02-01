package com.lahsuak.apps.tasks.util.preference

data class SettingPreferences(
    val theme: String,
    val fontSize: String,
    val swipeGestureEnable: Boolean,
    val showVoiceIcon: Boolean,
    val showCopyIcon: Boolean,
    val showProgress: Boolean,
    val showReminder: Boolean,
    val showSubTask: Boolean,
    val fingerPrintEnable: Boolean,
    val language: String
)