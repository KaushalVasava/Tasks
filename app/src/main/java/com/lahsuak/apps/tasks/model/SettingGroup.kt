package com.lahsuak.apps.tasks.model

import androidx.annotation.DrawableRes
data class SettingGroup(
    val title: String,
    val items: List<SettingItemModel>,
)

data class SettingItemModel(
    @DrawableRes
    val drawableRes: Int,
    val title: String,
    val subTitle: String,
    val isEnable: Boolean? = null,
)