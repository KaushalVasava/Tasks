package com.lahsuak.apps.mytask.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "status") var isDone: Boolean = false,
    @ColumnInfo(name = "importance") var isImp: Boolean = false,
    @ColumnInfo(name = "reminder") var reminder: String? = null,
    @ColumnInfo(name = "progress") var progress: Float = -1f,
    @ColumnInfo(name = "subtask") var subTaskList: String? = null,
    @ColumnInfo(name = "date") var date: Long,
)