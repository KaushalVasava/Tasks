package com.lahsuak.apps.mytask.receiver

import android.annotation.SuppressLint
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.ui.fragments.SubTaskFragmentArgs
import com.lahsuak.apps.mytask.util.AppConstants.NOTIFICATION_CHANNEL_ID
import com.lahsuak.apps.mytask.util.AppConstants.SEPARATOR
import com.lahsuak.apps.mytask.util.AppConstants.TASK_KEY
import com.lahsuak.apps.mytask.util.AppConstants.TASK_TITLE

class AlarmReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent?) {
        var textId: String = intent!!.getStringExtra(TASK_KEY) ?: ""
        val textTitle: String = intent.getStringExtra(TASK_TITLE) ?: ""
        Log.d("TAG", "onReceive: $textId and $textTitle")
        if (textId.isNotEmpty()) {
            textId = textId.substringBefore(SEPARATOR)
            val status = textId.substringAfter(SEPARATOR).toBoolean()
            val task = Task(id = textId.toInt(), title = textTitle, isDone = status)

            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.main_nav_graph)
                .setDestination(R.id.subTaskFragment)
                .setArguments(
                    SubTaskFragmentArgs.Builder(
                        task, false, null
                    ).build().toBundle()
                )
                .createPendingIntent()
//        val resumeIntent = Intent(context, MainActivity::class.java).apply {
//            action = "completed"
//        putExtra("status",true)
//        }
//        val sendPendingIntent = PendingIntent.getActivity(context, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//        val extras = intent.extras
//        val i = Intent("completed")
//        // Data you need to pass to activity
//        i.putExtra("status", true)
//
//        context.sendBroadcast(i)
            //val sendPendingIntent = PendingIntent.getBroadcast(context, 2, i, PendingIntent.FLAG_UPDATE_CURRENT)

//        val pendingIntent2 = NavDeepLinkBuilder(context)
//            .setGraph(R.navigation.main_nav_graph)
//            .setDestination(R.id.addUpdateFragment)
//            .setArguments(
//                AddUpdateFragmentArgs.Builder(
//                    textId,textTitle,true
//            ).build().toBundle())
//            .createPendingIntent()


            val notification: Notification =
                NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_reminder)
                    .setContentTitle(textTitle)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setLights(Color.WHITE, 200, 200)
                    .setContentIntent(pendingIntent)
                    //.addAction(R.drawable.ic_reminder,context.getString(R.string.mark_completed),sendPendingIntent)
                    .build()

            val notificationCompat = NotificationManagerCompat.from(context)
            //notification.build().flags.and(Notification.FLAG_AUTO_CANCEL)
            //  notification.build().flags = notification.build().flags or Notification.FLAG_AUTO_CANCEL
            notificationCompat.notify(1, notification)
        }
    }
}