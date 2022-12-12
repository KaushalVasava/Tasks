package com.lahsuak.apps.mytask.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.databinding.DialogAddUpdateTaskBinding
import com.lahsuak.apps.mytask.util.Constants.REM_KEY
import com.lahsuak.apps.mytask.util.Util.setClipboard
import com.lahsuak.apps.mytask.util.Util.showReminder
import com.lahsuak.apps.mytask.ui.viewmodel.SubTaskViewModel
import com.lahsuak.apps.mytask.util.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddUpdateTaskFragmentDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogAddUpdateTaskBinding
    private val args: AddUpdateTaskFragmentDialogArgs by navArgs()
    private val model: TaskViewModel by viewModels()
    private val subModel: SubTaskViewModel by viewModels()
    private var task: Task = Task(id = -1, title = "")
    private var subTask: SubTask = SubTask(id = -1, sId = -1, subTitle = "")

    private var taskId = -1
    private var taskTitle: String? = null
    private var source = false
    private var subtaskId = -1

    companion object {
        const val SOURCE_ARG = "source"
        const val TASK_ID_ARG = "task_id"
        const val TASK_TITLE_ARG = "task_title"
        const val SUBTASK_ID_ARG = "subTaskId"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogAddUpdateTaskBinding.inflate(layoutInflater)
        @Suppress("DEPRECATION")
        requireDialog().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val args = arguments
        if (args != null) {
            taskId = args.getInt(TASK_ID_ARG, -1)
            taskTitle = args.getString(TASK_TITLE_ARG, null)
            subtaskId = args.getInt(SUBTASK_ID_ARG, -1)
            source = args.getBoolean(SOURCE_ARG)
        }
        if (source) {
            binding.txtReminder.visibility = View.GONE
        }
        (dialog as? BottomSheetDialog)?.run {
            behavior.applyCommonBottomSheetBehaviour()
            behavior.isDraggable = false // for make it's scrollable
        }
        if (!taskTitle.isNullOrEmpty()) {
            binding.txtRename.setText(taskTitle)
        }
        binding.txtRename.requestFocus()
        binding.txtRename.post {
            binding.txtRename.setSelection(binding.txtRename.text.length)
        }
        setPrefetchData()
        addClickListeners()
        return binding.root
    }

    private fun setPrefetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (!source) {
                if (args.taskId != -1) {
                    task = model.getById(args.taskId)
                    binding.cbImpTask.isChecked = task.isImp
                    binding.txtRename.setText(task.title)
                    val taskReminder = task.reminder
                    binding.txtReminder.isSelected = taskReminder != null
                    if (taskReminder != null) {
                        binding.txtReminder.text = DateUtil.getReminderDateTime(taskReminder)
                        val diff = DateUtil.getTimeDiff(taskReminder)
                        binding.txtReminder.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.background_reminder
                        )
                        binding.txtReminder.setTextDrawableColor(requireContext(), R.color.black)
                        if (diff < 0) {
                            binding.txtReminder.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            )
                        }
                    } else {
                        binding.txtReminder.background = null
                    }
                }
            } else {
                if (args.subTaskId != -1) {
                    subTask = subModel.getBySubTaskId(args.subTaskId)
                    binding.cbImpTask.isChecked = subTask.isImportant
                    binding.txtRename.setText(subTask.subTitle)
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
                setClipboard(
                    requireContext(),
                    binding.txtRename.toTrimString()
                )
            }
        }
        binding.btnPaste.setOnClickListener {
            val text = Util.pasteText(requireContext())
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
                task = Task(
                    id = 0,
                    title = binding.txtRename.toTrimString(),
                    isImp = binding.cbImpTask.isChecked
                )
            }
            task = showReminder(requireActivity(), binding.txtReminder, task)
        }
    }

    private fun saveData() {
        if (args.taskId == -1 && !source) {
            //new task
            if (!binding.txtRename.text.isNullOrEmpty()) {
                if (binding.txtReminder.text == getString(R.string.add_date_time)) {
                    task = Task(
                        id = 0,
                        title = binding.txtRename.toTrimString(),
                        isImp = binding.cbImpTask.isChecked,
                        date = System.currentTimeMillis()
                    )
                }
                model.insert(task)
            }
        } else if (args.taskId != -1 && args.subTaskId == -1 && source) {
            //new subtask
            subTask = SubTask(
                id = args.taskId,
                subTitle = binding.txtRename.toTrimString(),
                isDone = false,
                isImportant = binding.cbImpTask.isChecked,
                sId = 0,
                dateTime = System.currentTimeMillis()
            )
            subModel.insertSubTask(subTask)
        } else {
            if (!binding.txtRename.text.isNullOrEmpty()) {
                if (!source) {
                    //update task
                    task.apply {
                        title = binding.txtRename.toTrimString()
                        isImp = binding.cbImpTask.isChecked
                        date = System.currentTimeMillis()
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
                    model.update(task)
                } else {
                    //update subtask
                    subTask.apply {
                        subTitle = binding.txtRename.toTrimString()
                        isImportant = binding.cbImpTask.isChecked
                        dateTime = System.currentTimeMillis()
                        subModel.updateSubTask(this)
                    }
                }
            }
        }
        dismiss()
    }
}