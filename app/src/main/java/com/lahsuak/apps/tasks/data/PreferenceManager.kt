package com.lahsuak.apps.tasks.data

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppConstants.SETTING
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class SortOrder {
    BY_NAME,
    BY_NAME_DESC,
    BY_DATE,
    BY_DATE_DESC,
    BY_CATEGORY,
    BY_CATEGORY_DESC
    ;

    companion object {
        fun getOrder(order: Int): SortOrder {
            return when (order) {
                0 -> BY_NAME
                1 -> BY_NAME_DESC
                2 -> BY_DATE
                3 -> BY_DATE_DESC
                4 -> BY_CATEGORY
                5 -> BY_CATEGORY_DESC
                else -> {
                    throw IllegalArgumentException()
                }
            }
        }
    }
}

data class FilterPreferences(
    val sortOrder: SortOrder,
    val hideCompleted: Boolean,
    val viewType: Boolean,
)

data class SettingPreferences(
//    val themes: Map<String, Int>,
//    val fonts: Map<String, Float>,
    val voiceButtonEnable: Boolean,
    val progressBarEnable: Boolean,
    val showReminder: Boolean,
    val showSubTask: Boolean,
//    val languages: Map<String, Int>,
)

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = SETTING
    )

    val preferencesFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            val viewType = preferences[PreferencesKeys.VIEW_TYPE] ?: false

            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: false
            FilterPreferences(sortOrder, hideCompleted, viewType)
        }
    val preferencesFlow2 = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER2] ?: SortOrder.BY_DATE.name
            )
            val viewType = preferences[PreferencesKeys.VIEW_TYPE] ?: false
            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED2] ?: false
            FilterPreferences(sortOrder, hideCompleted, viewType)
        }

    val settingPreferenceFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val showVoice =
                preferences[SettingPreferenceKeys.SHOW_VOICE_TASK] ?: true
            val showProgress = preferences[SettingPreferenceKeys.SHOW_PROGRESS] ?: false
            val showReminder = preferences[SettingPreferenceKeys.SHOW_REMINDER] ?: true
            val showSubTask = preferences[SettingPreferenceKeys.SHOW_SUBTASK] ?: true
            SettingPreferences(showVoice,showProgress,showReminder,showSubTask)
        }

    suspend fun updateSortOrder(sortOrder: SortOrder, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_COMPLETED] = hideCompleted
        }
    }

    suspend fun updateSortOrder2(sortOrder: SortOrder, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER2] = sortOrder.name
        }
    }

    suspend fun updateHideCompleted2(hideCompleted: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_COMPLETED2] = hideCompleted
        }
    }

    suspend fun updateViewType(viewType: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIEW_TYPE] = viewType
        }
    }

    suspend fun updateVoiceTask(isVoiceTaskEnable: Boolean, context: Context){
        context.dataStore.edit { preferences ->
            preferences[SettingPreferenceKeys.SHOW_VOICE_TASK] = isVoiceTaskEnable
        }
    }
    suspend fun updateTaskProgress(isTaskProgressEnable: Boolean, context: Context){
        context.dataStore.edit { preferences ->
            preferences[SettingPreferenceKeys.SHOW_PROGRESS] = isTaskProgressEnable
        }
    }
    suspend fun updateReminder(isReminderEnable: Boolean, context: Context){
        context.dataStore.edit { preferences ->
            preferences[SettingPreferenceKeys.SHOW_REMINDER] = isReminderEnable
        }
    }
    suspend fun updateSubtask(isSubTaskEnable: Boolean, context: Context){
        context.dataStore.edit { preferences ->
            preferences[SettingPreferenceKeys.SHOW_SUBTASK] = isSubTaskEnable
        }
    }

    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey(AppConstants.SORT_ORDER)
        val HIDE_COMPLETED = booleanPreferencesKey(AppConstants.HIDE_COMPLETED)
        val SORT_ORDER2 = stringPreferencesKey(AppConstants.SORT_ORDER2)
        val HIDE_COMPLETED2 = booleanPreferencesKey(AppConstants.HIDE_COMPLETED2)
        val VIEW_TYPE = booleanPreferencesKey(AppConstants.VIEW_TYPE)
    }

    private object SettingPreferenceKeys {
//        val THEMES = (AppConstants.SORT_ORDER)
//        val FONTS = booleanPreferencesKey(AppConstants.HIDE_COMPLETED)
        val SHOW_VOICE_TASK = booleanPreferencesKey(AppConstants.SharedPreference.SHOW_VOICE_TASK_KEY)
        val SHOW_PROGRESS = booleanPreferencesKey(AppConstants.SharedPreference.TASK_PROGRESS_KEY)
        val SHOW_REMINDER = booleanPreferencesKey(AppConstants.SharedPreference.SHOW_REMINDER_KEY)
        val SHOW_SUBTASK = booleanPreferencesKey(AppConstants.SharedPreference.SHOW_SUBTASK_KEY)
    }
}