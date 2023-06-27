package com.lahsuak.apps.tasks.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {
    fun getTaskDateTime(millisecond: Long): String {
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

    fun getDate(date: Long): String {
        val sdf = SimpleDateFormat(AppConstants.REMINDER_DATE_TIME_FORMAT, Locale.getDefault())
        return sdf.format(date)
    }

    fun getDateRange(date1: Long, date2: Long?): String {
        val sdf = SimpleDateFormat(AppConstants.DATE_FORMAT, Locale.getDefault())
        val d1 = sdf.format(date1)
        return if (date2 == null) {
            d1
        } else {
            d1 + AppConstants.SEPARATOR + sdf.format(date2)
        }
    }
}