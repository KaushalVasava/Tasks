package com.lahsuak.apps.mytask.util

import android.util.Log
import com.lahsuak.apps.mytask.BuildConfig

private const val LOG_BOTH_TAG = "[logBoth]"
fun Throwable.logBoth(): Throwable {
    if (BuildConfig.DEBUG) {
        Log.e(LOG_BOTH_TAG, this.message, this)
    }
    return this
}