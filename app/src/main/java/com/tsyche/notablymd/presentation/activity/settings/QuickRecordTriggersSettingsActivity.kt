package com.tsyche.notablymd.presentation.activity.settings

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.preference.*
import com.tsyche.notablymd.R
import com.tsyche.notablymd.utils.quickrecord.QuickRecordTriggerManager
import com.tsyche.notablymd.utils.quickrecord.deviceadmin.DeviceAdminManager

/**
 * Settings fragment for Quick Voice Recording Triggers Allows users to enable/disable and configure
 * different trigger methods
 */
class QuickRecordTriggersSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var triggerManager: QuickRecordTriggerManager
    private lateinit var deviceAdminManager: DeviceAdminManager

    // Preference keys
    companion object {
        private const val KEY_ASSISTANT_TRIGGER = "assistant_trigger"
        private const val KEY_QUICK_TILE_TRIGGER = "quick_tile_trigger"
        private const val KEY_ACCESSIBILITY_TRIGGER = "accessibility_trigger"
        private const val KEY_DEVICE_ADMIN_TRIGGER = "device_admin_trigger"
        private const val KEY_CUSTOM_WAKE_PHRASE = "custom_wake_phrase"
        private const val KEY_HARDWARE_BUTTON_COMBO = "hardware_button_combo"
        private const val KEY_TRIGGER_SUMMARY = "trigger_summary"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.quick_record_triggers_preferences, rootKey)

        triggerManager = QuickRecordTriggerManager.getInstance(requireContext())
        deviceAdminManager = DeviceAdminManager(requireContext())

        setupPreferences()
        updateTriggerSummary()
    }

    private fun setupPreferences() {
        // Assistant trigger preference
        val assistantPref = findPreference<SwitchPreferenceCompat>(KEY_ASSISTANT_TRIGGER)
        assistantPref?.apply {
            isChecked = triggerManager.isAssistantTriggerEnabled
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                triggerManager.isAssistantTriggerEnabled = enabled
                updateTriggerSummary()
                true
            }
        }

        // Quick tile trigger preference
        val quickTilePref = findPreference<SwitchPreferenceCompat>(KEY_QUICK_TILE_TRIGGER)
        quickTilePref?.apply {
            isChecked = triggerManager.isQuickTileTriggerEnabled
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                triggerManager.isQuickTileTriggerEnabled = enabled
                updateTriggerSummary()
                true
            }
        }

        // Accessibility trigger preference
        val accessibilityPref = findPreference<SwitchPreferenceCompat>(KEY_ACCESSIBILITY_TRIGGER)
        accessibilityPref?.apply {
            isChecked = triggerManager.isAccessibilityTriggerEnabled
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                if (enabled) {
                    // Open accessibility settings
                    openAccessibilitySettings()
                } else {
                    triggerManager.isAccessibilityTriggerEnabled = enabled
                }
                updateTriggerSummary()
                true
            }
        }

        // Device admin trigger preference
        val deviceAdminPref = findPreference<SwitchPreferenceCompat>(KEY_DEVICE_ADMIN_TRIGGER)
        deviceAdminPref?.apply {
            isChecked =
                triggerManager.isDeviceAdminTriggerEnabled && deviceAdminManager.isAdminActive()
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                if (enabled) {
                    if (!deviceAdminManager.isAdminActive()) {
                        deviceAdminManager.requestAdminRights()
                    } else {
                        triggerManager.isDeviceAdminTriggerEnabled = enabled
                    }
                } else {
                    triggerManager.isDeviceAdminTriggerEnabled = enabled
                    if (deviceAdminManager.isAdminActive()) {
                        // Optionally remove admin rights
                        // deviceAdminManager.removeAdminRights()
                    }
                }
                updateTriggerSummary()
                true
            }
        }

        // Custom wake phrase preference
        val wakePhrasePref = findPreference<EditTextPreference>(KEY_CUSTOM_WAKE_PHRASE)
        wakePhrasePref?.apply {
            text = triggerManager.customWakePhrase
            setOnPreferenceChangeListener { _, newValue ->
                val phrase = newValue as String
                if (phrase.isNotBlank()) {
                    triggerManager.customWakePhrase = phrase
                    true
                } else {
                    false // Don't allow empty phrase
                }
            }
        }

        // Hardware button combo preference
        val buttonComboPref = findPreference<ListPreference>(KEY_HARDWARE_BUTTON_COMBO)
        buttonComboPref?.apply {
            val currentValue = triggerManager.hardwareButtonCombo
            val index = findIndexOfValue(currentValue)
            if (index >= 0) {
                setValueIndex(index)
            }

            setOnPreferenceChangeListener { _, newValue ->
                val combo = newValue as String
                triggerManager.hardwareButtonCombo = combo
                true
            }
        }

        // Trigger summary preference
        val summaryPref = findPreference<Preference>(KEY_TRIGGER_SUMMARY)
        summaryPref?.setOnPreferenceClickListener {
            showTriggerDetails()
            true
        }
    }

    private fun updateTriggerSummary() {
        val summaryPref = findPreference<Preference>(KEY_TRIGGER_SUMMARY)
        summaryPref?.summary = triggerManager.getTriggerSummary()

        // Update device admin preference state
        val deviceAdminPref = findPreference<SwitchPreferenceCompat>(KEY_DEVICE_ADMIN_TRIGGER)
        deviceAdminPref?.isChecked =
            triggerManager.isDeviceAdminTriggerEnabled && deviceAdminManager.isAdminActive()
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun showTriggerDetails() {
        val enabledSources = triggerManager.getEnabledTriggerSources()
        val message =
            if (enabledSources.isEmpty()) {
                "No triggers enabled. Enable at least one trigger method to use quick voice recording."
            } else {
                val sources =
                    enabledSources.joinToString(", ") {
                        when (it) {
                            com.tsyche.notablymd.utils.quickrecord.TriggerSource.ASSISTANT ->
                                "Voice Assistant"
                            com.tsyche.notablymd.utils.quickrecord.TriggerSource.QUICK_TILE ->
                                "Quick Settings Tile"
                            com.tsyche.notablymd.utils.quickrecord.TriggerSource.ACCESSIBILITY ->
                                "Hardware Buttons"
                            com.tsyche.notablymd.utils.quickrecord.TriggerSource.DEVICE_ADMIN ->
                                "Device Admin"
                        }
                    }
                "Enabled triggers: $sources"
            }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Quick Record Triggers")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}

/** Activity for Quick Record Triggers Settings */
class QuickRecordTriggersSettingsActivity : androidx.appcompat.app.AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, QuickRecordTriggersSettingsFragment())
                .commit()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Quick Record Triggers"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
