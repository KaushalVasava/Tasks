package com.lahsuak.apps.tasks.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lahsuak.apps.tasks.BuildConfig
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.ui.viewmodel.SettingsViewModel
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.DEFAULT_FONT_SIZE
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.DEFAULT_LANGUAGE_VALUE
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.DEFAULT_THEME
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.LanguageUtil.Companion.changeLocale
import com.lahsuak.apps.tasks.util.biometric.BiometricAuthListener
import com.lahsuak.apps.tasks.util.biometric.BiometricUtil
import com.lahsuak.apps.tasks.util.preference.SettingPreferences
import com.lahsuak.apps.tasks.util.toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavController,
    settingViewModel: SettingsViewModel,
) {
    val preference by settingViewModel.preferencesFlow.collectAsState(
        initial = SettingPreferences(
            theme = DEFAULT_THEME,
            fontSize = DEFAULT_FONT_SIZE,
            swipeGestureEnable = true,
            showVoiceIcon = true,
            showCopyIcon = true,
            showProgress = false,
            showReminder = true,
            showSubTask = true,
            fingerPrintEnable = false,
            language = DEFAULT_LANGUAGE_VALUE
        )
    )

    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(AppConstants.ANY_MIME_TYPE),
        onResult = { uri ->
            uri?.let { settingViewModel.onExport(uri) }
        }
    )
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { settingViewModel.onImport(uri) }
        }
    )

    val langEntries by remember {
        mutableStateOf(
            context.resources.getStringArray(R.array.language_entries).toList()
        )
    }
    val langValues by remember {
        mutableStateOf(
            context.resources.getStringArray(R.array.language_values).toList()
        )
    }
    val languages by remember {
        mutableStateOf(
            langEntries.mapIndexed { index, item ->
                item to langValues[index]
            }
        )
    }
    val themeEntries by remember {
        mutableStateOf(
            context.resources.getStringArray(R.array.theme_entries).toList()
        )
    }
    val themeValues by remember {
        mutableStateOf(
            context.resources.getStringArray(R.array.theme_values).toList()
        )
    }
    val themes by remember {
        mutableStateOf(
            themeEntries.mapIndexed { index, item ->
                item to themeValues[index]
            }
        )
    }
    val fontEntries by remember {
        mutableStateOf(
            context.resources.getStringArray(R.array.font_entries).toList()
        )
    }
    val fontValues by remember {
        mutableStateOf(
            context.resources.getStringArray(R.array.font_values).toList()
        )
    }
    val fontSizes by remember {
        mutableStateOf(
            fontEntries.mapIndexed { index, item ->
                item to fontValues[index]
            }
        )
    }
    val enable = stringResource(id = R.string.enable)
    val disable = stringResource(id = R.string.disable)

    val settings = listOf(
                SettingModel(
                    category = context.getString(R.string.general),
                    items = listOf(
                        SettingItem(
                            title = context.getString(R.string.theme),
                            placeholder = if (preference.theme.toInt() == -1) {
                                themeEntries[0]
                            } else {
                                themeEntries[preference.theme.toInt()]
                            },
                            icon = R.drawable.ic_theme,
                            items = themes,
                            type = PreferenceType.DROPDOWN,
                            action = { t, _ ->
                                val index = (t as String).toInt()
                                setTheme(index)
                                settingViewModel.onThemeChange(t, context)
                            }
                        ) {
                        },
                        SettingItem(
                            title = context.getString(R.string.font_size),
                            placeholder = when (preference.fontSize.toInt()) {
                                12 -> context.getString(R.string.very_small)
                                14 -> context.getString(R.string.medium_small)
                                16 -> context.getString(R.string.small)
                                18 -> context.getString(R.string.medium)
                                20 -> context.getString(R.string.large)
                                22 -> context.getString(R.string.huge)
                                else -> {
                                    context.getString(R.string.medium)
                                }
                            },
                            items = fontSizes,
                            icon = R.drawable.ic_font,
                            type = PreferenceType.DROPDOWN,
                            action = { item, _ ->
                                settingViewModel.onFontSizeChange(item as String, context)
                            }
                        ) {},

                        SettingItem(
                            title = "Swipe Gesture Enable",
                            placeholder = if (preference.swipeGestureEnable) enable else disable,
                            icon = R.drawable.ic_swipe,
                            type = PreferenceType.SWITCH,
                            initialValue = preference.swipeGestureEnable,
                            action = { _, _ -> }
                        ) {
                            settingViewModel.onSwipePreferenceChange(it, context)
                        },
                        SettingItem(
                            title = context.getString(R.string.show_voice_to_add_task_option),
                            placeholder = if (preference.showVoiceIcon) enable else disable,
                            icon = R.drawable.ic_mic,
                            type = PreferenceType.SWITCH,
                            initialValue = preference.showVoiceIcon,
                            action = { _, _ -> }
                        ) {
                            settingViewModel.onVoiceIconChange(it, context)
                        },
                        SettingItem(
                            title = context.getString(R.string.show_copy),
                            placeholder = if (preference.showCopyIcon) enable else disable,
                            icon = R.drawable.ic_copy,
                            type = PreferenceType.SWITCH,
                            initialValue = preference.showCopyIcon,
                            action = { _, _ -> }
                        ) {
                            settingViewModel.onCopyIconChange(it, context)
                        },
                        SettingItem(
                            title = context.getString(R.string.progress_setting),
                            placeholder = if (preference.showProgress) enable else disable,
                            icon = R.drawable.ic_progress,
                            type = PreferenceType.SWITCH,
                            initialValue = preference.showProgress,
                            action = { _, _ -> }
                        ) {
                            settingViewModel.onProgressVisibilityChange(it, context)
                        },
                        SettingItem(
                            title = context.getString(R.string.reminder_setting),
                            placeholder = if (preference.showReminder) enable else disable,
                            icon = R.drawable.ic_reminder,
                            type = PreferenceType.SWITCH,
                            initialValue = preference.showReminder,
                            action = { _, _ -> }
                        ) {
                            settingViewModel.onReminderVisibilityChange(it, context)
                        },
                        SettingItem(
                            title = context.getString(R.string.subtask_of_task),
                            placeholder = if (preference.showSubTask) enable else disable,
                            icon = R.drawable.ic_sub_tasks,
                            type = PreferenceType.SWITCH,
                            initialValue = preference.showSubTask,
                            action = { _, _ -> }
                        ) {
                            settingViewModel.onSubTaskVisibilityChange(it, context)
                        }
                    )
                ),
                SettingModel(
                    category = context.getString(R.string.security_and_data),
                    items = listOf(
                        SettingItem(
                            title = context.getString(R.string.app_lock),
                            placeholder = if (preference.fingerPrintEnable) enable else disable,
                            icon = R.drawable.ic_fingerprint,
                            initialValue = preference.fingerPrintEnable,
                            type = PreferenceType.SWITCH,
                            action = { _, _ -> }
                        ) {
                            BiometricUtil.showBiometricPrompt(
                                activity = (context as AppCompatActivity),
                                listener = object : BiometricAuthListener {
                                    override fun onBiometricAuthSuccess() {
                                        settingViewModel.onFingerPrintChange(it, context)
                                    }

                                    override fun onUserCancelled() {
                                        context.toast {
                                            context.getString(R.string.user_cancelled_the_operation)
                                        }
                                    }

                                    override fun onErrorOccurred() {
                                        context.toast {
                                            context.getString(R.string.something_went_wrong)
                                        }
                                    }
                                },
                                cryptoObject = null,
                                allowDeviceCredential = true
                            )
                            settingViewModel.onFingerPrintChange(it, context)
                        },
                        SettingItem(
                            title = context.getString(R.string.backup_data),
                            placeholder = null,
                            icon = R.drawable.ic_backup,
                            type = PreferenceType.NORMAL,
                            action = { _, _ ->
                                exportLauncher.launch(AppConstants.BACKUP_FILE_NAME)
                            }
                        ) {},
                        SettingItem(
                            title = context.getString(R.string.restore_data),
                            placeholder = null,
                            icon = R.drawable.ic_restore,
                            type = PreferenceType.NORMAL,
                            action = { _, _ ->
                                importLauncher.launch(arrayOf(AppConstants.ANY_MIME_TYPE))
                            }
                        ) {},
                    ),
                ),
                SettingModel(
                    category = context.getString(R.string.language),
                    items = listOf(
                        SettingItem(
                            title = context.getString(R.string.language),
                            placeholder = langEntries[langValues.indexOfFirst {
                                preference.language == it
                            }],
                            icon = R.drawable.ic_language,
                            items = languages,
                            initialValue = null,
                            type = PreferenceType.DROPDOWN,
                            action = { item, _ ->
                                context.changeLocale(item as String)
                                settingViewModel.onLanguageChange(item, context)
                            }
                        ) {}
                    )
                ),
                SettingModel(
                    category = context.getString(R.string.more),
                    items = listOf(
                        SettingItem(
                            title = context.getString(R.string.donate),
                            placeholder = context.getString(R.string.donate_me_desc),
                            icon = R.drawable.ic_coffee,
                            type = PreferenceType.NORMAL,
                            action = { _, _ ->
                                AppUtil.openWebsite(context, AppConstants.DONATION)
                            }
                        ) {},
                        SettingItem(
                            title = context.getString(R.string.share_app),
                            placeholder = context.getString(R.string.share_text),
                            icon = R.drawable.ic_share,
                            type = PreferenceType.NORMAL,
                            action = { _, _ ->
                                AppUtil.shareApp(context)
                            }
                        ) {},
                        SettingItem(
                            title = context.getString(R.string.more_apps),
                            placeholder = context.getString(R.string.more_app_text),
                            icon = R.drawable.ic_apps,
                            type = PreferenceType.NORMAL,
                            action = { _, _ ->
                                AppUtil.openMoreApp(context)
                            }
                        ) {},
                        SettingItem(
                            title = context.getString(R.string.rating),
                            placeholder = context.getString(R.string.rating_text),
                            icon = R.drawable.ic_reviews,
                            type = PreferenceType.NORMAL,
                            action = { _, _ ->
                                AppUtil.appRating(context)
                            }
                        ) {},
                        SettingItem(
                            title = context.getString(R.string.contact_support),
                            placeholder = context.getString(R.string.feedback_text),
                            icon = R.drawable.ic_email,
                            type = PreferenceType.NORMAL,
                            action = { _, _ ->
                                AppUtil.openWebsite(context, context.getString(R.string.portfolio))
                            }
                        ) {}
                    )
                ),
                SettingModel(
                    category = context.getString(R.string.about),
                    items = listOf(
                        SettingItem(
                            title = context.getString(R.string.developer),
                            placeholder = context.getString(R.string.developer_name),
                            icon = R.drawable.ic_developer,
                            type = PreferenceType.NORMAL,
                            action = { _, _ ->
                                AppUtil.openWebsite(context, context.getString(R.string.portfolio))
                            }
                        ) {},
                        SettingItem(
                            title = context.getString(R.string.follow_instagram),
                            icon = R.drawable.ic_instagram,
                            type = PreferenceType.NORMAL,
                            action = { _, _ ->
                                AppUtil.openInstagram(context)
                            }
                        ) {},
                        SettingItem(
                            title = context.getString(R.string.visit_github),
                            icon = R.drawable.ic_github,
                            type = PreferenceType.NORMAL,
                            action = { _, _ ->
                                AppUtil.openWebsite(
                                    context,
                                    context.getString(R.string.github_link)
                                )
                            }
                        ) {},
                        SettingItem(
                            title = context.getString(R.string.privacy_policy),
                            icon = R.drawable.ic_policy,
                            type = PreferenceType.NORMAL,
                            action = { _, _ ->
                                AppUtil.openWebsite(
                                    context,
                                    context.getString(R.string.privacy_policy_link)
                                )
                            }
                        ) {},
                        SettingItem(
                            title = context.getString(R.string.current_version),
                            placeholder = BuildConfig.VERSION_NAME,
                            type = PreferenceType.NORMAL,
                            action = { _, _ -> }
                        ) {},
                        SettingItem(
                            title = context.getString(R.string.made_in_india),
                            type = PreferenceType.NORMAL,
                            action = { _, _ -> }
                        ) {},
                    )
                )
            )

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(stringResource(id = R.string.settings))
            }, navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        painterResource(id = R.drawable.ic_back),
                        stringResource(id = R.string.back)
                    )
                }
            })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            settings.groupBy {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        it.category,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(it.items, key = { settingItem ->
                    settingItem.id
                }) { item ->
                    Spacer(modifier = Modifier.height(2.dp))
                    when (item.type) {
                        PreferenceType.NORMAL -> ClickPreference(
                            title = item.title,
                            placeHolder = item.placeholder,
                            icon = item.icon
                        ) {
                            item.action("", -1)
                        }

                        PreferenceType.DROPDOWN -> DropDownPreference(
                            title = item.title,
                            initialValue = item.placeholder!!,
                            icon = item.icon,
                            items = item.items
                        ) { selectedOption, index ->
                            item.action(selectedOption, index)
                        }

                        PreferenceType.SWITCH -> SwitchPreference(
                            title = item.title,
                            placeHolder = item.placeholder,
                            icon = item.icon,
                            value = item.initialValue ?: false,
                            onValueChange = { checked ->
                                item.onCheckedChange(checked)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun setTheme(value: Int) {
    when (value) {
        -1 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM //system theme
        1 -> AppCompatDelegate.MODE_NIGHT_NO
        2 -> AppCompatDelegate.MODE_NIGHT_YES //dark theme
    }
    AppCompatDelegate.setDefaultNightMode(value)
}
