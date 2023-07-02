package com.lahsuak.apps.tasks.ui.adapters.viewholders

import android.graphics.Paint
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.databinding.OverviewItemBinding

class OverviewViewHolder(private val binding: OverviewItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(task: Task) {
        binding.txtTaskName.text = task.title
        binding.progressLayout.progressBar.progress = task.progress.toInt()
        if (task.progress != -1f) {
            binding.progressLayout.txtTaskProgress.text =
                String.format(
                    binding.root.context.getString(R.string.percentage),
                    task.progress.toInt()
                )
        }
        binding.txtTaskName.paintFlags = if (task.isDone) {
            binding.txtTaskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding.txtTaskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        binding.progressLayout.root.isVisible = task.progress != -1f
    }
}