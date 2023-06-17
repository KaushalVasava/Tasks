package com.lahsuak.apps.tasks.ui.adapters.viewholders

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
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.databinding.SubTaskItemGridBinding
import com.lahsuak.apps.tasks.ui.adapters.SubTaskAdapter
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.SelectionListener
import com.lahsuak.apps.tasks.util.setDrawableColor

class SubTaskViewHolder2(
    private val adapter: SubTaskAdapter,
    private val binding: SubTaskItemGridBinding,
    private val listener: SubTaskAdapter.SubTaskListener,
    private val selectionListener: SelectionListener
) :
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.apply {
            root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    if (selectionListener.getActionModeStatus()) {
                        root.strokeWidth = if (!selectionListener.getItemStatus(position)) {
                            5
                        } else {
                            0
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
                    val subTask = adapter.currentList[position]
                    if (!selectionListener.getActionModeStatus())
                        listener.onDeleteClicked(subTask)
                }
            }

            root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAnyItemLongClicked(position)
                    if (!selectionListener.getSelectedItemEmpty()) {
                        root.strokeWidth =
                            if (selectionListener.getItemStatus(position)) {
                                5
                            } else {
                                0
                            }
                    }
                }
                return@setOnLongClickListener true
            }
            btnCancel.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val subTask = adapter.currentList[position]
                    listener.cancelReminderClicked(subTask, binding.txtReminder)
                    txtReminder.background = null
                    txtReminder.isVisible = false
                    btnCancel.isVisible = false
                }
            }
        }
    }

    fun bind(subTask: SubTask) {
        binding.apply {
            val context = root.context
            val prefMgr = PreferenceManager.getDefaultSharedPreferences(context)
            val txtSize =
                prefMgr.getString(AppConstants.FONT_SIZE_KEY, AppConstants.INITIAL_FONT_SIZE)!!
                    .toFloat()
            val showReminder = prefMgr.getBoolean(AppConstants.SHOW_REMINDER_KEY, true)

            txtTitle.text = subTask.subTitle
            Linkify.addLinks(txtTitle, Linkify.ALL)
            txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize)
            txtDate.backgroundTintList = ColorStateList.valueOf(listener.getColor())
            txtReminder.backgroundTintList = ColorStateList.valueOf(listener.getColor())
            txtReminder.setTextColor(Color.BLACK)
            txtReminder.setDrawableColor(Color.BLACK)
            txtDate.text =
                DateUtil.getTaskDateTime(
                    subTask.dateTime ?: System.currentTimeMillis(),
                    true
                )
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
            checkbox.isChecked = subTask.isDone
            txtTitle.paintFlags = if (subTask.isDone) {
                btnDelete.setImageResource(R.drawable.ic_delete)
                txtTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                btnDelete.setImageResource(R.drawable.ic_edit)
                txtTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            imgImp.isVisible = subTask.isImportant
            imgImp2.visibility = if (subTask.isImportant) {
                imgImp.background = AppCompatResources.getDrawable(
                    context,
                    R.drawable.ic_pin
                )
                View.INVISIBLE
            } else {
                imgImp.background = null
                View.GONE
            }
            val taskReminder = subTask.reminder
            txtReminder.isVisible = taskReminder != null
            btnCancel.isVisible = taskReminder != null
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
        }
    }
}