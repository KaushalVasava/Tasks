package com.lahsuak.apps.mytask.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.SortOrder
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.util.Util.createNotification
import com.lahsuak.apps.mytask.databinding.FragmentSubtaskBinding
import com.lahsuak.apps.mytask.model.SubTaskEvent
import com.lahsuak.apps.mytask.ui.adapters.SubTaskAdapter
import com.lahsuak.apps.mytask.ui.fragments.TaskFragment.Companion.viewType
import com.lahsuak.apps.mytask.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.mytask.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.mytask.util.*
import com.lahsuak.apps.mytask.util.Util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class SubTaskFragment : Fragment(R.layout.fragment_subtask),
    SubTaskAdapter.SubTaskListener {

    private lateinit var binding: FragmentSubtaskBinding

    private val model: TaskViewModel by viewModels()
    private val subModel: SubTaskViewModel by viewModels()
    private val args: SubTaskFragmentArgs by navArgs()
    private val navController: NavController by unsafeLazy {
        findNavController()
    }
    private lateinit var subTaskAdapter: SubTaskAdapter
    private lateinit var task: Task
    private var searchView: SearchView? = null
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
                val subTask = SubTask(
                    id = args.id,
                    subTitle = result1!![0],
                    sId = 0
                )
                subModel.insertSubTask(subTask)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubtaskBinding.bind(view)
        val prefManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.btnVoiceTask.isVisible =
            prefManager.getBoolean(Constants.SHOW_VOICE_TASK_KEY, true)
        setOptionMenu()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewType = model.preferencesFlow.first().viewType
            binding.subTaskRecyclerView.layoutManager =
                if (viewType) {
                    StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
                } else {
                    LinearLayoutManager(requireContext())
                }
        }
        subModel.taskId.value = args.id
        subTaskAdapter = SubTaskAdapter(this)
        createNotification(requireContext())
        reminderHandling()

        binding.subTaskRecyclerView.apply {
            adapter = subTaskAdapter
            setHasFixedSize(true)
        }

        swipeGesturesHandler() //swipe to delete and mark as imp functionality
        completedSubTaskObserver() //completed and uncompleted task observer
        subModel.subTasks.observe(viewLifecycleOwner) { //this is for adapter
            subTaskAdapter.submitList(it)
        }
        subTaskEventCollector() //subtask event handler
        addClickListeners()
    }

    private fun setOptionMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.app_menu, menu)
                val searchItem = menu.findItem(R.id.action_search)
                searchView = searchItem.actionView as SearchView

                val pendingQuery = subModel.searchQuery.value
                if (pendingQuery != null && pendingQuery.isNotEmpty()) {
                    searchItem.expandActionView()
                    searchView?.setQuery(pendingQuery, false)
                }

                searchView?.onQueryTextChanged {
                    subModel.searchQuery.value = it
                }
                searchView?.queryHint = getString(R.string.search_subtask)

                menu.findItem(R.id.itemView).isVisible = false
                menu.findItem(R.id.setting).title = getString(R.string.share)
                viewLifecycleOwner.lifecycleScope.launch {
                    menu.findItem(R.id.showTask).isChecked =
                        subModel.preferencesFlow.first().hideCompleted
                }

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.sortByName -> {
                        subModel.onSortOrderSelected(SortOrder.BY_NAME, requireContext())
                        true
                    }
                    R.id.sortByDate -> {
                        subModel.onSortOrderSelected(SortOrder.BY_DATE, requireContext())
                        true
                    }
                    R.id.showTask -> {
                        menuItem.isChecked = !menuItem.isChecked
                        subModel.onHideCompleted(menuItem.isChecked, requireContext())
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
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun addClickListeners() {
        binding.btnCreateNewTask.setOnClickListener {
            addNewTask()
        }
        binding.btnAddTask.setOnClickListener {
            addNewTask()
        }

        binding.btnVoiceTask.setOnClickListener {
            Util.speakToAddTask(requireActivity(), speakLauncher)
        }

        binding.reminderLayout.setOnClickListener {
            subModel.showReminder(
                binding,
                requireActivity(),
                mCalendar,
                task,
                model
            )
        }

        binding.btnCancelReminder.setOnClickListener {
            subModel.cancelReminder(requireActivity(), args.id, task, binding.txtReminder, model)
            binding.btnCancelReminder.isVisible = false
            binding.reminderLayout.background = null
            binding.txtReminder.isEnabled = false
            binding.txtReminder.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            binding.imgReminder.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey
                )
            )
        }

        binding.cbTaskCompleted.setOnCheckedChangeListener { _, isChecked ->
            binding.cbTaskCompleted.isChecked = isChecked
            task.isDone = isChecked
            model.update(task)
        }
    }

    private fun reminderHandling() {
        viewLifecycleOwner.lifecycleScope.launch {
            task = model.getById(args.id)
            val taskReminder = task.reminder
            binding.cbTaskCompleted.isChecked = args.completed
            binding.btnCancelReminder.isVisible =
                if (taskReminder != null) {
                    val diff = DateUtil.getTimeDiff(taskReminder)
                    binding.txtReminder.text = DateUtil.getReminderDateTime(taskReminder)
                    binding.txtReminder.isSelected = true
                    binding.imgReminder.setColorFilter(Color.BLACK)
                    binding.reminderLayout.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.background_reminder
                    )
                    if (diff < 0) {
                        binding.txtReminder.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.red)
                        )
                    }
                    true
                } else {
                    binding.reminderLayout.background = null
                    false
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
        }).attachToRecyclerView(binding.subTaskRecyclerView)
    }

    private fun completedSubTaskObserver() {
        subModel.subTasks2.observe(viewLifecycleOwner) { //this is for all task
            var count = 0
            for (element in it) {
                if (element.isDone)
                    count++
            }
            val hasTasks = it.isNotEmpty()
            binding.btnCreateNewTask.isVisible = !hasTasks
            binding.subTaskRecyclerView.isVisible = hasTasks
            binding.txtTaskProgress.isVisible = hasTasks
            binding.progressBar.isVisible = hasTasks

            val value = (count.toFloat() / it.size.toFloat()) * 100
            binding.progressBar.progress = value.toInt()
            binding.txtTaskProgress.text = getString(R.string.subtask_progress, count, it.size)
        }
    }

    private fun subTaskEventCollector() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            subModel.subTasksEvent.collect { event ->
                when (event) {
                    is SubTaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(
                            requireView(),
                            getString(R.string.task_deleted),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction(getString(R.string.undo)) {
                                subModel.onUndoDeleteClick(event.subTask)
                            }.show()
                    }
                    SubTaskEvent.NavigateToAllCompletedScreen -> {
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
            selectedItem2!![position] =
                if (selectedItem2!![position]) {
                    counter2--
                    false
                } else {
                    counter2++
                    true
                }
            actionMode!!.title =
                getString(R.string.task_selected, counter2, subTaskAdapter.itemCount)
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
                        showDialog(counter2 == subTaskAdapter.currentList.size)
                    }
                    true
                }
                R.id.action_selectAll -> {
                    is_select_all2 =
                        if (!is_select_all2) {
                            item.setIcon(R.drawable.ic_select_all_on)
                            for (i in 0 until subTaskAdapter.currentList.size)
                                selectedItem2!![i] == true
                            counter2 = subTaskAdapter.currentList.size
                            true
                        } else {
                            item.setIcon(R.drawable.ic_select_all)
                            for (i in 0 until subTaskAdapter.currentList.size)
                                selectedItem2!![i] == false
                            counter2 = 0
                            false
                        }
                    actionMode!!.title =
                        getString(R.string.task_selected, counter2, subTaskAdapter.itemCount)
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
            selectedItem2!![position] = true
            counter2 = 1
        } else {
            selectedItem2!![position] =
                if (selectedItem2!![position]) {
                    counter2--
                    false
                } else {
                    counter2++
                    true
                }
        }
        if (actionMode == null) {
            actionMode =
                (activity as AppCompatActivity).startSupportActionMode(callback)!!
        }
        actionMode!!.title = getString(R.string.task_selected, counter2, subTaskAdapter.itemCount)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onActionMode(isActionModeOn: Boolean) {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.btnVoiceTask.isVisible = !isActionModeOn &&
                prefManager.getBoolean(Constants.SHOW_VOICE_TASK_KEY, true)
        binding.btnAddTask.isVisible = !isActionModeOn
        is_in_action_mode2 = isActionModeOn

        if (isActionModeOn) {
            selectedItem2 = Array(subTaskAdapter.currentList.size) { false }
        } else {
            is_select_all2 = false
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
                context.toast {
                    getString(R.string.notify_delete)
                }
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
        if (binding.progressBar.progress == 100) {
            binding.cbTaskCompleted.isChecked = true
            task.isDone = true
            model.update(task)
        }
        searchView?.setOnQueryTextListener(null)
    }
}