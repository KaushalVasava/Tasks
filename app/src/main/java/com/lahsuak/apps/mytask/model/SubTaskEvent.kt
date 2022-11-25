package com.lahsuak.apps.mytask.model

import com.lahsuak.apps.mytask.data.model.SubTask

sealed class SubTaskEvent {
    data class ShowUndoDeleteTaskMessage(val subTask: SubTask) : SubTaskEvent()
    object NavigateToAllCompletedScreen : SubTaskEvent()
}

