package com.lahsuak.apps.tasks.util.demo

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.SettingPreferences
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.model.SettingGroup
import com.lahsuak.apps.tasks.model.SettingItemModel
import com.lahsuak.apps.tasks.ui.screens.components.Theme
import com.lahsuak.apps.tasks.ui.viewmodel.SettingViewModel
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppUtil
import kotlin.random.Random

fun getDemoTaskData(): List<Task> {
    val list = mutableListOf<Task>()
    for (i in 0..50) {
        list.add(
            Task(
                id = i,
                title = "Index Task $i",
                color = Random.nextInt(0, 4),
                startDate = System.currentTimeMillis(),
                progress = Random.nextFloat()
            )
        )
    }
    return list
}

fun getDemoSubTaskData(): List<SubTask> {
    val list = mutableListOf<SubTask>()
    for (i in 0..50) {
        list.add(
            SubTask(
                id = 1,
                subTitle = "Index Sub Task $i",
                sId = i,
            )
        )
    }
    return list
}

fun getThemes(): List<Theme> {
    return listOf(
        Theme("System", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        Theme("Light", AppCompatDelegate.MODE_NIGHT_NO),
        Theme("Dark", AppCompatDelegate.MODE_NIGHT_YES)
    )
}

fun getSettings(
    context: Context,
    preference: SettingPreferences,
    settingViewModel: SettingViewModel
): List<SettingGroup> {
    return listOf(
        SettingGroup(
            context.getString(R.string.general),
            listOf(
                SettingItemModel(
                    R.drawable.ic_theme,
                    context.getString(R.string.theme),
                    AppConstants.SharedPreference.THEME_KEY,
                    context.getString(R.string.follow_system),
                    null
                ) {
//                    ThemeDialog {}
//                    SettingsFragment.selectedTheme = 1
//                    setTheme()
                },
                SettingItemModel(
                    R.drawable.ic_font,
                    context.getString(R.string.font_size),
                    AppConstants.SharedPreference.FONT_SIZE_KEY,
                    context.getString(R.string.medium),
                    null
                ) {},
                SettingItemModel(
                    R.drawable.ic_mic,
                    context.getString(R.string.show_voice_to_add_task_option),
                    AppConstants.SharedPreference.SHOW_VOICE_TASK_KEY,
                    context.getString(R.string.enable),
                    true
                ) {
                    Log.d("TAG", "demo data: click")
                  settingViewModel.onVoiceTaskChange(it,context)
                },
                SettingItemModel(
                    R.drawable.ic_progress,
                    context.getString(R.string.progress_setting),
                    AppConstants.SharedPreference.TASK_PROGRESS_KEY,
                    context.getString(R.string.disable),
                    false
                ) {
                    settingViewModel.onProgressChange(!preference.progressBarEnable,context)
                },
                SettingItemModel(
                    R.drawable.ic_reminder,
                    context.getString(R.string.reminder_setting),
                    AppConstants.SharedPreference.SHOW_REMINDER_KEY,
                    context.getString(R.string.enable),
                    true
                ) {
                    settingViewModel.onReminderChange(!preference.progressBarEnable,context)
                },
                SettingItemModel(
                    R.drawable.ic_sub_tasks,
                    context.getString(R.string.subtask_of_task),
                    AppConstants.SharedPreference.SHOW_SUBTASK_KEY,
                    context.getString(R.string.enable),
                    true
                ) {
                    settingViewModel.onSubTaskChange(!preference.showSubTask,context)
                }
            )
        ),
        SettingGroup(
            context.getString(R.string.language),
            listOf(
                SettingItemModel(
                    R.drawable.ic_language,
                    context.getString(R.string.language),
                    AppConstants.SharedPreference.LANGUAGE_SHARED_PREFERENCE_LANGUAGE_KEY,
                    context.getString(R.string.follow_system),
                    null
                ) {}
            )
        ),
        SettingGroup(
            context.getString(R.string.more),
            listOf(
                SettingItemModel(
                    R.drawable.ic_share,
                    context.getString(R.string.share_app),
                    "",
                    context.getString(R.string.share_text),
                    null
                ) {
                    AppUtil.shareApp(context)
                },
                SettingItemModel(
                    R.drawable.ic_reviews,
                    context.getString(R.string.rating),"",
                    context.getString(R.string.rating_text),
                    null
                ) {
                    AppUtil.appRating(context)
                }
            )
        )
    )
}