package com.lahsuak.apps.mytask

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.lahsuak.apps.mytask.model.Category
import com.lahsuak.apps.mytask.util.AppConstants.LANGUAGE_DEFAULT_VALUE
import com.lahsuak.apps.mytask.util.AppConstants.LANGUAGE_SHARED_PREFERENCE
import com.lahsuak.apps.mytask.util.AppConstants.LANGUAGE_SHARED_PREFERENCE_KEY
import com.lahsuak.apps.mytask.util.AppConstants.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY
import com.lahsuak.apps.mytask.util.RuntimeLocaleChanger
import com.lahsuak.apps.mytask.util.Util.getLanguage
import com.lahsuak.apps.mytask.util.getColorCode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyTaskApp : Application() {
    companion object {
        var mylang = getLanguage()
        lateinit var appContext: Context
        val categoryTypes = mutableListOf<Category>()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(RuntimeLocaleChanger.wrapContext(base, mylang))
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        languageChange()
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

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            DynamicColors.applyToActivitiesIfAvailable(this)
//        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        languageChange()
    }

    private fun languageChange() {
        val pref = getSharedPreferences(LANGUAGE_SHARED_PREFERENCE, MODE_PRIVATE)
        val langNo = pref.getString(LANGUAGE_SHARED_PREFERENCE_KEY, LANGUAGE_DEFAULT_VALUE)
        if (langNo == LANGUAGE_DEFAULT_VALUE) {
            if (mylang != getLanguage()) {
                mylang = getLanguage()
            }
        } else {
            mylang = pref.getString(LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY, getLanguage())!!
        }
    }
}