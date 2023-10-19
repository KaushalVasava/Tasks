package com.lahsuak.apps.tasks.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.model.SortOrder
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.data.repository.TaskRepository
import com.lahsuak.apps.tasks.model.TaskEvent
import com.lahsuak.apps.tasks.util.AppConstants.SEARCH_INITIAL_VALUE
import com.lahsuak.apps.tasks.util.AppConstants.SEARCH_QUERY
import com.lahsuak.apps.tasks.util.preference.PreferenceManager
import com.lahsuak.apps.tasks.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val preferenceManager: PreferenceManager,
    state: SavedStateHandle,
) : ViewModel() {
    private val _taskFlow = MutableStateFlow<Task?>(null)
    val taskFlow get() = _taskFlow.asStateFlow()
    val searchQuery = state.getLiveData(SEARCH_QUERY, SEARCH_INITIAL_VALUE)
    val preferencesFlow = preferenceManager.preferencesFlow
    private val taskEventChannel = Channel<TaskEvent>()
    val tasksEvent = taskEventChannel.receiveAsFlow()
    val tasksFlow = combine(
        searchQuery.asFlow(), preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        repository.getAllTasks(
            query,
            filterPreferences.sortOrder
        )
    }.distinctUntilChanged()

    fun onSortOrderSelected(sortOrder: SortOrder, context: Context) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder, context)
    }

    //new method for layout of items
    fun onViewTypeChanged(viewType: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateViewType(viewType, context)
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        repository.updateTask(task.copy(isDone = isChecked))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAllCompletedScreen)
    }

    fun insert(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTask(task)
    }

    fun update(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTask(task)
    }

    fun delete(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTask(task)
    }

    fun getById(id: Int) {
        viewModelScope.launch {
            _taskFlow.value = repository.getById(id)
        }
    }

    fun resetTaskValue() {
        _taskFlow.value = null
    }

    fun deleteCompletedTask() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllCompletedTask()
    }

    fun setTask(task: Task){
        _taskFlow.value = task
    }

    fun cancelReminderCompose(
        context: Context,
        task: Task
    ) {
        task.reminder = null
        update(task)
        context.toast {
            context.getString(R.string.cancel_reminder)
        }
    }
}