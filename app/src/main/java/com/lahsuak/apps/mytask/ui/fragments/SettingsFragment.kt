package com.lahsuak.apps.mytask.ui.fragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.*
import com.lahsuak.apps.mytask.BuildConfig
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.util.Constants.THEME_DEFAULT
import com.lahsuak.apps.mytask.data.util.Constants.THEME_KEY
import com.lahsuak.apps.mytask.data.util.Util.appRating
import com.lahsuak.apps.mytask.data.util.Util.moreApp
import com.lahsuak.apps.mytask.data.util.Util.sendFeedbackMail
import com.lahsuak.apps.mytask.data.util.Util.shareApp

class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        var selectedTheme = -1
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

}