package com.lahsuak.apps.mytask.ui.adapters.viewholders

import android.graphics.Paint
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.databinding.TaskItemBinding
import com.lahsuak.apps.mytask.ui.adapters.SubTaskAdapter
import com.lahsuak.apps.mytask.ui.fragments.SubTaskFragment

class SubTaskViewHolder1(
    private val adapter: SubTaskAdapter,
    private val binding: TaskItemBinding,
    listener: SubTaskAdapter.SubTaskListener
) :
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.apply {
            subTask.visibility = View.GONE
            root.setOnClickListener {
                val position = adapterPosition
                //checking for -1 index when task will be deleted
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
                //checking for -1 index when task will be deleted
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    if (!SubTaskFragment.is_in_action_mode2) {
                        listener.onCheckBoxClicked(task, checkbox.isChecked)
                    }
                }
            }
            delete.setOnClickListener {
                val position = adapterPosition
                //checking for -1 index when task will be deleted
                if (position != RecyclerView.NO_POSITION) {
                    val subTask = adapter.currentList[position]
                    if (!SubTaskFragment.is_in_action_mode2)
                        listener.onDeleteClicked(subTask)
                }
            }

            root.setOnLongClickListener {
                val position = adapterPosition
                //checking for -1 index when task will be deleted
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
            title.text = subTask.subTitle
            Linkify.addLinks(title, Linkify.ALL)

            val prefMgr = PreferenceManager.getDefaultSharedPreferences(context)
            val txtSize = prefMgr.getString("font_size", "18")!!.toFloat()
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize)
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
            if (subTask.isDone) {
                checkbox.isChecked = true
                title.paintFlags = title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                delete.setImageResource(R.drawable.ic_delete)
            } else {
                checkbox.isChecked = false
                title.paintFlags = title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                delete.setImageResource(R.drawable.ic_edit)
            }
            if (subTask.isImportant) {
                isImportant.background = AppCompatResources.getDrawable(
                    context,
                    R.drawable.ic_pin
                )
                isImportant.visibility = View.VISIBLE
                isImportant2.visibility = View.INVISIBLE
            } else {
                isImportant.background = null
                isImportant.visibility = View.GONE
                isImportant2.visibility = View.GONE
            }
            progressBar.visibility = View.GONE
            taskProgress.visibility = View.GONE
        }
    }
}