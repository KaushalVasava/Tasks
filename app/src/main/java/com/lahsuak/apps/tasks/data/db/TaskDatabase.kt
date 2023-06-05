package com.lahsuak.apps.tasks.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task

@Database(
    entities = [Task::class, SubTask::class],
    version = 1
)
abstract class TaskDatabase : RoomDatabase() {
    abstract val dao: TaskDao
}