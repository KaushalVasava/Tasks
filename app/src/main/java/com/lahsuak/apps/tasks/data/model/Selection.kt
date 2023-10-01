package com.lahsuak.apps.tasks.data.model

data class Selection<T>(
    val model: T,
    var isSelected: Boolean,
)