package com.lahsuak.apps.tasks.util

import android.app.Activity
import android.content.pm.PackageManager
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.lahsuak.apps.tasks.R

object PermissionUtil {
    inline fun checkAndLaunchPermission(
        fragment: Fragment?,
        permissions: Array<String>,
        permissionLauncher: ActivityResultLauncher<String>, // changed from array of string to string
        showRationaleUi: (permission: String) -> Unit,
        lazyBlock: () -> Unit
    ) {
        fragment ?: return
        when {
            permissions.all {
                ContextCompat.checkSelfPermission(
                    fragment.requireContext(),
                    it
                ) == PackageManager.PERMISSION_GRANTED
            } -> {
                lazyBlock()
            }
            permissions.any {
                fragment.shouldShowRequestPermissionRationale(it)
            } -> {
                val permission = permissions.first {
                    fragment.shouldShowRequestPermissionRationale(it)
                }
                showRationaleUi(permission)
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                permissionLauncher.launch(
                    permissions.toString()
                )
            }
        }
    }

    fun showSettingsSnackBar(
        activity: Activity?,
        view: View,
    ) {
        activity ?: return
        val snackBar = Snackbar.make(
            view.context,
            view,
            view.context.getString(R.string.permission_denied),
            Snackbar.LENGTH_LONG
        ).setAction(R.string.open_settings) {
            AppUtil.openSettingsPage(activity)
        }
        snackBar.show()
    }

}