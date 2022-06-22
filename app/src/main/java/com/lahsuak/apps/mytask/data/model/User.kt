package com.lahsuak.apps.mytask.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val userId: String,
    val userName: String
)
