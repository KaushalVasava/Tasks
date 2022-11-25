package com.lahsuak.apps.mytask.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.data.model.Task

@Database(
    entities = [Task::class, SubTask::class],
    version = 5
)
abstract class TaskDatabase : RoomDatabase() {
    abstract val dao: TaskDao
}