package com.lahsuak.apps.tasks.ui.fragments.overview

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.databinding.FragmentOverviewBinding
import com.lahsuak.apps.tasks.ui.adapters.OverviewAdapter
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.DateUtil
import com.lahsuak.apps.tasks.util.isTabletOrLandscape
import com.lahsuak.apps.tasks.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class OverviewFragment : Fragment(R.layout.fragment_overview) {

    private val binding: FragmentOverviewBinding by viewBinding {
        FragmentOverviewBinding.bind(it)
    }

    private val taskViewModel: TaskViewModel by viewModels()
    private val overviewAdapter: OverviewAdapter by lazy {
        OverviewAdapter()
    }
    private val calendar = Calendar.getInstance()
    private val taskList = mutableListOf<Task>()
    private var selectedDate: Long? = null

    companion object {
        private const val DATE_BUNDLE_KEY = "date_bundle_key"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(DATE_BUNDLE_KEY, selectedDate ?: System.currentTimeMillis())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            selectedDate = it.getLong(DATE_BUNDLE_KEY)
            binding.dateView.setDate(selectedDate ?: System.currentTimeMillis())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.doOnLayout {
            if (requireContext().isTabletOrLandscape()) {
                binding.flow.setMaxElementsWrap(2)
            } else {
                binding.flow.setMaxElementsWrap(1)
            }
        }

        binding.overviewRecyclerView.apply {
            setHasFixedSize(true)
            adapter = overviewAdapter
        }
        binding.dateView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
            val selectedTasks = getTasksByDate(taskList, false)
            binding.txtEmpty.isVisible = selectedTasks.isEmpty()
            overviewAdapter.submitList(selectedTasks)
        }
        selectedDate?.let {
            binding.dateView.date = it
        }
        addTaskObserver()
    }

    private fun getTasksByDate(list: List<Task>, isNew: Boolean): List<Task> {
        val selectedDateStr = if (isNew) {
            selectedDate = Calendar.getInstance().timeInMillis
            DateUtil.getDateForOverview(Calendar.getInstance().timeInMillis)
        } else {
            DateUtil.getDateForOverview(selectedDate ?: calendar.timeInMillis)
        }
        return list.filter {
            val taskDate = DateUtil.getDateForOverview(it.startDate!!)
            taskDate == selectedDateStr
        }
    }

    private fun addTaskObserver() {
//        val categoryArray = arrayOf(
//            binding.btnCategory1,
//            binding.btnCategory2,
//            binding.btnCategory3,
//            binding.btnCategory4,
//            binding.btnCategory5,
//        )
        taskViewModel.tasksFlow.asLiveData().observe(viewLifecycleOwner) { tasks ->
            binding.txtEmpty.isVisible = tasks.isEmpty()
            taskList.addAll(tasks)
//            val categoryMap = taskList.groupBy {
//                it.color
//            }.map {
//                it.key to it.value.size
//            }
//            categoryMap.forEachIndexed { index, pair ->
////                categoryArray[index].isVisible = pair.second != 0
//                categoryArray[index].backgroundTintList =
//                    ColorStateList.valueOf(TaskApp.categoryTypes[pair.first].color)
//                categoryArray[index].text = pair.second.toString()//TaskApp.categoryTypes[pair.first].name
////                categoryArray[index].txtSize.text = pair.second.toString()
//            }

            val selectedTasks = if (selectedDate == null) {
                getTasksByDate(tasks, true)
            } else {
                getTasksByDate(tasks, false)
            }
            overviewAdapter.submitList(selectedTasks)
            binding.txtEmpty.isVisible = selectedTasks.isEmpty()
            binding.txtPending.text = String.format(
                getString(R.string.pending),
                tasks.count {
                    !it.isDone
                }
            )
            binding.txtCompleted.text = String.format(
                getString(R.string.completed),
                tasks.count {
                    it.isDone
                }
            )
        }
    }
}