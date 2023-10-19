package com.lahsuak.apps.tasks.ui.viewmodel

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.lifecycle.*
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.model.SortOrder
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.data.repository.TaskRepository
import com.lahsuak.apps.tasks.model.SubTaskEvent
import com.lahsuak.apps.tasks.util.AppConstants.SEARCH_INITIAL_VALUE
import com.lahsuak.apps.tasks.util.AppConstants.SEARCH_QUERY
import com.lahsuak.apps.tasks.util.AppConstants.SHARE_FORMAT
import com.lahsuak.apps.tasks.util.AppConstants.TASK_ID
import com.lahsuak.apps.tasks.util.preference.PreferenceManager
import com.lahsuak.apps.tasks.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SubTaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val preferenceManager: PreferenceManager,
    state: SavedStateHandle,
) : ViewModel() {
    private val _subTaskFlow = MutableStateFlow<SubTask?>(null)
    val subTaskFlow
        get() = _subTaskFlow.asStateFlow()
    val searchQuery = state.getLiveData(SEARCH_QUERY, SEARCH_INITIAL_VALUE)
    val taskId = state.getLiveData(TASK_ID, 0)

    val preferencesFlow = preferenceManager.preferencesFlow2
    private val subTaskEventChannel = Channel<SubTaskEvent>()
    val subTasksEvent = subTaskEventChannel.receiveAsFlow()

    private val subTasksFlow = combine(
        taskId.asFlow(), searchQuery.asFlow(), preferencesFlow
    ) { tId, query, filterPreferences ->
        Triple(tId, query, filterPreferences)
    }.flatMapLatest { (tid, query, filterPreferences) ->
        repository.getAllSubTasks(
            tid,
            query,
            filterPreferences.sortOrder
        ).distinctUntilChanged()
    }

    val subTasks = subTasksFlow
    fun onSortOrderSelected(sortOrder: SortOrder, context: Context) = viewModelScope.launch {
        preferenceManager.updateSortOrder2(sortOrder, context)
    }

    fun onSubTaskSwiped(subTask: SubTask) = viewModelScope.launch {
        repository.deleteSubTask(subTask)
        subTaskEventChannel.send(SubTaskEvent.ShowUndoDeleteTaskMessage(subTask))
    }

    fun onSubTaskCheckedChanged(subTask: SubTask, isChecked: Boolean) = viewModelScope.launch {
        repository.updateSubTask(subTask.copy(isDone = isChecked))
    }

    fun onUndoDeleteClick(subTask: SubTask) = viewModelScope.launch {
        repository.insertSubTask(subTask)
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        subTaskEventChannel.send(SubTaskEvent.NavigateToAllCompletedScreen)
    }

    fun insertSubTask(subTask: SubTask) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertSubTask(subTask)
    }

    fun updateSubTask(subTask: SubTask) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateSubTask(subTask)
    }

    fun deleteSubTask(subTask: SubTask) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteSubTask(subTask)
    }

    fun deleteAllCompletedSubTasks(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllCompletedSubTask(id)
    }

    fun getBySubTaskId(id: Int){
        viewModelScope.launch {
            _subTaskFlow.value = repository.getBySubTaskId(id)
        }
    }

    fun resetSubTaskValue(){
        _subTaskFlow.value = null
    }


    fun update(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTask(task)
    }

    fun shareTask(context: Context, text: String?) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            if (!text.isNullOrEmpty()) {
                putExtra(Intent.EXTRA_TEXT, text)
                type = SHARE_FORMAT
            }
        }
        try {
            context.startActivity(sendIntent)
        } catch (e: ActivityNotFoundException) {
            context.toast { context.getString(R.string.empty_task) }
        }
    }

    fun setSubTask(subTask: SubTask){
        _subTaskFlow.value = subTask
    }
}