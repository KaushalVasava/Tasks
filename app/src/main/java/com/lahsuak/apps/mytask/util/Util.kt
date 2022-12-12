package com.lahsuak.apps.mytask.util

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.res.ColorStateList
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.timepicker.MaterialTimePicker
import com.lahsuak.apps.mytask.BuildConfig
import com.lahsuak.apps.mytask.MyTaskApp
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.util.Constants.CHANNEL_ID
import com.lahsuak.apps.mytask.util.Constants.MAIL_TO
import com.lahsuak.apps.mytask.util.Constants.MARKET_PLACE_HOLDER
import com.lahsuak.apps.mytask.util.Constants.SHARE_FORMAT
import com.lahsuak.apps.mytask.receiver.AlarmReceiver
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object Util {
    private const val COPY_TAG = "Copied Text"
    private const val COMMA_SEPARATOR = ","
    private const val CHANNEL_NAME = "Reminder"
    private const val DESCRIPTION = "Task Reminder"

    fun <T> unsafeLazy(initializer: () -> T): Lazy<T> {
        return lazy(LazyThreadSafetyMode.NONE, initializer)
    }

    fun setClipboard(context: Context, text: String) {
        val clipboard =
            context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as android.content.ClipboardManager
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

    fun createNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            channel.description = DESCRIPTION

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
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(context.getString(R.string.market_developer_string))
                    )
                )
            } catch (e: Exception) {
                e.logError()
                context.toast {
                    context.getString(R.string.something_went_wrong)
                }
            }
        } catch (e: Exception) {
            e.logError()
            context.toast {
                context.getString(R.string.something_went_wrong)
            }
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

    fun sendFeedbackMail(context: Context) {
        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(MAIL_TO) // only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.feedback_email)))
                val info =
                    Build.MODEL + COMMA_SEPARATOR + Build.MANUFACTURER + Build.VERSION.SDK_INT
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.write_suggestions))
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Feedback from ${context.getString(R.string.app_name)}, $info"
                )
            }
            context.startActivity(emailIntent)
        } catch (e: Exception) {
            e.logError()
        }
    }

    fun appRating(context: Context) {
        val uri = Uri.parse(MARKET_PLACE_HOLDER + context.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            e.logError()
            context.toast {
                context.getString(R.string.something_went_wrong)
            }
        } catch (e: Exception) {
            e.logError()
        }
    }

    fun showReminder(activity: FragmentActivity, timerTxt: TextView, task: Task): Task {

        val mCalendar = Calendar.getInstance()
        val formatter = SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault())
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

                    timerTxt.apply {
                        setTextDrawableColor(activity.baseContext, R.color.black)
                        text = time
                        background =
                            ContextCompat.getDrawable(activity, R.drawable.background_reminder)
                        isSelected = true
                    }

                    val intent = Intent(activity.baseContext, AlarmReceiver::class.java).apply {
                        putExtra(Constants.TASK_KEY, task.id)
                        putExtra(Constants.TASK_TITLE, task.title)
                        putExtra(Constants.TASK_STATUS, task.isDone)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntentFlag =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            PendingIntent.FLAG_IMMUTABLE
                        } else {
                            0
                        }
                    val pendingIntent = PendingIntent.getBroadcast(
                        activity.baseContext, task.id, intent, pendingIntentFlag
                    )

                    val alarmManager =
                        activity.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        mCalendar.timeInMillis,
                        pendingIntent
                    )
                    task.reminder = mCalendar.timeInMillis
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

    @SuppressLint("QueryPermissionsNeeded")
    fun speakToAddTask(activity: FragmentActivity, speakLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        if (intent.resolveActivity(activity.packageManager) != null) {
            //startActivityForResult(intent, 1)
            speakLauncher.launch(intent)
        } else {
            activity.baseContext.toast {
                activity.baseContext.getString(R.string.speach_not_support)
            }
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

    fun openSettingsPage(activity: Activity?) {
        activity ?: return
        activity.runActivityCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", MyTaskApp.appContext.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        }
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

fun Context.getAttribute(resId: Int): Int {
    val value = TypedValue()
    this.theme.resolveAttribute(resId, value, true);
    return value.data
}

class SportsListOnBackPressedCallback(
    private val slidingPaneLayout: SlidingPaneLayout
): OnBackPressedCallback(slidingPaneLayout.isSlideable && slidingPaneLayout.isOpen),
    SlidingPaneLayout.PanelSlideListener{

    init {
        slidingPaneLayout.addPanelSlideListener(this)
    }

    override fun handleOnBackPressed() {
        slidingPaneLayout.closePane()
    }

    override fun onPanelSlide(panel: View, slideOffset: Float) {
    }

    override fun onPanelOpened(panel: View) {
        isEnabled = true
    }

    override fun onPanelClosed(panel: View) {
        isEnabled = false
    }
}
