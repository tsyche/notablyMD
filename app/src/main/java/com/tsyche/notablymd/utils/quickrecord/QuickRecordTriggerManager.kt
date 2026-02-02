package com.tsyche.notablymd.utils.quickrecord

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import com.tsyche.notablymd.presentation.service.VoiceRecordingService

/**
 * Unified trigger management system for Quick Voice Recording Triggers Manages settings and
 * coordinates between different trigger methods
 */
class QuickRecordTriggerManager private constructor(private val context: Context) {

    companion object {
        @Volatile private var INSTANCE: QuickRecordTriggerManager? = null

        fun getInstance(context: Context): QuickRecordTriggerManager {
            return INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: QuickRecordTriggerManager(context.applicationContext).also {
                            INSTANCE = it
                        }
                }
        }

        // SharedPreferences keys
        private const val PREFS_NAME = "quick_record_triggers"
        private const val KEY_ASSISTANT_ENABLED = "assistant_trigger_enabled"
        private const val KEY_QUICK_TILE_ENABLED = "quick_tile_trigger_enabled"
        private const val KEY_ACCESSIBILITY_ENABLED = "accessibility_trigger_enabled"
        private const val KEY_DEVICE_ADMIN_ENABLED = "device_admin_trigger_enabled"
        private const val KEY_CUSTOM_WAKE_PHRASE = "custom_wake_phrase"
        private const val KEY_HARDWARE_BUTTON_COMBO = "hardware_button_combo"

        // Default values
        private const val DEFAULT_WAKE_PHRASE = "Hey Notably"
        private const val DEFAULT_BUTTON_COMBO = "power_volume_up"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Trigger settings
    var isAssistantTriggerEnabled: Boolean
        get() = prefs.getBoolean(KEY_ASSISTANT_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ASSISTANT_ENABLED, value).apply()

    var isQuickTileTriggerEnabled: Boolean
        get() = prefs.getBoolean(KEY_QUICK_TILE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_QUICK_TILE_ENABLED, value).apply()

    var isAccessibilityTriggerEnabled: Boolean
        get() = prefs.getBoolean(KEY_ACCESSIBILITY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ACCESSIBILITY_ENABLED, value).apply()

    var isDeviceAdminTriggerEnabled: Boolean
        get() = prefs.getBoolean(KEY_DEVICE_ADMIN_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_DEVICE_ADMIN_ENABLED, value).apply()

    var customWakePhrase: String
        get() = prefs.getString(KEY_CUSTOM_WAKE_PHRASE, DEFAULT_WAKE_PHRASE) ?: DEFAULT_WAKE_PHRASE
        set(value) = prefs.edit().putString(KEY_CUSTOM_WAKE_PHRASE, value).apply()

    var hardwareButtonCombo: String
        get() =
            prefs.getString(KEY_HARDWARE_BUTTON_COMBO, DEFAULT_BUTTON_COMBO) ?: DEFAULT_BUTTON_COMBO
        set(value) = prefs.edit().putString(KEY_HARDWARE_BUTTON_COMBO, value).apply()

    /** Trigger voice recording from any enabled source */
    fun triggerVoiceRecording(source: TriggerSource) {
        if (!isAnyTriggerEnabled()) {
            return
        }

        // Check if specific trigger source is enabled
        if (!isTriggerSourceEnabled(source)) {
            return
        }

        // Start voice recording service
        val intent =
            Intent(context, VoiceRecordingService::class.java).apply {
                action = VoiceRecordingService.ACTION_START_RECORDING
                putExtra(VoiceRecordingService.EXTRA_TRIGGER_SOURCE, source.name)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /** Check if any trigger is enabled */
    fun isAnyTriggerEnabled(): Boolean {
        return isAssistantTriggerEnabled ||
            isQuickTileTriggerEnabled ||
            isAccessibilityTriggerEnabled ||
            isDeviceAdminTriggerEnabled
    }

    /** Check if specific trigger source is enabled */
    fun isTriggerSourceEnabled(source: TriggerSource): Boolean {
        return when (source) {
            TriggerSource.ASSISTANT -> isAssistantTriggerEnabled
            TriggerSource.QUICK_TILE -> isQuickTileTriggerEnabled
            TriggerSource.ACCESSIBILITY -> isAccessibilityTriggerEnabled
            TriggerSource.DEVICE_ADMIN -> isDeviceAdminTriggerEnabled
        }
    }

    /** Get all enabled trigger sources */
    fun getEnabledTriggerSources(): List<TriggerSource> {
        val enabledSources = mutableListOf<TriggerSource>()

        if (isAssistantTriggerEnabled) enabledSources.add(TriggerSource.ASSISTANT)
        if (isQuickTileTriggerEnabled) enabledSources.add(TriggerSource.QUICK_TILE)
        if (isAccessibilityTriggerEnabled) enabledSources.add(TriggerSource.ACCESSIBILITY)
        if (isDeviceAdminTriggerEnabled) enabledSources.add(TriggerSource.DEVICE_ADMIN)

        return enabledSources
    }

    /** Reset all trigger settings to defaults */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
        isQuickTileTriggerEnabled = true // Enable quick tile by default
    }

    /** Get trigger configuration summary */
    fun getTriggerSummary(): String {
        val enabledCount = getEnabledTriggerSources().size
        return "$enabledCount trigger(s) enabled"
    }
}

/** Enum representing different trigger sources */
enum class TriggerSource {
    ASSISTANT,
    QUICK_TILE,
    ACCESSIBILITY,
    DEVICE_ADMIN,
}
