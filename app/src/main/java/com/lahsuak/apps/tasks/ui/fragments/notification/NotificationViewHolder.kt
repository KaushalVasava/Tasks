package com.lahsuak.apps.tasks.ui.fragments.notification

import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.apps.tasks.data.model.Notification
import com.lahsuak.apps.tasks.databinding.NotificationItemBinding
import com.lahsuak.apps.tasks.util.DateUtil

class NotificationViewHolder(
    private val binding: NotificationItemBinding,
    itemClickListener: NotificationAdapter.ItemClickListener,
    adapter: NotificationAdapter
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.root.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                itemClickListener.onItemClicked(adapter.currentList[position])
            }
        }
    }

    fun bind(item: Notification) {
        binding.txtDateTime.text =
            DateUtil.getDate(item.date)
        binding.txtTitle.text = item.title
    }
}