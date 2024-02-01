package com.lahsuak.apps.tasks.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.ui.MainActivity

object NotificationUtil {
    @SuppressLint("MissingPermission")
    fun createNotificationDaily(
        context: Context,
        title: String,
        msg: String,
    ) {
        createNotificationChannel(
            context,
            AppConstants.NOTIFICATION_DAILY_CHANNEL_ID,
            AppConstants.NOTIFICATION_DAILY_CHANNEL_NAME,
            AppConstants.NOTIFICATION_DAILY_CHANNEL_DESCRIPTION
        )
        val defaultAction = Intent(context, MainActivity::class.java)
            .setAction(Intent.ACTION_DEFAULT)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val notification =
            NotificationCompat.Builder(context, AppConstants.NOTIFICATION_DAILY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        0,
                        defaultAction,
                        PendingIntent.FLAG_UPDATE_CURRENT.toImmutableFlag()
                    )
                )
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        if (isPermissionGranted(context)) {
            NotificationManagerCompat.from(context).notify(1, notification)
        }
    }

    @SuppressLint("MissingPermission")
    fun createNotification(
        context: Context,
        id: Int,
        title: String,
        parentTitle: String?,
        isDone: Boolean,
        startDate: Long,
        endDate: Long,
    ) {
        createNotificationChannel(
            context,
            AppConstants.NOTIFICATION_CHANNEL_ID,
            AppConstants.NOTIFICATION_CHANNEL_NAME,
            AppConstants.NOTIFICATION_CHANNEL_DESCRIPTION
        )
        val task = if (parentTitle != null) {
            Task(id = id, title = parentTitle)
        } else {
            val lastDate = if (endDate == -1L) null else endDate
            Task(id, title, isDone, startDate = startDate, endDate = lastDate)
        }
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            "${AppConstants.DEEP_LINK_SUBTASK}${task.id}/true".toUri(),
            context,
            MainActivity::class.java
        )
        val flag = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else PendingIntent.FLAG_UPDATE_CURRENT
        val deepLinkPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(0, flag)
        }
//        deepLinkPendingIntent.send()

        val notification = NotificationCompat.Builder(context, AppConstants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_reminder)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setContentIntent(deepLinkPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setLights(Color.WHITE, 200, 200)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (isPermissionGranted(context)) {
            NotificationManagerCompat.from(context).notify(1, notification)
        }
    }

    private fun createNotificationChannel(
        context: Context,
        id: String,
        name: String,
        desc: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                id,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = desc
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun isPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun Int.toImmutableFlag(): Int {
        return PendingIntent.FLAG_IMMUTABLE or this
    }
}