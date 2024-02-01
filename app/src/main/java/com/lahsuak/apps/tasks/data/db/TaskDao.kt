package com.lahsuak.apps.tasks.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.model.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // task methods
    fun getAllTasks(
        query: String,
        sortOrder: SortOrder,
    ): Flow<List<Task>> =
        when (sortOrder) {
            SortOrder.BY_NAME -> getAllTaskByName(query)
            SortOrder.BY_NAME_DESC -> getAllTaskByNameDesc(query)
            SortOrder.BY_DATE -> getAllTaskByDate(query)
            SortOrder.BY_DATE_DESC -> getAllTaskByDateDesc(query)
            SortOrder.BY_CATEGORY -> getAllTaskByCategory(query)
            SortOrder.BY_CATEGORY_DESC -> getAllTaskByCategoryDesc(query)
        }

    @Query(
        "SELECT * FROM task_table WHERE title LIKE '%' || :searchQuery || '%' ORDER BY importance DESC,title ASC"
    )
    fun getAllTaskByName(searchQuery: String): Flow<List<Task>>

    @Query(
        "SELECT * FROM task_table WHERE title LIKE '%' || :searchQuery || '%' ORDER BY importance DESC,title DESC"
    )
    fun getAllTaskByNameDesc(searchQuery: String): Flow<List<Task>>

    //date DESC
    @Query(
        "SELECT * FROM task_table WHERE title LIKE '%' || :searchQuery || '%' ORDER BY importance DESC, start_date ASC"
    )
    fun getAllTaskByDate(searchQuery: String): Flow<List<Task>>

    @Query(
        "SELECT * FROM task_table WHERE title LIKE '%' || :searchQuery || '%' ORDER BY importance DESC, start_date DESC"
    )
    fun getAllTaskByDateDesc(searchQuery: String): Flow<List<Task>>

    @Query(
        "SELECT * FROM task_table WHERE title LIKE '%' || :searchQuery || '%' ORDER BY importance DESC, color ASC"
    )
    fun getAllTaskByCategory(searchQuery: String): Flow<List<Task>>

    @Query(
        "SELECT * FROM task_table WHERE title LIKE '%' || :searchQuery || '%' ORDER BY importance DESC, color DESC"
    )
    fun getAllTaskByCategoryDesc(searchQuery: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(task: Task)

    @Query("SELECT * FROM TASK_TABLE WHERE id=:taskId")
    suspend fun getById(taskId: Int): Task

    @Query("DELETE FROM TASK_TABLE WHERE status = 1")
    suspend fun deleteAllCompletedTask()

    @Query("DELETE FROM TASK_TABLE")
    suspend fun deleteAllTask()

    //subtask methods

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTask)

    @Delete
    suspend fun deleteSubTask(subTask: SubTask)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSubTask(subTask: SubTask)

    fun getAllSubTasks(
        id: Int,
        query: String,
        sortOrder: SortOrder
    ): Flow<List<SubTask>> =
        when (sortOrder) {
            SortOrder.BY_NAME -> getAllSubTaskByName(id, query)
            SortOrder.BY_NAME_DESC -> getAllSubTaskByNameDesc(id, query)
            SortOrder.BY_DATE -> getAllSubTaskByDate(id, query)
            SortOrder.BY_DATE_DESC -> getAllSubTaskByDateDesc(id, query)
            else -> {
                getAllSubTaskByName(id, query)
            }
        }

    @Query("SELECT * FROM sub_task_table WHERE id=:id AND subTitle LIKE '%' || :searchQuery || '%' ORDER BY isImportant DESC, subTitle ASC")
    fun getAllSubTaskByName(
        id: Int,
        searchQuery: String
    ): Flow<List<SubTask>>

    @Query("SELECT * FROM sub_task_table WHERE id=:id AND subTitle LIKE '%' || :searchQuery || '%' ORDER BY isImportant DESC,dateTime ASC")
    fun getAllSubTaskByDate(
        id: Int,
        searchQuery: String
    ): Flow<List<SubTask>>

    @Query("SELECT * FROM sub_task_table WHERE id=:id AND subTitle LIKE '%' || :searchQuery || '%' ORDER BY isImportant DESC, subTitle DESC")
    fun getAllSubTaskByNameDesc(
        id: Int,
        searchQuery: String
    ): Flow<List<SubTask>>

    @Query("SELECT * FROM sub_task_table WHERE id=:id AND subTitle LIKE '%' || :searchQuery || '%' ORDER BY isImportant DESC,dateTime DESC")
    fun getAllSubTaskByDateDesc(
        id: Int,
        searchQuery: String
    ): Flow<List<SubTask>>

    @Query("DELETE FROM sub_task_table WHERE isDone = 1 AND id=:taskId")
    suspend fun deleteAllCompletedSubTask(taskId: Int) //please add id for deleting =   BUG

    @Query("DELETE FROM SUB_TASK_TABLE where id=:id")
    suspend fun deleteAllSubTask(id: Int)

    @Query("SELECT * FROM sub_task_table WHERE sId=:sID")
    suspend fun getBySubTaskId(sID: Int): SubTask
}