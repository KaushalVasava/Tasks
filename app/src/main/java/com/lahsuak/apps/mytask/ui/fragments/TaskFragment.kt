package com.lahsuak.apps.mytask.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.graphics.Canvas
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.SortOrder
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.data.util.Constants.REM_KEY
import com.lahsuak.apps.mytask.data.util.Constants.UPDATE_REQUEST_CODE
import com.lahsuak.apps.mytask.data.util.Util.notifyUser
import com.lahsuak.apps.mytask.data.util.Util.signOut
import com.lahsuak.apps.mytask.data.util.Util.speakToAddTask
import com.lahsuak.apps.mytask.data.util.onQueryTextChanged
import com.lahsuak.apps.mytask.data.util.viewBinding
import com.lahsuak.apps.mytask.databinding.FragmentTaskBinding
import com.lahsuak.apps.mytask.ui.MainActivity.Companion.isWidgetClick
import com.lahsuak.apps.mytask.ui.MainActivity.Companion.shareTxt
import com.lahsuak.apps.mytask.ui.adapters.TaskAdapter
import com.lahsuak.apps.mytask.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import hotchemi.android.rate.AppRate
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_task), TaskAdapter.TaskListener {

    private val binding: FragmentTaskBinding by viewBinding {
        FragmentTaskBinding.bind(it)
    }

    private lateinit var navController: NavController
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter
    private var searchView: SearchView? = null
    private var actionMode: ActionMode? = null
    private var appUpdateManager: AppUpdateManager? = null
    private var isLayoutChange = false

    private var taskPosition: Int = -1

    companion object {
        var viewType = false // listview = false, gridView = true
        var selectedItem: Array<Boolean>? = null
        var counter = 0
        var is_in_action_mode = false
        var is_select_all = false
    }

    private val speakLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val result1 = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val task = Task(0,
                    result1!![0],
                    isDone = false,
                    false,
                    null,
                    0f,
                    userId = viewModel.userId!!)
                viewModel.insert(task)
            }
        }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewType = viewModel.preferencesFlow.first().viewType
            if (viewType) {
                binding.todoRecyclerView.layoutManager =
                    StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
            } else {
                binding.todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        }

        val lastSignIN = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (lastSignIN != null) {
            viewModel.userId = lastSignIN.id
        }
        taskAdapter = TaskAdapter(this)

        setHasOptionsMenu(true)
        showRateDialog()

        navController = findNavController()
        appUpdateManager = AppUpdateManagerFactory.create(requireContext())

        // checking update of application
        checkUpdate()
        appUpdateManager!!.registerListener(appUpdateListener)

        if (shareTxt != null) {
            val action = TaskFragmentDirections.actionTaskFragmentToRenameFragmentDialog(
                false,
                -1,
                shareTxt!!
            )
            navController.navigate(action)
        }

        binding.todoRecyclerView.apply {
            adapter = taskAdapter
            setHasFixedSize(true)
        }

        swipeGesturesHandler() //swipe to delete and mark as imp functionality

        taskObserver() //observer for tasks and layout changes

        completedTaskObserver() //observer for completed and uncompleted tasks

        taskEventCollector()

        binding.fab.setOnClickListener {
            addNewTask()
        }

        binding.createNewTask.setOnClickListener {
            addNewTask()
        }

        binding.soundTask.setOnClickListener {
            speakToAddTask(requireActivity(), speakLauncher)
        }
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>(REM_KEY)
            ?.observe(viewLifecycleOwner) {
                if (taskPosition != -1) {
                    val list = taskAdapter.currentList
                    val task = list[taskPosition]
                    task.reminder = it
                    viewModel.update(task)
                    taskAdapter.notifyItemChanged(taskPosition)
                    taskPosition = -1
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.app_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView!!.setQuery(pendingQuery, false)
        }
        searchView!!.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
        searchView!!.queryHint = getString(R.string.search_task)
        viewLifecycleOwner.lifecycleScope.launch {
            if (viewType) {
                menu.findItem(R.id.itemView).title = getString(R.string.list_view)
            } else {
                menu.findItem(R.id.itemView).title = getString(R.string.grid_view)
            }
            menu.findItem(R.id.showTask).isChecked = viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sortByName -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME, requireContext())
                true
            }
            R.id.sortByOld -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE, requireContext())
                true
            }
            R.id.itemView -> {
                if (!viewType) {
                    item.title = getString(R.string.list_view)
                    binding.todoRecyclerView.adapter = taskAdapter
                    viewType = true
                } else {
                    item.title = getString(R.string.grid_view)
                    binding.todoRecyclerView.adapter = taskAdapter
                    viewType = false
                }
                isLayoutChange = true
                viewModel.onViewTypeChanged(viewType, requireContext())
                true
            }
            R.id.showTask -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompleted(item.isChecked, requireContext())
                true
            }
            R.id.delete_all_completed_task -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }
            R.id.setting -> {
                val action = TaskFragmentDirections.actionTaskFragmentToSettingsFragment()
                navController.navigate(action)
                true
            }
            R.id.logout -> {
                val lastSignIN = GoogleSignIn.getLastSignedInAccount(requireContext())
                if (lastSignIN != null) {
                    signOut(requireContext())
                    navController.popBackStack(R.id.main_nav_graph,true)
                    navController.navigate(R.id.loginFragment)
                    true
                } else
                    false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showRateDialog() {
        AppRate.with(requireContext())
            .setInstallDays(0) // default 10, 0 means install day.
            .setLaunchTimes(3) // default 10
            .setRemindInterval(2) // default 1
            .setShowLaterButton(true) // default true
            .setDebug(false) // default false
            .setOnClickButtonListener {
                //    Log.d(MainActivity::class.java.name, Integer.toString(which))
            }
            .monitor()

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(requireActivity())
    }

    private fun swipeGesturesHandler() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                } else {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    task.isImp = !task.isImp
                    viewModel.update(task)
                    taskAdapter.notifyItemChanged(viewHolder.adapterPosition) //changed from notify-all to change-one
                }
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean,
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

    private fun taskEventCollector() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is TaskViewModel.TaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(
                            requireView(),
                            getString(R.string.task_deleted),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction(getString(R.string.undo)) {
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }
                    TaskViewModel.TaskEvent.NavigateToAllCompletedScreen -> {
                        val action =
                            TaskFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        navController.navigate(action)
                    }
                }
            }
        }
    }

    private fun taskObserver() {
        viewModel.todos.observe(viewLifecycleOwner) {
            if (isLayoutChange) {
                if (viewType) {
                    binding.todoRecyclerView.layoutManager =
                        StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
                } else {
                    binding.todoRecyclerView.layoutManager =
                        LinearLayoutManager(requireContext())
                }
                isLayoutChange = false
            }
            taskAdapter.submitList(it)
        }
    }

    private fun completedTaskObserver() {
        viewModel.todos2.observe(viewLifecycleOwner) {
            var count = 0
            for (element in it) {
                if (element.isDone)
                    count++
            }
            if (it.isEmpty()) {
                binding.createNewTask.visibility = View.VISIBLE
                binding.todoRecyclerView.visibility = View.GONE
                binding.taskProgress.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.VISIBLE
                binding.taskProgress.visibility = View.VISIBLE
                binding.createNewTask.visibility = View.GONE
                binding.todoRecyclerView.visibility = View.VISIBLE
                val value = (count.toFloat() / it.size.toFloat()) * 100
                binding.progressBar.progress = value.toInt()
                binding.taskProgress.text = getString(R.string.task_progress, count, it.size)
            }
        }
    }

    private fun addNewTask() {
        val action =
            TaskFragmentDirections.actionTaskFragmentToRenameFragmentDialog(false, -1, null)
        navController.navigate(action)
    }

    override fun onItemClicked(task: Task, position: Int) {
        if (is_in_action_mode) {
            if (selectedItem!![position]) {
                selectedItem!![position] = false
                counter--
                actionMode!!.title = "${counter}/${taskAdapter.currentList.size} Selected"
            } else {
                selectedItem!![position] = true
                counter++
                actionMode!!.title = "${counter}/${taskAdapter.currentList.size} Selected"
            }
        } else if (!is_in_action_mode) {
            val action =
                TaskFragmentDirections.actionTaskFragmentToSubTaskFragment(
                    task.id,
                    task.title, task.isDone
                )
            navController.navigate(action)
        }
    }

    override fun onCheckBoxClicked(
        task: Task,
        taskCompleted: Boolean,
    ) {
        viewModel.onTaskCheckedChanged(task, taskCompleted)
    }

    override fun onDeleteClicked(task: Task, position: Int) {
        if (task.isDone) {
            viewModel.showDeleteDialog(requireContext(), task)
        } else {
            taskPosition = position
            val action = TaskFragmentDirections.actionTaskFragmentToRenameFragmentDialog(
                false,
                task.id,
                task.title
            )
            navController.navigate(action)
        }
    }

    private val callback = object : ActionMode.Callback {
        override fun onCreateActionMode(
            mode: ActionMode?,
            menu: Menu?,
        ): Boolean {
            val menuInflater = MenuInflater(requireContext())
            menuInflater.inflate(R.menu.action_menu, menu)
            return true
        }

        override fun onPrepareActionMode(
            mode: ActionMode?,
            menu: Menu?,
        ): Boolean {
            return false
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onActionItemClicked(
            mode: ActionMode?,
            item: MenuItem?,
        ): Boolean {
            return when (item?.itemId) {
                R.id.action_delete -> {
                    if (counter == 0) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_select_task),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (selectedItem!!.isNotEmpty()) {
                        if (counter == taskAdapter.currentList.size) {
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
                    if (!is_select_all) {
                        item.setIcon(R.drawable.ic_select_all_on)
                        for (i in 0 until taskAdapter.currentList.size)
                            selectedItem!![i] == true

                        counter = taskAdapter.currentList.size
                        actionMode!!.title =
                            "${counter}/${taskAdapter.currentList.size} Selected"
                        is_select_all = true
                    } else {
                        item.setIcon(R.drawable.ic_select_all)
                        for (i in 0 until taskAdapter.currentList.size)
                            selectedItem!![i] == false

                        counter = 0
                        is_select_all = false
                        actionMode!!.title =
                            "${counter}/${taskAdapter.currentList.size} Selected"
                    }
                    taskAdapter.notifyDataSetChanged()
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
        if (!is_in_action_mode) {
            onActionMode(true)
            counter = 1
            selectedItem!![position] = true
        } else {
            if (selectedItem!![position]) {
                selectedItem!![position] = false
                counter--
            } else {
                selectedItem!![position] = true
                counter++
            }
        }
        if (actionMode == null) {
            actionMode =
                (activity as AppCompatActivity).startSupportActionMode(callback)!!
        }
        actionMode!!.title = "${counter}/${taskAdapter.currentList.size} Selected"
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onActionMode(actionModeOn: Boolean) {
        if (actionModeOn) {
            selectedItem = Array(taskAdapter.currentList.size) { false }
            is_in_action_mode = true
            binding.soundTask.visibility = View.GONE
            binding.fab.visibility = View.GONE
        } else {
            is_in_action_mode = false
            is_select_all = false
            binding.fab.visibility = View.VISIBLE
            binding.soundTask.visibility = View.VISIBLE
            taskAdapter.notifyDataSetChanged()
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
                        viewModel.deleteAllTasks()
                    }
                } else {
                    for (i in selectedItem!!.indices) {
                        if (selectedItem!![i]) {
                            viewModel.delete(taskAdapter.currentList[i])
                        }
                    }
                }
                counter = 0
                actionMode!!.finish()
                onActionMode(false)
                notifyUser(requireContext(), getString(R.string.notify_delete))
                dialog.dismiss()
            }.show()
    }

    private fun checkUpdate() {
        val appUpdateInfoTask = appUpdateManager!!.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    appUpdateManager!!.startUpdateFlowForResult(
                        appUpdateInfo, AppUpdateType.FLEXIBLE,
                        requireActivity(), UPDATE_REQUEST_CODE
                    )
                } catch (exception: IntentSender.SendIntentException) {
                    notifyUser(requireContext(), exception.message.toString())
                }
            }
        }
    }

    private val appUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Snackbar.make(requireView(), "New app is ready", Snackbar.LENGTH_INDEFINITE)
                .setAction("Restart") {
                    appUpdateManager!!.completeUpdate()
                }.show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("deprecation")
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        if (requestCode == UPDATE_REQUEST_CODE) {
            notifyUser(requireContext(), "Downloading start")
            if (resultCode != Activity.RESULT_OK) {
                notifyUser(requireActivity().applicationContext, "Update failed")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (searchView != null)
            searchView!!.setOnQueryTextListener(null)
        shareTxt = null
        isWidgetClick = false
    }
}