package com.lahsuak.apps.tasks.model

import com.lahsuak.apps.tasks.data.model.Task

sealed class TaskEvent {
    object Initial:TaskEvent()
    data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
    object NavigateToAllCompletedScreen : TaskEvent()
}