package com.lahsuak.apps.tasks.util.preference

import com.lahsuak.apps.tasks.model.SortOrder

data class FilterPreferences(
    val sortOrder: SortOrder,
    val viewType: Boolean,
)