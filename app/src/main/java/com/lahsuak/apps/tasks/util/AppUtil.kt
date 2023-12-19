package com.lahsuak.apps.tasks.util

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.timepicker.MaterialTimePicker
import com.lahsuak.apps.tasks.BuildConfig
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.util.AppConstants.MARKET_PLACE_HOLDER
import com.lahsuak.apps.tasks.util.AppConstants.SHARE_FORMAT
import com.lahsuak.apps.tasks.util.worker.NotificationWorker
import com.lahsuak.apps.tasks.util.worker.ReminderWorker
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object AppUtil {
    private const val FIRST = "1. "
    private const val COPY_TAG = "Copied Text"
    private const val INSTAGRAM_ANDROID = "com.instagram.android"
    private const val SUFFIX_END = "/"
    private const val INSTAGRAM_USER = "http://instagram.com/_u/"

    fun setClipboard(context: Context, text: String) {
        val clipboard =
            context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(COPY_TAG, text)
        clipboard.setPrimaryClip(clip)
        context.toast { context.getString(R.string.text_copied, text) }
    }

    fun pasteText(context: Context): String {
        val myClipboard =
            context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val abc = myClipboard.primaryClip
        val item = abc?.getItemAt(0)
        return item?.text.toString()
    }

    //settings methods
    fun openMoreApp(context: Context) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.market_string)))
            )
        } catch (e: ActivityNotFoundException) {
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(context.getString(R.string.market_developer_string))
                    )
                )
            } catch (e: Throwable) {
                e.logError()
                context.toast { context.getString(R.string.something_went_wrong) }
            }
        } catch (e: Throwable) {
            e.logError()
            context.toast { context.getString(R.string.something_went_wrong) }
        }
    }

    fun shareApp(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = SHARE_FORMAT
            intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_app))
            val shareMsg =
                context.getString(R.string.play_store_share) + BuildConfig.APPLICATION_ID + "\n\n"
            intent.putExtra(Intent.EXTRA_TEXT, shareMsg)
            context.startActivity(
                Intent.createChooser(
                    intent,
                    context.getString(R.string.share_by)
                )
            )
        } catch (e: Exception) {
            e.logError()
            context.toast {
                context.getString(R.string.something_went_wrong)
            }
        }
    }

    fun appRating(context: Context) {
        val uri = Uri.parse(MARKET_PLACE_HOLDER + context.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            e.logError()
            context.toast { context.getString(R.string.something_went_wrong) }
        } catch (e: Throwable) {
            e.logError()
        }
    }

    fun openWebsite(context: Context?, url: String) {
        context ?: return
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.logError()
            context.toast {
                context.getString(R.string.no_application_found)
            }
        } catch (e: Throwable) {
            e.logError()
            context.toast { context.getString(R.string.something_went_wrong) }
        }
    }

    fun openInstagram(context: Context) {
        var url = AppConstants.INSTAGRAM_URL
        val intent = Intent(Intent.ACTION_VIEW)
        try {
            if (context.packageManager.getPackageInfo(INSTAGRAM_ANDROID, 0) != null) {
                if (url.endsWith(SUFFIX_END)) {
                    url = url.substring(0, url.length - 1)
                }
                val username: String = url.substring(url.lastIndexOf(SUFFIX_END) + 1)
                intent.data = Uri.parse(INSTAGRAM_USER+username)
                intent.setPackage(INSTAGRAM_ANDROID)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }
    }

    fun setDateTime(
        context: Context,
        doWork: (calendar: Calendar, time: String) -> Unit,
    ) {
        val mCalendar = Calendar.getInstance()
        val formatter = SimpleDateFormat(AppConstants.TIME_FORMAT, Locale.getDefault())
        var hour = formatter.format(mCalendar.time).substring(0, 2).trim().toInt()
        val min = formatter.format(mCalendar.time).substring(3, 5).trim().toInt()

        val isAm = formatter.format(mCalendar.time).substring(6).trim().lowercase()

        if (isAm == context.getString(R.string.pm_format))
            hour += 12

        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTitleText(context.getString(R.string.set_time))
            .setHour(hour)
            .setMinute(min)
            .build()
        val dateListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                materialTimePicker.show(
                    (context as AppCompatActivity).supportFragmentManager,
                    context.getString(R.string.set_time)
                )
                // dialog update the TextView accordingly
                materialTimePicker.addOnPositiveButtonClickListener {
                    val pickedHour: Int = materialTimePicker.hour
                    val pickedMinute: Int = materialTimePicker.minute

                    mCalendar.set(Calendar.HOUR_OF_DAY, pickedHour)
                    mCalendar.set(Calendar.MINUTE, pickedMinute)
                    mCalendar.set(Calendar.SECOND, 0)

                    val time = DateFormat.getDateTimeInstance(
                        DateFormat.MEDIUM,
                        DateFormat.SHORT
                    ).format(mCalendar.time)
                    doWork(mCalendar, time)
                }
            }
        val datePickerDialog = DatePickerDialog(
            context,
            dateListener,
            mCalendar.get(Calendar.YEAR),
            mCalendar.get(Calendar.MONTH),
            mCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    fun createWorkRequest(
        context: Context,
        id: Int,
        message: String,
        parentTitle: String?,
        isDone: Boolean,
        startDate: Long,
        endDate: Long?,
        timeDelayInSeconds: Long,
    ) {
        val myWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(timeDelayInSeconds, TimeUnit.SECONDS)
            .setInputData(
                workDataOf(
                    AppConstants.WorkManager.ID_KEY to id,
                    AppConstants.WorkManager.MESSAGE_KEY to message,
                    AppConstants.WorkManager.PARENT_TITLE_KEY to parentTitle,
                    AppConstants.WorkManager.STATUS_KEY to isDone,
                    AppConstants.WorkManager.START_DATE_KEY to startDate,
                    AppConstants.WorkManager.END_DATE_KEY to endDate,
                )
            )
            .build()
        WorkManager.getInstance(context).enqueue(myWorkRequest)
    }

    fun createNotificationWorkRequest(
        delay: Long,
        context: Context,
        title: String,
        message: String,
    ) {
        val myWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.HOURS)
            .setInputData(
                workDataOf(
                    AppConstants.WorkManager.DAILY_TITLE_KEY to title,
                    AppConstants.WorkManager.DAILY_MSG_KEY to message
                )
            )
            .build()
        WorkManager.getInstance(context).enqueue(
            myWorkRequest
        )
    }

    inline fun <reified M> setReminderWorkRequest(
        context: Context,
        title: String,
        data: M,
        calendar: Calendar,
    ) {
        val todayDateTime = Calendar.getInstance()
        val delayInSeconds =
            (calendar.timeInMillis / 1000L) - (todayDateTime.timeInMillis / 1000L)
        when (data) {
            is Task -> {
                createWorkRequest(
                    context,
                    data.id,
                    data.title,
                    null,
                    data.isDone,
                    data.startDate ?: System.currentTimeMillis(),
                    data.endDate,
                    delayInSeconds
                )
            }

            is SubTask -> {
                createWorkRequest(
                    context,
                    data.id,
                    data.subTitle,
                    title,
                    data.isDone,
                    data.dateTime ?: System.currentTimeMillis(),
                    null,
                    delayInSeconds
                )
            }

            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun speakToAddTask(context: Context, speakLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        if (intent.resolveActivity(context.packageManager) != null) {
            speakLauncher.launch(intent)
        } else {
            context.toast {
                context.getString(R.string.speech_not_support)
            }
        }
    }

    fun getSubTasks(list: List<String>): String? {
        var sendtxt: String?
        sendtxt = FIRST
        if (list.isNotEmpty()) {
            sendtxt += list.first()
        }
        for (i in 1 until list.size) {
            sendtxt += "\n${i + 1}. " + list[i]
        }
        if (sendtxt == FIRST) {
            sendtxt = null
        }
        return sendtxt
    }
}

fun Context.getSizeInDp(): Float {
    return resources.displayMetrics.widthPixels / resources.displayMetrics.density
}
