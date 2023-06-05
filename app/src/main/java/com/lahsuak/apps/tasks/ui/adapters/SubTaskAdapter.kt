package com.lahsuak.apps.tasks.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.databinding.SubTaskItemBinding
import com.lahsuak.apps.tasks.databinding.SubTaskItemGridBinding
import com.lahsuak.apps.tasks.ui.adapters.viewholders.SubTaskViewHolder1
import com.lahsuak.apps.tasks.ui.adapters.viewholders.SubTaskViewHolder2
import com.lahsuak.apps.tasks.util.SelectionListener

class SubTaskAdapter(
    private val listener: SubTaskListener,
    private val selectionListener: SelectionListener
) : ListAdapter<SubTask, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (!selectionListener.getViewType()) {
            val binding = SubTaskItemBinding.inflate(layoutInflater, parent, false)
            SubTaskViewHolder1(this, binding, listener, selectionListener)
        } else {
            val binding = SubTaskItemGridBinding.inflate(layoutInflater, parent, false)
            SubTaskViewHolder2(this, binding, listener, selectionListener)
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
        fun getColor(): Int

        fun cancelReminderClicked(subTask: SubTask, timerTxt: TextView)
    }

    class DiffCallback : DiffUtil.ItemCallback<SubTask>() {
        override fun areItemsTheSame(oldItem: SubTask, newItem: SubTask) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SubTask, newItem: SubTask) =
            oldItem == newItem
    }
}