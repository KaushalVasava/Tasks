package com.lahsuak.apps.tasks.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

class LanguageUtil {
    companion object {
        fun Context.changeLocale(language: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.getSystemService(LocaleManager::class.java)
                    .applicationLocales = LocaleList(
                    Locale.forLanguageTag(language)
                )
            } else {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(language)
                )
            }
        }
    }
}
