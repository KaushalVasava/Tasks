package com.lahsuak.apps.tasks.ui.fragments.settings

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.*
import com.lahsuak.apps.tasks.BuildConfig
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp.Companion.mylang
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_DEFAULT_VALUE
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE_KEY
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY
import com.lahsuak.apps.tasks.util.AppConstants.THEME_DEFAULT
import com.lahsuak.apps.tasks.util.AppConstants.THEME_KEY
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.AppUtil.appRating
import com.lahsuak.apps.tasks.util.AppUtil.getLanguage
import com.lahsuak.apps.tasks.util.AppUtil.shareApp
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        var selectedTheme = -1
        var selectedLang = LANGUAGE_DEFAULT_VALUE
    }

    @Inject
    @Named(LANGUAGE_SHARED_PREFERENCE)
    lateinit var preference: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.show()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)
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
        val prefDeveloper = findPreference<Preference>("developer")

        selectedLang =
            preference.getString(LANGUAGE_SHARED_PREFERENCE_KEY, LANGUAGE_DEFAULT_VALUE)
                ?: Locale.getDefault().language

        prefVersion!!.summary = BuildConfig.VERSION_NAME
        val prefManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        when (prefManager.getString(
            AppConstants.SharedPreference.FONT_SIZE_KEY,
            AppConstants.SharedPreference.INITIAL_FONT_SIZE
        ).toString().toInt()
        ) {
            12 -> prefFont?.summary = getString(R.string.very_small)
            14 -> prefFont?.summary = getString(R.string.medium_small)
            16 -> prefFont?.summary = getString(R.string.small)
            18 -> prefFont?.summary = getString(R.string.medium)
            20 -> prefFont?.summary = getString(R.string.large)
            22 -> prefFont?.summary = getString(R.string.huge)
        }

        prefFont?.setOnPreferenceChangeListener { _, newValue ->
            when ((newValue as String).toInt()) {
                12 -> prefFont.summary = getString(R.string.very_small)
                14 -> prefFont.summary = getString(R.string.medium_small)
                16 -> prefFont.summary = getString(R.string.small)
                18 -> prefFont.summary = getString(R.string.medium)
                20 -> prefFont.summary = getString(R.string.large)
                22 -> prefFont.summary = getString(R.string.huge)
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
            AppUtil.openWebsite(context, AppConstants.WEBSITE)
            true
        }
        prefShare?.setOnPreferenceClickListener {
            shareApp(requireContext())
            true
        }
        prefMoreApp?.setOnPreferenceClickListener {
            AppUtil.openMoreApp(requireContext())
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
        prefDeveloper?.setOnPreferenceClickListener {
            AppUtil.openWebsite(context, AppConstants.WEBSITE)
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
            @Suppress(AppConstants.DEPRECATION)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
        preference.edit().apply {
            putString(LANGUAGE_SHARED_PREFERENCE_KEY, selectedLang)
            putString(LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY, lang)
            mylang = lang
            apply()
        }
        requireActivity().recreate()
    }
}