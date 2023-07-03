package com.lahsuak.apps.tasks.ui.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lahsuak.apps.tasks.R
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.databinding.DialogAddUpdateTaskBinding
import com.lahsuak.apps.tasks.ui.viewmodel.TaskViewModel
import com.lahsuak.apps.tasks.util.AppConstants
import com.lahsuak.apps.tasks.util.AppUtil
import com.lahsuak.apps.tasks.util.AppUtil.setDateTime
import com.lahsuak.apps.tasks.util.toTrimString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShortcutFragmentDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAddUpdateTaskBinding? = null
    private val binding: DialogAddUpdateTaskBinding
        get() = _binding!!
    private val args: ShortcutFragmentDialogArgs by navArgs()
    private val model: TaskViewModel by viewModels()
    private lateinit var task: Task

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogAddUpdateTaskBinding.inflate(layoutInflater)

        viewLifecycleOwner.lifecycleScope.launch {
            if (args.taskId != -1)
                task = model.getById(args.taskId)
        }
        @Suppress(AppConstants.DEPRECATION)
        if (dialog?.window != null) {
            dialog?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        binding.txtRename.requestFocus()
        binding.cbImpTask.setOnCheckedChangeListener { _, isChecked ->
            binding.cbImpTask.isChecked = isChecked
        }
        binding.txtReminder.setOnClickListener {
            if (binding.txtRename.text.isNullOrEmpty()) {
                return@setOnClickListener
            }
            if (binding.txtReminder.text == getString(R.string.add_date_time)) {
                task = Task(
                    0,
                    binding.txtRename.toTrimString(),
                    false,
                    binding.cbImpTask.isChecked,
                    null,
                    -1f,
                    null,
                    startDate = System.currentTimeMillis()
                )
            }
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
        }
        binding.btnSave.setOnClickListener {
            if (args.taskId == -1) {
                if (!binding.txtRename.text.isNullOrEmpty()) {
                    if (binding.txtReminder.text == getString(R.string.add_date_time)) {
                        task = Task(
                            0,
                            binding.txtRename.toTrimString(),
                            false,
                            binding.cbImpTask.isChecked,
                            null,
                            -1f,
                            null,
                            startDate = System.currentTimeMillis()
                        )
                    }
                    model.insert(task)
                }
            } else {
                if (!binding.txtRename.text.isNullOrEmpty()) {
                    task.title = binding.txtRename.toTrimString()
                    task.isImp = binding.cbImpTask.isChecked
                    model.update(task)
                }
            }
            dismiss()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}