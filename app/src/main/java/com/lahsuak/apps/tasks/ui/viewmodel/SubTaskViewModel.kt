package com.lahsuak.apps.tasks.ui.viewmodel

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.gson.GsonBuilder
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.PreferenceManager
import com.lahsuak.apps.tasks.data.SortOrder
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.data.repository.TaskRepository
import com.lahsuak.apps.tasks.databinding.FragmentSubtaskBinding
import com.lahsuak.apps.tasks.model.SubTaskEvent
import com.lahsuak.apps.tasks.receiver.AlarmReceiver
import com.lahsuak.apps.tasks.receiver.BootReceiver.Companion.timeList
import com.lahsuak.apps.tasks.receiver.Reminder
import com.lahsuak.apps.tasks.util.AppConstants.REMINDER_DATA
import com.lahsuak.apps.tasks.util.AppConstants.REMINDER_KEY
import com.lahsuak.apps.tasks.util.AppConstants.SEARCH_INITIAL_VALUE
import com.lahsuak.apps.tasks.util.AppConstants.SEARCH_QUERY
import com.lahsuak.apps.tasks.util.AppConstants.SEPARATOR
import com.lahsuak.apps.tasks.util.AppConstants.SHARE_FORMAT
import com.lahsuak.apps.tasks.util.AppConstants.TASK_ID
import com.lahsuak.apps.tasks.util.AppConstants.TASK_KEY
import com.lahsuak.apps.tasks.util.AppConstants.TASK_TITLE
import com.lahsuak.apps.tasks.util.AppConstants.TIME_FORMAT
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.getAttribute
import com.lahsuak.apps.tasks.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SubTaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val preferenceManager: PreferenceManager,
    state: SavedStateHandle
) : ViewModel() {
    val searchQuery = state.getLiveData(SEARCH_QUERY, SEARCH_INITIAL_VALUE)
    val taskId = state.getLiveData(TASK_ID, 0)

    val preferencesFlow = preferenceManager.preferencesFlow2
    private val subTaskEventChannel = Channel<SubTaskEvent>()
    val subTasksEvent = subTaskEventChannel.receiveAsFlow()

    private val subTasksFlow = combine(
        taskId.asFlow(), searchQuery.asFlow(), preferencesFlow
    ) { tId, query, filterPreferences ->
        Triple(tId, query, filterPreferences)
    }.flatMapLatest { (tid, query, filterPreferences) ->
        repository.getAllSubTasks(
            tid,
            query,
            filterPreferences.sortOrder,
            filterPreferences.hideCompleted
        ).distinctUntilChanged()
    }
    private val subTasksFlow2 = combine(
        taskId.asFlow(), searchQuery.asFlow(), preferencesFlow
    ) { tId, query, filterPreferences ->
        Triple(tId, query, filterPreferences)
    }.flatMapLatest { (tid, query, filterPreferences) ->
        repository.getAllSubTasks(
            tid,
            query,
            filterPreferences.sortOrder,
            false
        ).distinctUntilChanged()
    }

    val subTasks = subTasksFlow

    val subTasks2 = subTasksFlow2

    fun onSortOrderSelected(sortOrder: SortOrder, context: Context) = viewModelScope.launch {
        preferenceManager.updateSortOrder2(sortOrder, context)
    }

    fun onHideCompleted(hideCompleted: Boolean, context: Context) = viewModelScope.launch {
        preferenceManager.updateHideCompleted2(hideCompleted, context)
    }

    fun onSubTaskSwiped(subTask: SubTask) = viewModelScope.launch {
        repository.deleteSubTask(subTask)
        subTaskEventChannel.send(SubTaskEvent.ShowUndoDeleteTaskMessage(subTask))
    }

    fun onSubTaskCheckedChanged(subTask: SubTask, isChecked: Boolean) = viewModelScope.launch {
        repository.updateSubTask(subTask.copy(isDone = isChecked))
    }

    fun onUndoDeleteClick(subTask: SubTask) = viewModelScope.launch {
        repository.insertSubTask(subTask)
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        subTaskEventChannel.send(SubTaskEvent.NavigateToAllCompletedScreen)
    }

    fun insertSubTask(subTask: SubTask) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertSubTask(subTask)
    }

    fun updateSubTask(subTask: SubTask) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateSubTask(subTask)
    }

    fun deleteSubTask(subTask: SubTask) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteSubTask(subTask)
    }

    fun deleteAllSubTasks(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllSubTasks(id)
    }

    suspend fun getBySubTaskId(id: Int): SubTask {
        return repository.getBySubTaskId(id)
    }

    fun update(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTask(task)
    }

    fun showDeleteDialog(
        context: Context,
        subTask: SubTask
    ) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete))
            .setMessage(context.getString(R.string.delete_task))
            .setPositiveButton(context.getString(R.string.delete)) { dialog, _ ->
                deleteSubTask(subTask)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    fun shareTask(context: Context, text: String?) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            if (!text.isNullOrEmpty()) {
                putExtra(Intent.EXTRA_TEXT, text)
                type = SHARE_FORMAT
            }
        }
        try {
            context.startActivity(sendIntent)
        } catch (e: ActivityNotFoundException) {
            context.toast { context.getString(R.string.empty_task) }
        }
    }

    fun showReminder(
        binding: FragmentSubtaskBinding,
        activity: FragmentActivity,
        task: Task
    ) {
        val mCalendar = Calendar.getInstance()
        val formatter = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
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

                    val time = java.text.DateFormat.getDateTimeInstance(
                        java.text.DateFormat.MEDIUM,
                        java.text.DateFormat.SHORT
                    ).format(mCalendar.time)

                    binding.txtReminder.text = time
                    binding.btnCancelReminder.visibility = View.VISIBLE
                    val sharedPref =
                        activity.getSharedPreferences(REMINDER_DATA, MODE_PRIVATE).edit()
                    timeList.add(
                        Reminder(
                            mCalendar.timeInMillis,
                            task.id.toString() + SEPARATOR + task.isDone.toString(),
                            task.title
                        )
                    )
                    val json = GsonBuilder().create().toJson(timeList)
                    sharedPref.apply {
                        putString(REMINDER_KEY, json)
                        apply()
                    }

                    val intent = Intent(activity.baseContext, AlarmReceiver::class.java)
                    intent.putExtra(TASK_KEY, "${task.id}$SEPARATOR${task.isDone}")
                    intent.putExtra(TASK_TITLE, task.title)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    val pendingIntentFlag =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            PendingIntent.FLAG_IMMUTABLE
                        } else {
                            0
                        }

                    val pendingIntent = PendingIntent.getBroadcast(
                        activity.baseContext,
                        task.id,
                        intent,
                        pendingIntentFlag
                    )

                    val alarmManager =
                        activity.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        mCalendar.timeInMillis,
                        pendingIntent
                    )
                    val diff = DateUtil.getTimeDiff(mCalendar.timeInMillis)
                    if (diff < 0) {
                        binding.txtReminder.setTextColor(activity.getAttribute(R.attr.colorError))
                    }
                    binding.reminderLayout.background = ContextCompat.getDrawable(
                        activity.baseContext,
                        R.drawable.background_progress
                    )
                    binding.txtReminder.isSelected = true
                    binding.imgReminder.setColorFilter(activity.getAttribute(R.attr.colorOnSurface))
                    // then update the task
                    task.reminder = mCalendar.timeInMillis
                    update(task)
                }
            }

        DatePickerDialog(
            activity,
            dateListener,
            mCalendar.get(Calendar.YEAR),
            mCalendar.get(Calendar.MONTH),
            mCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun cancelReminder(
        activity: FragmentActivity,
        task: Task,
        timerTxt: TextView
    ) {
        val intent = Intent(activity.baseContext, AlarmReceiver::class.java)
        val pendingIntentFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        val pendingIntent = PendingIntent.getBroadcast(
            activity.baseContext, task.id, intent,
            pendingIntentFlag
        )
        val alarmManager =
            activity.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        timerTxt.text = activity.getString(R.string.add_date_time)
        task.reminder = null
        update(task)
        activity.baseContext.toast {
            activity.getString(R.string.cancel_reminder)
        }
    }

    fun cancelSubTaskReminder(
        activity: FragmentActivity,
        subTask: SubTask,
        timerTxt: TextView,
        task: Task
    ) {
        val intent = Intent(activity.baseContext, AlarmReceiver::class.java)
        val pendingIntentFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        val pendingIntent = PendingIntent.getBroadcast(
            activity.baseContext, subTask.id, intent,
            pendingIntentFlag
        )
        val alarmManager =
            activity.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        timerTxt.text = activity.getString(R.string.add_date_time)
        subTask.reminder = null
        updateSubTask(subTask)
        update(task.copy(startDate = System.currentTimeMillis()))
        activity.baseContext.toast { activity.getString(R.string.cancel_reminder) }
    }
}