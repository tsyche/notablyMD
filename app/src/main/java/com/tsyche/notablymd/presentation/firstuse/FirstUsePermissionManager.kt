package com.tsyche.notablymd.presentation.firstuse

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.tsyche.notablymd.presentation.viewmodel.preference.NotablyMDPreferences

/** Manages first-use permission prompts and checks */
class FirstUsePermissionManager(private val context: Context) {

    companion object {
        private const val PREF_NAME = "first_use_permissions"
        private const val KEY_PERMISSIONS_REQUESTED = "permissions_requested"
        private const val KEY_MICROPHONE_GRANTED = "microphone_granted"
        private const val KEY_NOTIFICATIONS_GRANTED = "notifications_granted"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val appPreferences = NotablyMDPreferences.getInstance(context)

    /** Check if this is the first launch of the app */
    fun isFirstLaunch(): Boolean {
        return !preferences.getBoolean(KEY_FIRST_LAUNCH, false)
    }

    /** Mark first launch as completed */
    fun markFirstLaunchCompleted() {
        preferences.edit().putBoolean(KEY_FIRST_LAUNCH, true).apply()
    }

    /** Check if permissions have been requested before */
    fun hasPermissionsBeenRequested(): Boolean {
        return preferences.getBoolean(KEY_PERMISSIONS_REQUESTED, false)
    }

    /** Mark that permissions have been requested */
    fun markPermissionsRequested() {
        preferences.edit().putBoolean(KEY_PERMISSIONS_REQUESTED, true).apply()
    }

    /** Check if microphone permission is granted */
    fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
    }

    /** Check if notification permission is granted (Android 13+) */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true // Notifications don't require permission on older versions
        }
    }

    /** Check if all required permissions are granted */
    fun hasAllRequiredPermissions(): Boolean {
        return hasMicrophonePermission() && hasNotificationPermission()
    }

    /** Get list of missing permissions */
    fun getMissingPermissions(): List<String> {
        val missing = mutableListOf<String>()

        if (!hasMicrophonePermission()) {
            missing.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
            missing.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return missing
    }

    /** Check if permission prompts should be shown */
    fun shouldShowPermissionPrompts(): Boolean {
        return isFirstLaunch() || !hasAllRequiredPermissions()
    }

    /** Save permission grant status */
    fun savePermissionStatus(microphoneGranted: Boolean, notificationsGranted: Boolean) {
        preferences
            .edit()
            .putBoolean(KEY_MICROPHONE_GRANTED, microphoneGranted)
            .putBoolean(KEY_NOTIFICATIONS_GRANTED, notificationsGranted)
            .apply()
    }

    /** Get permission status for UI display */
    fun getPermissionStatus(): PermissionStatus {
        return PermissionStatus(
            microphoneGranted = hasMicrophonePermission(),
            notificationsGranted = hasNotificationPermission(),
            isFirstLaunch = isFirstLaunch(),
            hasBeenRequested = hasPermissionsBeenRequested(),
        )
    }
}

/** Data class representing permission status */
data class PermissionStatus(
    val microphoneGranted: Boolean,
    val notificationsGranted: Boolean,
    val isFirstLaunch: Boolean,
    val hasBeenRequested: Boolean,
) {
    val allGranted: Boolean
        get() = microphoneGranted && notificationsGranted

    val needsPermissionRequest: Boolean
        get() = !allGranted || (isFirstLaunch && !hasBeenRequested)
}
