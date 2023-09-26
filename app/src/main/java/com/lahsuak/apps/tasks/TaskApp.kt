package com.lahsuak.apps.tasks

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import com.lahsuak.apps.tasks.model.Category
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_DEFAULT_VALUE
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE_KEY
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY
import com.lahsuak.apps.tasks.util.AppUtil.createNotificationWorkRequest
import com.lahsuak.apps.tasks.util.RuntimeLocaleChanger
import com.lahsuak.apps.tasks.util.AppUtil.getLanguage
import com.lahsuak.apps.tasks.util.getAttribute
import com.lahsuak.apps.tasks.util.getColorCode
import dagger.hilt.android.HiltAndroidApp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.hours

@HiltAndroidApp
class TaskApp : Application() {
    companion object {
        var counter = 0
        var mylang = getLanguage()
        lateinit var appContext: Context
        val categoryTypes = mutableListOf<Category>()
    }

    @Inject
    @Named(AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE)
    lateinit var preference: SharedPreferences

    @Inject
    @Named(AppConstants.SharedPreference.DAILY_NOTIFICATION)
    lateinit var notificationPreference: SharedPreferences

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(RuntimeLocaleChanger.wrapContext(base, mylang))
    }

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
//        24-16 = 8
//        16-9 = 7
//        24 - 15 = 9+9
//        24 - 16 = 8+9
//        24 - 12 = 12+9
        // 15+24-6
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
        languageChange()
        initCategory()
    }

    private fun initCategory() {
        categoryTypes.add(
            Category(0, getString(R.string.home), appContext.getColorCode(R.color.light_blue))
        )
        categoryTypes.add(
            Category(
                1,
                getString(R.string.personal),
                appContext.getColorCode(R.color.light_green)
            )
        )
        categoryTypes.add(
            Category(
                2,
                getString(R.string.school),
                appContext.getColorCode(R.color.light_yellow)
            )
        )
        categoryTypes.add(
            Category(3, getString(R.string.work), appContext.getColorCode(R.color.light_pink))
        )
        categoryTypes.add(
            Category(4, getString(R.string.other), appContext.getColorCode(R.color.light_purple))
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        languageChange()
    }

    private fun languageChange() {
        val langNo = preference.getString(LANGUAGE_SHARED_PREFERENCE_KEY, LANGUAGE_DEFAULT_VALUE)
        if (langNo == LANGUAGE_DEFAULT_VALUE) {
            if (mylang != getLanguage()) {
                mylang = getLanguage()
            }
        } else {
            mylang = preference.getString(LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY, getLanguage())!!
        }
    }
}