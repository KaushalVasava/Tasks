package com.lahsuak.apps.mytask.ui.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.*
import com.lahsuak.apps.mytask.BuildConfig
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.util.Constants.LANGUAGE_DEFAULT_VALUE
import com.lahsuak.apps.mytask.data.util.Constants.LANGUAGE_SHARED_PREFERENCE
import com.lahsuak.apps.mytask.data.util.Constants.LANGUAGE_SHARED_PREFERENCE_KEY
import com.lahsuak.apps.mytask.data.util.Constants.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY
import com.lahsuak.apps.mytask.data.util.Constants.THEME_DEFAULT
import com.lahsuak.apps.mytask.data.util.Constants.THEME_KEY
import com.lahsuak.apps.mytask.data.util.Util.appRating
import com.lahsuak.apps.mytask.data.util.Util.getLanguage
import com.lahsuak.apps.mytask.data.util.Util.moreApp
import com.lahsuak.apps.mytask.data.util.Util.sendFeedbackMail
import com.lahsuak.apps.mytask.data.util.Util.shareApp
import com.lahsuak.apps.mytask.di.TodoApp.Companion.mylang
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        var selectedTheme = -1
        var selectedLang = LANGUAGE_DEFAULT_VALUE
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        selectedTheme = sp.getString(THEME_KEY, THEME_DEFAULT)!!.toInt()

        val prefFeedback = findPreference<Preference>("feedback")
        val prefShare = findPreference<Preference>("share")
        val prefMoreApp = findPreference<Preference>("more_app")
        val prefVersion = findPreference<Preference>("app_version")
        val prefRating = findPreference<Preference>("rating")
        val prefFont = findPreference<ListPreference>("font_size")
        val prefTheme = findPreference<ListPreference>("theme_key")
        val prefLanguage = findPreference<ListPreference>("language")

        val pref = requireContext().getSharedPreferences(LANGUAGE_SHARED_PREFERENCE,
            Context.MODE_PRIVATE
        )
        selectedLang =
            pref.getString(LANGUAGE_SHARED_PREFERENCE_KEY, LANGUAGE_DEFAULT_VALUE)
                ?: Locale.getDefault().language

        prefVersion!!.summary = BuildConfig.VERSION_NAME
        val prefManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val txtSize = prefManager.getString("font_size", "18").toString().toInt()
        when (txtSize) {
            16 -> prefFont?.summary = "Small"
            18 -> prefFont?.summary = "Medium"
            20 -> prefFont?.summary = "Large"
            22 -> prefFont?.summary = "Huge"
            //  else -> prefFont.summary = "Medium"
        }

        prefFont?.setOnPreferenceChangeListener { _, newValue ->
            when ((newValue as String).toInt()) {
                16 -> prefFont.summary = "Small"
                18 -> prefFont.summary = "Medium"
                20 -> prefFont.summary = "Large"
                22 -> prefFont.summary = "Huge"
            }
            true
        }
        prefLanguage?.setOnPreferenceChangeListener { _, newValue ->
            selectedLang = newValue as String
            when (selectedLang) {
                LANGUAGE_DEFAULT_VALUE -> {
                    setLocal(getLanguage())
                }
                else -> {
                    setLocal(selectedLang)
                }
            }
            true
        }
        prefFeedback?.setOnPreferenceClickListener {
            sendFeedbackMail(requireContext())
            true
        }
        prefShare?.setOnPreferenceClickListener {
            shareApp(requireContext())
            true
        }
        prefMoreApp?.setOnPreferenceClickListener {
            moreApp(requireContext())
            true
        }
        prefRating?.setOnPreferenceClickListener {
            appRating(requireContext())
            true
        }
        prefTheme?.setOnPreferenceChangeListener { _, newValue ->
            selectedTheme = (newValue as String).toInt()
            setTheme()
            true
        }
    }

    private fun setTheme() {
        when (selectedTheme) {
            -1 -> MODE_NIGHT_FOLLOW_SYSTEM //system theme
            1 -> MODE_NIGHT_NO
            2 -> MODE_NIGHT_YES //dark theme
        }
        setDefaultNightMode(selectedTheme)
        if (selectedTheme == 2) {
            WindowInsetsControllerCompat(
                requireActivity().window,
                requireActivity().window.decorView
            ).isAppearanceLightStatusBars =
                false
            WindowInsetsControllerCompat(
                requireActivity().window,
                requireActivity().window.decorView
            ).isAppearanceLightNavigationBars =
                false
        }
    }

    private fun setLocal(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            config.setLocale(locale)
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            requireActivity().applicationContext.createConfigurationContext(config)
        } else {
            @Suppress("deprecation")
            resources.updateConfiguration(
                config, resources.displayMetrics
            )
        }
        val pref =
            requireContext().getSharedPreferences(LANGUAGE_SHARED_PREFERENCE, Context.MODE_PRIVATE)
                .edit()
        pref.putString(LANGUAGE_SHARED_PREFERENCE_KEY, selectedLang)
        pref.putString(LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY, lang)
        mylang = lang
        pref.apply()
        requireActivity().recreate()
    }
}