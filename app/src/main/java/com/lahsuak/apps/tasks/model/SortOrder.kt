package com.lahsuak.apps.tasks.model

import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.ui.MainActivity.Companion.activityContext

enum class SortOrder(val type: String) {
    BY_NAME(activityContext!!.getString(R.string.name)),
    BY_NAME_DESC(activityContext!!.getString(R.string.name_desc)),
    BY_DATE(activityContext!!.getString(R.string.date)),
    BY_DATE_DESC(activityContext!!.getString(R.string.date_desc)),
    BY_CATEGORY(activityContext!!.getString(R.string.category)),
    BY_CATEGORY_DESC(activityContext!!.getString(R.string.category_desc))
    ;

    companion object {
        fun getOrder(order: Int): SortOrder {
            return when (order) {
                0 -> BY_NAME
                1 -> BY_NAME_DESC
                2 -> BY_DATE
                3 -> BY_DATE_DESC
                4 -> BY_CATEGORY
                5 -> BY_CATEGORY_DESC
                else -> {
                    throw IllegalArgumentException()
                }
            }
        }
    }
}