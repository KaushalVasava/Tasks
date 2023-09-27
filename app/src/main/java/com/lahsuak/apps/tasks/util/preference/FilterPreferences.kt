package com.lahsuak.apps.tasks.util.preference

import com.lahsuak.apps.tasks.data.model.SortOrder

data class FilterPreferences(
    val sortOrder: SortOrder,
    val hideCompleted: Boolean,
    val viewType: Boolean,
)