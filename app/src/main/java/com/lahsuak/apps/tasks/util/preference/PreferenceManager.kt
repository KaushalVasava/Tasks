package com.lahsuak.apps.tasks.util.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lahsuak.apps.tasks.model.SortOrder
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.DEFAULT_FONT_SIZE
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.DEFAULT_LANGUAGE_VALUE
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.DEFAULT_THEME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = AppConstants.SETTING
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
            FilterPreferences(sortOrder, viewType)
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
            FilterPreferences(sortOrder, viewType)
        }

    val settingPreferenceFlow = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }
        .map { preferences ->
            val theme = preferences[PreferencesKeys.THEME] ?: DEFAULT_THEME
            val fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: DEFAULT_FONT_SIZE
            val swipeGestureEnable = preferences[PreferencesKeys.SWIPE_GESTURE] ?: true
            val showVoice = preferences[PreferencesKeys.VOICE] ?: true
            val showReminder = preferences[PreferencesKeys.REMINDER] ?: true
            val showCopy = preferences[PreferencesKeys.COPY] ?: true
            val showProgress = preferences[PreferencesKeys.PROGRESS] ?: false
            val showSubTask = preferences[PreferencesKeys.SUBTASK] ?: true
            val fingerprintEnabled = preferences[PreferencesKeys.FINGERPRINT] ?: false
            val language = preferences[PreferencesKeys.LANGUAGE] ?: DEFAULT_LANGUAGE_VALUE

            SettingPreferences(
                theme,
                fontSize,
                swipeGestureEnable,
                showVoice,
                showCopy,
                showProgress,
                showReminder,
                showSubTask,
                fingerprintEnabled,
                language
            )
        }

    suspend fun updateSortOrder(sortOrder: SortOrder, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateSortOrder2(sortOrder: SortOrder, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER2] = sortOrder.name
        }
    }

    suspend fun updateViewType(viewType: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIEW_TYPE] = viewType
        }
    }

    suspend fun updateTheme(theme: String, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
        }
    }

    suspend fun updateFontSize(fontSize: String, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = fontSize
        }
    }

    suspend fun updateVoiceIconVisibility(isVisible: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VOICE] = isVisible
        }
    }
    suspend fun updateSwipeIconVisibility(isVisible: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SWIPE_GESTURE] = isVisible
        }
    }

    suspend fun updateCopyIconVisibility(isVisible: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COPY] = isVisible
        }
    }

    suspend fun updateSubTaskVisibility(isVisible: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBTASK] = isVisible
        }
    }

    suspend fun updateProgressVisibility(isVisible: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROGRESS] = isVisible
        }
    }

    suspend fun updateReminderVisibility(isVisible: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER] = isVisible
        }
    }

    suspend fun updateFingerPrint(isEnable: Boolean, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FINGERPRINT] = isEnable
        }
    }

    suspend fun updateLanguage(lang: String, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = lang
        }
    }

    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey(AppConstants.SORT_ORDER)
        val SORT_ORDER2 = stringPreferencesKey(AppConstants.SORT_ORDER2)
        val VIEW_TYPE = booleanPreferencesKey(AppConstants.VIEW_TYPE)

        val THEME = stringPreferencesKey(AppConstants.SharedPreference.THEME_KEY)
        val FONT_SIZE = stringPreferencesKey(AppConstants.SharedPreference.FONT_SIZE_KEY)
        val SWIPE_GESTURE = booleanPreferencesKey(AppConstants.SharedPreference.SWIPE_GESTURE_KEY)
        val VOICE = booleanPreferencesKey(AppConstants.SharedPreference.SHOW_VOICE_TASK_KEY)
        val REMINDER = booleanPreferencesKey(AppConstants.SharedPreference.SHOW_REMINDER_KEY)
        val PROGRESS = booleanPreferencesKey(AppConstants.SharedPreference.TASK_PROGRESS_KEY)
        val SUBTASK = booleanPreferencesKey(AppConstants.SharedPreference.SHOW_SUBTASK_KEY)
        val COPY = booleanPreferencesKey(AppConstants.SharedPreference.SHOW_COPY_KEY)
        val FINGERPRINT = booleanPreferencesKey(AppConstants.SharedPreference.FINGERPRINT_KEY)
        val LANGUAGE = stringPreferencesKey(AppConstants.SharedPreference.LANGUAGE_KEY)
    }
}

