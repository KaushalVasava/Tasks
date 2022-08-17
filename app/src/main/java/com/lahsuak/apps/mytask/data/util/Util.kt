package com.lahsuak.apps.mytask.data.util

import android.annotation.SuppressLint
import android.app.*
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.speech.RecognizerIntent
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.lahsuak.apps.mytask.BuildConfig
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.data.util.Constants.CHANNEL_ID
import com.lahsuak.apps.mytask.data.util.Constants.DATE_FORMAT
import com.lahsuak.apps.mytask.data.util.Constants.MAIL_TO
import com.lahsuak.apps.mytask.data.util.Constants.MARKET_PLACE_HOLDER
import com.lahsuak.apps.mytask.data.util.Constants.SHARE_FORMAT
import com.lahsuak.apps.mytask.receiver.AlarmReceiver
import com.lahsuak.apps.mytask.ui.MainActivity
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Util {
    private const val COPY_TAG = "Copied Text"

    fun <T> unsafeLazy(initializer: () -> T): Lazy<T> {
        return lazy(LazyThreadSafetyMode.NONE, initializer)
    }

    fun setClipboard(context: Context, text: String) {
        val clipboard =
            context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText(COPY_TAG, text)
        clipboard.setPrimaryClip(clip)
        notifyUser(context, "$text copied")
    }

    fun createNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = "Reminder"
            val desc = "Task Reminder"

            val channel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = desc

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    //settings methods
    fun moreApp(context: Context) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(context.getString(R.string.market_string))
                )
            )
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(context.getString(R.string.market_developer_string))
                )
            )
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
            context.startActivity(Intent.createChooser(intent, "Share by"))
        } catch (e: Exception) {
            notifyUser(
                context,
                "Something went wrong!!"
            )
        }
    }

    fun sendFeedbackMail(context: Context) {
        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(MAIL_TO) // only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.feedback_email)))
                val info = Build.MODEL + "," + Build.MANUFACTURER
                putExtra(Intent.EXTRA_TEXT, "Please write your suggestions or issues")
                putExtra(Intent.EXTRA_SUBJECT,
                    "Feedback from ${context.getString(R.string.app_name)}, $info")
            }
            context.startActivity(emailIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun appRating(context: Context) {
        val uri = Uri.parse(MARKET_PLACE_HOLDER + context.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            notifyUser(context, "Sorry for inconvenience")
        }
    }

    fun getTimeDiff(task: Task): Int {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        var min = 0
        try {
            if (task.reminder != null) {

                val date2 = sdf.parse(task.reminder!!)
                val c = Calendar.getInstance()
                val time = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(c.timeInMillis)
                val date1 = sdf.parse(time)

                val difference: Long = date2!!.time - date1!!.time
                val days = (difference / (1000 * 60 * 60 * 24)).toInt()
                val hours =
                    ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()
                min =
                    (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours).toInt() / (1000 * 60)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            min = Int.MAX_VALUE
        }
        return min
    }

    fun showReminder(activity: FragmentActivity, timerTxt: TextView, task: Task): Task {

        val mCalendar = Calendar.getInstance()
        val formatter = SimpleDateFormat(Constants.DATE_FORMAT2, Locale.getDefault())
        var hour = formatter.format(mCalendar.time).substring(0, 2).trim().toInt()
        val min = formatter.format(mCalendar.time).substring(3, 5).trim().toInt()

        val isAm = formatter.format(mCalendar.time).substring(6).trim().lowercase()

        /** PLEASE ADD TRANSLATION FOR ALL LANGUAGES*/
        if (isAm == activity.getString(R.string.pm_format))
            hour += 12

        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTitleText(activity.getString(R.string.set_time))
            .setHour(hour)
            .setMinute(min)
            .build()

        //DATE PICKER LOGIC
        val dateListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                materialTimePicker.show(
                    activity.supportFragmentManager,
                    activity.getString(R.string.set_time)
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

                    timerTxt.text = time
                    timerTxt.background =
                        ContextCompat.getDrawable(activity, R.drawable.background_timer)

                    val intent = Intent(activity.baseContext, AlarmReceiver::class.java)
                    intent.putExtra(Constants.TASK_KEY, task.id)
                    intent.putExtra(Constants.TASK_TITLE, task.title)
                    intent.putExtra(Constants.TASK_STATUS, task.isDone)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    val pendingIntent = PendingIntent.getBroadcast(
                        activity.baseContext, task.id, intent, 0
                    )

                    val alarmManager =
                        activity.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        mCalendar.timeInMillis,
                        pendingIntent
                    )

                    // then update the task
                    //task.reminder = time
                }
            }
        val datePickerDialog = DatePickerDialog(
            activity,
            dateListener,
            mCalendar.get(Calendar.YEAR),
            mCalendar.get(Calendar.MONTH),
            mCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
        return task
    }

    fun notifyUser(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun speakToAddTask(context: FragmentActivity, speakLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        if (intent.resolveActivity(context.packageManager) != null) {
            //startActivityForResult(intent, 1)
            speakLauncher.launch(intent)
        } else {
            notifyUser(
                context.baseContext,
                context.getString(R.string.speach_not_support)
            )
        }
    }
    fun getLanguage(): String {
        val langCode: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            langCode = Resources.getSystem().configuration.locales[0].toString().substring(0, 2)
        } else {
            @Suppress("deprecation")
            langCode = Resources.getSystem().configuration.locale.toString().substring(0, 2)
        }
        return langCode
    }
}