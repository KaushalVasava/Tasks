package com.lahsuak.apps.mytask.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.lahsuak.apps.mytask.util.AppConstants
import com.lahsuak.apps.mytask.util.AppConstants.REMINDER_DATA
import com.lahsuak.apps.mytask.util.AppConstants.REMINDER_KEY

class BootReceiver : BroadcastReceiver() {
    companion object {
        val timeList = ArrayList<Reminder>()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("TAG", "onReceive:OUTSIDE")

        if(Intent.ACTION_BOOT_COMPLETED == intent?.action){
            Log.d("TAG", "onReceive: REBOOT ")
        }
        try {
            val sharedPref = context!!.getSharedPreferences(REMINDER_DATA, MODE_PRIVATE)
            val jsonString = sharedPref.getString(REMINDER_KEY, null)
            val typeToken = object : TypeToken<ArrayList<Reminder>>() {}.type
            if (jsonString != null) {
                timeList.clear()
                val data: ArrayList<Reminder> =
                    GsonBuilder().create().fromJson(jsonString, typeToken)
                timeList.addAll(data)
            }
            for (item in timeList) {
                val intent1 = Intent(context, AlarmReceiver::class.java)
                intent1.putExtra(AppConstants.TASK_KEY, item.taskId)
                intent1.putExtra(AppConstants.TASK_TITLE, item.taskTitle)

                //intent.putExtra(TASK_STATUS, task.isDone)
                intent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val taskId = item.taskId.substringBefore(AppConstants.SEPARATOR).toInt()

                val pendingIntentFlag =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        0
                    }
                val pendingIntent = PendingIntent.getBroadcast(
                    context, taskId, intent1, pendingIntentFlag
                )
                val alarmManager =
                    context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        item.time,
                        pendingIntent
                    )
                }
            }
        } catch (e: Exception) {
            Log.d("TAG", "onReceive: ${e.message}")
        }
    }
}