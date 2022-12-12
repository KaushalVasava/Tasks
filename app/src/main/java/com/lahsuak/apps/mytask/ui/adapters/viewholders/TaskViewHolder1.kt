package com.lahsuak.apps.mytask.ui.adapters.viewholders

import android.annotation.SuppressLint
import android.graphics.Paint
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.databinding.TaskItemBinding
import com.lahsuak.apps.mytask.ui.adapters.TaskAdapter
import com.lahsuak.apps.mytask.ui.fragments.TaskFragment
import com.lahsuak.apps.mytask.util.Constants.FONT_SIZE_KEY
import com.lahsuak.apps.mytask.util.Constants.SHOW_REMINDER_KEY
import com.lahsuak.apps.mytask.util.Constants.SHOW_SUBTASK_KEY
import com.lahsuak.apps.mytask.util.Constants.TASK_PROGRESS_KEY
import com.lahsuak.apps.mytask.util.DateUtil

class TaskViewHolder1(
    private val adapter: TaskAdapter,
    private val binding: TaskItemBinding,
    private val listener: TaskAdapter.TaskListener
) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        binding.apply {
            root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    if (TaskFragment.is_in_action_mode) {
                        if (!TaskFragment.selectedItem!![position]) root.strokeWidth = 5 else {
                            root.strokeWidth = 0
                        }
                    }
                    listener.onItemClicked(task, position)
                }
            }
            checkbox.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    if (!TaskFragment.is_in_action_mode) {
                        listener.onCheckBoxClicked(task, checkbox.isChecked)
                    }
                }
            }
            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    if (!TaskFragment.is_in_action_mode) {
                        listener.onDeleteClicked(task, position)
                    }
                }
            }
            root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAnyItemLongClicked(position)
                    if (TaskFragment.selectedItem != null) {
                        if (TaskFragment.selectedItem!![position]) {
                            root.strokeWidth = 5
                        } else {
                            root.strokeWidth = 0
                        }
                    }
                }
                return@setOnLongClickListener true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun bind(task: Task) {
        binding.apply {
            val context = root.context
            val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
            val progress = prefManager.getBoolean(TASK_PROGRESS_KEY, false)
            val showReminder = prefManager.getBoolean(SHOW_REMINDER_KEY, true)
            val showSubTask = prefManager.getBoolean(SHOW_SUBTASK_KEY, true)
            val prefMgr = PreferenceManager.getDefaultSharedPreferences(context)
            val txtSize = prefMgr.getString(FONT_SIZE_KEY, "18")!!.toFloat()

            txtTitle.text = task.title
            Linkify.addLinks(txtTitle, Linkify.ALL)
            txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize)
            txtDate.text = DateUtil.getTaskDateTime(task.date ?: System.currentTimeMillis(), false)

            if (showSubTask) {
                txtSubtask.isVisible = if (task.subTaskList != null) {
                    txtSubtask.text = task.subTaskList
                    true
                } else {
                    false
                }
            }
            //action mode
            if (!TaskFragment.is_in_action_mode) {
                root.strokeWidth = 0
            } else {
                if (TaskFragment.is_select_all) {
                    root.strokeWidth = 5
                    if (TaskFragment.selectedItem != null) {
                        TaskFragment.selectedItem!![adapterPosition] = true
                    }
                } else {
                    root.strokeWidth = 0
                    if (TaskFragment.selectedItem != null) {
                        TaskFragment.selectedItem!![adapterPosition] = false
                    }
                }
            }

            //check if task is completed or not
            checkbox.isChecked = task.isDone
            if (task.isDone) {
                txtTitle.paintFlags = txtTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                btnDelete.setImageResource(R.drawable.ic_delete)
            } else {
                txtTitle.paintFlags = txtTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                btnDelete.setImageResource(R.drawable.ic_edit)
            }

            //check if task is important or not
            imgImp.isVisible = task.isImp
            if (task.isImp) {
                imgImp.background = AppCompatResources.getDrawable(context, R.drawable.ic_pin)
                imgImp2.visibility = View.INVISIBLE
            } else {
                imgImp2.visibility = View.GONE
            }
            val taskReminder = task.reminder
            txtReminder.isVisible = taskReminder != null
            if (taskReminder != null && showReminder) {
                val min = DateUtil.getTimeDiff(taskReminder)
                txtReminder.isSelected = min > 0
                txtReminder.text = if (min < 0) {
                    txtReminder.setTextColor(ContextCompat.getColor(context, R.color.red))
                    context.getString(R.string.overdue)
                } else {
                    DateUtil.getReminderDateTime(taskReminder)
                }
            }
            val isProgressVisible =
                if (progress && task.progress != -1f) {
                    progressBar.background =
                        ContextCompat.getDrawable(context, R.drawable.background_progress)
                    progressBar.progress = task.progress.toInt()
                    taskProgress.text = "${task.progress.toInt()} %"
                    true
                } else {
                    progressBar.background = null
                    false
                }
            progressBar.isVisible = isProgressVisible
            taskProgress.isVisible = isProgressVisible
        }
    }
}