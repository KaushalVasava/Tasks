package com.lahsuak.apps.tasks

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.lahsuak.apps.tasks.model.Category
import com.lahsuak.apps.tasks.ui.theme.lightBlue
import com.lahsuak.apps.tasks.ui.theme.lightGreen
import com.lahsuak.apps.tasks.ui.theme.lightPink
import com.lahsuak.apps.tasks.ui.theme.lightPurple
import com.lahsuak.apps.tasks.ui.theme.lightYellow
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppUtil.createNotificationWorkRequest
import dagger.hilt.android.HiltAndroidApp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidApp
class TaskApp : Application() {
    companion object {
        lateinit var appContext: Context
        val categoryTypes = mutableListOf<Category>()
    }

    @Inject
    @Named(AppConstants.SharedPreference.DAILY_NOTIFICATION)
    lateinit var notificationPreference: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        appContext = this
        val isNotificationSent = notificationPreference.getBoolean(
            AppConstants.SharedPreference.DAILY_NOTIFICATION_KEY,
            false
        )

        val mCalendar = Calendar.getInstance()
        val formatter = SimpleDateFormat(AppConstants.TIME_FORMAT, Locale.getDefault())
        var hour = formatter.format(mCalendar.time).substring(0, 2).trim().toInt()
        val isAm = formatter.format(mCalendar.time).substring(6).trim().lowercase()
        if (isAm == getString(R.string.pm_format))
            hour += 12
        val startDelay = 24 - hour + 9 // 9 for 9 am notification

        if (!isNotificationSent) {
            createNotificationWorkRequest(
                startDelay.toLong(), this,
                getString(R.string.good_morning),
                getString(R.string.notification_daily_desc)
            )
            notificationPreference
                .edit()
                .putBoolean(AppConstants.SharedPreference.DAILY_NOTIFICATION_KEY, true)
                .apply()
        }
        initCategory()
    }

    private fun initCategory() {
        categoryTypes.add(
            Category(0, getString(R.string.home),lightBlue.hashCode())
        )
        categoryTypes.add(
            Category(1, getString(R.string.personal), lightGreen.hashCode())
        )
        categoryTypes.add(
            Category(2, getString(R.string.school), lightYellow.hashCode())
        )
        categoryTypes.add(
            Category(3, getString(R.string.work), lightPink.hashCode())
        )
        categoryTypes.add(
            Category(4, getString(R.string.other), lightPurple.hashCode())
        )
    }
}