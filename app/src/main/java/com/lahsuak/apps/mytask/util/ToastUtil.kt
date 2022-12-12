package com.lahsuak.apps.mytask.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.lahsuak.apps.mytask.BuildConfig
import com.lahsuak.apps.mytask.MyTaskApp

inline fun Context?.toast(crossinline msgProvider: () -> String) {
    this ?: return
    val block = {
        Toast.makeText(this, msgProvider(), Toast.LENGTH_SHORT).show()
    }
    postBlockInMainLooper(block)
}

inline fun Context?.toastLong(crossinline msgProvider: () -> String) {
    this ?: return
    val block = {
        Toast.makeText(this, msgProvider(), Toast.LENGTH_LONG).show()
    }
    postBlockInMainLooper(block)
}

inline fun toastDebug(crossinline msgProvider: () -> String) {
    if (BuildConfig.DEBUG) {
        val msg = msgProvider().toString()
        val block = {
            Toast.makeText(MyTaskApp.appContext, "[DEBUG] $msg", Toast.LENGTH_LONG).show()
        }
        postBlockInMainLooper(block)
    }
}

fun postBlockInMainLooper(block: () -> Unit) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        block()
    } else {
        Handler(Looper.getMainLooper()).post(block)
    }
}