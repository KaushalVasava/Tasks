package com.lahsuak.apps.tasks.ui.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskWidgetUpdater @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun updateTaskWidgets(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            runCatching {
                TaskWidgetCompose.updateAll(appContext)
            }.onFailure {
                Log.e("TaskWidgetUpdater", "Failed to update task widgets", it)
            }
        }
    }
}
