
package com.lahsuak.apps.tasks.ui.screens.settings

import androidx.annotation.DrawableRes
import java.util.UUID

data class SettingItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val placeholder: String? = null,
    @DrawableRes
    val icon: Int? = null,
    val items: List<Pair<String, Any>> = emptyList(),
    val initialValue: Boolean? = null,
    val type: PreferenceType,
    val action: (Any, Int) -> Unit,
    val onCheckedChange: (Boolean) -> Unit,
)