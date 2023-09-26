package com.lahsuak.apps.tasks.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lahsuak.apps.tasks.data.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
) : ViewModel() {
    val preferencesFlow = preferenceManager.settingPreferenceFlow

    fun onVoiceTaskChange(isVoiceTaskEnable: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateVoiceTask(isVoiceTaskEnable, context)
    }

    fun onProgressChange(isTaskProgressEnable: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateTaskProgress(isTaskProgressEnable, context)
    }

    fun onReminderChange(isReminderEnable: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateReminder(isReminderEnable, context)
    }

    fun onSubTaskChange(isSubTaskEnable: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateSubtask(isSubTaskEnable, context)
    }
}