package com.lahsuak.apps.tasks.util

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.TypedValue
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.timepicker.MaterialTimePicker
import com.lahsuak.apps.tasks.BuildConfig
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.ui.MainActivity
import com.lahsuak.apps.tasks.util.AppConstants.MARKET_PLACE_HOLDER
import com.lahsuak.apps.tasks.util.AppConstants.SHARE_FORMAT
import com.lahsuak.apps.tasks.util.AppUtil.UNDERSCORE
import com.lahsuak.apps.tasks.util.worker.NotificationWorker
import com.lahsuak.apps.tasks.util.worker.ReminderWorker
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object AppUtil {
    private const val FIRST = "1. "
    private const val COPY_TAG = "Copied Text"
    const val UNDERSCORE = "_"
    fun <T> unsafeLazy(initializer: () -> T): Lazy<T> {
        return lazy(LazyThreadSafetyMode.NONE, initializer)
    }

    fun getTransparentColor(color: Int): Int {
        var alpha = Color.alpha(color)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        // Set alpha based on your logic, here I'm making it 25% of it's initial value.
        alpha *= 0.25.toInt()
        return Color.argb(alpha, red, green, blue)
    }

    fun setClipboard(context: Context, text: String) {
        val clipboard =
            context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(COPY_TAG, text)
        clipboard.setPrimaryClip(clip)
        context.toast {
            context.getString(R.string.text_copied, text)
        }
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

    fun setDateTime(
        activity: FragmentActivity,
        doWork: (calendar: Calendar, time: String) -> Unit,
    ) {
        val mCalendar = Calendar.getInstance()
        val formatter = SimpleDateFormat(AppConstants.TIME_FORMAT, Locale.getDefault())
        var hour = formatter.format(mCalendar.time).substring(0, 2).trim().toInt()
        val min = formatter.format(mCalendar.time).substring(3, 5).trim().toInt()

        val isAm = formatter.format(mCalendar.time).substring(6).trim().lowercase()

        if (isAm == activity.getString(R.string.pm_format))
            hour += 12

        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTitleText(activity.getString(R.string.set_time))
            .setHour(hour)
            .setMinute(min)
            .build()
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
                    doWork(mCalendar, time)
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
    }

    fun setDateTimeCompose(
        context: Context,
        supportFragmentManager: FragmentManager,
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
                    supportFragmentManager,
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
    fun speakToAddTaskCompose(context: Context, speakLauncher: ActivityResultLauncher<Intent>) {
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

    @SuppressLint("QueryPermissionsNeeded")
    fun speakToAddTask(activity: FragmentActivity, speakLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        if (intent.resolveActivity(activity.packageManager) != null) {
            speakLauncher.launch(intent)
        } else {
            activity.baseContext.toast {
                activity.baseContext.getString(R.string.speech_not_support)
            }
        }
    }

    fun getLanguage(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0].toString().substring(0, 2)
        } else {
            @Suppress(AppConstants.DEPRECATION)
            Resources.getSystem().configuration.locale.toString().substring(0, 2)
        }
    }

    fun openSettingsPage(activity: Activity?) {
        activity ?: return
        activity.runActivityCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri =
                Uri.fromParts(AppConstants.PACKAGE, TaskApp.appContext.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        }
    }

    fun getSubText(list: List<String>): String? {
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

fun TextView.setTextDrawableColor(context: Context, colorId: Int) {
    val color = ContextCompat.getColor(context, colorId)
    val colorList = ColorStateList.valueOf(color)
    TextViewCompat.setCompoundDrawableTintList(this, colorList)
}

fun TextView.toTrimString() = this.text.toString().trim()

fun BottomSheetBehavior<*>.applyCommonBottomSheetBehaviour() {
    skipCollapsed = true
    state = BottomSheetBehavior.STATE_EXPANDED
}

inline fun Context?.runActivityCatching(block: () -> Unit) {
    this ?: return
    try {
        block()
    } catch (e: ActivityNotFoundException) {
        e.logError()
        this.toast { this.getString(R.string.no_application_found) }
    }
}

fun Context.getColorCode(@ColorRes colorId: Int): Int {
    return ContextCompat.getColor(this, colorId)
}

fun Context.getAttribute(resId: Int): Int {
    val value = TypedValue()
    this.theme.resolveAttribute(resId, value, true)
    return value.data
}

fun String.toSortForm(): String {
    val msg = this.lowercase().substringAfter(UNDERSCORE)
    return msg.toCamelCase()
}

fun TextView.setDrawableColor(color: Int) {
    for (drawable in this.compoundDrawables) {
        if (drawable != null) {
            drawable.colorFilter =
                PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }
}

fun String.toCamelCase(): String {
    return this.substring(0, 1).uppercase() + this.substring(1)
}

fun Context.isItLightMode(): Boolean {
    return this.resources.configuration.uiMode == Configuration.UI_MODE_NIGHT_NO
}

fun Context.getSizeInDp(): Float {
    return resources.displayMetrics.widthPixels / resources.displayMetrics.density
}

fun Context.isTabletOrLandscape(): Boolean {
    return getSizeInDp() >= resources.displayMetrics.densityDpi
}