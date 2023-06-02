package com.lahsuak.apps.tasks.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.SortOrder
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.databinding.FragmentTaskBinding
import com.lahsuak.apps.tasks.databinding.TaskSelectionDialogBinding
import com.lahsuak.apps.tasks.model.TaskEvent
import com.lahsuak.apps.tasks.ui.MainActivity.Companion.isWidgetClick
import com.lahsuak.apps.tasks.ui.MainActivity.Companion.shareTxt
import com.lahsuak.apps.tasks.ui.adapters.TaskAdapter
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.*
import com.lahsuak.apps.tasks.util.AppConstants.REM_KEY
import com.lahsuak.apps.tasks.util.AppConstants.UPDATE_REQUEST_CODE
import com.lahsuak.apps.tasks.util.AppUtil.speakToAddTask
import com.lahsuak.apps.tasks.util.AppUtil.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import hotchemi.android.rate.AppRate
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_task), TaskAdapter.TaskListener, SelectionListener {
    private var _binding: FragmentTaskBinding? = null
    private val binding: FragmentTaskBinding
        get() = _binding!!
    private val navController: NavController by unsafeLazy {
        findNavController()
    }
    private val viewModel: TaskViewModel by viewModels()
    private val taskAdapter: TaskAdapter by lazy {
        TaskAdapter(this, this)
    }
    private var searchView: SearchView? = null
    private var actionMode: ActionMode? = null
    private var appUpdateManager: AppUpdateManager? = null
    private var isLayoutChange = false
    private var taskPosition: Int = -1
    private var selectedSortPosition = 0

    private var selectedItem: Array<Boolean>? = null
    private var viewType = false // listview = false, gridView = true
    private var counter = 0
    private var actionModeEnable = false
    private var isSelectAll = false
    private var openTaskItems = mutableListOf<Boolean>()

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentTaskBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()

        binding.txtTitle.text = DateUtil.getToolbarDateTime(System.currentTimeMillis())
        val prefManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.btnVoiceTask.isVisible =
            prefManager.getBoolean(AppConstants.SHOW_VOICE_TASK_KEY, true)
        checkPermission()
        initView()
        showRateDialog()
        // checking update of application
        appUpdateManager = AppUpdateManagerFactory.create(requireContext())
        checkUpdate()
        appUpdateManager!!.registerListener(appUpdateListener)
        if (shareTxt != null) {
            showTitleSelectionDialog()
        }
        addSwipeGesturesHandler() //swipe to delete and mark as imp functionality
        setTaskObserver() //observer for tasks and layout changes
        setCompletedTaskObserver() //observer for completed and uncompleted tasks
        setTaskEventCollector()
        addClickListeners()
        savedStateHandleValueObserver()
    }

    private fun initView() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewType = viewModel.preferencesFlow.first().viewType
            binding.taskRecyclerView.layoutManager = if (viewType) {
                binding.btnView.setImageResource(R.drawable.ic_list_view)
                StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
            } else {
                binding.btnView.setImageResource(R.drawable.ic_grid_view)
                LinearLayoutManager(requireContext())
            }
        }
        binding.taskRecyclerView.apply {
            setHasFixedSize(true)
            adapter = taskAdapter
        }
    }

    private fun showTitleSelectionDialog() {
        val dialog = Dialog(requireContext())
        val taskSelectionBinding =
            TaskSelectionDialogBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(taskSelectionBinding.root)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        taskSelectionBinding.btnNewTask.setOnClickListener {
            addNewTask(shareTxt)
            dialog.dismiss()
            shareTxt = null
        }
        viewModel.tasksFlow.asLiveData().observe(viewLifecycleOwner) { list ->
            if (shareTxt != null) {
                val adapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, list.map { it.title }
                )
                adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )
                taskSelectionBinding.taskPicker.adapter = adapter
                var selectedPosition = -1
                taskSelectionBinding.taskPicker.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedPosition = position
                        }

                        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
                    }
                dialog.show()
                taskSelectionBinding.btnSet.setOnClickListener {
                    val action = TaskFragmentDirections.actionTaskFragmentToSubTaskFragment(
                        list[selectedPosition].copy(isDone = false),
                        true,
                        shareTxt!!
                    )
                    navController.navigate(action)
                    shareTxt = null
                    dialog.dismiss()
                }
                taskSelectionBinding.btnCancel.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) {
            PermissionUtil.checkAndLaunchPermission(
                fragment = this,
                permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                permissionLauncher = permissionResultLauncher,
                showRationaleUi = {
                    PermissionUtil.showSettingsSnackBar(
                        requireActivity(),
                        requireView(),
                    )
                },
                lazyBlock = {},
            )
        }
    }

    private fun addClickListeners() {
        searchView = binding.searchView
        val pendingQuery = viewModel.searchQuery.value
        if (!pendingQuery.isNullOrEmpty()) {
            searchView?.setQuery(pendingQuery, false)
        }
        searchView?.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
        searchView?.queryHint = getString(R.string.search_task)

        binding.btnAddTask.setOnClickListener {
            addNewTask(null)
        }
        binding.btnCreateNewTask.setOnClickListener {
            addNewTask(null)
        }
        binding.btnVoiceTask.setOnClickListener {
            speakToAddTask(requireActivity(), speakLauncher)
        }
        binding.btnView.setOnClickListener {
            setView()
        }
        binding.btnDeleteAll.setOnClickListener {
            viewModel.onDeleteAllCompletedClick()
        }
        binding.btnSettings.setOnClickListener {
            val action =
                TaskFragmentDirections.actionTaskFragmentToSettingsFragment()
            navController.navigate(action)
        }
        setSortMenu()
        setVisibilityOfTasks()
    }

    private fun setView() {
        viewType = if (!viewType) {
            binding.btnView.setImageResource(R.drawable.ic_list_view)
            true
        } else {
            binding.btnView.setImageResource(R.drawable.ic_grid_view)
            false
        }
        binding.taskRecyclerView.adapter = taskAdapter
        isLayoutChange = true
        viewModel.onViewTypeChanged(viewType, requireContext())
    }

    private fun setSortMenu() {
        val sortTypes = listOf(
            SortOrder.BY_NAME.name.toSortForm(),
            SortOrder.BY_DATE.name.toSortForm()
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortTypes
        )
        binding.sortMenu.adapter = adapter
        binding.sortMenu.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    if (selectedSortPosition != pos) {
                        selectedSortPosition = pos
                        val sortType = if (sortTypes[pos] != SortOrder.BY_NAME.name.toSortForm()) {
                            SortOrder.BY_NAME
                        } else {
                            SortOrder.BY_DATE
                        }
                        viewModel.onSortOrderSelected(sortType, requireContext())
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    /* no-op */
                }
            }
    }

    private fun setButtonVisibility(isVisible: Boolean) {
        binding.btnAddTask.isVisible = isVisible
        binding.btnVoiceTask.isVisible = isVisible
        binding.btnDeleteAll.isVisible = !isVisible
    }

    private fun setVisibilityOfTasks() {
        binding.chipActive.setOnClickListener {
            if (binding.chipActive.isChecked) {
                setButtonVisibility(true)
                viewModel.onHideCompleted(true, requireContext())
            } else {
                binding.chipActive.isChecked = true
            }
        }
        binding.chipDone.setOnClickListener {
            if (binding.chipDone.isChecked) {
                setButtonVisibility(false)
                viewModel.onHideCompleted(false, requireContext())
            } else {
                binding.chipDone.isChecked = true
            }
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
            .setOnClickButtonListener { /* no-op */ }
            .monitor()
        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(requireActivity())
    }

    private fun addSwipeGesturesHandler() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
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
                    taskAdapter.notifyItemChanged(viewHolder.adapterPosition)
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
                        ContextCompat.getColor(requireContext(), R.color.red)
                    )
                    .addSwipeRightBackgroundColor(
                        requireContext().getAttribute(R.attr.colorPrimary)
                    )
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    .addSwipeRightActionIcon(R.drawable.ic_pin)
                    .addSwipeRightLabel(getString(R.string.important_task))
                    .addSwipeLeftLabel(getString(R.string.delete))
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

    private fun setTaskEventCollector() {
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
                        val action =
                            TaskFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        navController.navigate(action)
                    }
                }
            }
        }
    }

    private fun setTaskObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksFlow.collectLatest {
                if (isLayoutChange) {
                    binding.taskRecyclerView.layoutManager =
                        if (viewType) {
                            StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
                        } else {
                            LinearLayoutManager(requireContext())
                        }
                    isLayoutChange = false
                }
                val data = if (binding.chipActive.isChecked) {
                    it.filter { task ->
                        !task.isDone
                    }
                } else {
                    it.filter { task ->
                        task.isDone
                    }
                }
                Log.d("TAG", "TASK : # ${data.map { it.title to it.date }}")
                taskAdapter.submitList(data)
                if (openTaskItems.isEmpty()) {
                    data.forEachIndexed { index, _ ->
                        openTaskItems.add(index, true)
                    }
                }
            }
        }
    }

    private fun setCompletedTaskObserver() {
        viewModel.tasksFlow2.asLiveData().observe(viewLifecycleOwner) {
            val count = it.count { task -> task.isDone }
            val hasTasks = it.isNotEmpty()
            binding.btnCreateNewTask.isVisible = !hasTasks
            binding.taskRecyclerView.isVisible = hasTasks
            binding.txtTaskProgress.isVisible = hasTasks
            binding.progressBar.isVisible = hasTasks
            binding.chipGroup.isVisible = hasTasks
            val value = (count.toFloat() / it.size.toFloat()) * TOTAL_PROGRESS_VALUE
            binding.progressBar.progress = value.toInt()
            binding.txtTaskProgress.text = getString(R.string.task_progress, count, it.size)
        }
    }

    private fun addNewTask(shareText: String?) {
        val action =
            TaskFragmentDirections.actionTaskFragmentToRenameFragmentDialog(
                false,
                -1,
                shareText
            )
        navController.navigate(action)
    }

    override fun onItemClicked(task: Task, position: Int) {
        if (actionModeEnable) {
            selectedItem!![position] =
                if (selectedItem!![position]) {
                    counter--
                    false
                } else {
                    counter++
                    true
                }
            actionMode!!.title =
                String.format(getString(R.string.task_selected), counter, taskAdapter.itemCount)
        } else {
            val action =
                TaskFragmentDirections.actionTaskFragmentToSubTaskFragment(
                    task,
                    false,
                    null
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
                        showDialog(counter == taskAdapter.currentList.size)
                    }
                    true
                }

                R.id.action_selectAll -> {
                    isSelectAll = if (!isSelectAll) {
                        item.setIcon(R.drawable.ic_select_all_on)
                        for (i in 0 until taskAdapter.currentList.size)
                            selectedItem!![i]
                        counter = taskAdapter.currentList.size
                        true
                    } else {
                        item.setIcon(R.drawable.ic_select_all)
                        for (i in 0 until taskAdapter.currentList.size)
                            !selectedItem!![i]
                        counter = 0
                        false
                    }
                    actionMode!!.title =
                        String.format(
                            getString(R.string.task_selected),
                            counter,
                            taskAdapter.itemCount
                        )
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
        if (!actionModeEnable) {
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
            String.format(getString(R.string.task_selected), counter, taskAdapter.itemCount)
    }

    override fun getExpandCollapseState(position: Int): Boolean {
        return openTaskItems[position]
    }

    override fun setExpandCollapseState(position: Int, isExpanded: Boolean) {
        openTaskItems[position] = isExpanded
    }

    private fun setViewVisibility(isVisible: Boolean) {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.btnVoiceTask.isVisible = !isVisible &&
                prefManager.getBoolean(AppConstants.SHOW_VOICE_TASK_KEY, true)
        binding.btnAddTask.isVisible = !isVisible
        binding.sortMenu.isVisible = !isVisible
        binding.chipGroup.isVisible = !isVisible
        binding.btnView.isVisible = !isVisible
        binding.searchView.isVisible = !isVisible
        binding.txtSort.isVisible = !isVisible
    }

    private fun onActionMode(isActionModeOn: Boolean) {
        setViewVisibility(isActionModeOn)
        actionModeEnable = isActionModeOn
        if (isActionModeOn) {
            selectedItem = Array(taskAdapter.currentList.size) { false }
        } else {
            isSelectAll = false
            taskAdapter.notifyItemRangeChanged(0, taskAdapter.currentList.size)
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
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo

        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo, AppUpdateType.FLEXIBLE,
                        requireActivity(), UPDATE_REQUEST_CODE
                    )
                } catch (exception: IntentSender.SendIntentException) {
                    context.toast { exception.message.toString() }
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
        @Suppress(AppConstants.DEPRECATION)
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        if (requestCode == UPDATE_REQUEST_CODE) {
            context.toast { getString(R.string.downloading_start) }
            if (resultCode != Activity.RESULT_OK) {
                TaskApp.appContext.toast { getString(R.string.update_failed) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (searchView != null)
            searchView!!.setOnQueryTextListener(null)
        shareTxt = null
        isWidgetClick = false
        _binding = null
    }

    override fun getActionModeStatus(): Boolean {
        return actionModeEnable
    }

    override fun getCounter(): Int {
        return counter
    }

    override fun getViewType(): Boolean {
        return viewType
    }

    override fun isAllSelected(): Boolean {
        return isSelectAll
    }

    override fun setItemStatus(status: Boolean, position: Int) {
        selectedItem?.set(position, status)
    }

    override fun getItemStatus(position: Int): Boolean {
        return selectedItem?.get(position) ?: false
    }

    override fun getSelectedItemEmpty(): Boolean {
        return selectedItem.isNullOrEmpty()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putBoolean(VIEW_TYPE_BUNDLE_KEY, viewType)
            putInt(COUNTER_BUNDLE_KEY, counter)
            putBoolean(IS_IN_ACTION_MODE_BUNDLE_KEY, actionModeEnable)
            putBoolean(IS_SELECT_ALL_BUNDLE_KEY, isSelectAll)
            putBooleanArray(SELECTED_ITEMS_BUNDLE_KEY, selectedItem?.toBooleanArray())
            putBooleanArray(OPEN_TASK_ITEMS_BUNDLE_KEY, openTaskItems.toBooleanArray())
            putBoolean(TASKS_STATUS_BUNDLE_KEY, _binding?.chipActive?.isChecked ?: false)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            viewType = it.getBoolean(VIEW_TYPE_BUNDLE_KEY)
            counter = it.getInt(COUNTER_BUNDLE_KEY, counter)
            actionModeEnable = it.getBoolean(IS_IN_ACTION_MODE_BUNDLE_KEY)
            isSelectAll = it.getBoolean(IS_SELECT_ALL_BUNDLE_KEY)
            selectedItem = it.getBooleanArray(SELECTED_ITEMS_BUNDLE_KEY)?.toTypedArray()
            openTaskItems =
                (it.getBooleanArray(OPEN_TASK_ITEMS_BUNDLE_KEY)?.toTypedArray()?.toMutableList()
                    ?: emptyList()) as MutableList<Boolean>
            val isTaskDone = it.getBoolean(TASKS_STATUS_BUNDLE_KEY)
            setButtonVisibility(isTaskDone)
            if (actionModeEnable) {
                if (actionMode == null) {
                    actionMode = (activity as AppCompatActivity).startSupportActionMode(callback)
                    setViewVisibility(true)
                }
                actionMode?.title =
                    String.format(getString(R.string.task_selected), counter, selectedItem?.size)
            }
        }
    }

    companion object {
        const val VIEW_TYPE_BUNDLE_KEY = "view_type_bundle_key"
        const val COUNTER_BUNDLE_KEY = "counter_bundle_key"
        const val IS_IN_ACTION_MODE_BUNDLE_KEY = "is_in_action_mode_bundle_key"
        const val IS_SELECT_ALL_BUNDLE_KEY = "is_select_all_bundle_key"
        const val SELECTED_ITEMS_BUNDLE_KEY = "selected_items_bundle_key"
        const val OPEN_TASK_ITEMS_BUNDLE_KEY = "selected_items_bundle_key"
        const val TASKS_STATUS_BUNDLE_KEY = "tasks_status_bundle_key"
        const val TOTAL_PROGRESS_VALUE = 100
    }
}