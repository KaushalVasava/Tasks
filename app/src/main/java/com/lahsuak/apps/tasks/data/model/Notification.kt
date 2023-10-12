package com.lahsuak.apps.tasks.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lahsuak.apps.tasks.util.AppConstants.NOTIFICATION_TABLE
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = NOTIFICATION_TABLE)
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val taskId: Int,
    val title: String,
    val date: Long
):Parcelable