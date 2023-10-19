package com.lahsuak.apps.tasks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lahsuak.apps.tasks.util.AppConstants

@Entity(tableName = AppConstants.SUB_TASK_TABLE)
data class SubTask(
    val id: Int,
    var subTitle: String,
    var isDone: Boolean = false,
    var isImportant: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    val sId: Int,
    var dateTime: Long? = System.currentTimeMillis(),
    var reminder: Long? = null
)