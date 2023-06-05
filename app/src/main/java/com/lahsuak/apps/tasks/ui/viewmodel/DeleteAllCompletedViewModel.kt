package com.lahsuak.apps.tasks.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.lahsuak.apps.tasks.data.repository.TaskRepository
import com.lahsuak.apps.tasks.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedViewModel @Inject constructor(
    private val repository: TaskRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    fun onConfirmClick() = applicationScope.launch {
        repository.deleteAllCompletedTask()
    }

    fun onConfirmClick2(id: Int) = applicationScope.launch {
        repository.deleteAllCompletedSubTask(id)
    }

    fun deleteAllTasks() = applicationScope.launch {
        repository.deleteAllTasks()
    }
}