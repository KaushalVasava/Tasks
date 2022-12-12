package com.lahsuak.apps.mytask.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sub_task_table")
data class SubTask(
    val id: Int,
    var subTitle: String,
    var isDone: Boolean = false,
    var isImportant: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    val sId: Int,
    var dateTime: Long? = System.currentTimeMillis()
)
