package com.lahsuak.apps.tasks.ui.fragments.dialog

import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.*
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.databinding.DialogAddUpdateTaskBinding
import com.lahsuak.apps.tasks.ui.adapters.CategoryAdapter
import com.lahsuak.apps.tasks.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.*
import com.lahsuak.apps.tasks.util.AppConstants.INVALID_ID
import com.lahsuak.apps.tasks.util.AppConstants.SharedPreference.REM_KEY
import com.lahsuak.apps.tasks.util.AppUtil.setClipboard
import com.lahsuak.apps.tasks.util.AppUtil.setDateTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddUpdateTaskFragmentDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAddUpdateTaskBinding? = null
    private val binding: DialogAddUpdateTaskBinding
        get() = _binding!!

    private val args: AddUpdateTaskFragmentDialogArgs by navArgs()
    private val taskViewModel: TaskViewModel by viewModels()
    private val subTaskViewModel: SubTaskViewModel by viewModels()
    private lateinit var task: Task
    private lateinit var subTask: SubTask
    private var selectedCategoryPosition = 0

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogAddUpdateTaskBinding.inflate(layoutInflater)
        setPrefetchData()
        @Suppress(AppConstants.DEPRECATION)
        requireDialog().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        (dialog as? BottomSheetDialog)?.run {
            behavior.applyCommonBottomSheetBehaviour()
            behavior.isDraggable = false // for make it's scrollable
        }
        if (!args.taskTitle.isNullOrEmpty()) {
            binding.txtRename.setText(args.taskTitle)
        }
        binding.txtRename.requestFocus()
        binding.txtRename.post {
            binding.txtRename.setSelection(binding.txtRename.text?.length ?: 0)
        }
        setVisibility()
        addClickListeners()
        setCategoryMenu()
        return binding.root
    }

    private fun setVisibility() {
        if (args.navigateFromSubtask) {
            binding.categoryMenu.visibility = View.GONE
            binding.endLayout.visibility = View.GONE
            binding.startLayout.visibility = View.GONE
        } else {
            binding.txtRename.filters = arrayOf<InputFilter>(LengthFilter(40))
        }
    }

    private fun setCategoryMenu() {
        val adapter = CategoryAdapter(requireContext(), TaskApp.categoryTypes)
        binding.categoryMenu.adapter = adapter
        binding.categoryMenu.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, pos: Int, id: Long,
                ) {
                    if (selectedCategoryPosition != pos) {
                        selectedCategoryPosition = pos
                        binding.categoryMenu.setSelection(pos)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    /* no-op */
                }
            }
    }

    private fun setPrefetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (!args.navigateFromSubtask) {
                if (args.taskId != INVALID_ID) {
                    taskViewModel.getById(args.taskId)
                    //todo Cause NULL POINTER EXCEPTION
                    task = taskViewModel.taskFlow.value!!
                    task.startDate?.let {
                        binding.etStartDate.setText(DateUtil.getDate(it))
                    }
                    task.endDate?.let {
                        binding.etEndDate.setText(DateUtil.getDate(it))
                    }
                    selectedCategoryPosition = task.color
                    binding.categoryMenu.setSelection(selectedCategoryPosition)
                    binding.cbImpTask.isChecked = task.isImp
                    binding.txtRename.setText(task.title)
                    val taskReminder = task.reminder
                    binding.txtReminder.isSelected = taskReminder != null
                    if (taskReminder != null) {
                        binding.txtReminder.text = DateUtil.getDate(taskReminder)
                        val diff = DateUtil.getTimeDiff(taskReminder)
                        binding.txtReminder.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.background_round
                        )
                        val attrId = if (diff < 0) {
                            com.google.android.material.R.attr.colorError
                        } else {
                            com.google.android.material.R.attr.colorOnSurface
                        }
                        binding.txtReminder.setTextColor(requireContext().getAttribute(attrId))
                    } else {
                        binding.txtReminder.background = null
                    }
                } else {
                    val startDate = System.currentTimeMillis()
                    binding.etStartDate.setText(DateUtil.getDate(startDate))
                    task = Task(id = 0, "", startDate = startDate)
                }
            } else {
                taskViewModel.getById(args.taskId)
                //todo Cause NULL POINTER EXCEPTION
                task = taskViewModel.taskFlow.value!!

                if (args.subTaskId != INVALID_ID) {
                    subTaskViewModel.getBySubTaskId(args.subTaskId)
                    subTask = subTaskViewModel.subTaskFlow.value!!
                    binding.cbImpTask.isChecked = subTask.isImportant
                    binding.txtRename.setText(subTask.subTitle)
                    binding.txtRename.requestFocus()
                    binding.txtRename.post {
                        binding.txtRename.setSelection(binding.txtRename.text?.length ?: 0)
                    }
                    val taskReminder = subTask.reminder
                    binding.txtReminder.isSelected = taskReminder != null
                    if (taskReminder != null) {
                        binding.txtReminder.text = DateUtil.getDate(taskReminder)
                        val diff = DateUtil.getTimeDiff(taskReminder)
                        binding.txtReminder.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.background_round
                        )
                        if (diff < 0) {
                            binding.txtReminder.setTextColor(
                                requireContext().getAttribute(com.google.android.material.R.attr.colorError)
                            )
                        } else {
                            binding.txtReminder.setTextColor(
                                requireContext().getAttribute(com.google.android.material.R.attr.colorOnSurface)
                            )
                        }
                    } else {
                        binding.txtReminder.background = null
                    }
                } else {
                    subTask = SubTask(id = args.taskId, subTitle = "", sId = 0)
                }
            }
        }
    }

    private fun addClickListeners() {
        binding.cbImpTask.setOnCheckedChangeListener { _, isChecked ->
            binding.cbImpTask.isChecked = isChecked
        }
        binding.btnCopy.setOnClickListener {
            if (binding.txtRename.toTrimString().isNotEmpty()) {
                setClipboard(requireContext(), binding.txtRename.toTrimString())
            }
        }
        binding.titleLayout.setEndIconOnClickListener {
            val text = AppUtil.pasteText(requireContext())
            binding.txtRename.setText(text)
        }
        binding.btnSave.setOnClickListener {
            saveData()
        }
        binding.txtReminder.setOnClickListener {
            if (binding.txtRename.text.isNullOrEmpty()) {
                return@setOnClickListener
            }
            if (binding.txtReminder.text == getString(R.string.add_date_time)) {
                if (!args.navigateFromSubtask) {
                    task = task.copy(
                        title = binding.txtRename.toTrimString(),
                        isImp = binding.cbImpTask.isChecked
                    )
                    setDateTime(requireActivity()) { calendar, time ->
                        binding.txtReminder.text = time
                        AppUtil.setReminderWorkRequest(
                            requireContext(),
                            task.title,
                            task,
                            calendar
                        )
                        task.reminder = calendar.timeInMillis
                    }
                } else {
                    setDateTime(requireActivity()) { calendar, time ->
                        binding.txtReminder.text = time
                        AppUtil.setReminderWorkRequest(
                            requireContext(),
                            task.title,
                            subTask,
                            calendar
                        )
                        subTask.reminder = calendar.timeInMillis
                    }
                }
            }
        }

        binding.etStartDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setDateTime(requireActivity()) { calendar, time ->
                    binding.etStartDate.setText(time)
                    task = Task(
                        id = 0,
                        title = binding.txtRename.text.toString(),
                        startDate = calendar.timeInMillis
                    )
                }
            }
        }
        binding.etEndDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setDateTime(requireActivity()) { calendar, time ->
                    binding.etEndDate.setText(time)
                    if (binding.etStartDate.text.isNullOrEmpty().not()) {
                        task = task.copy(endDate = calendar.timeInMillis)
                        taskViewModel.update(task)
                    }
                }
            }
        }
    }

    private fun saveData() {
        if (args.taskId == INVALID_ID && !args.navigateFromSubtask) {
            //new task
            if (binding.txtRename.text?.trim().isNullOrEmpty().not()) {
                if (binding.txtReminder.text == getString(R.string.add_date_time)) {
                    task = Task(
                        id = 0,
                        title = binding.txtRename.toTrimString(),
                        isImp = binding.cbImpTask.isChecked,
                        startDate = task.startDate,
                        endDate = task.endDate,
                        color = TaskApp.categoryTypes[selectedCategoryPosition].order
                    )
                } else {
                    task = Task(
                        id = 0,
                        title = binding.txtRename.toTrimString(),
                        isImp = binding.cbImpTask.isChecked,
                        startDate = task.startDate,
                        endDate = task.endDate,
                        reminder = task.reminder,
                        color = TaskApp.categoryTypes[selectedCategoryPosition].order
                    )
                }
                taskViewModel.insert(task)
            } else {
                context.toast {
                    getString(R.string.empty_task)
                }
            }
        } else if (args.taskId != INVALID_ID && args.subTaskId == INVALID_ID && args.navigateFromSubtask) {
            //new subtask
            if (binding.txtRename.text?.trim().isNullOrEmpty().not()) {
                if (binding.txtReminder.text == getString(R.string.add_date_time)) {
                    subTask = SubTask(
                        id = args.taskId,
                        subTitle = binding.txtRename.toTrimString(),
                        isDone = false,
                        isImportant = binding.cbImpTask.isChecked,
                        sId = 0,
                        dateTime = System.currentTimeMillis()
                    )
                } else {
                    subTask = SubTask(
                        id = args.taskId,
                        subTitle = binding.txtRename.toTrimString(),
                        isDone = false,
                        isImportant = binding.cbImpTask.isChecked,
                        sId = 0,
                        dateTime = System.currentTimeMillis(),
                        reminder = subTask.reminder
                    )
                }
                subTaskViewModel.insertSubTask(subTask)
                subTaskViewModel.update(task.copy(startDate = System.currentTimeMillis()))
            } else {
                context.toast {
                    getString(R.string.empty_task)
                }
            }
        } else {
            if (binding.txtRename.text?.trim().isNullOrEmpty().not()) {
                if (!args.navigateFromSubtask) {
                    //update task
                    task.apply {
                        title = binding.txtRename.toTrimString()
                        isImp = binding.cbImpTask.isChecked
                        startDate = task.startDate ?: System.currentTimeMillis()
                        endDate = task.endDate
                    }
                    if (!binding.txtRename.text.isNullOrEmpty()) {
                        if (binding.txtReminder.text != getString(R.string.add_date_time)) {
                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                REM_KEY,
                                task.reminder
                            )
                            dismiss()
                        }
                    }
                    taskViewModel.update(task.copy(color = TaskApp.categoryTypes[selectedCategoryPosition].order))
                } else {
                    //update subtask
                    subTask.apply {
                        subTitle = binding.txtRename.toTrimString()
                        isImportant = binding.cbImpTask.isChecked
                        dateTime = System.currentTimeMillis()
                    }
                    subTaskViewModel.updateSubTask(subTask)
                }
            }
        }
        dismiss()
    }
}