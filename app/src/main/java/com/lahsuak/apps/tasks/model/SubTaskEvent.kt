package com.lahsuak.apps.tasks.model

import com.lahsuak.apps.tasks.data.model.SubTask

sealed class SubTaskEvent {
    data class ShowUndoDeleteTaskMessage(val subTask: SubTask) : SubTaskEvent()
    object NavigateToAllCompletedScreen : SubTaskEvent()
}

