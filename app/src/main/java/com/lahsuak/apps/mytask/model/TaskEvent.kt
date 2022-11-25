package com.lahsuak.apps.mytask.model

import com.lahsuak.apps.mytask.data.model.Task

sealed class TaskEvent {
    data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
    object NavigateToAllCompletedScreen : TaskEvent()
}