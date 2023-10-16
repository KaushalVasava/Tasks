package com.lahsuak.apps.tasks.util.biometric

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.lahsuak.apps.tasks.R

/**
 * Helper class for managing Biometric Authentication Process
 */
object BiometricUtil {

    /**
     * Checks if the device has Biometric support
     */
    private fun hasBiometricCapability(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate()
    }

    /**
     * Checks if Biometric Authentication (example: Fingerprint) is set in the device
     */
    fun isBiometricReady(context: Context) =
        hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS

    /**
     * Prepares PromptInfo dialog with provided configuration
     */
    private fun setBiometricPromptInfo(
        context: Context,
        allowDeviceCredential: Boolean,
    ): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.prompt_info_title))
            .setSubtitle(context.getString(R.string.prompt_info_subtitle))
        // Use Device Credentials if allowed, otherwise show Cancel Button
        builder.apply {
            if (allowDeviceCredential) setDeviceCredentialAllowed(true)
            else setNegativeButtonText("Cancel")
        }

        return builder.build()
    }

    /**
     * Initializes BiometricPrompt with the caller and callback handlers
     */
    private fun initBiometricPrompt(
        activity: Activity,
        listener: BiometricAuthListener,
    ): BiometricPrompt {
        // Attach calling Activity
        val executor = ContextCompat.getMainExecutor(activity)

        // Attach callback handlers
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.onErrorOccurred()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                listener.onUserCancelled()
                Log.w(this.javaClass.simpleName, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener.onBiometricAuthSuccess()
            }
        }

        return BiometricPrompt(activity as FragmentActivity, executor, callback)
    }

    /**
     * Displays a BiometricPrompt with provided configurations
     */
    fun showBiometricPrompt(
        activity: Activity,
        listener: BiometricAuthListener,
        cryptoObject: BiometricPrompt.CryptoObject? = null,
        allowDeviceCredential: Boolean = false,
    ) {
        // Prepare BiometricPrompt Dialog
        val promptInfo = setBiometricPromptInfo(
            activity.baseContext,
            allowDeviceCredential
        )

        // Attach with caller and callback handler
        val biometricPrompt = initBiometricPrompt(activity, listener)

        // Authenticate with a CryptoObject if provided, otherwise default authentication
        biometricPrompt.apply {
            if (cryptoObject == null) authenticate(promptInfo)
            else authenticate(promptInfo, cryptoObject)
        }
    }

    /**
     * Navigates to Device's Settings screen Biometric Setup
     */
    fun lunchBiometricSettings(context: Context) {
        ActivityCompat.startActivity(
            context,
            Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS),
            null
        )
    }

}