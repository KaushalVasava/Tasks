package com.lahsuak.apps.tasks.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.TaskApp.Companion.mylang
import com.lahsuak.apps.tasks.databinding.ActivityMainBinding
import com.lahsuak.apps.tasks.util.AppConstants.LANGUAGE_SHARED_PREFERENCE
import com.lahsuak.apps.tasks.util.AppConstants.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY
import com.lahsuak.apps.tasks.util.AppConstants.SHARE_FORMAT
import com.lahsuak.apps.tasks.util.AppConstants.THEME_DEFAULT
import com.lahsuak.apps.tasks.util.AppConstants.THEME_KEY
import com.lahsuak.apps.tasks.util.AppUtil.getLanguage
import com.lahsuak.apps.tasks.util.AppUtil.setClipboard
import com.lahsuak.apps.tasks.util.RuntimeLocaleChanger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!
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
        setTheme(R.style.Theme_Tasks)
        _binding = ActivityMainBinding.inflate(layoutInflater)
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
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.taskFragment) {
                binding.toolbar.setNavigationIcon(R.drawable.ic_back)
            }
        }
        navController.addOnDestinationChangedListener(listener)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}