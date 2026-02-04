package com.tsyche.notablymd.presentation.firstuse

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Helper class for showing permission dialogs and handling permission requests */
class PermissionDialogHelper(private val activity: AppCompatActivity) {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var onPermissionResult: ((Boolean) -> Unit)? = null

    fun setupPermissionLauncher() {
        permissionLauncher =
            activity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.values.all { it }
                onPermissionResult?.invoke(allGranted)
            }
    }

    /** Show permission rationale dialog */
    fun showPermissionRationaleDialog(
        missingPermissions: List<String>,
        onResult: (Boolean) -> Unit,
    ) {
        onPermissionResult = onResult

        val message = buildPermissionRationaleMessage(missingPermissions)

        MaterialAlertDialogBuilder(activity)
            .setTitle("Permissions Required")
            .setMessage(message)
            .setPositiveButton("Grant Permissions") { _, _ ->
                requestPermissions(missingPermissions)
            }
            .setNegativeButton("Not Now") { _, _ -> onResult(false) }
            .setCancelable(false)
            .show()
    }

    /** Show settings dialog for permissions that were permanently denied */
    fun showSettingsDialog(onResult: (Boolean) -> Unit) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Permissions Required")
            .setMessage(
                "Some permissions were permanently denied. Please enable them in Settings to use voice recording features."
            )
            .setPositiveButton("Open Settings") { _, _ -> openAppSettings(onResult) }
            .setNegativeButton("Cancel") { _, _ -> onResult(false) }
            .setCancelable(false)
            .show()
    }

    /** Show first-time welcome dialog with permission request */
    fun showFirstTimeWelcomeDialog(onResult: (Boolean) -> Unit) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Welcome to NotablyMD!")
            .setMessage(
                "NotablyMD needs microphone and notification permissions to provide voice recording features. " +
                    "These permissions are only used for recording your voice notes and showing recording status."
            )
            .setPositiveButton("Continue") { _, _ -> onResult(true) }
            .setCancelable(false)
            .show()
    }

    /** Show widget setup guide */
    fun showWidgetSetupGuide(onResult: (Boolean) -> Unit) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Widget Setup")
            .setMessage(
                "To add the voice recording widget:\n\n" +
                    "1. Long-press on your home screen\n" +
                    "2. Select 'Widgets'\n" +
                    "3. Find 'NotablyMD' and add the 1x1 widget\n" +
                    "4. Tap the widget to start voice recording!"
            )
            .setPositiveButton("Got it") { _, _ -> onResult(true) }
            .setNegativeButton("Skip") { _, _ -> onResult(false) }
            .show()
    }

    private fun requestPermissions(permissions: List<String>) {
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun openAppSettings(onResult: (Boolean) -> Unit) {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
        activity.startActivity(intent)
        // Note: We can't directly detect when user returns from settings,
        // so we'll rely on them to trigger the check again
    }

    private fun buildPermissionRationaleMessage(missingPermissions: List<String>): String {
        val message = StringBuilder("NotablyMD needs the following permissions:\n\n")

        if (missingPermissions.contains(android.Manifest.permission.RECORD_AUDIO)) {
            message.append("• Microphone: To record your voice notes\n")
        }

        if (missingPermissions.contains(android.Manifest.permission.POST_NOTIFICATIONS)) {
            message.append("• Notifications: To show recording status and alerts\n")
        }

        message.append("\nThese permissions are only used for voice recording features.")

        return message.toString()
    }
}
