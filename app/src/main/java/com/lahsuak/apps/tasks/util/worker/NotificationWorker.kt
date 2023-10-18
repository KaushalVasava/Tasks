package com.lahsuak.apps.tasks.util.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.NotificationUtil

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        NotificationUtil.createNotificationDaily(
            context,
            inputData.getString(AppConstants.WorkManager.DAILY_TITLE_KEY) ?: "",
            inputData.getString(AppConstants.WorkManager.DAILY_MSG_KEY) ?: ""
        )
        return Result.success()
    }
}