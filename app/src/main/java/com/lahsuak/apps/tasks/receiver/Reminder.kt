package com.lahsuak.apps.tasks.receiver

data class Reminder(
    val time: Long,
    val taskId: String,
    val taskTitle: String
)