package com.lahsuak.apps.mytask.ui.adapters.viewholders

import android.annotation.SuppressLint
import android.graphics.Paint
import android.util.TypedValue
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.data.util.Util
import com.lahsuak.apps.mytask.databinding.TaskItemGridBinding
import com.lahsuak.apps.mytask.ui.adapters.TaskAdapter
import com.lahsuak.apps.mytask.ui.fragments.TaskFragment

class TaskViewHolder2(
    private val adapter: TaskAdapter,
    private val binding: TaskItemGridBinding,
    private val listener: TaskAdapter.TaskListener
) :
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.apply {
            root.setOnClickListener {
                val position = adapterPosition
                //checking for -1 index when task will be deleted
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    if (TaskFragment.is_in_action_mode) {
                        if (!TaskFragment.selectedItem!![position]) {
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
                    if (!TaskFragment.is_in_action_mode) {
                        listener.onCheckBoxClicked(task, checkbox.isChecked)
                    }
                }
            }
            delete.setOnClickListener {
                val position = adapterPosition
                //checking for -1 index when task will be deleted
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    if (!TaskFragment.is_in_action_mode) {
                        listener.onDeleteClicked(task, position)
                    }
                }
            }
            root.setOnLongClickListener {
                val position = adapterPosition
                //checking for -1 index when task will be deleted
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
            val progress = prefManager.getBoolean("task_progress", false)
            val showReminder = prefManager.getBoolean("show_reminder", true)
            val showSubTask = prefManager.getBoolean("show_subtask", true)
            title.text = task.title
            val prefMgr = PreferenceManager.getDefaultSharedPreferences(context)
            val txtSize = prefMgr.getString("font_size", "18")!!.toFloat()
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize)

            if (showSubTask) {
                if (task.subTaskList != null) {
                    subTask.visibility = View.VISIBLE
                    subTask.text = task.subTaskList
                } else {
                    subTask.visibility = View.GONE
                }
            }
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

            if (task.isDone) {
                checkbox.isChecked = true
                title.paintFlags = title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                delete.setImageResource(R.drawable.ic_delete)
            } else {
                checkbox.isChecked = false
                title.paintFlags = title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                delete.setImageResource(R.drawable.ic_edit)
            }
            if (task.isImp) {
                isImportant.background = AppCompatResources.getDrawable(context, R.drawable.ic_pin)
                isImportant.visibility = View.VISIBLE
                isImportant2.visibility = View.INVISIBLE
            } else {
                isImportant.visibility = View.GONE
                isImportant2.visibility = View.GONE
            }
            if (task.reminder != null && showReminder) {
                reminderText.text = task.reminder
                reminderText.visibility = View.VISIBLE
                if (progress && task.progress != -1f) {
                    progressBar.visibility = View.VISIBLE
                    taskProgress.visibility = View.VISIBLE
                    progressBar.background =
                        ContextCompat.getDrawable(context, R.drawable.background_progress2)
                    progressBar.progress = task.progress.toInt()
                    taskProgress.text = "${task.progress.toInt()} %"
                } else {
                    progressBar.visibility = View.GONE
                    taskProgress.visibility = View.GONE
                    progressBar.background = null
                }
            }
            if (task.reminder == null) {
                if (task.isImp) {
                    isImportant.visibility = View.VISIBLE
                    isImportant2.visibility = View.INVISIBLE
                } else {
                    isImportant.visibility = View.GONE
                    isImportant2.visibility = View.GONE
                }
                reminderText.visibility = View.GONE

                if (progress && task.progress != -1f) {
                    progressBar.visibility = View.VISIBLE
                    taskProgress.visibility = View.VISIBLE
                    progressBar.background =
                        ContextCompat.getDrawable(context, R.drawable.background_progress2)
                    progressBar.progress = task.progress.toInt()
                    taskProgress.text = "${task.progress.toInt()} %"
                } else {
                    progressBar.visibility = View.GONE
                    taskProgress.visibility = View.GONE
                    progressBar.background = null
                }
            }
            if (showReminder) {
                val min = Util.getTimeDiff(task)
                if (min < 0) {
                    reminderText.setTextColor(ContextCompat.getColor(context, R.color.red))
                    reminderText.text = context.getString(R.string.overdue)
                } else {
                    reminderText.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.blue_500
                        )
                    )
                }

            }
            if (progress && task.progress != -1f) {
                progressBar.visibility = View.VISIBLE
                taskProgress.visibility = View.VISIBLE
                progressBar.background =
                    ContextCompat.getDrawable(context, R.drawable.background_progress2)
                progressBar.progress = task.progress.toInt()
                taskProgress.text = "${task.progress.toInt()} %"
            } else {
                progressBar.visibility = View.GONE
                taskProgress.visibility = View.GONE
                progressBar.background = null
            }

        }
    }
}