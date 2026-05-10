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
import com.lahsuak.apps.tasks.ui.widget.TaskWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val preferenceManager: PreferenceManager,
    private val widgetUpdater: TaskWidgetUpdater,
    state: SavedStateHandle,
) : ViewModel() {
    private val _taskFlow = MutableStateFlow<Task?>(null)
    val taskFlow get() = _taskFlow.asStateFlow()
    
    // Date filtering for week view
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()
    
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

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean, context: Context) = viewModelScope.launch {
        repository.updateTask(task.copy(isDone = isChecked))
        widgetUpdater.updateTaskWidgets(context.applicationContext)
    }

    fun onUndoDeleteClick(task: Task, context: Context) = viewModelScope.launch {
        repository.insertTask(task)
        widgetUpdater.updateTaskWidgets(context.applicationContext)
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAllCompletedScreen)
    }

    fun insert(task: Task, context: Context? = null) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTask(task)
        context?.let { widgetUpdater.updateTaskWidgets(it.applicationContext) }
    }

    fun update(task: Task, context: Context? = null) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTask(task)
        context?.let { widgetUpdater.updateTaskWidgets(it.applicationContext) }
    }

    fun delete(task: Task, context: Context? = null) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTask(task)
        context?.let { widgetUpdater.updateTaskWidgets(it.applicationContext) }
    }

    fun getById(id: Int) {
        viewModelScope.launch {
            _taskFlow.value = repository.getById(id)
        }
    }

    fun resetTaskValue() {
        _taskFlow.value = null
    }

    fun deleteCompletedTask(context: Context? = null) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllCompletedTask()
        context?.let { widgetUpdater.updateTaskWidgets(it.applicationContext) }
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
    
    // Date selection for week view
    fun selectDate(date: LocalDate?) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }
    
    fun clearDateSelection() {
        _selectedDate.value = null
    }
}