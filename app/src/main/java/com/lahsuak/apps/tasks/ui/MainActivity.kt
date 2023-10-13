package com.lahsuak.apps.tasks.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
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
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.TaskApp.Companion.mylang
import com.lahsuak.apps.tasks.ui.navigation.TaskNavHost
import com.lahsuak.apps.tasks.ui.theme.TaskAppTheme
import com.lahsuak.apps.tasks.ui.viewmodel.NotificationViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppConstants.SHARE_FORMAT
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.BIOMETRIC_ENABLE_KEY
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.THEME_DEFAULT
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.THEME_KEY
import com.lahsuak.apps.tasks.util.AppConstants.UPDATE_REQUEST_CODE
import com.lahsuak.apps.tasks.util.AppUtil.getLanguage
import com.lahsuak.apps.tasks.util.RuntimeLocaleChanger
import com.lahsuak.apps.tasks.util.biometric.BiometricAuthListener
import com.lahsuak.apps.tasks.util.biometric.BiometricUtil
import com.lahsuak.apps.tasks.util.rememberWindowSize
import com.lahsuak.apps.tasks.util.toast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : AppCompatActivity() ,BiometricAuthListener{
    private val taskViewModel: TaskViewModel by viewModels()
    private val subTaskViewModel: SubTaskViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()
    private lateinit var appUpdateManager: AppUpdateManager

    @Inject
    @Named(LANGUAGE_SHARED_PREFERENCE)
    lateinit var preference: SharedPreferences
    @Inject
    @Named(AppConstants.SharedPreference.BIOMETRIC_SHARED_PREFERENCE)
    lateinit var bioMetricPreference: SharedPreferences

    companion object {
        var activityContext: Context? = null
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
        if(bioMetricPreference.getBoolean(BIOMETRIC_ENABLE_KEY, false)){
            Log.d("TAG", "onCreate: open biometric")
            BiometricUtil.showBiometricPrompt(
                activity = this,
                listener = this
            )
        }
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkUpdate()
        appUpdateManager.registerListener(appUpdateListener)

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
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = sharedPreference.getString(THEME_KEY, THEME_DEFAULT)!!.toInt()

        AppCompatDelegate.setDefaultNightMode(selectedTheme)
        if (intent?.action == Intent.ACTION_SEND) {
            if (SHARE_FORMAT == intent.type) {
                shareTxt = intent.getStringExtra(Intent.EXTRA_TEXT)
            }
        }
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
    private fun checkUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo, AppUpdateType.FLEXIBLE,
                        this, UPDATE_REQUEST_CODE
                    )
                } catch (exception: IntentSender.SendIntentException) {
                    toast { exception.message.toString() }
                }
            }
        }
    }

    private val appUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            val view = findViewById<View>(R.id.my_container)
            Snackbar.make(
                view,
                getString(R.string.new_app_ready),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.restart)) {
                appUpdateManager.completeUpdate()
            }.show()
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress(AppConstants.DEPRECATION)
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        if (requestCode == UPDATE_REQUEST_CODE) {
            toast { getString(R.string.downloading_start) }
            if (resultCode != Activity.RESULT_OK) {
                TaskApp.appContext.toast { getString(R.string.update_failed) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(appUpdateListener)
        activityContext = null
    }

    override fun onBiometricAuthSuccess() {
       //Successful
    }

    override fun onUserCancelled() {
        finish()
    }

    override fun onErrorOccurred() {
        finish()
    }
}