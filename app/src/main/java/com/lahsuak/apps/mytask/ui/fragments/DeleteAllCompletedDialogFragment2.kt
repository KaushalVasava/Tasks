package com.lahsuak.apps.mytask.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.lahsuak.apps.mytask.R
import com.lahsuak.apps.mytask.ui.viewmodel.DeleteAllCompletedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllCompletedDialogFragment2 : DialogFragment() {

    private val viewModel: DeleteAllCompletedViewModel by viewModels()
    private val args: DeleteAllCompletedDialogFragment2Args by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_deletion))
            .setMessage(getString(R.string.delete_completed_task))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.onConfirmClick2(args.taskID)
            }
            .create()
}