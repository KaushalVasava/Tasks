package com.lahsuak.apps.mytask.data.repository

import com.lahsuak.apps.mytask.data.SortOrder
import com.lahsuak.apps.mytask.data.db.TaskDao
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.*

class TodoRepositoryImpl(
    private val dao: TaskDao,
) : TodoRepository {

    override suspend fun insertTodo(task: Task) {
        dao.insert(task)
    }

    override suspend fun deleteTodo(task: Task) {
        dao.delete(task)
    }

    override suspend fun updateTodo(task: Task) {
        dao.update(task)
    }

    override fun getAllTasks(
        searchQuery: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean,
    ): Flow<List<Task>> {
        return dao.getAllTasks(searchQuery, sortOrder, hideCompleted)
    }

    override suspend fun getById(id: Int): Task {
        return dao.getById(id)
    }

    override suspend fun deleteAllCompletedTask() {
        dao.deleteAllCompletedTask()
    }

    override suspend fun deleteAllTasks() {
        dao.deleteAllTask()
    }

    //subtask methods

    override suspend fun insertSubTask(subTask: SubTask) {
        dao.insertSubTask(subTask)
    }

    override suspend fun deleteSubTask(subTask: SubTask) {
        dao.deleteSubTask(subTask)
    }

    override suspend fun updateSubTask(subTask: SubTask) {
        dao.updateSubTask(subTask)
    }

    override fun getAllSubTasks(
        id: Int,
        query: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean,
    ): Flow<List<SubTask>> {
        return dao.getAllSubTasks(id, query, sortOrder, hideCompleted)
    }

    override suspend fun deleteAllCompletedSubTask(id: Int) {
        dao.deleteAllCompletedSubTask(id)
    }

    override suspend fun deleteAllSubTasks(id: Int) {
        dao.deleteAllSubTask(id)
    }

    override suspend fun getBySubTaskId(id: Int): SubTask {
        return dao.getBySubTaskId(id)
    }

}