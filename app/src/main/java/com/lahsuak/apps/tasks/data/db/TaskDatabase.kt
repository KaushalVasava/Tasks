package com.lahsuak.apps.tasks.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lahsuak.apps.tasks.data.model.Notification
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task

@Database(
    entities = [Task::class, SubTask::class, Notification::class],
    version = 4
)
abstract class TaskDatabase : RoomDatabase() {
    abstract val dao: TaskDao
    abstract val notificationDao: NotificationDao
}
