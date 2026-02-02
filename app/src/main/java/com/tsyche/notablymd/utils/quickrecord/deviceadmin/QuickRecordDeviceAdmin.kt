package com.tsyche.notablymd.utils.quickrecord.deviceadmin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserHandle
import android.util.Log
import com.tsyche.notablymd.presentation.service.VoiceRecordingService
import com.tsyche.notablymd.utils.quickrecord.QuickRecordTriggerManager

/**
 * Device Administrator for system-level control Provides enhanced capabilities for hardware button
 * interception
 */
class QuickRecordDeviceAdmin : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "QuickRecordDeviceAdmin"

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, QuickRecordDeviceAdmin::class.java)
        }

        fun isAdminActive(context: Context): Boolean {
            val devicePolicyManager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            return devicePolicyManager.isAdminActive(getComponentName(context))
        }

        fun requestAdminRights(context: Context) {
            val intent =
                Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(context))
                    putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "NotablyMD needs device administrator access to provide hardware button shortcuts for voice recording.",
                    )
                }
            context.startActivity(intent)
        }

        fun removeAdminRights(context: Context) {
            val devicePolicyManager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            devicePolicyManager.removeActiveAdmin(getComponentName(context))
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "Device admin enabled")

        // Enable device admin trigger by default when admin rights are granted
        val triggerManager = QuickRecordTriggerManager.getInstance(context)
        triggerManager.isDeviceAdminTriggerEnabled = true

        // Show notification or toast to user
        showAdminEnabledNotification(context)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "Device admin disabled")

        // Disable device admin trigger when admin rights are revoked
        val triggerManager = QuickRecordTriggerManager.getInstance(context)
        triggerManager.isDeviceAdminTriggerEnabled = false

        showAdminDisabledNotification(context)
    }

    override fun onPasswordChanged(context: Context, intent: Intent) {
        super.onPasswordChanged(context, intent)
        Log.d(TAG, "Password changed")
    }

    override fun onPasswordFailed(context: Context, intent: Intent, userHandle: UserHandle) {
        super.onPasswordFailed(context, intent, userHandle)
        Log.d(TAG, "Password failed: $userHandle")

        // Could trigger voice recording on specific password failure patterns
        // This is a potential advanced feature
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent, userHandle: UserHandle) {
        super.onPasswordSucceeded(context, intent, userHandle)
        Log.d(TAG, "Password succeeded")
    }

    /** Handle lock screen events for voice recording */
    private fun handleLockScreenEvent(context: Context, eventType: String) {
        val triggerManager = QuickRecordTriggerManager.getInstance(context)

        if (!triggerManager.isDeviceAdminTriggerEnabled) {
            return
        }

        // Could implement lock screen specific triggers here
        when (eventType) {
            "SCREEN_LOCKED" -> {
                Log.d(TAG, "Screen locked - device admin ready for triggers")
            }
            "SCREEN_UNLOCKED" -> {
                Log.d(TAG, "Screen unlocked")
            }
        }
    }

    /** Show notification when device admin is enabled */
    private fun showAdminEnabledNotification(context: Context) {
        // Implementation would show a notification
        Log.d(TAG, "Device admin enabled - hardware button shortcuts available")
    }

    /** Show notification when device admin is disabled */
    private fun showAdminDisabledNotification(context: Context) {
        // Implementation would show a notification
        Log.d(TAG, "Device admin disabled - some features may not work")
    }
}

/** Helper class for managing device admin features */
class DeviceAdminManager(private val context: Context) {

    private val devicePolicyManager: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponent: ComponentName = QuickRecordDeviceAdmin.getComponentName(context)

    /** Check if device admin is active */
    fun isAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    /** Request device admin rights */
    fun requestAdminRights() {
        QuickRecordDeviceAdmin.requestAdminRights(context)
    }

    /** Remove device admin rights */
    fun removeAdminRights() {
        if (isAdminActive()) {
            QuickRecordDeviceAdmin.removeAdminRights(context)
        }
    }

    /** Get device admin capabilities */
    fun getAdminCapabilities(): List<String> {
        val capabilities = mutableListOf<String>()

        if (isAdminActive()) {
            capabilities.add("Hardware button interception")
            capabilities.add("Lock screen integration")
            capabilities.add("System-level shortcuts")
        }

        return capabilities
    }

    /** Trigger voice recording using device admin capabilities */
    fun triggerVoiceRecording() {
        if (!isAdminActive()) {
            Log.w("DeviceAdminManager", "Device admin not active")
            return
        }

        val triggerManager = QuickRecordTriggerManager.getInstance(context)

        if (!triggerManager.isDeviceAdminTriggerEnabled) {
            return
        }

        val intent =
            Intent(context, VoiceRecordingService::class.java).apply {
                action = VoiceRecordingService.ACTION_START_RECORDING
                putExtra(VoiceRecordingService.EXTRA_TRIGGER_SOURCE, "DEVICE_ADMIN")
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        Log.d("DeviceAdminManager", "Voice recording triggered via device admin")
    }
}
