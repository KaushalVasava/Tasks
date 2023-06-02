package com.lahsuak.apps.mytask.receiver

data class Reminder(
    val time: Long,
    val taskId: String,
    val taskTitle: String
)