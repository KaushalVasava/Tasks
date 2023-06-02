package com.lahsuak.apps.mytask.util

import android.app.Activity
import android.content.pm.PackageManager
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.lahsuak.apps.mytask.R

object PermissionUtil {
    inline fun checkAndLaunchPermission(
        fragment: Fragment?,
        permissions: Array<String>,
        permissionLauncher: ActivityResultLauncher<Array<String>>,
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
                    permissions
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
            Util.openSettingsPage(activity)
        }
        snackBar.show()
    }

}