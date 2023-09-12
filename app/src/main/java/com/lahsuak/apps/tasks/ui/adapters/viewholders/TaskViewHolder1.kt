package com.lahsuak.apps.tasks.ui.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.databinding.TaskItemBinding
import com.lahsuak.apps.tasks.ui.adapters.TaskAdapter
import com.lahsuak.apps.tasks.ui.screens.components.TaskItem
import com.lahsuak.apps.tasks.util.SelectionListener

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
//        binding.apply {
//            imgMore.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    imgMore.rotation = if (imgMore.rotation == CLOSE_ROTATION_ANGLE)
//                        OPEN_ROTATION_ANGLE
//                    else {
//                        CLOSE_ROTATION_ANGLE
//                    }
//                    if(txtSubtask.text.isNullOrEmpty().not()) {
//                        txtSubtask.isVisible = !txtSubtask.isVisible
//                        listener.setExpandCollapseState(position, txtSubtask.isVisible)
//                    }
//                }
//            }
//            itemView.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    val task = adapter.currentList[position]
//                    itemView.transitionName = task.title
//                    if (selectionListener.getActionModeStatus()) {
//                        root.strokeWidth =
//                            if (!selectionListener.getItemStatus(position))
//                                5
//                            else {
//                                0
//                            }
//                    }
//                    listener.onItemClicked(task, position, root)
//                }
//            }
//            checkbox.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    val task = adapter.currentList[position]
//                    if (!selectionListener.getActionModeStatus()) {
//                        listener.onCheckBoxClicked(task, checkbox.isChecked)
//                    }
//                }
//            }
//            btnDelete.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    val task = adapter.currentList[position]
//                    if (!selectionListener.getActionModeStatus()) {
//                        listener.onDeleteClicked(task, position)
//                    }
//                }
//            }
//            itemView.setOnLongClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    listener.onAnyItemLongClicked(position)
//                    if (!selectionListener.getSelectedItemEmpty()) {
//                        root.strokeWidth =
//                            if (selectionListener.getItemStatus(position)) {
//                                5
//                            } else {
//                                0
//                            }
//                    }
//                }
//                return@setOnLongClickListener true
//            }
//        }
    }

//    fun bind(task: Task) {
//        binding.apply {
//            val context = binding.root.context
//            val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
//            val progress = prefManager.getBoolean(TASK_PROGRESS_KEY, false)
//            val showReminder = prefManager.getBoolean(SHOW_REMINDER_KEY, true)
//            val showSubTask = prefManager.getBoolean(SHOW_SUBTASK_KEY, true)
//            val prefMgr = PreferenceManager.getDefaultSharedPreferences(context)
//            val txtSize =
//                prefMgr.getString(FONT_SIZE_KEY, INITIAL_FONT_SIZE)!!.toFloat()
//            txtTitle.text = task.title
//            Linkify.addLinks(txtTitle, Linkify.ALL)
//            txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize)
//            txtDate.text =
//                DateUtil.getDateRange(task.startDate ?: System.currentTimeMillis(), task.endDate)
//            val color = TaskApp.categoryTypes[task.color].color
//            imgCategory.setColorFilter(color)
//            progressLayout.progressBar.progressTintList = ColorStateList.valueOf(color)
//            txtDate.isSelected =
//                if (task.endDate != null && DateUtil.getTimeDiff(task.endDate!!) < 0) {
//                    txtDate.text = context.getString(R.string.overdue)
//                    txtDate.setTextColor(context.getAttribute(com.google.android.material.R.attr.colorError))
//                    dateLayout.setCardBackgroundColor(null)
//                    false
//                } else {
//                    dateLayout.setCardBackgroundColor(color)
//                    true
//                }
//            txtReminder.backgroundTintList = ColorStateList.valueOf(color)
//            txtReminder.setTextColor(Color.BLACK)
//            txtReminder.setDrawableColor(Color.BLACK)
//            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_checked)
//            drawable?.setTint(color)
//            if (showSubTask) {
//                imgMore.isVisible = task.subTaskList?.isNotEmpty() == true
//                txtSubtask.text = task.subTaskList
//                txtSubtask.isVisible = task.subTaskList.isNullOrEmpty().not()
//            }
//            val position = adapterPosition
//            //action mode
//            if (!selectionListener.getActionModeStatus()) {
//                root.strokeWidth = 0
//            } else {
//                if (position != RecyclerView.NO_POSITION) {
//                    if (selectionListener.isAllSelected) {
//                        root.strokeWidth = 5
//                        if (!selectionListener.getSelectedItemEmpty()) {
//                            selectionListener.setItemStatus(true, position)
//                        }
//                    } else {
//                        if (!selectionListener.getSelectedItemEmpty()) {
//                            Log.d("TAG", "bind: ${selectionListener.getItemStatus(position)}")
//                            if (!selectionListener.getItemStatus(position)) {
//                                selectionListener.setItemStatus(false, position)
//                                root.strokeWidth = 0
//                            } else {
//                                selectionListener.setItemStatus(true, position)
//                                root.strokeWidth = 5
//                            }
//                        }
//                    }
//                }
//            }
//            //check if task is completed or not
//            checkbox.isChecked = task.isDone
//            txtTitle.paintFlags = if (task.isDone) {
//                btnDelete.setImageResource(R.drawable.ic_delete)
//                txtTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
//            } else {
//                btnDelete.setImageResource(R.drawable.ic_edit)
//                txtTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
//            }
//
//            //check if task is important or not
//            imgImp.isInvisible = !task.isImp
//            val taskReminder = task.reminder
//            txtReminder.isVisible = taskReminder != null
//            if (taskReminder != null && showReminder) {
//                val min = DateUtil.getTimeDiff(taskReminder)
//                txtReminder.isSelected = min > 0
//                txtReminder.text = if (min < 0) {
//                    txtReminder.setTextColor(context.getAttribute(com.google.android.material.R.attr.colorError))
//                    context.getString(R.string.overdue)
//                } else {
//                    DateUtil.getDate(taskReminder)
//                }
//            }
//            val isProgressVisible =
//                if (progress && task.progress != -1f) {
//                    progressLayout.progressBar.background =
//                        ContextCompat.getDrawable(context, R.drawable.background_progress)
//                    progressLayout.progressBar.progress = task.progress.toInt()
//                    progressLayout.txtTaskProgress.text =
//                        String.format(context.getString(R.string.percentage), task.progress.toInt())
//                    true
//                } else {
//                    progressLayout.progressBar.background = null
//                    false
//                }
//            progressLayout.progressBar.isVisible = isProgressVisible
//            progressLayout.txtTaskProgress.isVisible = isProgressVisible
//        }
//    }

    fun bind(task: Task) {
        binding.composeView.setContent {
            TaskItem(task = task, onItemClick = {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemView.transitionName = task.title
                    listener.onItemClicked(task, position, binding.root)
                }
            }, onImpSwipe = {}) {}
        }
    }
}