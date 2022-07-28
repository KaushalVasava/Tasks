package com.lahsuak.apps.mytask.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.databinding.TaskItemBinding
import com.lahsuak.apps.mytask.databinding.TaskItemGridBinding
import com.lahsuak.apps.mytask.ui.adapters.viewholders.SubTaskViewHolder1
import com.lahsuak.apps.mytask.ui.adapters.viewholders.SubTaskViewHolder2
import com.lahsuak.apps.mytask.ui.fragments.TaskFragment

class SubTaskAdapter(private val listener: SubTaskListener) :
    ListAdapter<SubTask, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        if (!TaskFragment.viewType) {
            val binding = TaskItemBinding.inflate(layoutInflater, parent, false)
            return SubTaskViewHolder1(this, binding, listener)
        } else {
            val binding = TaskItemGridBinding.inflate(layoutInflater, parent, false)
            return SubTaskViewHolder2(this, binding, listener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        when (holder) {
            is SubTaskViewHolder1 -> holder.bind(currentItem)
            is SubTaskViewHolder2 -> holder.bind(currentItem)

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