package com.lahsuak.apps.tasks.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lahsuak.apps.tasks.BuildConfig
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.db.TaskDatabase
import com.lahsuak.apps.tasks.model.SortOrder
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.data.repository.BackupRepository
import com.lahsuak.apps.tasks.data.repository.TaskRepository
import com.lahsuak.apps.tasks.ui.screens.settings.PreferenceType
import com.lahsuak.apps.tasks.ui.screens.settings.SettingItem
import com.lahsuak.apps.tasks.ui.screens.settings.SettingModel
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.LanguageUtil.Companion.changeLocale
import com.lahsuak.apps.tasks.util.biometric.BiometricAuthListener
import com.lahsuak.apps.tasks.util.biometric.BiometricUtil
import com.lahsuak.apps.tasks.util.preference.PreferenceManager
import com.lahsuak.apps.tasks.util.preference.SettingPreferences
import com.lahsuak.apps.tasks.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    taskDatabase: TaskDatabase,
    private val taskRepository: TaskRepository,
    private val preferenceManager: PreferenceManager,
) : ViewModel() {

    private val _initAuth = MutableStateFlow(false)
    val initAuth = _initAuth.asStateFlow()

    val preferencesFlow = preferenceManager.settingPreferenceFlow
    
    // Settings list state
    private val _settingsList = MutableStateFlow<List<SettingModel>>(emptyList())
    val settingsList: StateFlow<List<SettingModel>> = _settingsList.asStateFlow()
    
    // Launcher references for backup/restore
    private var exportLauncher: ActivityResultLauncher<String>? = null
    private var importLauncher: ActivityResultLauncher<Array<String>>? = null
    private val backupRepository = BackupRepository(
        database = taskDatabase,
        mutex = Mutex(),
        scope = viewModelScope,
        dispatcher = Dispatchers.IO
    )
    private var tasks by mutableStateOf(emptyList<Task>())
    private var subTasks by mutableStateOf(emptyList<SubTask>())

    private var observeNoteJob: Job? = null

    init {
        observeNotes()
    }
    
    fun initializeSettings(context: Context) {
        viewModelScope.launch {
            preferencesFlow.collect { preference ->
                _settingsList.value = createSettingsList(context, preference)
            }
        }
    }
    
    fun setLaunchers(
        exportLauncher: ActivityResultLauncher<String>,
        importLauncher: ActivityResultLauncher<Array<String>>
    ) {
        this.exportLauncher = exportLauncher
        this.importLauncher = importLauncher
    }

    fun onThemeChange(theme: String, context: Context) = viewModelScope.launch {
        preferenceManager.updateTheme(theme, context)
    }

    fun onFontSizeChange(fontSize: String, context: Context) = viewModelScope.launch {
        preferenceManager.updateFontSize(fontSize, context)
    }

    fun onSwipePreferenceChange(isVisible: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateSwipeIconVisibility(isVisible, context)
    }
    fun onVoiceIconChange(isVisible: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateVoiceIconVisibility(isVisible, context)
    }

    fun onCopyIconChange(isVisible: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateCopyIconVisibility(isVisible, context)
    }

    fun onSubTaskVisibilityChange(isVisible: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateSubTaskVisibility(isVisible, context)
    }

    fun onReminderVisibilityChange(isVisible: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateReminderVisibility(isVisible, context)
    }

    fun onProgressVisibilityChange(isVisible: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateProgressVisibility(isVisible, context)
    }

    fun onFingerPrintChange(isVisible: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateFingerPrint(isVisible, context)
    }

    fun onLanguageChange(lang: String, context: Context) = viewModelScope.launch {
        preferenceManager.updateLanguage(lang, context)
    }

    fun onExport(uri: Uri) {
        viewModelScope.launch {
            backupRepository.export(uri)
            observeNotes()
        }
    }

    fun onImport(uri: Uri) {
        viewModelScope.launch {
            backupRepository.import(uri)
            observeNotes()
        }
    }

    fun updateAuth(authorized: Boolean) {
        _initAuth.value = authorized
    }

    private fun observeNotes() {
        observeNoteJob?.cancel()
        observeNoteJob = viewModelScope.launch {
            taskRepository.getAllTasks("", SortOrder.BY_NAME).collect { task ->
                tasks = task
            }
            tasks.map {
                taskRepository.getAllSubTasks(it.id, "", SortOrder.BY_NAME)
                    .collect { subTask ->
                        subTasks = subTask
                    }
            }
        }
    }
    
    private fun createSettingsList(context: Context, preference: SettingPreferences): List<SettingModel> {
        val langEntries = context.resources.getStringArray(R.array.language_entries).toList()
        val langValues = context.resources.getStringArray(R.array.language_values).toList()
        val languages = langEntries.mapIndexed { index, item ->
            item to langValues[index]
        }
        
        val themeEntries = context.resources.getStringArray(R.array.theme_entries).toList()
        val themeValues = context.resources.getStringArray(R.array.theme_values).toList()
        val themes = themeEntries.mapIndexed { index, item ->
            item to themeValues[index]
        }
        
        val fontEntries = context.resources.getStringArray(R.array.font_entries).toList()
        val fontValues = context.resources.getStringArray(R.array.font_values).toList()
        val fontSizes = fontEntries.mapIndexed { index, item ->
            item to fontValues[index]
        }
        
        val enable = context.getString(R.string.enable)
        val disable = context.getString(R.string.disable)
        
        return listOf(
            SettingModel(
                id = "general_category",
                category = context.getString(R.string.general),
                items = listOf(
                    SettingItem(
                        id = "theme_setting",
                        title = context.getString(R.string.theme),
                        placeholder = if (preference.theme.toInt() == -1) {
                            themeEntries[0]
                        } else {
                            themeEntries[preference.theme.toInt()]
                        },
                        icon = R.drawable.ic_theme,
                        items = themes,
                        type = PreferenceType.DROPDOWN,
                        action = { theme, _ ->
                            onThemeChange(theme as String, context)
                        },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "font_size_setting",
                        title = context.getString(R.string.font_size),
                        placeholder = when (preference.fontSize.toInt()) {
                            12 -> context.getString(R.string.very_small)
                            14 -> context.getString(R.string.medium_small)
                            16 -> context.getString(R.string.small)
                            18 -> context.getString(R.string.medium)
                            20 -> context.getString(R.string.large)
                            22 -> context.getString(R.string.huge)
                            else -> context.getString(R.string.medium)
                        },
                        items = fontSizes,
                        icon = R.drawable.ic_font,
                        type = PreferenceType.DROPDOWN,
                        action = { item, _ ->
                            onFontSizeChange(item as String, context)
                        },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "swipe_gesture_setting",
                        title = context.getString(R.string.swipe_gesture),
                        placeholder = if (preference.swipeGestureEnable) enable else disable,
                        icon = R.drawable.ic_swipe,
                        type = PreferenceType.SWITCH,
                        initialValue = preference.swipeGestureEnable,
                        action = { _, _ -> },
                        onCheckedChange = { onSwipePreferenceChange(it, context) }
                    ),
                    SettingItem(
                        id = "voice_icon_setting",
                        title = context.getString(R.string.show_voice_to_add_task_option),
                        placeholder = if (preference.showVoiceIcon) enable else disable,
                        icon = R.drawable.ic_mic,
                        type = PreferenceType.SWITCH,
                        initialValue = preference.showVoiceIcon,
                        action = { _, _ -> },
                        onCheckedChange = { onVoiceIconChange(it, context) }
                    ),
                    SettingItem(
                        id = "copy_icon_setting",
                        title = context.getString(R.string.show_copy),
                        placeholder = if (preference.showCopyIcon) enable else disable,
                        icon = R.drawable.ic_copy,
                        type = PreferenceType.SWITCH,
                        initialValue = preference.showCopyIcon,
                        action = { _, _ -> },
                        onCheckedChange = { onCopyIconChange(it, context) }
                    ),
                    SettingItem(
                        id = "progress_setting",
                        title = context.getString(R.string.progress_setting),
                        placeholder = if (preference.showProgress) enable else disable,
                        icon = R.drawable.ic_progress,
                        type = PreferenceType.SWITCH,
                        initialValue = preference.showProgress,
                        action = { _, _ -> },
                        onCheckedChange = { onProgressVisibilityChange(it, context) }
                    ),
                    SettingItem(
                        id = "reminder_setting",
                        title = context.getString(R.string.reminder_setting),
                        placeholder = if (preference.showReminder) enable else disable,
                        icon = R.drawable.ic_reminder,
                        type = PreferenceType.SWITCH,
                        initialValue = preference.showReminder,
                        action = { _, _ -> },
                        onCheckedChange = { onReminderVisibilityChange(it, context) }
                    ),
                    SettingItem(
                        id = "subtask_setting",
                        title = context.getString(R.string.subtask_of_task),
                        placeholder = if (preference.showSubTask) enable else disable,
                        icon = R.drawable.ic_sub_tasks,
                        type = PreferenceType.SWITCH,
                        initialValue = preference.showSubTask,
                        action = { _, _ -> },
                        onCheckedChange = { onSubTaskVisibilityChange(it, context) }
                    )
                )
            ),
            SettingModel(
                id = "security_category",
                category = context.getString(R.string.security_and_data),
                items = listOf(
                    SettingItem(
                        id = "app_lock_setting",
                        title = context.getString(R.string.app_lock),
                        placeholder = if (preference.fingerPrintEnable) enable else disable,
                        icon = R.drawable.ic_fingerprint,
                        initialValue = preference.fingerPrintEnable,
                        type = PreferenceType.SWITCH,
                        action = { _, _ -> },
                        onCheckedChange = { checked ->
                            BiometricUtil.showBiometricPrompt(
                                activity = (context as AppCompatActivity),
                                listener = object : BiometricAuthListener {
                                    override fun onBiometricAuthSuccess() {
                                        onFingerPrintChange(checked, context)
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
                        }
                    ),
                    SettingItem(
                        id = "backup_data_setting",
                        title = context.getString(R.string.backup_data),
                        placeholder = null,
                        icon = R.drawable.ic_backup,
                        type = PreferenceType.NORMAL,
                        action = { _, _ ->
                            exportLauncher?.launch(AppConstants.BACKUP_FILE_NAME)
                        },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "restore_data_setting",
                        title = context.getString(R.string.restore_data),
                        placeholder = null,
                        icon = R.drawable.ic_restore,
                        type = PreferenceType.NORMAL,
                        action = { _, _ ->
                            importLauncher?.launch(arrayOf(AppConstants.ANY_MIME_TYPE))
                        },
                        onCheckedChange = {}
                    )
                )
            ),
            SettingModel(
                id = "language_category",
                category = context.getString(R.string.language),
                items = listOf(
                    SettingItem(
                        id = "language_setting",
                        title = context.getString(R.string.language),
                        placeholder = langEntries[langValues.indexOfFirst {
                            preference.language == it
                        }],
                        icon = R.drawable.ic_language,
                        items = languages,
                        initialValue = false,
                        type = PreferenceType.DROPDOWN,
                        action = { item, _ ->
                            context.changeLocale(item as String)
                            onLanguageChange(item, context)
                        },
                        onCheckedChange = {}
                    )
                )
            ),
            SettingModel(
                id = "more_category",
                category = context.getString(R.string.more),
                items = listOf(
                    SettingItem(
                        id = "donate_setting",
                        title = context.getString(R.string.donate),
                        placeholder = context.getString(R.string.donate_me_desc),
                        icon = R.drawable.ic_coffee,
                        type = PreferenceType.NORMAL,
                        action = { _, _ ->
                            AppUtil.openWebsite(context, AppConstants.DONATION)
                        },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "share_app_setting",
                        title = context.getString(R.string.share_app),
                        placeholder = context.getString(R.string.share_text),
                        icon = R.drawable.ic_share,
                        type = PreferenceType.NORMAL,
                        action = { _, _ ->
                            AppUtil.shareApp(context)
                        },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "more_apps_setting",
                        title = context.getString(R.string.more_apps),
                        placeholder = context.getString(R.string.more_app_text),
                        icon = R.drawable.ic_apps,
                        type = PreferenceType.NORMAL,
                        action = { _, _ ->
                            AppUtil.openMoreApp(context)
                        },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "rating_setting",
                        title = context.getString(R.string.rating),
                        placeholder = context.getString(R.string.rating_text),
                        icon = R.drawable.ic_reviews,
                        type = PreferenceType.NORMAL,
                        action = { _, _ ->
                            AppUtil.appRating(context)
                        },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "contact_support_setting",
                        title = context.getString(R.string.contact_support),
                        placeholder = context.getString(R.string.feedback_text),
                        icon = R.drawable.ic_email,
                        type = PreferenceType.NORMAL,
                        action = { _, _ ->
                            AppUtil.openWebsite(context, context.getString(R.string.portfolio))
                        },
                        onCheckedChange = {}
                    )
                )
            ),
            SettingModel(
                id = "about_category",
                category = context.getString(R.string.about),
                items = listOf(
                    SettingItem(
                        id = "developer_setting",
                        title = context.getString(R.string.developer),
                        placeholder = context.getString(R.string.developer_name),
                        icon = R.drawable.ic_developer,
                        type = PreferenceType.NORMAL,
                        action = { _, _ ->
                            AppUtil.openWebsite(context, context.getString(R.string.portfolio))
                        },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "instagram_setting",
                        title = context.getString(R.string.follow_instagram),
                        icon = R.drawable.ic_instagram,
                        type = PreferenceType.NORMAL,
                        action = { _, _ ->
                            AppUtil.openInstagram(context)
                        },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "github_setting",
                        title = context.getString(R.string.visit_github),
                        icon = R.drawable.ic_github,
                        type = PreferenceType.NORMAL,
                        action = { _, _ ->
                            AppUtil.openWebsite(
                                context,
                                context.getString(R.string.github_link)
                            )
                        },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "privacy_policy_setting",
                        title = context.getString(R.string.privacy_policy),
                        icon = R.drawable.ic_policy,
                        type = PreferenceType.NORMAL,
                        action = { _, _ ->
                            AppUtil.openWebsite(
                                context,
                                context.getString(R.string.privacy_policy_link)
                            )
                        },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "version_setting",
                        title = context.getString(R.string.current_version),
                        placeholder = BuildConfig.VERSION_NAME,
                        type = PreferenceType.NORMAL,
                        action = { _, _ -> },
                        onCheckedChange = {}
                    ),
                    SettingItem(
                        id = "made_in_india_setting",
                        title = context.getString(R.string.made_in_india),
                        type = PreferenceType.NORMAL,
                        action = { _, _ -> },
                        onCheckedChange = {}
                    )
                )
            )
        )
    }
}