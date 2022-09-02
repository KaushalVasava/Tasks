package com.lahsuak.apps.mytask.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.util.Constants.LANGUAGE_SHARED_PREFERENCE
import com.lahsuak.apps.mytask.util.Constants.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY
import com.lahsuak.apps.mytask.util.Constants.SHARE_FORMAT
import com.lahsuak.apps.mytask.util.Constants.THEME_DEFAULT
import com.lahsuak.apps.mytask.util.Constants.THEME_KEY
import com.lahsuak.apps.mytask.util.RuntimeLocaleChanger
import com.lahsuak.apps.mytask.util.Util.getLanguage
import com.lahsuak.apps.mytask.util.Util.setClipboard
import com.lahsuak.apps.mytask.databinding.ActivityMainBinding
import com.lahsuak.apps.mytask.di.TodoApp.Companion.mylang
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        var shareTxt: String? = null
        var isWidgetClick = false
    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(RuntimeLocaleChanger.wrapContext(base, mylang))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val pref = getSharedPreferences(LANGUAGE_SHARED_PREFERENCE, MODE_PRIVATE)
        val lang = pref.getString(LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY, getLanguage())!!
        RuntimeLocaleChanger.overrideLocale(this, lang)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MyTask)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = sp.getString(THEME_KEY, THEME_DEFAULT)!!.toInt()

        AppCompatDelegate.setDefaultNightMode(selectedTheme)

        //shared text received from other apps
        if (intent?.action == Intent.ACTION_SEND) {
            if (SHARE_FORMAT == intent.type) {
                shareTxt = intent.getStringExtra(Intent.EXTRA_TEXT)
            }
        }

        //this is for transparent status bar and navigation bar
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            true
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars =
            true
        setSupportActionBar(binding.toolbar)

        binding.toolbar.setOnLongClickListener {
            if (navController.currentDestination?.id == R.id.subTaskFragment) {
                setClipboard(this, binding.toolbar.title.toString())
            }
            true
        }

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.my_container) as NavHostFragment)
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}