@file:Suppress("DEPRECATION")

package com.lahsuak.apps.tasks.ui.screens.components

import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Patterns
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.util.LinkifyCompat
import  androidx.compose.ui.graphics.Color
import android.graphics.Paint

@Composable
fun LinkifyText(
    text: String?, fontSize: Float, color: Color,
    textDecoration: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val customLinkifyTextView = remember {
        TextView(context)
    }
    AndroidView(modifier = modifier, factory = { customLinkifyTextView }) { textView ->
        textView.text = text ?: ""
        textView.textSize = fontSize
        textView.paintFlags = if (textDecoration) {
            textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        textView.setTextColor(color.hashCode())
        LinkifyCompat.addLinks(textView, Linkify.ALL)
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}