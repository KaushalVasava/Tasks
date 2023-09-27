package com.lahsuak.apps.tasks.data.model

enum class SortOrder {
    BY_NAME,
    BY_NAME_DESC,
    BY_DATE,
    BY_DATE_DESC,
    BY_CATEGORY,
    BY_CATEGORY_DESC
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