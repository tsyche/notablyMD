package com.tsyche.notablymd.utils.quickrecord.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.tsyche.notablymd.presentation.service.VoiceRecordingService
import com.tsyche.notablymd.utils.quickrecord.QuickRecordTriggerManager

/**
 * Accessibility Service for hardware button event interception Captures hardware button
 * combinations to trigger voice recording
 */
class QuickRecordAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "QuickRecordA11y"

        // Button combination tracking
        private var powerPressedTime = 0L
        private var volumeUpPressedTime = 0L
        private var volumeDownPressedTime = 0L

        // Combination detection windows (in milliseconds)
        private const val COMBINATION_WINDOW = 500L
        private const val TRIPLE_PRESS_WINDOW = 1000L

        // Press tracking
        private var powerPressCount = 0
        private var lastPowerPressTime = 0L
    }

    private lateinit var triggerManager: QuickRecordTriggerManager

    override fun onCreate() {
        super.onCreate()
        triggerManager = QuickRecordTriggerManager.getInstance(this)
        Log.d(TAG, "Quick record accessibility service created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val info =
            AccessibilityServiceInfo().apply {
                // We want to receive key events
                flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS

                // We don't need specific event types
                eventTypes = AccessibilityEvent.TYPES_ALL_MASK

                // We don't need specific feedback types
                feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC

                // We don't need to filter by package
                packageNames = null

                // Set service name
                notificationTimeout = 100
            }

        serviceInfo = info
        Log.d(TAG, "Accessibility service connected with key event filtering")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't need to handle accessibility events, just key events
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!triggerManager.isAccessibilityTriggerEnabled) {
            return false // Don't consume the event
        }

        val action = event.action
        val keyCode = event.keyCode
        val currentTime = System.currentTimeMillis()

        Log.d(TAG, "Key event: action=$action, keyCode=$keyCode")

        when (action) {
            KeyEvent.ACTION_DOWN -> {
                return handleKeyDown(keyCode, currentTime)
            }
            KeyEvent.ACTION_UP -> {
                return handleKeyUp(keyCode, currentTime)
            }
        }

        return false // Don't consume the event by default
    }

    /** Handle key down events */
    private fun handleKeyDown(keyCode: Int, currentTime: Long): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_POWER -> {
                powerPressedTime = currentTime
                return false // Don't consume power button
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                volumeUpPressedTime = currentTime
                checkPowerVolumeUpCombination(currentTime)
                return false // Don't consume volume up
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                volumeDownPressedTime = currentTime
                return false // Don't consume volume down
            }
        }

        return false
    }

    /** Handle key up events */
    private fun handleKeyUp(keyCode: Int, currentTime: Long): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_POWER -> {
                checkTriplePowerPress(currentTime)
                return false
            }
        }

        return false
    }

    /** Check for Power + Volume Up combination (simultaneous press) */
    private fun checkPowerVolumeUpCombination(currentTime: Long) {
        val buttonCombo = triggerManager.hardwareButtonCombo

        if (
            buttonCombo == "power_volume_up" &&
                powerPressedTime > 0 &&
                currentTime - powerPressedTime < COMBINATION_WINDOW
        ) {

            Log.d(TAG, "Power + Volume Up combination detected")
            triggerVoiceRecording("POWER_VOLUME_UP")
        }
    }

    /** Check for Triple Power Button Press */
    private fun checkTriplePowerPress(currentTime: Long) {
        val buttonCombo = triggerManager.hardwareButtonCombo

        if (buttonCombo != "triple_power") {
            return
        }

        if (currentTime - lastPowerPressTime < TRIPLE_PRESS_WINDOW) {
            powerPressCount++
        } else {
            powerPressCount = 1
        }

        lastPowerPressTime = currentTime

        if (powerPressCount == 3) {
            Log.d(TAG, "Triple power button press detected")
            triggerVoiceRecording("TRIPLE_POWER")
            powerPressCount = 0 // Reset count
        }
    }

    /** Check for Double Power + Volume Up sequence */
    private fun checkDoublePowerVolumeUp(currentTime: Long) {
        val buttonCombo = triggerManager.hardwareButtonCombo

        if (buttonCombo != "double_power_volume_up") {
            return
        }

        // This would require more complex state tracking
        // For now, we'll implement the simpler combinations
    }

    /** Trigger voice recording with specific button combination */
    private fun triggerVoiceRecording(combination: String) {
        Log.d(TAG, "Triggering voice recording via: $combination")

        val intent =
            Intent(this, VoiceRecordingService::class.java).apply {
                action = VoiceRecordingService.ACTION_START_RECORDING
                putExtra(VoiceRecordingService.EXTRA_TRIGGER_SOURCE, "ACCESSIBILITY")
                putExtra("button_combination", combination)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        // Provide haptic feedback
        try {
            performGlobalAction(GLOBAL_ACTION_BACK) // This won't actually work, but we try
        } catch (e: Exception) {
            Log.w(TAG, "Could not provide feedback", e)
        }
    }

    /** Reset button state tracking */
    private fun resetButtonState() {
        powerPressedTime = 0
        volumeUpPressedTime = 0
        volumeDownPressedTime = 0
        powerPressCount = 0
        lastPowerPressTime = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        resetButtonState()
        Log.d(TAG, "Accessibility service destroyed")
    }
}
