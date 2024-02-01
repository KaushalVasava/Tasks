package com.lahsuak.apps.tasks.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lahsuak.apps.tasks.data.db.TaskDatabase
import com.lahsuak.apps.tasks.model.SortOrder
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.data.repository.BackupRepository
import com.lahsuak.apps.tasks.data.repository.TaskRepository
import com.lahsuak.apps.tasks.util.preference.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
}