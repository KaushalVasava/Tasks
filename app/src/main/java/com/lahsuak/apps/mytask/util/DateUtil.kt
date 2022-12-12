package com.lahsuak.apps.mytask.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {
    fun getTaskDateTime(millisecond: Long, isGridView: Boolean): String {
        val currentMillis = System.currentTimeMillis()
        val diff = (currentMillis - millisecond) / 3600000
        val sdf = if (diff <= 24) {
            SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault())
        } else {
            if (isGridView) {
                SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())
            } else {
                SimpleDateFormat(Constants.DATE_FORMAT_VERTICAL, Locale.getDefault())
            }
        }
        return sdf.format(millisecond)
    }

    fun getReminderDateTime(timeInMillis: Long): String {
        val sdf = SimpleDateFormat(Constants.REMINDER_DATE_TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

    fun getToolbarDateTime(timeInMillis: Long): String {
        val sdf = SimpleDateFormat(Constants.TOOLBAR_DATE_TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

    fun getMillis(time: String): Long {
        val date = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
        val d = date.parse(time)
        Log.d("TAG", "getMillis: ${d?.time} $time")
        return d?.time ?: 0L
    }

    fun getTimeDiff(timeInMillis: Long): Long {
        val systemTimeInMillis = System.currentTimeMillis()
        return timeInMillis - systemTimeInMillis
    }
}