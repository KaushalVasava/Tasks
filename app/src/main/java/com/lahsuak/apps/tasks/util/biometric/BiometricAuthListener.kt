package com.lahsuak.apps.tasks.util.biometric

interface BiometricAuthListener {
    fun onBiometricAuthSuccess()
    fun onUserCancelled()
    fun onErrorOccurred(errorCode: Int, errorMessage: String)
}