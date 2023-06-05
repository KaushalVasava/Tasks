package com.lahsuak.apps.tasks.util

import android.util.Log
import com.lahsuak.apps.tasks.BuildConfig

const val LOG_ERROR_TAG = "[logError]"
inline fun Throwable.logError(causeProvider: Throwable.() -> Throwable = { this }) {
    val cause = this.causeProvider()
    if (BuildConfig.DEBUG) {
        Log.e(LOG_ERROR_TAG, this.message, cause)
        throw cause
    }
}