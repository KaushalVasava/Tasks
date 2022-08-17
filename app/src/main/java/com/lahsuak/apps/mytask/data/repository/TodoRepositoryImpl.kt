package com.lahsuak.apps.mytask.data.repository

import com.lahsuak.apps.mytask.data.SortOrder
import com.lahsuak.apps.mytask.data.db.TaskDao
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.data.model.Task
import kotlinx.coroutines.flow.Flow

class TodoRepositoryImpl(
    private val dao: TaskDao,
) : TodoRepository {

    override suspend fun insertTodo(todo: Task) {
        dao.insert(todo)
    }

    override suspend fun deleteTodo(todo: Task) {
        dao.delete(todo)
    }

    override suspend fun updateTodo(todo: Task) {
        dao.update(todo)
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

    override suspend fun insertSubTask(todo: SubTask) {
        dao.insertSubTask(todo)
    }

    override suspend fun deleteSubTask(todo: SubTask) {
        dao.deleteSubTask(todo)
    }

    override suspend fun updateSubTask(todo: SubTask) {
        dao.updateSubTask(todo)
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