package com.lahsuak.apps.mytask.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.lahsuak.apps.mytask.data.repository.TaskRepository
import com.lahsuak.apps.mytask.di.ApplicationScope
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