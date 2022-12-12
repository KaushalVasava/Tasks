package com.lahsuak.apps.mytask.ui.viewmodel

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.PreferenceManager
import com.lahsuak.apps.mytask.data.SortOrder
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.data.repository.TodoRepository
import com.lahsuak.apps.mytask.model.TaskEvent
import com.lahsuak.apps.mytask.util.Constants.SEARCH_INITIAL_VALUE
import com.lahsuak.apps.mytask.util.Constants.SEARCH_QUERY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val preferenceManager: PreferenceManager,
    state: SavedStateHandle,
) : ViewModel() {
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
            filterPreferences.sortOrder,
            filterPreferences.hideCompleted
        )
    }

    val tasksFlow2 = combine(
        searchQuery.asFlow(), preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        repository.getAllTasks(query, filterPreferences.sortOrder, false)
    }

    fun onSortOrderSelected(sortOrder: SortOrder, context: Context) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder, context)
    }

    fun onHideCompleted(hideCompleted: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateHideCompleted(hideCompleted, context)
    }

    //new method for layout of items
    fun onViewTypeChanged(viewType: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateViewType(viewType, context)
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        repository.deleteTodo(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        repository.updateTodo(task.copy(isDone = isChecked))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        repository.insertTodo(task)
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAllCompletedScreen)
    }

    fun insert(todo: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTodo(todo)
    }

    fun update(todo: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTodo(todo)
    }

    fun delete(todo: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTodo(todo)
    }

    suspend fun getById(id: Int): Task {
        return repository.getById(id)
    }

    suspend fun deleteAllTasks() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllTasks()
    }

    fun showDeleteDialog(
        context: Context,
        task: Task,
    ) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete))
            .setMessage(context.getString(R.string.delete_task))
            .setPositiveButton(context.getString(R.string.delete)) { dialog, _ ->
                viewModelScope.launch {
                    delete(task)
                }
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}