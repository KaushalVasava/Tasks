package com.lahsuak.apps.tasks.data.repository

import com.lahsuak.apps.tasks.model.SortOrder
import com.lahsuak.apps.tasks.data.db.TaskDao
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepositoryImpl(private val dao: TaskDao) : TaskRepository {

    override suspend fun insertTask(task: Task) {
        dao.insert(task)
    }

    override suspend fun deleteTask(task: Task) {
        dao.delete(task)
    }

    override suspend fun updateTask(task: Task) {
        dao.update(task)
    }

    override fun getAllTasks(
        searchQuery: String,
        sortOrder: SortOrder
    ): Flow<List<Task>> {
        return dao.getAllTasks(searchQuery, sortOrder)
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
    ): Flow<List<SubTask>> {
        return dao.getAllSubTasks(id, query, sortOrder)
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