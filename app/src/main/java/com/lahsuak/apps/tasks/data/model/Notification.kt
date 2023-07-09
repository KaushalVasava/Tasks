package com.lahsuak.apps.tasks.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "notification_table")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val taskId: Int,
    val title: String,
    val date: Long
):Parcelable