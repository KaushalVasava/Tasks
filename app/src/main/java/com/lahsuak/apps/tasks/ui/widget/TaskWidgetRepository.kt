package com.lahsuak.apps.tasks.ui.widget

import com.lahsuak.apps.tasks.data.db.TaskDao
import com.lahsuak.apps.tasks.data.model.Task
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskWidgetRepository @Inject constructor(
    private val taskDao: TaskDao
) {

    suspend fun getRecentTasks(limit: Int = 10): List<Task> {
        return taskDao.getPendingTasksSnapshot(limit)
    }
    
    suspend fun getTaskById(taskId: Int): Task {
        return taskDao.getById(taskId)
    }
    
    suspend fun updateTaskStatus(task: Task, isDone: Boolean) {
        taskDao.update(task.copy(isDone = isDone))
    }
}
