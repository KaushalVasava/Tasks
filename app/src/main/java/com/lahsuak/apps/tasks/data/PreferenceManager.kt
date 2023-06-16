package com.lahsuak.apps.tasks.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
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
        fun getOrder(typeName: String): SortOrder {
            return when (typeName) {
                TaskApp.appContext.getString(R.string.date)-> BY_DATE
                TaskApp.appContext.getString(R.string.date_desc)-> BY_DATE_DESC
                TaskApp.appContext.getString(R.string.name) -> BY_NAME
                TaskApp.appContext.getString(R.string.name_desc) -> BY_NAME_DESC
                TaskApp.appContext.getString(R.string.category) -> BY_CATEGORY
                TaskApp.appContext.getString(R.string.category_desc) -> BY_CATEGORY_DESC
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
    val viewType: Boolean
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

    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey(AppConstants.SORT_ORDER)
        val HIDE_COMPLETED = booleanPreferencesKey(AppConstants.HIDE_COMPLETED)
        val SORT_ORDER2 = stringPreferencesKey(AppConstants.SORT_ORDER2)
        val HIDE_COMPLETED2 = booleanPreferencesKey(AppConstants.HIDE_COMPLETED2)
        val VIEW_TYPE = booleanPreferencesKey(AppConstants.VIEW_TYPE)
    }
}