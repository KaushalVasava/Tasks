package com.lahsuak.apps.tasks.ui.widget

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.padding
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.ui.MainActivity


object TaskWidgetCompose : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Image(
                provider = ImageProvider(R.drawable.ic_edit), contentDescription = "Add",
                modifier = GlanceModifier
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp)
                    .cornerRadius(8.dp)
                    .clickable(
                        actionRunCallback(TaskActionCallback::class.java)
                    )
            )
        }
    }
}

class TaskWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = TaskWidgetCompose
}

class TaskActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val deepLinkIntent = Intent(
                Intent.ACTION_VIEW,
                "myapp://kmv.com/shortcut".toUri(),
                context,
                MainActivity::class.java
            )
            val flag = if(Build.VERSION.SDK_INT > Build.VERSION_CODES.S){
                PendingIntent.FLAG_IMMUTABLE
            } else PendingIntent.FLAG_UPDATE_CURRENT
            val deepLinkPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(deepLinkIntent)
                getPendingIntent(0, flag)
            }
            deepLinkPendingIntent.send()
        }
        TaskWidgetCompose.update(context, glanceId)
    }
}