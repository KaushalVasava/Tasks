package com.lahsuak.apps.tasks.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lahsuak.apps.tasks.data.db.TaskDao
import com.lahsuak.apps.tasks.data.db.TaskDatabase
import com.lahsuak.apps.tasks.data.model.SortOrder
import com.lahsuak.apps.tasks.data.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*

class TaskDaoTest {
    private lateinit var taskDatabase: TaskDatabase
    private lateinit var taskDao: TaskDao

    @get:Rule
    val instantExecutor = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        taskDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TaskDatabase::class.java
        ).allowMainThreadQueries().build()
        taskDao = taskDatabase.dao
    }


    @Test
    fun insertTask_expectedSingleTask() = runBlocking {
        val task = Task(1, "task 1")
        taskDao.insert(task)
        val result =
            taskDao.getAllTasks("", SortOrder.BY_NAME).first()
        Assert.assertEquals(1, result.size)
        Assert.assertEquals("task 1", result[0].title)
    }

    @Test
    fun updateTask_expectedUpdateTask() = runBlocking {
        val task = Task(1, "task 1")
        taskDao.insert(task)
        taskDao.update(
            task.copy(
                title = "newTask"
            )
        )
        val result =
            taskDao.getAllTasks("", SortOrder.BY_NAME).first()
        Assert.assertEquals("newTask", result[0].title)
    }

    @Test
    fun deleteTask_expectedEmptyTask() = runBlocking {
        val task = Task(1, "task 1")
        taskDao.insert(task)
        taskDao.delete(task)
        val result =
            taskDao.getAllTasks("", SortOrder.BY_NAME).asLiveData().getOrAwaitValue()
        Assert.assertEquals(0, result.size)
    }

    @After
    fun tearDown() {
        taskDatabase.close()
    }
}