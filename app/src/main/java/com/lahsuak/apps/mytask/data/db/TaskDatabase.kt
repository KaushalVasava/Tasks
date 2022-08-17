package com.lahsuak.apps.mytask.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.data.model.Task

@Database(
    entities = [Task::class, SubTask::class],
    version = 2
)
abstract class TaskDatabase : RoomDatabase() {
    abstract val dao: TaskDao
//    companion object{
//        val migration_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                   database.execSQL("ALTER TABLE task_table ADD COLUMN subtask STRING NULL")
//            }
//        }
//
//    }
}