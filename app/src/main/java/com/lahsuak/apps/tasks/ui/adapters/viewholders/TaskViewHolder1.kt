package com.lahsuak.apps.tasks.ui.adapters.viewholders

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.databinding.TaskItemBinding
import com.lahsuak.apps.tasks.ui.adapters.TaskAdapter
import com.lahsuak.apps.tasks.util.AppConstants.FONT_SIZE_KEY
import com.lahsuak.apps.tasks.util.AppConstants.INITIAL_FONT_SIZE
import com.lahsuak.apps.tasks.util.AppConstants.SHOW_REMINDER_KEY
import com.lahsuak.apps.tasks.util.AppConstants.SHOW_SUBTASK_KEY
import com.lahsuak.apps.tasks.util.AppConstants.TASK_PROGRESS_KEY
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.SelectionListener
import com.lahsuak.apps.tasks.util.getAttribute
import com.lahsuak.apps.tasks.util.setDrawableColor

class TaskViewHolder1(
    private val adapter: TaskAdapter,
    private val binding: TaskItemBinding,
    private val listener: TaskAdapter.TaskListener,
    private val selectionListener: SelectionListener,
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        const val OPEN_ROTATION_ANGLE = 0F
        const val CLOSE_ROTATION_ANGLE = 180F
    }

    init {
        binding.apply {
            imgMore.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    imgMore.rotation = if (imgMore.rotation == CLOSE_ROTATION_ANGLE)
                        OPEN_ROTATION_ANGLE
                    else {
                        CLOSE_ROTATION_ANGLE
                    }
                    txtSubtask.isVisible = !txtSubtask.isVisible
                    listener.setExpandCollapseState(position, txtSubtask.isVisible)
                }
            }
            root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    binding.root.transitionName = task.title
                    if (selectionListener.getActionModeStatus()) {
                        root.strokeWidth = if (!selectionListener.getItemStatus(position))
                            5
                        else {
                            0
                        }
                    }
                    listener.onItemClicked(task, position, root)
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

    fun bind(task: Task) {
        binding.apply {
            val context = root.context
            val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
            val progress = prefManager.getBoolean(TASK_PROGRESS_KEY, false)
            val showReminder = prefManager.getBoolean(SHOW_REMINDER_KEY, true)
            val showSubTask = prefManager.getBoolean(SHOW_SUBTASK_KEY, true)
            val prefMgr = PreferenceManager.getDefaultSharedPreferences(context)
            val txtSize = prefMgr.getString(FONT_SIZE_KEY, INITIAL_FONT_SIZE)!!.toFloat()
            txtTitle.text = task.title
            Linkify.addLinks(txtTitle, Linkify.ALL)
            txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize)
            txtDate.text = DateUtil.getTaskDateTime(
                task.date ?: System.currentTimeMillis(),
                false
            )
            val color = TaskApp.categoryTypes[task.color].color
            imgCategory.setColorFilter(color)
            progressBar.progressTintList = ColorStateList.valueOf(color)
            dateLayout.setCardBackgroundColor(color)
            txtReminder.backgroundTintList = ColorStateList.valueOf(color)
            txtReminder.setTextColor(Color.BLACK)
            txtReminder.setDrawableColor(Color.BLACK)
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_checked)
            drawable?.setTint(color)
            if (showSubTask) {
                imgMore.isVisible = task.subTaskList?.isNotEmpty() == true
                txtSubtask.isVisible = if (task.subTaskList != null) {
                    txtSubtask.text = task.subTaskList
                    true
                } else {
                    false
                }
            }
            val position = adapterPosition
            //action mode
            if (!selectionListener.getActionModeStatus()) {
                root.strokeWidth = 0
            } else {
                if (position != RecyclerView.NO_POSITION) {
                    if (selectionListener.isAllSelected) {
                        root.strokeWidth = 5
                        if (!selectionListener.getSelectedItemEmpty()) {
                            selectionListener.setItemStatus(true, position)
                        }
                    } else {
                        if (!selectionListener.getSelectedItemEmpty()) {
                            root.strokeWidth =
                                if (!selectionListener.getItemStatus(position)) {
                                    selectionListener.setItemStatus(false, position)
                                    0
                                } else {
                                    selectionListener.setItemStatus(true, position)
                                    5
                                }
                        }
                    }
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
            imgImp.isInvisible = !task.isImp
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
                    val taskPrs = "${task.progress.toInt()} %"
                    taskProgress.text = taskPrs
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