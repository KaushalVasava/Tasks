package com.lahsuak.apps.tasks.ui.fragments.notification

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.Notification
import com.lahsuak.apps.tasks.databinding.FragmentNotificationBinding
import com.lahsuak.apps.tasks.ui.viewmodel.NotificationViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class NotificationFragment : Fragment(R.layout.fragment_notification),
    NotificationAdapter.ItemClickListener {

    private val binding: FragmentNotificationBinding by viewBinding {
        FragmentNotificationBinding.bind(it)
    }
    private val viewModel: NotificationViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()
    private val notificationAdapter: NotificationAdapter by lazy {
        NotificationAdapter(this)
    }
    private var isSortIsApplied = false

    companion object {
        private const val SORT_APPLY_BUNDLE_KEY = "sort_apply_bundle_key"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SORT_APPLY_BUNDLE_KEY, isSortIsApplied)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.notificationRecyclerView.apply {
            setHasFixedSize(true)
            adapter = notificationAdapter
        }
        binding.sortSwitch.setOnCheckedChangeListener { _, isChecked ->
            val list = if (!isSortIsApplied) {
                notificationAdapter.currentList.sortedBy { it.date }
            } else {
                notificationAdapter.currentList.sortedByDescending { it.date }
            }
            notificationAdapter.submitList(list)
            isSortIsApplied = isChecked
        }
        addNotificationsObserver()
    }

    private fun addNotificationsObserver() {
        viewModel.notifications.observe(viewLifecycleOwner) {
            val isEmpty = it.isEmpty()
            binding.txtEmptyNotification.isVisible = isEmpty
            binding.notificationRecyclerView.isVisible = !isEmpty
            binding.sortSwitch.isVisible = !isEmpty
            notificationAdapter.submitList(it)
        }
    }

    override fun onItemClicked(notification: Notification) {
        viewLifecycleOwner.lifecycleScope.launch {
            val task = taskViewModel.getById(notification.taskId)
            withContext(Dispatchers.Main) {
                val action =
                    NotificationFragmentDirections.actionNotificationFragmentToSubTaskFragment(
                        task, false, null, null
                    )
                findNavController().navigate(action)
            }
        }
    }
}