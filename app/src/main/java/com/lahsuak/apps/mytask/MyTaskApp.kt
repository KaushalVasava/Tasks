package com.lahsuak.apps.mytask

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.lahsuak.apps.mytask.util.Constants.LANGUAGE_DEFAULT_VALUE
import com.lahsuak.apps.mytask.util.Constants.LANGUAGE_SHARED_PREFERENCE
import com.lahsuak.apps.mytask.util.Constants.LANGUAGE_SHARED_PREFERENCE_KEY
import com.lahsuak.apps.mytask.util.Constants.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY
import com.lahsuak.apps.mytask.util.RuntimeLocaleChanger
import com.lahsuak.apps.mytask.util.Util.getLanguage
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyTaskApp : Application() {
    companion object {
        var mylang = getLanguage()
        lateinit var appContext: Context
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(RuntimeLocaleChanger.wrapContext(base, mylang))
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        languageChange()
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