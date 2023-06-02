package com.lahsuak.apps.mytask.util

interface SelectionListener {
    fun getViewType(): Boolean

    fun getCounter(): Int

    fun getActionModeStatus(): Boolean

    fun isAllSelected(): Boolean

    fun getItemStatus(position: Int): Boolean

    fun setItemStatus(status: Boolean, position: Int)

    fun getSelectedItemEmpty(): Boolean
}