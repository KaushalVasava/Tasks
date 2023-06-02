package com.lahsuak.apps.mytask.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {
    fun getTaskDateTime(millisecond: Long, isGridView: Boolean): String {
        val currentMillis = System.currentTimeMillis()
        val diff = (currentMillis - millisecond) / 3600000
        val sdf = if (diff <= 24) {
            SimpleDateFormat(AppConstants.TIME_FORMAT, Locale.getDefault())
        } else {
            SimpleDateFormat(AppConstants.DATE_FORMAT, Locale.getDefault())
        }
        return sdf.format(millisecond)
    }

    fun getReminderDateTime(timeInMillis: Long): String {
        val sdf = SimpleDateFormat(AppConstants.REMINDER_DATE_TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

    fun getToolbarDateTime(timeInMillis: Long): String {
        val sdf = SimpleDateFormat(AppConstants.TOOLBAR_DATE_TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

    fun getTimeDiff(timeInMillis: Long): Long {
        val systemTimeInMillis = System.currentTimeMillis()
        return timeInMillis - systemTimeInMillis
    }
}