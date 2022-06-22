package com.lahsuak.apps.mytask.ui.fragments

import android.annotation.SuppressLint
import android.app.*
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.SortOrder
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.data.util.Util
import com.lahsuak.apps.mytask.data.util.Util.createNotification
import com.lahsuak.apps.mytask.data.util.Util.getTimeDiff
import com.lahsuak.apps.mytask.data.util.Util.notifyUser
import com.lahsuak.apps.mytask.ui.adapters.SubTaskAdapter
import com.lahsuak.apps.mytask.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.mytask.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.mytask.data.util.onQueryTextChanged
import com.lahsuak.apps.mytask.data.util.viewBinding
import com.lahsuak.apps.mytask.databinding.FragmentSubtaskBinding
import com.lahsuak.apps.mytask.ui.fragments.TaskFragment.Companion.viewType
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class SubTaskFragment : Fragment(R.layout.fragment_subtask),
    SubTaskAdapter.SubTaskListener {

    private val binding: FragmentSubtaskBinding by viewBinding {
        FragmentSubtaskBinding.bind(it)
    }
    private val model: TaskViewModel by viewModels()
    private val subModel: SubTaskViewModel by viewModels()
    private val args: SubTaskFragmentArgs by navArgs()

    private lateinit var navController: NavController
    private lateinit var subTaskAdapter: SubTaskAdapter
    private lateinit var task: Task
    private lateinit var searchView: SearchView
    private var actionMode: ActionMode? = null
    private val mCalendar = Calendar.getInstance()

    companion object {
        var selectedItem2: Array<Boolean>? = null
        var counter2 = 0
        var is_in_action_mode2 = false
        var is_select_all2 = false
    }

    private val speakLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val result1 = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val subTask = SubTask(args.id, result1!![0], isDone = false, false, 0)
                subModel.insertSubTask(subTask)
            }
        }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewType = model.preferencesFlow.first().viewType
            if (viewType) {
                binding.todoRecyclerView.layoutManager =
                    StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
            } else {
                binding.todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        }
        subTaskAdapter = SubTaskAdapter(requireContext(), this)
        setHasOptionsMenu(true)

        createNotification(requireContext())

        subModel.taskId.value = args.id
        navController = findNavController()

        reminderHandling()

        binding.todoRecyclerView.apply {
            adapter = subTaskAdapter
            setHasFixedSize(true)
        }

        swipeGesturesHandler() //swipe to delete and mark as imp functionality

        completedSubTaskObserver() //completed and uncompleted task observer

        subModel.subTasks.observe(viewLifecycleOwner) { //this is for adapter
            subTaskAdapter.submitList(it)
        }

        subTaskEventCollector() //subtask event handler

        binding.createNewTask.setOnClickListener {
            addNewTask()
        }
        binding.addBtn.setOnClickListener {
            addNewTask()
        }

        binding.soundTask.setOnClickListener {
            Util.speakToAddTask(requireActivity(), speakLauncher)
        }

        binding.timerLayout.setOnClickListener {
            subModel.showReminder(
                requireActivity(),
                mCalendar,
                binding.timerTxt,
                binding.cancelTimer,
                task,
                model
            )
        }

        binding.cancelTimer.setOnClickListener {
            subModel.cancelReminder(requireActivity(), args.id, task, binding.timerTxt, model)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.timerTxt.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.darker_gray
                    )
                )
            } else {
                @Suppress("deprecation")
                binding.timerTxt.setTextColor(resources.getColor(android.R.color.darker_gray))
            }
            binding.timerTxt.background = null
            binding.cancelTimer.visibility = View.GONE
        }

        binding.isCompleted.setOnCheckedChangeListener { _, isChecked ->
            binding.isCompleted.isChecked = isChecked

            task.isDone = isChecked
            model.update(task)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = subModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            subModel.searchQuery.value = it
        }
        searchView.queryHint = getString(R.string.search_subtask)

        menu.findItem(R.id.itemView).isVisible = false
        menu.findItem(R.id.setting).title = getString(R.string.share)
        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.showTask).isChecked = subModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sortByName -> {
                subModel.onSortOrderSelected(SortOrder.BY_NAME, requireContext())
                true
            }
            R.id.sortByOld -> {
                subModel.onSortOrderSelected(SortOrder.BY_DATE, requireContext())
                true
            }
            R.id.showTask -> {
                item.isChecked = !item.isChecked
                subModel.onHideCompleted(item.isChecked, requireContext())
                true
            }
            R.id.delete_all_completed_task -> {
                subModel.onDeleteAllCompletedClick()
                true
            }
            R.id.setting -> {
                subModel.shareTask(requireContext(), getAllText())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun reminderHandling() {
        viewLifecycleOwner.lifecycleScope.launch {
            task = model.getById(args.id)
            val diff = getTimeDiff(task)
            if (diff < 0) {
                binding.timerTxt.text = task.reminder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.timerTxt.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                } else {
                    @Suppress("deprecation")
                    binding.timerTxt.setTextColor(resources.getColor(R.color.red))
                }
            }
            if (args.completed) {
                binding.isCompleted.isChecked = true
            }
            if (task.reminder != null) {
                binding.timerTxt.text = task.reminder
                binding.timerTxt.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.background_timer)
                binding.cancelTimer.visibility = View.VISIBLE
            } else {
                binding.timerTxt.background = null
            }
        }
    }

    private fun swipeGesturesHandler() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                    val subTask = subTaskAdapter.currentList[viewHolder.adapterPosition]
                    subModel.onSubTaskSwiped(subTask)
                } else {
                    val subTask = subTaskAdapter.currentList[viewHolder.adapterPosition]
                    subTask.isImportant = !subTask.isImportant
                    subModel.updateSubTask(subTask)
                    subTaskAdapter.notifyDataSetChanged()
                }
                //   notifyUser(requireContext(), "Already pinned!")
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    requireContext(),
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addSwipeLeftBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    .addSwipeRightBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.blue_500
                        )
                    )
                    .addSwipeRightActionIcon(R.drawable.ic_pin)
                    .create()
                    .decorate()
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }).attachToRecyclerView(binding.todoRecyclerView)
    }

    private fun completedSubTaskObserver() {
        subModel.subTasks2.observe(viewLifecycleOwner) { //this is for all task
            var count = 0
            for (element in it) {
                if (element.isDone)
                    count++
            }
            if (it.isEmpty()) {
                binding.createNewTask.visibility = View.VISIBLE
                binding.todoRecyclerView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.taskProgress.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.VISIBLE
                binding.taskProgress.visibility = View.VISIBLE
                binding.createNewTask.visibility = View.GONE
                binding.todoRecyclerView.visibility = View.VISIBLE
                val value = (count.toFloat() / it.size.toFloat()) * 100
                binding.progressBar.progress = value.toInt()
                binding.taskProgress.text = getString(R.string.subtask_progress, count, it.size)
            }
        }
    }

    private fun subTaskEventCollector() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            subModel.subTasksEvent.collect { event ->
                when (event) {
                    is SubTaskViewModel.SubTaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(
                            requireView(),
                            getString(R.string.task_deleted),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction(getString(R.string.undo)) {
                                subModel.onUndoDeleteClick(event.subTask)
                            }.show()
                    }
                    SubTaskViewModel.SubTaskEvent.NavigateToAllCompletedScreen -> {
                        val action =
                            SubTaskFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment2(
                                args.id
                            )//actionGlobalDeleteAllCompletedDialogFragment()
                        navController.navigate(action)
                    }
                }
            }
        }
    }

    private fun addNewTask() {
        val action = SubTaskFragmentDirections.actionSubTaskFragmentToRenameFragmentDialog(
            true,
            args.id,
            null, -1
        )
        navController.navigate(action)
    }

    private fun getAllText(): String {
        var sendtxt: String = task.title + " :"
        for (i in 0 until subTaskAdapter.currentList.size) {
            sendtxt += "\n${i + 1}. " + subTaskAdapter.currentList[i].subTitle
        }
        return sendtxt
    }

    private fun getSubText(): String? {
        var sendtxt: String?
        sendtxt = "1. "
        if (subTaskAdapter.currentList.size != 0) {

            sendtxt += subTaskAdapter.currentList[0].subTitle//subTaskAdapter.currentList[0].subTitle
        }
        for (i in 1 until subTaskAdapter.currentList.size) {//subTaskAdapter.currentList.size) {

            sendtxt += "\n${i + 1}. " + subTaskAdapter.currentList[i].subTitle//subTaskAdapter.currentList[i].subTitle
        }
        if (sendtxt == "1. ") {
            sendtxt = null
        }
        return sendtxt
    }

    override fun onItemClicked(subTask: SubTask, position: Int) {
        if (is_in_action_mode2) {
            if (selectedItem2!![position]) {
                selectedItem2!![position] = false
                counter2--
                actionMode!!.title = "${counter2}/${subTaskAdapter.currentList.size} Selected"
            } else {
                selectedItem2!![position] = true
                counter2++
                actionMode!!.title = "${counter2}/${subTaskAdapter.currentList.size} Selected"
            }
        } else {
            val action = SubTaskFragmentDirections.actionSubTaskFragmentToRenameFragmentDialog(
                true,
                args.id,
                subTask.subTitle,
                subTask.sId
            )
            navController.navigate(action)
        }
    }

    override fun onDeleteClicked(subTask: SubTask) {
        if (subTask.isDone) {
            subModel.showDeleteDialog(requireContext(), subTask)
        } else {
            val action = SubTaskFragmentDirections.actionSubTaskFragmentToRenameFragmentDialog(
                true,
                args.id,
                subTask.subTitle,
                subTask.sId
            )
            navController.navigate(action)
        }
    }

    override fun onCheckBoxClicked(subTask: SubTask, taskCompleted: Boolean) {
        subModel.onSubTaskCheckedChanged(subTask, taskCompleted)
    }

    private val callback = object : ActionMode.Callback {
        override fun onCreateActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            val menuInflater = MenuInflater(requireContext())
            menuInflater.inflate(R.menu.action_menu, menu)
            return true
        }

        override fun onPrepareActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            return false
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onActionItemClicked(
            mode: ActionMode?,
            item: MenuItem?
        ): Boolean {
            return when (item?.itemId) {
                R.id.action_delete -> {
                    if (counter2 == 0) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_select_task),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (selectedItem2!!.isNotEmpty()) {
                        if (counter2 == subTaskAdapter.currentList.size) {
                            //delete all tasks
                            showDialog(true)
                        } else {
                            //delete one by one
                            showDialog(false)
                        }
                    }
                    true
                }
                R.id.action_selectAll -> {
                    if (!is_select_all2) {
                        item.setIcon(R.drawable.ic_select_all_on)
                        for (i in 0 until subTaskAdapter.currentList.size)
                            selectedItem2!![i] == true

                        counter2 = subTaskAdapter.currentList.size
                        actionMode!!.title =
                            "${counter2}/${subTaskAdapter.currentList.size} Selected"
                        is_select_all2 = true
                    } else {
                        item.setIcon(R.drawable.ic_select_all)
                        for (i in 0 until subTaskAdapter.currentList.size)
                            selectedItem2!![i] == false

                        counter2 = 0
                        is_select_all2 = false
                        actionMode!!.title =
                            "${counter2}/${subTaskAdapter.currentList.size} Selected"
                    }
                    subTaskAdapter.notifyDataSetChanged()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            onActionMode(false)
            actionMode = null
        }
    }

    override fun onAnyItemLongClicked(position: Int) {
        if (!is_in_action_mode2) {
            onActionMode(true)
            counter2 = 1
            selectedItem2!![position] = true
        } else {
            if (selectedItem2!![position]) {
                selectedItem2!![position] = false
                counter2--
            } else {
                selectedItem2!![position] = true
                counter2++
            }
        }
        if (actionMode == null) {
            actionMode =
                (activity as AppCompatActivity).startSupportActionMode(callback)!!
        }
        actionMode!!.title = "${counter2}/${subTaskAdapter.currentList.size} Selected"
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onActionMode(actionModeOn: Boolean) {
        if (actionModeOn) {
            selectedItem2 = Array(subTaskAdapter.currentList.size) { false }
            is_in_action_mode2 = true
            binding.soundTask.visibility = View.GONE
            binding.addBtn.visibility = View.GONE
        } else {
            is_in_action_mode2 = false
            is_select_all2 = false
            binding.addBtn.visibility = View.VISIBLE
            binding.soundTask.visibility = View.VISIBLE
            subTaskAdapter.notifyDataSetChanged()
        }
    }

    private fun showDialog(isAllDeleted: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_deletion))
            .setMessage(getString(R.string.delete_all_task))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                if (isAllDeleted) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        subModel.deleteAllSubTasks(args.id)
                    }
                } else {
                    for (i in selectedItem2!!.indices) {
                        if (selectedItem2!![i]) {
                            subModel.deleteSubTask(subTaskAdapter.currentList[i])
                        }
                    }
                }
                counter2 = 0
                actionMode!!.finish()
                onActionMode(false)
                notifyUser(requireContext(), getString(R.string.notify_delete))
                dialog.dismiss()
            }.show()
    }

    override fun onPause() {
        super.onPause()
        if (binding.progressBar.visibility == View.GONE) {
            task.progress = -1f
        } else {
            task.progress = binding.progressBar.progress.toFloat()
        }
        task.subTaskList = getSubText()

        model.update(task)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("TAG", "onDestroyView: ${binding.progressBar.progress}")
        if(binding.progressBar.progress==100)
        {
            binding.isCompleted.isChecked=true
            task.isDone=true
            model.update(task)
        }
        searchView.setOnQueryTextListener(null)
    }
}