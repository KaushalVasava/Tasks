package com.lahsuak.apps.mytask.data.repository

import com.lahsuak.apps.mytask.data.SortOrder
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.data.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    suspend fun insertTask(task: Task)

    suspend fun deleteTask(task: Task)

    suspend fun updateTask(task: Task)

    fun getAllTasks(
        searchQuery: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean,
    ): Flow<List<Task>>

    suspend fun getById(id: Int): Task

    suspend fun deleteAllCompletedTask()

    suspend fun deleteAllTasks()

    //subtask methods
    suspend fun insertSubTask(subTask: SubTask)

    suspend fun deleteSubTask(subTask: SubTask)

    suspend fun updateSubTask(subTask: SubTask)

    fun getAllSubTasks(
        id: Int,
        query: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean,
    ): Flow<List<SubTask>>

    suspend fun deleteAllCompletedSubTask(id: Int)

    suspend fun deleteAllSubTasks(id: Int)

    suspend fun getBySubTaskId(id: Int): SubTask
}