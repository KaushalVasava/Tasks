package com.lahsuak.apps.mytask.ui.adapters

import android.content.Context
import android.graphics.Paint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.databinding.TaskItemBinding
import com.lahsuak.apps.mytask.databinding.TaskItemGridBinding
import com.lahsuak.apps.mytask.ui.fragments.SubTaskFragment.Companion.is_in_action_mode2
import com.lahsuak.apps.mytask.ui.fragments.SubTaskFragment.Companion.is_select_all2
import com.lahsuak.apps.mytask.ui.fragments.SubTaskFragment.Companion.selectedItem2
import com.lahsuak.apps.mytask.ui.fragments.TaskFragment

class SubTaskAdapter(private val context: Context, private val listener: SubTaskListener) :
    ListAdapter<SubTask, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (!TaskFragment.viewType) {
            val layoutInflater = LayoutInflater.from(context)
            val binding = TaskItemBinding.inflate(layoutInflater, parent, false)
            return SubTaskViewHolder1(binding)
        } else {
            val binding = TaskItemGridBinding.inflate(LayoutInflater.from(context), parent, false)
            return SubTaskViewHolder2(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        when (holder) {
            is SubTaskViewHolder1 -> holder.bind(currentItem)
            is SubTaskViewHolder2 -> holder.bind(currentItem)

        }
    }

    inner class SubTaskViewHolder1(private val binding: TaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                subTask.visibility = View.GONE
                root.setOnClickListener {
                    val position = adapterPosition
                    //checking for -1 index when task will be deleted
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        if (is_in_action_mode2) {
                            if (!selectedItem2!![position]) {
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
                        val task = getItem(position)
                        if (!is_in_action_mode2) {
                            listener.onCheckBoxClicked(task, checkbox.isChecked)
                        }
                    }
                }
                delete.setOnClickListener {
                    val position = adapterPosition
                    //checking for -1 index when task will be deleted
                    if (position != RecyclerView.NO_POSITION) {
                        val subTask = getItem(position)
                        if (!is_in_action_mode2)
                            listener.onDeleteClicked(subTask)
                    }
                }

                root.setOnLongClickListener {
                    val position = adapterPosition
                    //checking for -1 index when task will be deleted
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAnyItemLongClicked(position)
                        if (selectedItem2 == null) {
                        } else {
                            if (selectedItem2!![position]) {
                                root.strokeWidth = 5
                            } else {
                                root.strokeWidth = 0
                            }
                        }
//                        return@setOnLongClickListener true
                    }
                    return@setOnLongClickListener true
                }
            }
        }

        fun bind(subTask: SubTask) {
            binding.apply {
                title.text = subTask.subTitle
                val prefMgr = PreferenceManager.getDefaultSharedPreferences(context)
                val txtSize = prefMgr.getString("font_size", "18")!!.toFloat()
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize)
                if (!is_in_action_mode2) {
                    root.strokeWidth = 0
                } else {
                    if (is_select_all2) {
                        root.strokeWidth = 5
                        if (selectedItem2 != null) {
                            selectedItem2!![adapterPosition] = true
                        }
                    } else {
                        root.strokeWidth = 0
                        if (selectedItem2 != null) {
                            selectedItem2!![adapterPosition] = false
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

    inner class SubTaskViewHolder2(private val binding: TaskItemGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                subTask.visibility = View.GONE

                root.setOnClickListener {
                    val position = adapterPosition
                    //checking for -1 index when task will be deleted
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        if (is_in_action_mode2) {
                            if (!selectedItem2!![position]) {
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
                        val task = getItem(position)
                        if (!is_in_action_mode2) {
                            listener.onCheckBoxClicked(task, checkbox.isChecked)
                        }
                    }
                }
                delete.setOnClickListener {
                    val position = adapterPosition
                    //checking for -1 index when task will be deleted
                    if (position != RecyclerView.NO_POSITION) {
                        val subTask = getItem(position)
                        if (!is_in_action_mode2)
                            listener.onDeleteClicked(subTask)
                    }
                }

                root.setOnLongClickListener {
                    val position = adapterPosition
                    //checking for -1 index when task will be deleted
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAnyItemLongClicked(position)
                        if (selectedItem2 == null) {
                        } else {
                            if (selectedItem2!![position]) {
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
                title.text = subTask.subTitle
                val prefMgr = PreferenceManager.getDefaultSharedPreferences(context)
                val txtSize = prefMgr.getString("font_size", "18")!!.toFloat()
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, txtSize)
                if (!is_in_action_mode2) {
                    root.strokeWidth = 0
                } else {
                    if (is_select_all2) {
                        root.strokeWidth = 5
                        if (selectedItem2 != null) {
                            selectedItem2!![adapterPosition] = true
                        }
                    } else {
                        root.strokeWidth = 0
                        if (selectedItem2 != null) {
                            selectedItem2!![adapterPosition] = false
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


    interface SubTaskListener {
        fun onItemClicked(subTask: SubTask, position: Int)
        fun onDeleteClicked(subTask: SubTask)
        fun onCheckBoxClicked(subTask: SubTask, taskCompleted: Boolean)
        fun onAnyItemLongClicked(position: Int)
    }

    class DiffCallback : DiffUtil.ItemCallback<SubTask>() {
        override fun areItemsTheSame(oldItem: SubTask, newItem: SubTask) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SubTask, newItem: SubTask) =
            oldItem == newItem
    }
}
