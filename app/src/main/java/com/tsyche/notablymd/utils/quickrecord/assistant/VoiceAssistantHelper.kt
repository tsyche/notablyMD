package com.tsyche.notablymd.utils.quickrecord.assistant

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log

/** Helper class to check and configure voice assistant settings */
object VoiceAssistantHelper {

    private const val TAG = "VoiceAssistantHelper"

    /** Check if voice assistant is properly configured */
    fun checkVoiceAssistantConfiguration(context: Context): Boolean {
        return try {
            // Check if voice interaction service is available
            val pm = context.packageManager
            val serviceInfo =
                pm.resolveService(
                    Intent(context, NotablyVoiceInteractionService::class.java),
                    PackageManager.GET_META_DATA,
                )

            serviceInfo != null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking voice assistant configuration", e)
            false
        }
    }

    /** Get instructions for enabling voice assistant */
    fun getVoiceAssistantInstructions(context: Context): String {
        return buildString {
            appendLine("To enable NotablyMD as a voice assistant:")
            appendLine()
            appendLine(
                "1. Go to Settings > Apps & notifications > Special app access > Voice assistant"
            )
            appendLine("2. Look for 'NotablyMD' in the list")
            appendLine("3. If NotablyMD doesn't appear, try these steps:")
            appendLine("   - Make sure you're running Android 8.0+")
            appendLine("   - Reboot your device after installing NotablyMD")
            appendLine("   - Check if your device manufacturer supports custom voice assistants")
            appendLine()
            appendLine("Alternative methods:")
            appendLine("• Use 'Hey Notably' wake phrase (if enabled in settings)")
            appendLine("• Use Quick Settings tile for voice recording")
            appendLine("• Use Power + Volume Up hardware button")
            appendLine("• Use the widget for one-tap recording")
        }
    }

    /** Open voice assistant settings (if available) */
    fun openVoiceAssistantSettings(context: Context) {
        try {
            val intent =
                Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not open voice assistant settings", e)
            // Fallback to general settings
            try {
                val settingsIntent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                context.startActivity(settingsIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Could not open app settings either", e2)
            }
        }
    }
}
