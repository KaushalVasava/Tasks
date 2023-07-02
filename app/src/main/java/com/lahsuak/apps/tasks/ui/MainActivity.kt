package com.lahsuak.apps.tasks.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp.Companion.mylang
import com.lahsuak.apps.tasks.databinding.ActivityMainBinding
import com.lahsuak.apps.tasks.util.AppConstants.SHARE_FORMAT
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY
import com.lahsuak.apps.tasks.util.AppConstants.THEME_DEFAULT
import com.lahsuak.apps.tasks.util.AppConstants.THEME_KEY
import com.lahsuak.apps.tasks.util.AppUtil.getLanguage
import com.lahsuak.apps.tasks.util.AppUtil.setClipboard
import com.lahsuak.apps.tasks.util.RuntimeLocaleChanger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var listener: NavController.OnDestinationChangedListener

    @Inject
    @Named(LANGUAGE_SHARED_PREFERENCE)
    lateinit var preference: SharedPreferences

    companion object {
        var shareTxt: String? = null
        var isWidgetClick = false
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(RuntimeLocaleChanger.wrapContext(base, mylang))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val lang = preference.getString(LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY, getLanguage())!!
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
        addDestinationChangeListener()
        setupActionBarWithNavController(navController)
    }

    private fun addDestinationChangeListener() {
        listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.taskFragment) {
                binding.toolbar.setNavigationIcon(R.drawable.ic_back)
            }
            when (destination.id) {
                R.id.taskFragment, R.id.subTaskFragment, R.id.renameFragmentDialog,
                R.id.shortcutFragmentDialog, R.id.deleteAllCompletedDialogFragment,
                R.id.deleteAllCompletedDialogFragment2 -> {
                    supportActionBar?.hide()
                }

                else -> {
                    supportActionBar?.show()
                }
            }
        }
        navController.addOnDestinationChangedListener(listener)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}