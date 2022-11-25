package com.lahsuak.apps.mytask.ui.adapters.viewholders

import android.graphics.Paint
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.databinding.TaskItemBinding
import com.lahsuak.apps.mytask.ui.adapters.SubTaskAdapter
import com.lahsuak.apps.mytask.ui.fragments.SubTaskFragment
import com.lahsuak.apps.mytask.util.Constants
import com.lahsuak.apps.mytask.util.DateUtil

class SubTaskViewHolder1(
    private val adapter: SubTaskAdapter,
    private val binding: TaskItemBinding,
    listener: SubTaskAdapter.SubTaskListener
) :
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.apply {
            txtSubtask.visibility = View.GONE
            root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    if (SubTaskFragment.is_in_action_mode2) {
                        if (!SubTaskFragment.selectedItem2!![position]) {
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
                    if (!SubTaskFragment.is_in_action_mode2) {
                        listener.onCheckBoxClicked(task, checkbox.isChecked)
                    }
                }
            }
            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val subTask = adapter.currentList[position]
                    if (!SubTaskFragment.is_in_action_mode2)
                        listener.onDeleteClicked(subTask)
                }
            }

            root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAnyItemLongClicked(position)
                    if (SubTaskFragment.selectedItem2 != null) {
                        if (SubTaskFragment.selectedItem2!![position]) {
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

    fun bind(subTask: SubTask) {
        binding.apply {
            val context = root.context
            val prefMgr = PreferenceManager.getDefaultSharedPreferences(context)
            val txtSize = prefMgr.getString(Constants.FONT_SIZE_KEY, "18")!!.toFloat()

            txtTitle.text = subTask.subTitle
            Linkify.addLinks(txtTitle, Linkify.ALL)
            txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize)
            txtDate.text =
                DateUtil.getTaskDateTime(
                    subTask.dateTime ?: System.currentTimeMillis(),
                    false
                )

            if (!SubTaskFragment.is_in_action_mode2) {
                root.strokeWidth = 0
            } else {
                if (SubTaskFragment.is_select_all2) {
                    root.strokeWidth = 5
                    if (SubTaskFragment.selectedItem2 != null) {
                        SubTaskFragment.selectedItem2!![adapterPosition] = true
                    }
                } else {
                    root.strokeWidth = 0
                    if (SubTaskFragment.selectedItem2 != null) {
                        SubTaskFragment.selectedItem2!![adapterPosition] = false
                    }
                }
            }
            checkbox.isChecked = subTask.isDone
            if (subTask.isDone) {
                txtTitle.paintFlags = txtTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                btnDelete.setImageResource(R.drawable.ic_delete)
            } else {
                txtTitle.paintFlags = txtTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                btnDelete.setImageResource(R.drawable.ic_edit)
            }

            imgImp.isVisible = subTask.isImportant
            if (subTask.isImportant) {
                imgImp.background = AppCompatResources.getDrawable(
                    context,
                    R.drawable.ic_pin
                )
                imgImp2.visibility = View.INVISIBLE
            } else {
                imgImp.background = null
                imgImp2.visibility = View.GONE
            }
            progressBar.isGone = true
            taskProgress.isGone = true
        }
    }
}