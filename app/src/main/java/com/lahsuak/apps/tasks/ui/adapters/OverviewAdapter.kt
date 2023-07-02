package com.lahsuak.apps.tasks.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.databinding.OverviewItemBinding
import com.lahsuak.apps.tasks.ui.adapters.viewholders.OverviewViewHolder

class OverviewAdapter : ListAdapter<Task, OverviewViewHolder>(TaskAdapter.DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverviewViewHolder {
        val binding =
            OverviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OverviewViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: OverviewViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}