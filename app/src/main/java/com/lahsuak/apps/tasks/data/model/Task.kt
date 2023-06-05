package com.lahsuak.apps.tasks.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "status") var isDone: Boolean = false,
    @ColumnInfo(name = "importance") var isImp: Boolean = false,
    @ColumnInfo(name = "reminder") var reminder: Long? = null,
    @ColumnInfo(name = "progress") var progress: Float = -1f,
    @ColumnInfo(name = "subtask") var subTaskList: String? = null,
    @ColumnInfo(name = "color") val color: Int = 0,
    @ColumnInfo(name = "date") var date: Long? = null,
) : Parcelable