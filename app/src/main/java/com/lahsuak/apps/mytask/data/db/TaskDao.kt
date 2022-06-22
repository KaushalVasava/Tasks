package com.lahsuak.apps.mytask.data.db

import androidx.room.*
import com.lahsuak.apps.mytask.data.SortOrder
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    //user methods
    @Query("SELECT * FROM USER_TABLE")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM USER_TABLE WHERE userId=:userID")
    suspend fun getUserById(userID: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Delete
    suspend fun delete(user: User)

    // task methods
    fun getAllTasks(
        userId: String,
        query: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean,
    ): Flow<List<Task>> =
        when (sortOrder) {
            SortOrder.BY_DATE -> getAllTaskByDate(userId, query, hideCompleted)
            SortOrder.BY_NAME -> getAllTaskByName(userId, query, hideCompleted)
        }

    @Query("SELECT * FROM task_table WHERE (userId = :userID) AND (status!= :hideCompleted OR status = 0) AND title LIKE '%' || :searchQuery || '%' ORDER BY importance DESC,title")
    fun getAllTaskByName(
        userID: String,
        searchQuery: String, hideCompleted: Boolean,
    ): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (userId = :userID) AND (status!= :hideCompleted OR status = 0) AND title LIKE '%' || :searchQuery || '%' ORDER BY importance DESC,id DESC")
    fun getAllTaskByDate(
        userID: String,
        searchQuery: String,
        hideCompleted: Boolean,
    ): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Task)

    @Delete
    suspend fun delete(note: Task)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(note: Task)

    @Query("SELECT * FROM TASK_TABLE WHERE id=:todoID")
    suspend fun getById(todoID: Int): Task

    @Query("DELETE FROM TASK_TABLE WHERE status = 1")
    suspend fun deleteAllCompletedTask()

    @Query("DELETE FROM TASK_TABLE")
    suspend fun deleteAllTask()
    //subtask methods

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(task: SubTask)

    @Delete
    suspend fun deleteSubTask(task: SubTask)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateSubTask(task: SubTask)

    fun getAllSubTasks(
        id: Int,
        query: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean,
    ): Flow<List<SubTask>> =
        when (sortOrder) {
            SortOrder.BY_DATE -> getAllSubTaskByDate(id, query, hideCompleted)
            SortOrder.BY_NAME -> getAllSubTaskByName(id, query, hideCompleted)
        }

    @Query("SELECT * FROM sub_task_table WHERE id=:id AND (isDone!= :hideCompleted OR isDone = 0) AND subTitle LIKE '%' || :searchQuery || '%' ORDER BY isImportant DESC,subTitle")
    fun getAllSubTaskByName(
        id: Int,
        searchQuery: String,
        hideCompleted: Boolean,
    ): Flow<List<SubTask>>

    @Query("SELECT * FROM sub_task_table WHERE id=:id AND (isDone!= :hideCompleted OR isDone = 0) AND subTitle LIKE '%' || :searchQuery || '%' ORDER BY isImportant DESC,sId DESC")
    fun getAllSubTaskByDate(
        id: Int,
        searchQuery: String,
        hideCompleted: Boolean,
    ): Flow<List<SubTask>>

    @Query("DELETE FROM sub_task_table WHERE isDone = 1 AND id=:taskId")
    suspend fun deleteAllCompletedSubTask(taskId: Int) //please add id for deleting =   BUG

    @Query("DELETE FROM SUB_TASK_TABLE where id=:id")
    suspend fun deleteAllSubTask(id: Int)

    @Query("SELECT * FROM sub_task_table WHERE sId=:sID")
    suspend fun getBySubTaskId(sID: Int): SubTask
}
