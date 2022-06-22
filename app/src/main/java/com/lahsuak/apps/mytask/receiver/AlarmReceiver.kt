package com.lahsuak.apps.mytask.receiver

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.util.Constants.CHANNEL_ID
import com.lahsuak.apps.mytask.data.util.Constants.SEPARATOR
import com.lahsuak.apps.mytask.data.util.Constants.TASK_KEY
import com.lahsuak.apps.mytask.data.util.Constants.TASK_TITLE
import com.lahsuak.apps.mytask.ui.fragments.SubTaskFragmentArgs


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        var textId: String = intent!!.getStringExtra(TASK_KEY) ?: ""
        val textTitle: String = intent.getStringExtra(TASK_TITLE) ?: ""
        if (textId != "") {
            textId = textId.substringBefore(SEPARATOR)
            val status = textId.substringAfter(SEPARATOR).toBoolean()
            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.main_nav_graph)
                .setDestination(R.id.subTaskFragment)
                .setArguments(
                    SubTaskFragmentArgs.Builder(
                        textId.toInt(), textTitle, status
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


            val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
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