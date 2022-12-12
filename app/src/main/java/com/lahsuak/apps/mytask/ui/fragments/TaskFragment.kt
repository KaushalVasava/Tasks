package com.lahsuak.apps.mytask.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.*
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.lahsuak.apps.mytask.MyTaskApp
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.SortOrder
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.databinding.FragmentTaskBinding
import com.lahsuak.apps.mytask.model.TaskEvent
import com.lahsuak.apps.mytask.ui.MainActivity.Companion.isWidgetClick
import com.lahsuak.apps.mytask.ui.MainActivity.Companion.shareTxt
import com.lahsuak.apps.mytask.ui.adapters.TaskAdapter
import com.lahsuak.apps.mytask.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.mytask.util.*
import com.lahsuak.apps.mytask.util.Constants.REM_KEY
import com.lahsuak.apps.mytask.util.Constants.UPDATE_REQUEST_CODE
import com.lahsuak.apps.mytask.util.Util.speakToAddTask
import com.lahsuak.apps.mytask.util.Util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import hotchemi.android.rate.AppRate
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.flow.collect
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
                val task = Task(
                    id = 0,
                    result1!![0],
                    isDone = false,
                )
                viewModel.insert(task)
            }
        }
    private val permissionResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                /* no-op */
            } else {
                context.toast {
                    getString(R.string.user_cancelled_the_operation)
                }
            }
        }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.setOnLongClickListener {
            if (navController.currentDestination?.id == R.id.subTaskFragment) {
                Util.setClipboard(MyTaskApp.appContext, binding.toolbar.title.toString())
            }
            true
        }

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
//        setupActionBarWithNavController((activity as AppCompatActivity), navController)

        val slidingPaneLayout = binding.slidingPaneLayout
        slidingPaneLayout.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            SportsListOnBackPressedCallback(slidingPaneLayout)
        )

        val prefManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.btnVoiceTask.isVisible =
            prefManager.getBoolean(Constants.SHOW_VOICE_TASK_KEY, true)
        setOptionMenu()
        checkPermission()
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewType = viewModel.preferencesFlow.first().viewType
            if (viewType) {
                binding.taskRecyclerView.layoutManager =
                    StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
            } else {
                binding.taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        }
        taskAdapter = TaskAdapter(this)
        binding.taskRecyclerView.apply {
            adapter = taskAdapter
            setHasFixedSize(true)
        }
        showRateDialog()
        // checking update of application
        appUpdateManager = AppUpdateManagerFactory.create(requireContext())
        checkUpdate()
        appUpdateManager!!.registerListener(appUpdateListener)

        if (shareTxt != null) {
            navController.navigate(
                R.id.renameFragmentDialog,
                bundleOf(
                    AddUpdateTaskFragmentDialog.TASK_ID_ARG to -1,
                    AddUpdateTaskFragmentDialog.TASK_TITLE_ARG to shareTxt,
                    AddUpdateTaskFragmentDialog.SOURCE_ARG to false,
                    AddUpdateTaskFragmentDialog.SUBTASK_ID_ARG to -1
                )
            )
            binding.slidingPaneLayout.open()
//            val action = TaskFragmentDirections.actionTaskFragmentToRenameFragmentDialog(
//                false,
//                -1,
//                shareTxt!!
//            )
//            navController.navigate(action)
        }
        swipeGesturesHandler() //swipe to delete and mark as imp functionality
        taskObserver() //observer for tasks and layout changes
        completedTaskObserver() //observer for completed and uncompleted tasks
        taskEventCollector()
        addClickListeners()
        savedStateHandleValueObserver()
    }

    private fun setOptionMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.app_menu, menu)
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
                    menu.findItem(R.id.itemView).title =
                        if (viewType) {
                            getString(R.string.list_view)
                        } else {
                            getString(R.string.grid_view)
                        }
                    menu.findItem(R.id.showTask).isChecked =
                        viewModel.preferencesFlow.first().hideCompleted
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.sortByName -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_NAME, requireContext())
                        true
                    }
                    R.id.sortByDate -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_DATE, requireContext())
                        true
                    }
                    R.id.itemView -> {
                        if (!viewType) {
                            menuItem.title = getString(R.string.list_view)
                            binding.taskRecyclerView.adapter = taskAdapter
                            viewType = true
                        } else {
                            menuItem.title = getString(R.string.grid_view)
                            binding.taskRecyclerView.adapter = taskAdapter
                            viewType = false
                        }
                        isLayoutChange = true
                        viewModel.onViewTypeChanged(viewType, requireContext())
                        true
                    }
                    R.id.showTask -> {
                        menuItem.isChecked = !menuItem.isChecked
                        viewModel.onHideCompleted(menuItem.isChecked, requireContext())
                        true
                    }
                    R.id.delete_all_completed_task -> {
                        viewModel.onDeleteAllCompletedClick()
                        true
                    }
                    R.id.setting -> {
//                        val action = TaskFragmentDirections.actionTaskFragmentToSettingsFragment()
                        navController.navigate(R.id.settingsFragment)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun checkPermission() {
        if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) {
            PermissionUtil.checkAndLaunchPermission(
                fragment = this,
                permissions = arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                permissionLauncher = permissionResultLauncher,
                showRationaleUi = {
                    PermissionUtil.showSettingsSnackbar(
                        requireActivity(),
                        requireView(),
                    )
                },
                lazyBlock = {},
            )
        }
    }

    private fun addClickListeners() {
        binding.btnAddTask.setOnClickListener {
            addNewTask()
        }

        binding.btnCreateNewTask.setOnClickListener {
            addNewTask()
        }

        binding.btnVoiceTask.setOnClickListener {
            speakToAddTask(requireActivity(), speakLauncher)
        }
    }

    private fun savedStateHandleValueObserver() {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Long?>(REM_KEY)
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
        }).attachToRecyclerView(binding.taskRecyclerView)
    }

    private fun taskEventCollector() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is TaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(
                            requireView(),
                            getString(R.string.task_deleted),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction(getString(R.string.undo)) {
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }
                    TaskEvent.NavigateToAllCompletedScreen -> {
//                        val action =
//                            TaskFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        navController.navigate(R.id.deleteAllCompletedDialogFragment)
                    }
                }
            }
        }
    }

    private fun taskObserver() {
        viewModel.tasksFlow.asLiveData().observe(viewLifecycleOwner) {
            if (isLayoutChange) {
                if (viewType) {
                    binding.taskRecyclerView.layoutManager =
                        StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
                } else {
                    binding.taskRecyclerView.layoutManager =
                        LinearLayoutManager(requireContext())
                }
                isLayoutChange = false
            }
            taskAdapter.submitList(it)
        }
    }

    private fun completedTaskObserver() {
        viewModel.tasksFlow2.asLiveData().observe(viewLifecycleOwner) {
            var count = 0
            for (element in it) {
                if (element.isDone)
                    count++
            }
            val hasTasks = it.isNotEmpty()

            binding.btnCreateNewTask.isVisible = !hasTasks
            binding.taskRecyclerView.isVisible = hasTasks
            binding.txtTaskProgress.isVisible = hasTasks
            binding.progressBar.isVisible = hasTasks
            val value = (count.toFloat() / it.size.toFloat()) * 100
            binding.progressBar.progress = value.toInt()
            binding.txtTaskProgress.text = getString(R.string.task_progress, count, it.size)
        }
    }

    private fun addNewTask() {
//        val action =
//            TaskFragmentDirections.actionTaskFragmentToRenameFragmentDialog(
//                false,
//                -1,
//                null
//            )
        try {
            val bundle = bundleOf(
                AddUpdateTaskFragmentDialog.TASK_ID_ARG to -1,
                AddUpdateTaskFragmentDialog.TASK_TITLE_ARG to "",
                AddUpdateTaskFragmentDialog.SOURCE_ARG to false,
                AddUpdateTaskFragmentDialog.SUBTASK_ID_ARG to -1
            )
            navController.navigate(
                R.id.renameFragmentDialog,
                bundle
            )
        } catch (e: Throwable) {
            e.logError()
        }
        binding.slidingPaneLayout.open()
    }

    override fun onItemClicked(task: Task, position: Int) {
        if (is_in_action_mode) {
            selectedItem!![position] =
                if (selectedItem!![position]) {
                    counter--
                    false
                } else {
                    counter++
                    true
                }
            actionMode!!.title =
                getString(R.string.task_selected, counter, taskAdapter.itemCount)
        } else {
//            val action =
//                TaskFragmentDirections.actionTaskFragmentToSubTaskFragment(
//                    task.id,
//                    task.title, task.isDone
//                )
//            navController.navigate(action)
            navController.navigate(
                R.id.subTaskFragment,
                bundleOf(
                    SubTaskFragment.ID_ARG to task.id,
                    SubTaskFragment.TITLE_ARG to task.title,
                    SubTaskFragment.STATUS_ARG to task.isDone,
                )
            )
            binding.slidingPaneLayout.open()
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
//            val action = TaskFragmentDirections.actionTaskFragmentToRenameFragmentDialog(
//                false,
//                task.id,
//                task.title
//            )
//            navController.navigate(action)
            navController.navigate(
                R.id.renameFragmentDialog,
                bundleOf(
                    AddUpdateTaskFragmentDialog.TASK_ID_ARG to task.id,
                    AddUpdateTaskFragmentDialog.TASK_TITLE_ARG to task.title,
                    AddUpdateTaskFragmentDialog.SOURCE_ARG to false,
                    AddUpdateTaskFragmentDialog.SUBTASK_ID_ARG to -1
                )
            )
            binding.slidingPaneLayout.open()
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
                        showDialog(counter == taskAdapter.currentList.size)
                    }
                    true
                }
                R.id.action_selectAll -> {
                    is_select_all = if (!is_select_all) {
                        item.setIcon(R.drawable.ic_select_all_on)
                        for (i in 0 until taskAdapter.currentList.size)
                            selectedItem!![i] == true
                        counter = taskAdapter.currentList.size
                        true
                    } else {
                        item.setIcon(R.drawable.ic_select_all)
                        for (i in 0 until taskAdapter.currentList.size)
                            selectedItem!![i] == false
                        counter = 0
                        false
                    }
                    actionMode!!.title =
                        getString(R.string.task_selected, counter, taskAdapter.itemCount)
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
            selectedItem!![position] = true
            counter = 1
        } else {
            selectedItem!![position] = if (selectedItem!![position]) {
                counter--
                false
            } else {
                counter++
                true
            }
        }
        if (actionMode == null) {
            actionMode =
                (activity as AppCompatActivity).startSupportActionMode(callback)!!
        }
        actionMode!!.title =
            getString(R.string.task_selected, counter, taskAdapter.itemCount)
    }

    private fun onActionMode(isActionModeOn: Boolean) {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.btnVoiceTask.isVisible = !isActionModeOn &&
                prefManager.getBoolean(Constants.SHOW_VOICE_TASK_KEY, true)
        binding.btnAddTask.isVisible = !isActionModeOn

        is_in_action_mode = isActionModeOn
        if (isActionModeOn) {
            selectedItem = Array(taskAdapter.currentList.size) { false }
        } else {
            is_select_all = false
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
                context.toast {
                    getString(R.string.notify_delete)
                }
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
                    context.toast {
                        exception.message.toString()
                    }
                }
            }
        }
    }

    private val appUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Snackbar.make(
                requireView(),
                getString(R.string.new_app_ready),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.restart)) {
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
            context.toast {
                getString(R.string.downloading_start)
            }
            if (resultCode != Activity.RESULT_OK) {
                MyTaskApp.appContext.toast {
                    getString(R.string.update_failed)
                }
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