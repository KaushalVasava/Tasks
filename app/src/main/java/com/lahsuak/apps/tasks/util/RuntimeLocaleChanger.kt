package com.lahsuak.apps.tasks.util

import android.content.Context
import android.content.res.Configuration
import java.util.*

object RuntimeLocaleChanger {

    fun wrapContext(context: Context,lang:String): Context {

        val savedLocale = Locale(lang) // load the user language from SharedPreferences

        // as part of creating a new context that contains the new locale we also need to override the default locale.
        Locale.setDefault(savedLocale)

        // create new configuration with the saved locale
        val newConfig = Configuration()
        newConfig.setLocale(savedLocale)

        return context.createConfigurationContext(newConfig)
    }

    fun overrideLocale(context: Context,lang: String) {
        val savedLocale = Locale(lang) // load the user language from SharedPreferences
        // as part of creating a new context that contains the new locale we also need to override the default locale.
        Locale.setDefault(savedLocale)

        // create new configuration with the saved locale
        val newConfig = Configuration()
        newConfig.setLocale(savedLocale)

        // override the locale on the given context (Activity, Fragment, etc...)
        context.createConfigurationContext(newConfig)

        // override the locale on the application context
        if (context != context.applicationContext) {
            context.applicationContext.run { createConfigurationContext(newConfig) }
        }
    }
}