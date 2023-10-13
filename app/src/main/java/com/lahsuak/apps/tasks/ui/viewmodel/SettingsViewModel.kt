package com.lahsuak.apps.tasks.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lahsuak.apps.tasks.data.db.TaskDatabase
import com.lahsuak.apps.tasks.data.model.SortOrder
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.data.repository.BackupRepository
import com.lahsuak.apps.tasks.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    taskDatabase: TaskDatabase,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val backupRepository = BackupRepository(
        database = taskDatabase,
        mutex = Mutex(),
        scope = viewModelScope,
        dispatcher = Dispatchers.IO
    )
    private var tasks by mutableStateOf(emptyList<Task>())

    private var observeNoteJob: Job? = null

    init {
       observe()
    }

    fun onExport(uri: Uri) {
        viewModelScope.launch {
            backupRepository.export(uri)
            observe()
        }
    }

    fun onImport(uri: Uri) {
        viewModelScope.launch {
            backupRepository.import(uri)
            observe()
        }
    }

    private fun observe() {
        observeNotes()
    }

    private fun observeNotes() {
        observeNoteJob?.cancel()
        observeNoteJob = viewModelScope.launch {
            taskRepository.getAllTasks("", SortOrder.BY_NAME, false).collect { task ->
                tasks = task
            }
        }
    }
}