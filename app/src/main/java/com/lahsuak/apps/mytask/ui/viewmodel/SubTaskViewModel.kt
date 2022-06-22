package com.lahsuak.apps.mytask.ui.viewmodel

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.gson.GsonBuilder
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.PreferenceManager
import com.lahsuak.apps.mytask.data.SortOrder
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.data.repository.TodoRepository
import com.lahsuak.apps.mytask.data.util.Constants.DATE_FORMAT2
import com.lahsuak.apps.mytask.data.util.Constants.REMINDER_DATA
import com.lahsuak.apps.mytask.data.util.Constants.REMINDER_KEY
import com.lahsuak.apps.mytask.data.util.Constants.SEARCH_INITIAL_VALUE
import com.lahsuak.apps.mytask.data.util.Constants.SEARCH_QUERY
import com.lahsuak.apps.mytask.data.util.Constants.SEPARATOR
import com.lahsuak.apps.mytask.data.util.Constants.SHARE_FORMAT
import com.lahsuak.apps.mytask.data.util.Constants.TASK_ID
import com.lahsuak.apps.mytask.data.util.Constants.TASK_KEY
import com.lahsuak.apps.mytask.data.util.Constants.TASK_TITLE
import com.lahsuak.apps.mytask.receiver.AlarmReceiver
import com.lahsuak.apps.mytask.data.util.Util
import com.lahsuak.apps.mytask.receiver.BootReceiver.Companion.timeList
import com.lahsuak.apps.mytask.receiver.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SubTaskViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val preferenceManager: PreferenceManager,
    private val state: SavedStateHandle
) : ViewModel() {

    sealed class SubTaskEvent {
        data class ShowUndoDeleteTaskMessage(val subTask: SubTask) : SubTaskEvent()
        object NavigateToAllCompletedScreen : SubTaskEvent()
    }

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
        )
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
        )
    }

    val subTasks = subTasksFlow.asLiveData()

    val subTasks2 = subTasksFlow2.asLiveData()


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

    fun insertSubTask(todo: SubTask) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertSubTask(todo)
    }

    fun updateSubTask(todo: SubTask) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateSubTask(todo)
    }

    fun deleteSubTask(todo: SubTask) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteSubTask(todo)
    }

    fun deleteAllSubTasks(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllSubTasks(id)
    }

    suspend fun getBySubTaskId(id: Int): SubTask {
        return repository.getBySubTaskId(id)
    }

    fun showDeleteDialog(
        context: Context,
        subTask: SubTask
    ) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.delete))
            .setMessage(context.getString(R.string.delete_task))
            .setPositiveButton(context.getString(R.string.delete)) { dialog, _ ->
                viewModelScope.launch {
                    deleteSubTask(subTask)
                }
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
            Util.notifyUser(context, context.getString(R.string.empty_task))
        }
    }

    fun showReminder(
        activity: FragmentActivity,
        mCalendar: Calendar,
        timerTxt: TextView,
        cancelTimer: ImageView?,
        task: Task,
        model: TaskViewModel
    ) {
        val formatter = SimpleDateFormat(DATE_FORMAT2, Locale.getDefault())
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

                    val time = java.text.DateFormat.getDateTimeInstance(
                        java.text.DateFormat.MEDIUM,
                        java.text.DateFormat.SHORT
                    )
                        .format(mCalendar.time)
                    timerTxt.text = time
                    timerTxt.background =
                        ContextCompat.getDrawable(activity, R.drawable.background_timer)

                    cancelTimer?.visibility = View.VISIBLE
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
                    intent.putExtra(TASK_KEY, task.id.toString() + "-" + task.isDone.toString())
                    intent.putExtra(TASK_TITLE, task.title)
                    //intent.putExtra(TASK_STATUS, task.isDone)
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
                    task.reminder = time
                    model.update(task)
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

    fun cancelReminder(
        activity: FragmentActivity,
        taskID: Int,
        task: Task,
        timerTxt: TextView,
        model: TaskViewModel
    ) {
        val intent = Intent(activity.baseContext, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            activity.baseContext, taskID, intent, 0
        )
        val alarmManager =
            activity.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        timerTxt.text = activity.getString(R.string.add_date_time)
        task.reminder = null
        model.update(task)
        Util.notifyUser(activity, activity.getString(R.string.cancel_reminder))
    }
}