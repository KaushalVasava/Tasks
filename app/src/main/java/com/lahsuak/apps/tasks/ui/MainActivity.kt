package com.lahsuak.apps.tasks.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp.Companion.mylang
import com.lahsuak.apps.tasks.ui.navigation.TaskNavHost
import com.lahsuak.apps.tasks.ui.theme.TaskAppTheme
import com.lahsuak.apps.tasks.ui.viewmodel.NotificationViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppConstants.SHARE_FORMAT
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.THEME_DEFAULT
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.THEME_KEY
import com.lahsuak.apps.tasks.util.AppUtil.getLanguage
import com.lahsuak.apps.tasks.util.RuntimeLocaleChanger
import com.lahsuak.apps.tasks.util.rememberWindowSize
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
    private val subTaskViewModel: SubTaskViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()

    @Inject
    @Named(LANGUAGE_SHARED_PREFERENCE)
    lateinit var preference: SharedPreferences

    companion object {
        lateinit var activityContext: Context
        var shareTxt: String? = null
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
        activityContext = this
        setContent {

            val navController = rememberNavController()
            TaskAppTheme {
                SetupTransparentSystemUi(
                    systemUiController = rememberSystemUiController(),
                    actualBackgroundColor = MaterialTheme.colorScheme.surface
                )
                Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
                    TaskNavHost(
                        taskViewModel,
                        subTaskViewModel,
                        notificationViewModel,
                        navController,
                        fragmentManager = supportFragmentManager,
                        windowSize = rememberWindowSize(),
                    )
                }
            }
        }
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = sp.getString(THEME_KEY, THEME_DEFAULT)!!.toInt()

        AppCompatDelegate.setDefaultNightMode(selectedTheme)
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

    }

    @Composable
    internal fun SetupTransparentSystemUi(
        systemUiController: SystemUiController = rememberSystemUiController(),
        actualBackgroundColor: androidx.compose.ui.graphics.Color,
    ) {
        val minLuminanceForDarkIcons = .5f

        SideEffect {
            systemUiController.setStatusBarColor(
                color = actualBackgroundColor,
                darkIcons = actualBackgroundColor.luminance() > minLuminanceForDarkIcons
            )

            systemUiController.setNavigationBarColor(
                color = actualBackgroundColor,
                darkIcons = actualBackgroundColor.luminance() > minLuminanceForDarkIcons,
                navigationBarContrastEnforced = false
            )
        }
    }
}