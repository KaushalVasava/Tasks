package com.lahsuak.apps.tasks.ui.screens.settings

import java.util.UUID

data class SettingModel(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val items: List<SettingItem>,
)