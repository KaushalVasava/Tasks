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
import androidx.lifecycle.lifecycleScope
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.util.Util
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShortcutFragmentDialog : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentDialogRenameBinding
    private val args: ShortcutFragmentDialogArgs by navArgs()
    private val model: TaskViewModel by viewModels()
    private lateinit var task: Task

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentDialogRenameBinding.inflate(layoutInflater)
        @Suppress("deprecation")
        if (dialog!!.window != null) {
            dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            if (args.taskId != -1)
                task = model.getById(args.taskId)
        }

        var isImp = false
        binding.renameText.requestFocus()
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
                ,
                    System.currentTimeMillis()
                )
            }
            task = Util.showReminder(requireActivity(), binding.timerTxt, task)
        }
        binding.saveBtn.setOnClickListener {
            if (args.taskId == -1) {
                if (!binding.renameText.text.isNullOrEmpty()) {
                    if (binding.timerTxt.text == getString(R.string.add_date_time)) {
                        task = Task(
                            0,
                            binding.renameText.text.toString(),
                            false,
                            binding.impTask.isChecked,
                            null,
                            -1f,
                            null,
                            System.currentTimeMillis()
                        )
                    }
                    model.insert(task)
                }
            } else {
                if (!binding.renameText.text.isNullOrEmpty()) {
                    task.title = binding.renameText.text.toString()
                    if (isImp)
                        task.isImp = isImp
                    model.update(task)
                }
            }
            dismiss()
        }
        return binding.root
    }
}