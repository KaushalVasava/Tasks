package com.lahsuak.apps.tasks.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.ui.navigation.TaskNavHost
import com.lahsuak.apps.tasks.ui.theme.TaskAppTheme
import com.lahsuak.apps.tasks.ui.viewmodel.MainViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.NotificationViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.SettingsViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppConstants.SHARE_FORMAT
import com.lahsuak.apps.tasks.util.AppConstants.UPDATE_REQUEST_CODE
import com.lahsuak.apps.tasks.util.biometric.BiometricAuthListener
import com.lahsuak.apps.tasks.util.biometric.BiometricUtil
import com.lahsuak.apps.tasks.util.preference.SettingPreferences
import com.lahsuak.apps.tasks.util.rememberWindowSize
import com.lahsuak.apps.tasks.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()
    private val subTaskViewModel: SubTaskViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()
    private val settingViewModel: SettingsViewModel by viewModels()
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var view: View
    private var reviewInfo: ReviewInfo? = null
    private lateinit var reviewManager: ReviewManager

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // handle callback
        if (result.data == null) return@registerForActivityResult
        if (result.resultCode == UPDATE_REQUEST_CODE) {
            toast { getString(R.string.downloading_start) }
            if (result.resultCode != Activity.RESULT_OK) {
                TaskApp.appContext.toast { getString(R.string.update_failed) }
            }
        }
    }

    private val updateResultStarter =
        IntentSenderForResultStarter { intent, _, fillInIntent, flagsMask, flagsValues, _, _ ->
            val request = IntentSenderRequest.Builder(intent)
                .setFillInIntent(fillInIntent)
                .setFlags(flagsValues, flagsMask)
                .build()

            updateLauncher.launch(request)
        }

    companion object {
        var activityContext: Context? = null
        var shareTxt: String? = null
    }

    private val appUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Snackbar.make(
                view,
                getString(R.string.new_app_ready),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.restart)) {
                appUpdateManager.completeUpdate()
            }.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Tasks)
        activityContext = this
        activateReviewInfo()

        observePreferences()
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkUpdate()
        appUpdateManager.registerListener(appUpdateListener)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEnabled) {
                    isEnabled = false
                    startReviewFlow()
                }
            }
        })

        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                RequestPermission()
            }
            view = LocalView.current
            val navController = rememberNavController()
            TaskAppTheme {
                SetupTransparentSystemUi(
                    systemUiController = rememberSystemUiController(),
                    actualBackgroundColor = MaterialTheme.colorScheme.surface
                )
                val isScreenLoaded by mainViewModel.isScreenLoaded.collectAsState()
                val settingsPreferences by settingViewModel.preferencesFlow.collectAsState(
                    initial = SettingPreferences(
                        theme = AppConstants.SharedPreference.DEFAULT_THEME,
                        fontSize = AppConstants.SharedPreference.DEFAULT_FONT_SIZE,
                        swipeGestureEnable = true,
                        showVoiceIcon = true,
                        showCopyIcon = true,
                        showProgress = false,
                        showReminder = true,
                        showSubTask = true,
                        fingerPrintEnable = false,
                        language = AppConstants.SharedPreference.DEFAULT_LANGUAGE_VALUE
                    )
                )
                Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
                    if (isScreenLoaded) {
                        TaskNavHost(
                            taskViewModel,
                            subTaskViewModel,
                            notificationViewModel,
                            settingViewModel,
                            navController,
                            settingPreferences = settingsPreferences,
                            windowSize = rememberWindowSize(),
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_add_task),
                                contentDescription = null,
                                modifier = Modifier.size(120.dp)
                            )
                        }
                    }
                }
            }
        }
        if (intent?.action == Intent.ACTION_SEND) {
            if (SHARE_FORMAT == intent.type) {
                shareTxt = intent.getStringExtra(Intent.EXTRA_TEXT)
            }
        }
    }

    private fun observePreferences() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingViewModel.preferencesFlow.collect { value ->
                    if (value.fingerPrintEnable && !settingViewModel.initAuth.value) {
                        BiometricUtil.showBiometricPrompt(
                            activity = this@MainActivity,
                            listener = object : BiometricAuthListener {
                                override fun onBiometricAuthSuccess() {
                                    mainViewModel.update(true)
                                    settingViewModel.updateAuth(true)
                                }

                                override fun onUserCancelled() {
                                    finish()
                                }

                                override fun onErrorOccurred() {
                                    toast { getString(R.string.something_went_wrong) }
                                    finish()
                                }
                            }
                        )
                    } else {
                        mainViewModel.update(true)
                    }
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingViewModel.preferencesFlow.collectLatest { preference ->
                    val theme = preference.theme.toInt()
                    val currentNightMode =
                        resources.configuration.uiMode and UI_MODE_NIGHT_MASK
                    //32 = dark mode and 16 = light mode
                    if (currentNightMode / 16 != if (theme == -1) {
                            0
                        } else theme
                    ) {
                        AppCompatDelegate.setDefaultNightMode(theme)
                    }
                }
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    private fun RequestPermission() {
        val launcher: ManagedActivityResultLauncher<String, Boolean> =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    /* no-op */
                } else {
                    toast {
                        getString(R.string.user_cancelled_the_operation)
                    }
                }
            }

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                LocalContext.current,
                android.Manifest.permission.POST_NOTIFICATIONS
            ),
            -> {
                // Some works that require permission
            }

            else -> {
                // Asking for permission
                SideEffect {
                    launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
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
                        appUpdateInfo,
                        updateResultStarter,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                        UPDATE_REQUEST_CODE
                    )
                } catch (exception: IntentSender.SendIntentException) {
                    toast { exception.message.toString() }
                }
            }
        }
    }

    private fun activateReviewInfo() {
        reviewManager = ReviewManagerFactory.create(this)
        val reviewTask = reviewManager.requestReviewFlow()
        reviewTask.addOnCompleteListener {
            if (it.isSuccessful) {
                reviewInfo = it.result
            }
        }
    }

    private fun startReviewFlow() {
        if (reviewInfo != null) {
            reviewManager.launchReviewFlow(this, reviewInfo!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(appUpdateListener)
        activityContext = null
    }
}