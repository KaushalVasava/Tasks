package com.lahsuak.apps.mytask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.databinding.FragmentDialogRenameBinding
import com.lahsuak.apps.mytask.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.data.model.SubTask
import com.lahsuak.apps.mytask.data.util.Constants.REM_KEY
import com.lahsuak.apps.mytask.data.util.Util.setClipboard
import com.lahsuak.apps.mytask.data.util.Util.showReminder
import com.lahsuak.apps.mytask.ui.viewmodel.SubTaskViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RenameFragmentDialog : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentDialogRenameBinding
    private val args: RenameFragmentDialogArgs by navArgs()
    private val model: TaskViewModel by viewModels()
    private val subModel: SubTaskViewModel by viewModels()
    private lateinit var task: Task
    private lateinit var subTask: SubTask

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        var isImp = false
        binding = FragmentDialogRenameBinding.inflate(layoutInflater)
        @Suppress("deprecation")
        if (dialog!!.window != null) {
            dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        if (args.source) {
            binding.timerTxt.visibility = View.GONE
        }

        binding.btnCopy.setOnClickListener {
            if (binding.renameText.text.toString().isEmpty().not()) {
                setClipboard(requireContext(), binding.renameText.text.toString())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            if (!args.source) {
                if (args.taskId != -1) {
                    task = model.getById(args.taskId)
                    binding.impTask.isChecked = task.isImp
                    binding.renameText.setText(task.title)
                    isImp = task.isImp
                    if (task.reminder != null) {
                        binding.timerTxt.text = task.reminder
                        binding.timerTxt.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.background_timer)
                    }
                }
            } else {
                if (args.subTaskId != -1) {
                    subTask = subModel.getBySubTaskId(args.subTaskId)
                    binding.impTask.isChecked = subTask.isImportant
                    binding.renameText.setText(subTask.subTitle)
                    isImp = subTask.isImportant
                }
            }
        }

        if (!args.takTitle.isNullOrEmpty()) {
            binding.renameText.setText(args.takTitle)
        }
        binding.renameText.requestFocus()
        binding.renameText.post {
            binding.renameText.setSelection(binding.renameText.text.length)
        }
        binding.impTask.setOnCheckedChangeListener { _, isChecked ->
            binding.impTask.isChecked = isChecked
            isImp = isChecked
        }
        binding.timerTxt.setOnClickListener {
            if (binding.renameText.text.isNullOrEmpty()) {
                return@setOnClickListener
            }
            if (binding.timerTxt.text == getString(R.string.add_date_time)) {
                task = Task(
                    0,
                    binding.renameText.text.toString(),
                    false,
                    binding.impTask.isChecked,
                    null,
                    -1f,
                    null
                )
            }

            task = showReminder(requireActivity(), binding.timerTxt, task)
        }

        binding.saveBtn.setOnClickListener {
            if (args.taskId == -1 && !args.source) { //new task
                if (!binding.renameText.text.isNullOrEmpty()) {
                    if (binding.timerTxt.text == getString(R.string.add_date_time)) {
                        task = Task(
                            0,
                            binding.renameText.text.toString(),
                            false,
                            binding.impTask.isChecked,
                            null,
                            -1f,
                            null
                        )
                    }//task = Task(0, binding.renameText.text.toString(), false, isImp, , -1f)
                    model.insert(task)
                }
            } else if (args.taskId != -1 && args.subTaskId == -1 && args.source) { //new subtask
                subTask = SubTask(
                    args.taskId,
                    binding.renameText.text.toString(),
                    false,
                    isImp,
                    0
                )
                subModel.insertSubTask(subTask)
            } else {
                if (!binding.renameText.text.isNullOrEmpty()) {
                    if (!args.source) {//update task
                        task.title = binding.renameText.text.toString()
                        task.isImp = isImp
                        if (!binding.renameText.text.isNullOrEmpty()) {
                            if (binding.timerTxt.text != getString(R.string.add_date_time)) {
                                task.reminder = binding.timerTxt.text.toString()
                                val navController = findNavController()
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    REM_KEY,
                                    task.reminder
                                )
                                dismiss()
                            }
                        }
                        model.update(task)
                    } else {//update subtask
                        subTask.subTitle = binding.renameText.text.toString()
                        subTask.isImportant = isImp
                        subModel.updateSubTask(subTask)
                    }
                }
            }
            dismiss()
        }

        return binding.root
    }
}