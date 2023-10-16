package com.lahsuak.apps.tasks.ui.widget

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import androidx.compose.ui.res.stringResource
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
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.ui.MainActivity
import com.lahsuak.apps.tasks.ui.theme.lightBlue
import com.lahsuak.apps.tasks.util.NavigationConstants


object TaskWidgetCompose : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Image(
                provider = ImageProvider(R.drawable.ic_edit),
                contentDescription = stringResource(id = R.string.add_task),
                modifier = GlanceModifier
                    .background(lightBlue)
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
        updateAppWidgetState(context, glanceId) {
            val deepLinkIntent = Intent(
                Intent.ACTION_VIEW,
                NavigationConstants.Key.ADD_UPDATE_TASK_DEEP_LINK.toUri(),
                context,
                MainActivity::class.java
            ).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
            }
            val flag = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
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