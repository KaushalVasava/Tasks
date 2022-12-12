package com.lahsuak.apps.mytask.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lahsuak.apps.mytask.data.model.Task
import com.lahsuak.apps.mytask.ui.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.databinding.DialogAddUpdateTaskBinding
import com.lahsuak.apps.mytask.util.Util
import com.lahsuak.apps.mytask.util.toTrimString
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShortcutFragmentDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogAddUpdateTaskBinding
    private val args: ShortcutFragmentDialogArgs by navArgs()
    private val model: TaskViewModel by viewModels()
    private lateinit var task: Task

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = DialogAddUpdateTaskBinding.inflate(layoutInflater)
        @Suppress("deprecation")
        if (dialog!!.window != null) {
            dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            if (args.taskId != -1)
                task = model.getById(args.taskId)
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
                    date = System.currentTimeMillis()
                )
            }
            task = Util.showReminder(requireActivity(), binding.txtReminder, task)
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
                            date = System.currentTimeMillis()
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
}