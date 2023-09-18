package com.lahsuak.apps.tasks.util.demo

import android.content.Context
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.model.SettingGroup
import com.lahsuak.apps.tasks.model.SettingItemModel
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

fun getSettings(context: Context): List<SettingGroup> {
    return listOf(
        SettingGroup(
            context.getString(R.string.general),
            listOf(
                SettingItemModel(
                    R.drawable.ic_theme,
                    context.getString(R.string.theme),
                    context.getString(R.string.follow_system),
                    null
                ),
                SettingItemModel(
                    R.drawable.ic_font,
                    context.getString(R.string.font_size),
                    context.getString(R.string.medium),
                    null
                ),
                SettingItemModel(
                    R.drawable.ic_mic,
                    context.getString(R.string.show_voice_to_add_task_option),
                    context.getString(R.string.enable),
                    true
                ), SettingItemModel(
                    R.drawable.ic_progress,
                    context.getString(R.string.progress_setting),
                    context.getString(R.string.disable),
                    false
                ), SettingItemModel(
                    R.drawable.ic_reminder,
                    context.getString(R.string.reminder_setting),
                    context.getString(R.string.enable),
                    true
                ), SettingItemModel(
                    R.drawable.ic_sub_tasks,
                    context.getString(R.string.subtask_of_task),
                    context.getString(R.string.enable),
                    true
                )
            )
        ),
        SettingGroup(
            context.getString(R.string.language),
            listOf(
                SettingItemModel(
                    R.drawable.ic_language,
                    context.getString(R.string.language),
                    context.getString(R.string.follow_system),
                    null
                )
            )
        ),
        SettingGroup(
            context.getString(R.string.more),
            listOf(
                SettingItemModel(
                    R.drawable.ic_share,
                    context.getString(R.string.share_app),
                    context.getString(R.string.share_text),

                    null
                ),
                SettingItemModel(
                    R.drawable.ic_reviews,
                    context.getString(R.string.rating),
                    context.getString(R.string.rating_text),
                    null
                )
            )
        )
    )
}