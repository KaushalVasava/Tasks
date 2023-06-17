package com.lahsuak.apps.tasks.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.SortOrder
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.databinding.FragmentSubtaskBinding
import com.lahsuak.apps.tasks.model.SubTaskEvent
import com.lahsuak.apps.tasks.ui.adapters.SubTaskAdapter
import com.lahsuak.apps.tasks.ui.fragments.TaskFragment.Companion.TOTAL_PROGRESS_VALUE
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.util.*
import com.lahsuak.apps.tasks.util.AppUtil.createNotification
import com.lahsuak.apps.tasks.util.AppUtil.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SubTaskFragment : Fragment(R.layout.fragment_subtask),
    SubTaskAdapter.SubTaskListener, SelectionListener {

    private var _binding: FragmentSubtaskBinding? = null
    private val binding: FragmentSubtaskBinding
        get() = _binding!!

    private val subTaskViewModel: SubTaskViewModel by viewModels()
    private val args: SubTaskFragmentArgs by navArgs()
    private val navController: NavController by unsafeLazy {
        findNavController()
    }
    private val subTaskAdapter: SubTaskAdapter by lazy {
        SubTaskAdapter(this, this)
    }
    private lateinit var task: Task
    private var searchView: SearchView? = null
    private var actionMode: ActionMode? = null
    private val mCalendar = Calendar.getInstance()
    private var selectedItem: Array<Boolean>? = null
    private var counter = 0
    private var actionModeEnable = false
    private var isSelectedAll = false
    private var viewType = false // listview = false, gridView = true
    private var isTaskActive = true
    private var selectedSortPosition = 0

    private val speakLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val result1 = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val subTask = SubTask(
                    id = task.id,
                    subTitle = result1!![0],
                    sId = 0
                )
                subTaskViewModel.insertSubTask(subTask)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            // Scope the transition to a view in the hierarchy so we know it will be added under
            // the bottom app bar but over the elevation scale of the exiting HomeFragment.
            drawingViewId = R.id.my_container
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().getAttribute(R.attr.colorSurface))
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSubtaskBinding.inflate(inflater, container, false)
        selectedItem = null
//        postponeEnterTransition(200, TimeUnit.MILLISECONDS)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
        task = args.task
        initView()
        subTaskViewModel.taskId.value = task.id
        handleReminder()
        if (args.isSharedSubtask) {
            val action = SubTaskFragmentDirections.actionSubTaskFragmentToRenameFragmentDialog(
                true,
                task.id,
                args.sharedText,
                -1,
            )
            navController.navigate(action)
        }
        createNotification(requireContext())
        addSwipeGesturesHandler() //swipe to delete and mark as imp functionality
        completedSubTaskObserver() //completed and uncompleted task observer
        setSubTaskObserver()
        subTaskEventCollector() //subtask event handler
        addClickListeners()
        setTransition()
    }

    private fun initView() {
        binding.root.setBackgroundColor(AppUtil.getTransparentColor(task.color))
        binding.txtTitle.text = task.title
        setColors(TaskApp.categoryTypes[task.color].color)
        val prefManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.btnVoiceTask.isVisible =
            prefManager.getBoolean(AppConstants.SHOW_VOICE_TASK_KEY, true)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewType = subTaskViewModel.preferencesFlow.first().viewType
            binding.subTaskRecyclerView.layoutManager =
                if (viewType) {
                    StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
                } else {
                    LinearLayoutManager(requireContext())
                }
        }
        binding.subTaskRecyclerView.apply {
            setHasFixedSize(true)
            adapter = subTaskAdapter
        }
    }

    private fun setSubTaskObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            subTaskViewModel.subTasks.collectLatest { //this is for adapter
                val data = if (binding.chipActive.isChecked) {
                    it.filter { task ->
                        !task.isDone
                    }
                } else {
                    it.filter { task ->
                        task.isDone
                    }
                }
                subTaskAdapter.submitList(data)
            }
        }
    }

    private fun addClickListeners() {
        searchView = binding.searchView
        val pendingQuery = subTaskViewModel.searchQuery.value
        if (!pendingQuery.isNullOrEmpty()) {
            searchView?.setQuery(pendingQuery, false)
        }
        searchView?.onQueryTextChanged {
            subTaskViewModel.searchQuery.value = it
        }
        searchView?.queryHint = getString(R.string.search_subtask)

        binding.btnBack.setOnClickListener {
            navController.popBackStack()
        }
        binding.btnCreateNewTask.setOnClickListener {
            addNewTask()
        }
        binding.btnAddTask.setOnClickListener {
            addNewTask()
        }
        binding.btnVoiceTask.setOnClickListener {
            AppUtil.speakToAddTask(requireActivity(), speakLauncher)
        }
        binding.reminderLayout.setOnClickListener {
            subTaskViewModel.showReminder(
                binding,
                requireActivity(),
                mCalendar,
                task
            )
        }
        binding.btnCancelReminder.setOnClickListener {
            subTaskViewModel.cancelReminder(
                requireActivity(),
                task,
                binding.txtReminder
            )
            binding.btnCancelReminder.isVisible = false
            binding.reminderLayout.background = null
            binding.txtReminder.isEnabled = false
            binding.txtReminder.setTextColor(
                requireContext().getAttribute(R.attr.colorOnSurface)
            )
            binding.imgReminder.setColorFilter(
                requireContext().getAttribute(R.attr.colorOnSurface)
            )
        }
        binding.cbTaskCompleted.setOnCheckedChangeListener { _, isChecked ->
            binding.cbTaskCompleted.isChecked = isChecked
            task.isDone = isChecked
            subTaskViewModel.update(task)
        }
        binding.btnDeleteAll.setOnClickListener {
            subTaskViewModel.onDeleteAllCompletedClick()
        }
        binding.btnSettings.setOnClickListener {
            val action = SubTaskFragmentDirections.actionSubTaskFragmentToSettingsFragment()
            navController.navigate(action)
        }
        binding.btnShare.setOnClickListener {
            subTaskViewModel.shareTask(requireContext(), getAllText())
        }
        binding.txtTitle.setOnClickListener {
            val action = SubTaskFragmentDirections.actionSubTaskFragmentToRenameFragmentDialog(
                false,
                args.task.id,
                args.task.title,
                0
            )
            navController.navigate(action)
        }
        setSortMenu()
        setVisibilityOfTasks()
    }

    private fun setTransition() {
        binding.root.transitionName = args.task.title
    }

    private fun setSortMenu() {
        val sortTypes = listOf(
            getString(R.string.name),
            getString(R.string.name_desc),
            getString(R.string.date),
            getString(R.string.date_desc)
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
                        val sortType = sortTypes[pos]
                        subTaskViewModel.onSortOrderSelected(
                            SortOrder.getOrder(sortType),
                            requireContext()
                        )
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
                setChipColor(true, TaskApp.categoryTypes[task.color].color)
                subTaskViewModel.onHideCompleted(true, requireContext())
            } else {
                binding.chipActive.isChecked = true
            }
            isTaskActive = true
        }
        binding.chipDone.setOnClickListener {
            if (binding.chipDone.isChecked) {
                setChipColor(false, TaskApp.categoryTypes[task.color].color)
                setButtonVisibility(false)
                subTaskViewModel.onHideCompleted(false, requireContext())
            } else {
                binding.chipDone.isChecked = true
            }
            isTaskActive = false
        }
    }

    private fun setColors(color: Int) {
        binding.progressBar.progressTintList = ColorStateList.valueOf(color)
        binding.btnAddTask.backgroundTintList = ColorStateList.valueOf(color)
        binding.btnVoiceTask.backgroundTintList = ColorStateList.valueOf(color)
        binding.btnDeleteAll.backgroundTintList = ColorStateList.valueOf(color)
        binding.btnCreateNewTask.setDrawableColor(color)
        setChipColor(true, color)
    }

    private fun setChipColor(isEnable: Boolean, color: Int) {
        if (isEnable) {
            binding.chipActive.chipBackgroundColor = ColorStateList.valueOf(color)
            binding.chipActive.setTextColor(requireContext().getAttribute(R.attr.colorSurface))
            binding.chipDone.chipBackgroundColor =
                ColorStateList.valueOf(requireContext().getAttribute(R.attr.colorSurfaceVariant))
            binding.chipDone.setTextColor(requireContext().getAttribute(R.attr.colorOnSurface))
        } else {
            binding.chipDone.chipBackgroundColor = ColorStateList.valueOf(color)
            binding.chipDone.setTextColor(requireContext().getAttribute(R.attr.colorSurface))
            binding.chipActive.chipBackgroundColor =
                ColorStateList.valueOf(requireContext().getAttribute(R.attr.colorSurfaceVariant))
            binding.chipActive.setTextColor(requireContext().getAttribute(R.attr.colorOnSurface))
        }
    }

    private fun handleReminder() {
        viewLifecycleOwner.lifecycleScope.launch {
            val taskReminder = task.reminder
            binding.cbTaskCompleted.isChecked = task.isDone
            binding.btnCancelReminder.isVisible =
                if (taskReminder != null) {
                    val diff = DateUtil.getTimeDiff(taskReminder)
                    binding.txtReminder.text = DateUtil.getReminderDateTime(taskReminder)
                    binding.txtReminder.isSelected = true
                    binding.imgReminder.setColorFilter(requireContext().getAttribute(R.attr.colorOnSurface))
                    binding.reminderLayout.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.background_reminder
                    )
                    if (diff < 0) {
                        binding.txtReminder.setTextColor(requireContext().getAttribute(R.attr.colorError))
                    }
                    true
                } else {
                    binding.reminderLayout.background = null
                    false
                }
        }
    }

    private fun addSwipeGesturesHandler() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
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
                    subTaskViewModel.onSubTaskSwiped(subTask)
                } else {
                    val subTask = subTaskAdapter.currentList[viewHolder.adapterPosition]
                    subTask.isImportant = !subTask.isImportant
                    subTaskViewModel.updateSubTask(subTask)
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
                    .addSwipeLeftBackgroundColor(requireContext().getAttribute(R.attr.colorError))
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    .addSwipeRightBackgroundColor(requireContext().getAttribute(R.attr.colorPrimary))
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
        }).attachToRecyclerView(binding.subTaskRecyclerView)
    }

    private fun completedSubTaskObserver() {
        subTaskViewModel.subTasks2.asLiveData().observe(viewLifecycleOwner) { //this is for all task
            val count = it.count { subtask ->
                subtask.isDone
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
            subTaskViewModel.subTasksEvent.collect { event ->
                when (event) {
                    is SubTaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(
                            requireView(),
                            getString(R.string.task_deleted),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction(getString(R.string.undo)) {
                                subTaskViewModel.onUndoDeleteClick(event.subTask)
                            }.show()
                    }

                    SubTaskEvent.NavigateToAllCompletedScreen -> {
                        val action =
                            SubTaskFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment2(
                                task.id
                            )
                        navController.navigate(action)
                    }
                }
            }
        }
    }

    private fun addNewTask() {
        val action = SubTaskFragmentDirections.actionSubTaskFragmentToRenameFragmentDialog(
            true,
            task.id,
            null, -1
        )
        navController.navigate(action)
    }

    private fun getAllText(): String {
        var sendtxt: String = task.title + COLON
        for (i in 0 until subTaskAdapter.currentList.size) {
            sendtxt += "\n${i + 1}. " + subTaskAdapter.currentList[i].subTitle
        }
        return sendtxt
    }

    private fun getSubText(): String? {
        var sendtxt: String?
        sendtxt = FIRST
        if (subTaskAdapter.currentList.isNotEmpty()) {
            sendtxt += subTaskAdapter.currentList.first().subTitle//subTaskAdapter.currentList[0].subTitle
        }
        for (i in 1 until subTaskAdapter.currentList.size) {//subTaskAdapter.currentList.size) {
            sendtxt += "\n${i + 1}. " + subTaskAdapter.currentList[i].subTitle//subTaskAdapter.currentList[i].subTitle
        }
        if (sendtxt == FIRST) {
            sendtxt = null
        }
        return sendtxt
    }

    override fun onItemClicked(subTask: SubTask, position: Int) {
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
                String.format(getString(R.string.task_selected), counter, subTaskAdapter.itemCount)
        } else {
            val action = SubTaskFragmentDirections.actionSubTaskFragmentToRenameFragmentDialog(
                true,
                task.id,
                subTask.subTitle,
                subTask.sId
            )
            navController.navigate(action)
        }
    }

    override fun onDeleteClicked(subTask: SubTask) {
        if (subTask.isDone) {
            subTaskViewModel.showDeleteDialog(requireContext(), subTask)
        } else {
            val action = SubTaskFragmentDirections.actionSubTaskFragmentToRenameFragmentDialog(
                true,
                task.id,
                subTask.subTitle,
                subTask.sId
            )
            navController.navigate(action)
        }
    }

    override fun onCheckBoxClicked(subTask: SubTask, taskCompleted: Boolean) {
        subTaskViewModel.onSubTaskCheckedChanged(subTask, taskCompleted)
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

        override fun onActionItemClicked(
            mode: ActionMode?,
            item: MenuItem?
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
                        showDialog(counter == subTaskAdapter.currentList.size)
                    }
                    true
                }

                R.id.action_selectAll -> {
                    isSelectedAll =
                        if (!isSelectedAll) {
                            item.setIcon(R.drawable.ic_select_all_on)
                            for (i in 0 until subTaskAdapter.currentList.size)
                                selectedItem!![i]
                            counter = subTaskAdapter.currentList.size
                            true
                        } else {
                            item.setIcon(R.drawable.ic_select_all)
                            for (i in 0 until subTaskAdapter.currentList.size)
                                !selectedItem!![i]
                            counter = 0
                            false
                        }
                    actionMode!!.title =
                        String.format(
                            getString(R.string.task_selected),
                            counter,
                            subTaskAdapter.itemCount
                        )
                    subTaskAdapter.notifyItemRangeChanged(0, subTaskAdapter.itemCount)
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
            selectedItem!![position] =
                if (selectedItem!![position]) {
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
            String.format(getString(R.string.task_selected), counter, subTaskAdapter.itemCount)
    }

    override fun getColor(): Int {
        return TaskApp.categoryTypes[task.color].color
    }

    override fun getViewType(): Boolean {
        return viewType
    }

    override fun getCounter(): Int {
        return counter
    }

    override fun getActionModeStatus(): Boolean {
        return actionModeEnable
    }

    override var isAllSelected: Boolean
        get() = isSelectedAll
        set(value) {
            isSelectedAll = value
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

    private fun setViewVisibility(isVisible: Boolean) {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.btnVoiceTask.isVisible = !isVisible &&
                prefManager.getBoolean(AppConstants.SHOW_VOICE_TASK_KEY, true)
        binding.btnAddTask.isVisible = !isVisible
        binding.sortMenu.isVisible = !isVisible
        binding.chipGroup.isVisible = !isVisible
        binding.reminderLayout.isVisible = !isVisible
        binding.searchView.isVisible = !isVisible
        binding.txtSort.isVisible = !isVisible
        binding.btnShare.isVisible = !isVisible
    }

    private fun onActionMode(isActionModeOn: Boolean) {
        setViewVisibility(isActionModeOn)
        actionModeEnable = isActionModeOn

        if (isActionModeOn) {
            val transition = TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.transition2)
            TransitionManager.beginDelayedTransition(binding.root, transition)
            selectedItem = Array(subTaskAdapter.currentList.size) { false }
        } else {
            isSelectedAll = false
            subTaskAdapter.notifyItemRangeChanged(0, subTaskAdapter.itemCount)
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
                        subTaskViewModel.deleteAllSubTasks(task.id)
                    }
                } else {
                    for (i in selectedItem!!.indices) {
                        if (selectedItem!![i]) {
                            subTaskViewModel.deleteSubTask(subTaskAdapter.currentList[i])
                        }
                    }
                }
                counter = 0
                actionMode?.finish()
                onActionMode(false)
                context.toast { getString(R.string.notify_delete) }
                dialog.dismiss()
            }.show()
    }

    override fun cancelReminderClicked(subTask: SubTask, timerTxt: TextView) {
        subTaskViewModel.cancelSubTaskReminder(requireActivity(), subTask, timerTxt, task)
    }

    override fun onPause() {
        super.onPause()
        task.progress =
            if (binding.progressBar.visibility == View.GONE) {
                -1f
            } else {
                binding.progressBar.progress.toFloat()
            }
        task.subTaskList = getSubText()
        subTaskViewModel.update(task)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (binding.progressBar.progress == TOTAL_PROGRESS_VALUE) {
            binding.cbTaskCompleted.isChecked = true
            task.isDone = true
            subTaskViewModel.update(task)
        }
        searchView?.setOnQueryTextListener(null)
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putBoolean(SUBTASK_STATUS_BUNDLE_KEY, isTaskActive)
            putBoolean(VIEW_TYPE_BUNDLE_KEY, viewType)
            putInt(COUNTER_BUNDLE_KEY, counter)
            putBoolean(IS_IN_ACTION_MODE_BUNDLE_KEY, actionModeEnable)
            putBoolean(IS_SELECT_ALL_BUNDLE_KEY, isSelectedAll)
            putBooleanArray(SELECTED_ITEMS_BUNDLE_KEY, selectedItem?.toBooleanArray())
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            viewType = it.getBoolean(VIEW_TYPE_BUNDLE_KEY)
            counter = it.getInt(COUNTER_BUNDLE_KEY, counter)
            actionModeEnable = it.getBoolean(IS_IN_ACTION_MODE_BUNDLE_KEY)
            isSelectedAll = it.getBoolean(IS_SELECT_ALL_BUNDLE_KEY)
            selectedItem =
                it.getBooleanArray(SELECTED_ITEMS_BUNDLE_KEY)?.toTypedArray()
            isTaskActive = it.getBoolean(SUBTASK_STATUS_BUNDLE_KEY)
            setButtonVisibility(isTaskActive)
            setChipColor(isTaskActive, TaskApp.categoryTypes[task.color].color)
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
        private const val FIRST = "1. "
        private const val COLON = " :"
        private const val VIEW_TYPE_BUNDLE_KEY = "subtask_view_type_bundle_key"
        private const val COUNTER_BUNDLE_KEY = "subtask_counter_bundle_key"
        private const val IS_IN_ACTION_MODE_BUNDLE_KEY = "subtask_is_in_action_mode_bundle_key"
        private const val IS_SELECT_ALL_BUNDLE_KEY = "subtask_is_select_all_bundle_key"
        private const val SELECTED_ITEMS_BUNDLE_KEY = "subtask_selected_items_bundle_key"
        private const val SUBTASK_STATUS_BUNDLE_KEY = "subtasks_status_bundle_key"
    }
}