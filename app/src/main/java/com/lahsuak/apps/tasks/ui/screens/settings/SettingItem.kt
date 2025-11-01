
package com.lahsuak.apps.tasks.ui.screens.settings

import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

data class SettingItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val placeholder: String? = null,
    @DrawableRes
    val icon: Int? = null,
    val items: List<Pair<String, Any>> = emptyList(),
    val initialValue: Boolean = false,
    val type: PreferenceType,
    val action: (Any, Int) -> Unit,
    val onCheckedChange: (Boolean) -> Unit,
) {
    var checked by mutableStateOf(initialValue)
}