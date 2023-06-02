package com.lahsuak.apps.mytask.ui.adapters.viewholders

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.mytask.MyTaskApp
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.databinding.TaskItemGridBinding
import com.lahsuak.apps.mytask.ui.adapters.TaskAdapter
import com.lahsuak.apps.mytask.util.AppConstants
import com.lahsuak.apps.mytask.util.DateUtil
import com.lahsuak.apps.mytask.util.SelectionListener
import com.lahsuak.apps.mytask.util.getAttribute
import com.lahsuak.apps.mytask.util.setDrawableColor

class TaskViewHolder2(
    private val adapter: TaskAdapter,
    private val binding: TaskItemGridBinding,
    private val listener: TaskAdapter.TaskListener,
    private val selectionListener: SelectionListener
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.apply {
//            imgMore.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    imgMore.rotation = if (imgMore.rotation == TaskViewHolder1.CLOSE_ROTATION_ANGLE)
//                        TaskViewHolder1.OPEN_ROTATION_ANGLE
//                    else {
//                        TaskViewHolder1.CLOSE_ROTATION_ANGLE
//                    }
//                    txtSubtask.isVisible = !txtSubtask.isVisible
//                    listener.onSubTaskClicked(position, txtSubtask.isVisible)
//                }
//            }
            root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    if (selectionListener.getActionModeStatus()) {
                        if (!selectionListener.getItemStatus(position)) {
                            root.strokeWidth = 5
                        } else {
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
                    if (!selectionListener.getActionModeStatus()) {
                        listener.onCheckBoxClicked(task, checkbox.isChecked)
                    }
                }
            }
            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    if (!selectionListener.getActionModeStatus()) {
                        listener.onDeleteClicked(task, position)
                    }
                }
            }
            root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAnyItemLongClicked(position)
                    if (!selectionListener.getSelectedItemEmpty()) {
                        root.strokeWidth = if (selectionListener.getItemStatus(position)) {
                            5
                        } else {
                            0
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
            val progress = prefManager.getBoolean(AppConstants.TASK_PROGRESS_KEY, false)
            val showReminder = prefManager.getBoolean(AppConstants.SHOW_REMINDER_KEY, true)
            val showSubTask = prefManager.getBoolean(AppConstants.SHOW_SUBTASK_KEY, true)
            val prefMgr = PreferenceManager.getDefaultSharedPreferences(context)
            val txtSize =
                prefMgr.getString(AppConstants.FONT_SIZE_KEY, AppConstants.INITIAL_FONT_SIZE)!!.toFloat()

            txtTitle.text = task.title
            Linkify.addLinks(txtTitle, Linkify.ALL)
            txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize)
            val color = MyTaskApp.categoryTypes[task.color].color
            imgCategory.setColorFilter(color)
            progressBar.progressTintList = ColorStateList.valueOf(color)
            txtDate.backgroundTintList = ColorStateList.valueOf(color)
            txtReminder.backgroundTintList = ColorStateList.valueOf(color)
            txtReminder.setTextColor(Color.BLACK)
            txtReminder.setDrawableColor(Color.BLACK)
            txtDate.text = DateUtil.getTaskDateTime(
                task.date ?: System.currentTimeMillis(),
                true
            )

            if (showSubTask) {
                txtSubtask.isVisible = if (task.subTaskList != null) {
                    txtSubtask.text = task.subTaskList
                    true
                } else {
                    false
                }
            }
            //action mode
            root.strokeWidth = if (!selectionListener.getActionModeStatus()) {
                0
            } else {
                if (selectionListener.isAllSelected()) {
                    if (!selectionListener.getSelectedItemEmpty()) {
                        selectionListener.setItemStatus(true, adapterPosition)
                    }
                    5
                } else {
                    if (!selectionListener.getSelectedItemEmpty()) {
                        selectionListener.setItemStatus(false, adapterPosition)
                    }
                    0
                }
            }

            //check if task is completed or not
            checkbox.isChecked = task.isDone
            txtTitle.paintFlags = if (task.isDone) {
                btnDelete.setImageResource(R.drawable.ic_delete)
                txtTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                btnDelete.setImageResource(R.drawable.ic_edit)
                txtTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            //check if task is important or not
            imgImp.isVisible = task.isImp
            imgImp2.visibility = if (task.isImp) {
                imgImp.background = AppCompatResources.getDrawable(context, R.drawable.ic_pin)
                View.INVISIBLE
            } else {
                View.GONE
            }
            val taskReminder = task.reminder
            txtReminder.isVisible = taskReminder != null
            if (taskReminder != null && showReminder) {
                val min = DateUtil.getTimeDiff(taskReminder)
                txtReminder.isSelected = min > 0
                txtReminder.text = if (min < 0) {
                    txtReminder.setTextColor(context.getAttribute(R.attr.colorError))
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