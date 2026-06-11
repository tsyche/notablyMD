package com.tsyche.notablymd.presentation.activity.main.fragment.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tsyche.notablymd.presentation.viewmodel.preference.NotablyMDPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class to verify widget customization functionality works correctly These tests verify the
 * widget customization preferences and UI infrastructure
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class WidgetCustomizationTest {

    private lateinit var context: Context
    private lateinit var preferences: NotablyMDPreferences

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Reset the NotablyMDPreferences singleton to ensure fresh state
        resetNotablyMDPreferencesSingleton()

        // Clear SharedPreferences to ensure fresh state with new defaults
        val sharedPreferences =
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().clear().commit()

        // Also clear encrypted preferences
        try {
            val encryptedPrefs =
                context.getSharedPreferences("secret_shared_prefs", Context.MODE_PRIVATE)
            encryptedPrefs.edit().clear().commit()
        } catch (e: Exception) {
            // Encrypted prefs might not be available in test environment
        }

        // Force recreation of preferences instance
        preferences = NotablyMDPreferences.getInstance(context)
    }

    private fun resetNotablyMDPreferencesSingleton() {
        // Use reflection to reset the singleton instance for test isolation
        try {
            val instanceField = NotablyMDPreferences::class.java.getDeclaredField("instance")
            instanceField.isAccessible = true
            instanceField.set(null, null)
        } catch (e: Exception) {
            // If reflection fails, at least we tried
            println("Warning: Could not reset NotablyMDPreferences singleton: ${e.message}")
        }
    }

    @Test
    fun widgetCustomizationPreferencesShouldHaveCorrectDefaults() {
        // Test that widget customization preferences have correct default values
        val customizationEnabled = preferences.widgetCustomizationEnabled.value
        val iconStyle = preferences.widgetIconStyle.value
        val colorScheme = preferences.widgetColorScheme.value
        val showStatusIndicator = preferences.widgetShowStatusIndicator.value
        val behaviorOnTap = preferences.widgetBehaviorOnTap.value

        assertEquals(
            "Widget customization should be enabled by default",
            true,
            customizationEnabled,
        )
        assertEquals("Widget icon style should default to 'default'", "default", iconStyle)
        assertEquals("Widget color scheme should default to 'blue'", "blue", colorScheme)
        assertEquals(
            "Widget status indicator should be shown by default",
            true,
            showStatusIndicator,
        )
        assertEquals("Widget behavior should default to 'record'", "record", behaviorOnTap)
    }

    @Test
    fun widgetCustomizationPreferencesShouldBeSettable() {
        // Test that widget customization preferences can be set to new values
        preferences.widgetCustomizationEnabled.save(false)
        preferences.widgetIconStyle.save("minimal")
        preferences.widgetColorScheme.save("red")
        preferences.widgetShowStatusIndicator.save(false)
        preferences.widgetBehaviorOnTap.save("open")

        assertEquals(
            "Widget customization should be settable to false",
            false,
            preferences.widgetCustomizationEnabled.value,
        )
        assertEquals(
            "Widget icon style should be settable to 'minimal'",
            "minimal",
            preferences.widgetIconStyle.value,
        )
        assertEquals(
            "Widget color scheme should be settable to 'red'",
            "red",
            preferences.widgetColorScheme.value,
        )
        assertEquals(
            "Widget status indicator should be settable to false",
            false,
            preferences.widgetShowStatusIndicator.value,
        )
        assertEquals(
            "Widget behavior should be settable to 'open'",
            "open",
            preferences.widgetBehaviorOnTap.value,
        )
    }

    @Test
    fun triggerMethodPreferencesShouldHaveCorrectDefaults() {
        // Test that trigger method preferences have correct default values
        val quickTileEnabled = preferences.quickTileTriggerEnabled.value
        val hardwareButtonEnabled = preferences.hardwareButtonTriggerEnabled.value
        val voiceAssistantEnabled = preferences.voiceAssistantTriggerEnabled.value
        val deviceAdminEnabled = preferences.deviceAdminTriggerEnabled.value
        val accessibilityServiceEnabled = preferences.accessibilityServiceEnabled.value

        assertEquals("Quick Settings tile should be enabled by default", true, quickTileEnabled)
        assertEquals(
            "Hardware button trigger should be enabled by default",
            true,
            hardwareButtonEnabled,
        )
        assertEquals(
            "Voice assistant trigger should be enabled by default",
            true,
            voiceAssistantEnabled,
        )
        assertEquals(
            "Device admin trigger should be disabled by default",
            false,
            deviceAdminEnabled,
        )
        assertEquals(
            "Accessibility service should be enabled by default",
            true,
            accessibilityServiceEnabled,
        )
    }

    @Test
    fun triggerMethodPreferencesShouldBeSettable() {
        // Test that trigger method preferences can be enabled/disabled
        preferences.quickTileTriggerEnabled.save(false)
        preferences.hardwareButtonTriggerEnabled.save(false)
        preferences.voiceAssistantTriggerEnabled.save(false)
        preferences.deviceAdminTriggerEnabled.save(true)
        preferences.accessibilityServiceEnabled.save(false)

        assertEquals(
            "Quick Settings tile should be settable to false",
            false,
            preferences.quickTileTriggerEnabled.value,
        )
        assertEquals(
            "Hardware button trigger should be settable to false",
            false,
            preferences.hardwareButtonTriggerEnabled.value,
        )
        assertEquals(
            "Voice assistant trigger should be settable to false",
            false,
            preferences.voiceAssistantTriggerEnabled.value,
        )
        assertEquals(
            "Device admin trigger should be settable to true",
            true,
            preferences.deviceAdminTriggerEnabled.value,
        )
        assertEquals(
            "Accessibility service should be settable to false",
            false,
            preferences.accessibilityServiceEnabled.value,
        )
    }

    @Test
    fun quickTileCustomizationPreferencesShouldHaveCorrectDefaults() {
        // Test that Quick Settings tile customization preferences have correct defaults
        val iconStyle = preferences.quickTileIconStyle.value
        val colorScheme = preferences.quickTileColorScheme.value
        val showStatus = preferences.quickTileShowStatus.value

        assertEquals("Quick tile icon style should default to 'default'", "default", iconStyle)
        assertEquals("Quick tile color scheme should default to 'blue'", "blue", colorScheme)
        assertEquals("Quick tile status should be shown by default", true, showStatus)
    }

    @Test
    fun quickTileCustomizationPreferencesShouldBeSettable() {
        // Test that Quick Settings tile customization preferences can be set to new values
        preferences.quickTileIconStyle.save("bold")
        preferences.quickTileColorScheme.save("green")
        preferences.quickTileShowStatus.save(false)

        assertEquals(
            "Quick tile icon style should be settable to 'bold'",
            "bold",
            preferences.quickTileIconStyle.value,
        )
        assertEquals(
            "Quick tile color scheme should be settable to 'green'",
            "green",
            preferences.quickTileColorScheme.value,
        )
        assertEquals(
            "Quick tile status should be settable to false",
            false,
            preferences.quickTileShowStatus.value,
        )
    }

    @Test
    fun widgetCustomizationPreferencesShouldPersist() {
        // Test that widget customization preferences persist across instances
        val testIconStyle = "outline"
        val testColorScheme = "purple"
        val testBehavior = "last"

        // Set values
        preferences.widgetIconStyle.save(testIconStyle)
        preferences.widgetColorScheme.save(testColorScheme)
        preferences.widgetBehaviorOnTap.save(testBehavior)

        // Create new instance to test persistence
        val newPreferences = NotablyMDPreferences.getInstance(context)

        assertEquals(
            "Widget icon style should persist",
            testIconStyle,
            newPreferences.widgetIconStyle.value,
        )
        assertEquals(
            "Widget color scheme should persist",
            testColorScheme,
            newPreferences.widgetColorScheme.value,
        )
        assertEquals(
            "Widget behavior should persist",
            testBehavior,
            newPreferences.widgetBehaviorOnTap.value,
        )
    }

    @Test
    fun triggerMethodPreferencesShouldPersist() {
        // Test that trigger method preferences persist across instances
        val testQuickTileEnabled = false
        val testHardwareButtonEnabled = false
        val testVoiceAssistantEnabled = false

        // Set values
        preferences.quickTileTriggerEnabled.save(testQuickTileEnabled)
        preferences.hardwareButtonTriggerEnabled.save(testHardwareButtonEnabled)
        preferences.voiceAssistantTriggerEnabled.save(testVoiceAssistantEnabled)

        // Create new instance to test persistence
        val newPreferences = NotablyMDPreferences.getInstance(context)

        assertEquals(
            "Quick Settings tile state should persist",
            testQuickTileEnabled,
            newPreferences.quickTileTriggerEnabled.value,
        )
        assertEquals(
            "Hardware button trigger state should persist",
            testHardwareButtonEnabled,
            newPreferences.hardwareButtonTriggerEnabled.value,
        )
        assertEquals(
            "Voice assistant trigger state should persist",
            testVoiceAssistantEnabled,
            newPreferences.voiceAssistantTriggerEnabled.value,
        )
    }

    @Test
    fun widgetCustomizationShouldHaveRequiredProperties() {
        // Test that widget customization preferences have required properties for UI binding
        val customizationPref = preferences.widgetCustomizationEnabled
        val iconStylePref = preferences.widgetIconStyle
        val colorSchemePref = preferences.widgetColorScheme
        val statusIndicatorPref = preferences.widgetShowStatusIndicator
        val behaviorPref = preferences.widgetBehaviorOnTap

        // Verify titleResId exists (required for PreferenceBinding.setup())
        assertNotNull(
            "Widget customization enabled must have titleResId",
            customizationPref.titleResId,
        )
        assertNotNull("Widget icon style must have titleResId", iconStylePref.titleResId)
        assertNotNull("Widget color scheme must have titleResId", colorSchemePref.titleResId)
        assertNotNull(
            "Widget status indicator must have titleResId",
            statusIndicatorPref.titleResId,
        )
        assertNotNull("Widget behavior must have titleResId", behaviorPref.titleResId)

        // Verify titleResId points to actual string resources
        val customizationTitle = context.getString(customizationPref.titleResId!!)
        val iconStyleTitle = context.getString(iconStylePref.titleResId!!)
        val colorSchemeTitle = context.getString(colorSchemePref.titleResId!!)
        val statusIndicatorTitle = context.getString(statusIndicatorPref.titleResId!!)
        val behaviorTitle = context.getString(behaviorPref.titleResId!!)

        assertEquals(
            "Widget customization title should match",
            "Widget Customization",
            customizationTitle,
        )
        assertEquals("Widget icon style title should match", "Widget Icon Style", iconStyleTitle)
        assertEquals(
            "Widget color scheme title should match",
            "Widget Color Scheme",
            colorSchemeTitle,
        )
        assertEquals(
            "Widget status indicator title should match",
            "Show Status Indicator",
            statusIndicatorTitle,
        )
        assertEquals("Widget behavior title should match", "Widget Behavior on Tap", behaviorTitle)
    }

    @Test
    fun triggerMethodsShouldHaveRequiredProperties() {
        // Test that trigger method preferences have required properties for UI binding
        val quickTilePref = preferences.quickTileTriggerEnabled
        val hardwareButtonPref = preferences.hardwareButtonTriggerEnabled
        val voiceAssistantPref = preferences.voiceAssistantTriggerEnabled
        val deviceAdminPref = preferences.deviceAdminTriggerEnabled
        val accessibilityPref = preferences.accessibilityServiceEnabled

        // Verify titleResId exists (required for PreferenceBinding.setup())
        assertNotNull("Quick Settings tile must have titleResId", quickTilePref.titleResId)
        assertNotNull("Hardware button trigger must have titleResId", hardwareButtonPref.titleResId)
        assertNotNull("Voice assistant trigger must have titleResId", voiceAssistantPref.titleResId)
        assertNotNull("Device admin trigger must have titleResId", deviceAdminPref.titleResId)
        assertNotNull("Accessibility service must have titleResId", accessibilityPref.titleResId)

        // Verify titleResId points to actual string resources
        val quickTileTitle = context.getString(quickTilePref.titleResId!!)
        val hardwareButtonTitle = context.getString(hardwareButtonPref.titleResId!!)
        val voiceAssistantTitle = context.getString(voiceAssistantPref.titleResId!!)
        val deviceAdminTitle = context.getString(deviceAdminPref.titleResId!!)
        val accessibilityTitle = context.getString(accessibilityPref.titleResId!!)

        assertEquals(
            "Quick Settings tile title should match",
            "Quick Settings Tile",
            quickTileTitle,
        )
        assertEquals(
            "Hardware button trigger title should match",
            "Hardware Button Trigger",
            hardwareButtonTitle,
        )
        assertEquals(
            "Voice assistant trigger title should match",
            "Voice Assistant Trigger",
            voiceAssistantTitle,
        )
        assertEquals(
            "Device admin trigger title should match",
            "Device Administrator",
            deviceAdminTitle,
        )
        assertEquals(
            "Accessibility service title should match",
            "Accessibility Service",
            accessibilityTitle,
        )
    }

    @Test
    fun quickTileCustomizationShouldHaveRequiredProperties() {
        // Test that Quick Settings tile customization preferences have required properties for UI
        // binding
        val iconStylePref = preferences.quickTileIconStyle
        val colorSchemePref = preferences.quickTileColorScheme
        val showStatusPref = preferences.quickTileShowStatus

        // Verify titleResId exists (required for PreferenceBinding.setup())
        assertNotNull("Quick tile icon style must have titleResId", iconStylePref.titleResId)
        assertNotNull("Quick tile color scheme must have titleResId", colorSchemePref.titleResId)
        assertNotNull("Quick tile show status must have titleResId", showStatusPref.titleResId)

        // Verify titleResId points to actual string resources
        val iconStyleTitle = context.getString(iconStylePref.titleResId!!)
        val colorSchemeTitle = context.getString(colorSchemePref.titleResId!!)
        val showStatusTitle = context.getString(showStatusPref.titleResId!!)

        assertEquals(
            "Quick tile icon style title should match",
            "Quick Tile Icon Style",
            iconStyleTitle,
        )
        assertEquals(
            "Quick tile color scheme title should match",
            "Quick Tile Color Scheme",
            colorSchemeTitle,
        )
        assertEquals(
            "Quick tile show status title should match",
            "Show Quick Tile Status",
            showStatusTitle,
        )
    }

    @Test
    fun widgetCustomizationShouldHandleInvalidValues() {
        // Test that widget customization preferences handle edge cases gracefully
        val originalIconStyle = preferences.widgetIconStyle.value
        val originalColorScheme = preferences.widgetColorScheme.value
        val originalBehavior = preferences.widgetBehaviorOnTap.value

        // Test with empty string
        preferences.widgetIconStyle.save("")
        preferences.widgetColorScheme.save("")
        preferences.widgetBehaviorOnTap.save("")

        assertEquals(
            "Widget icon style should handle empty string",
            "",
            preferences.widgetIconStyle.value,
        )
        assertEquals(
            "Widget color scheme should handle empty string",
            "",
            preferences.widgetColorScheme.value,
        )
        assertEquals(
            "Widget behavior should handle empty string",
            "",
            preferences.widgetBehaviorOnTap.value,
        )

        // Test with very long string
        val longString = "a".repeat(1000)
        preferences.widgetIconStyle.save(longString)
        preferences.widgetColorScheme.save(longString)
        preferences.widgetBehaviorOnTap.save(longString)

        assertEquals(
            "Widget icon style should handle long string",
            longString,
            preferences.widgetIconStyle.value,
        )
        assertEquals(
            "Widget color scheme should handle long string",
            longString,
            preferences.widgetColorScheme.value,
        )
        assertEquals(
            "Widget behavior should handle long string",
            longString,
            preferences.widgetBehaviorOnTap.value,
        )

        // Restore original values
        preferences.widgetIconStyle.save(originalIconStyle)
        preferences.widgetColorScheme.save(originalColorScheme)
        preferences.widgetBehaviorOnTap.save(originalBehavior)
    }

    @Test
    fun triggerMethodsShouldHandleBooleanTransitions() {
        // Test that trigger method preferences handle boolean transitions correctly
        val originalQuickTile = preferences.quickTileTriggerEnabled.value
        val originalHardwareButton = preferences.hardwareButtonTriggerEnabled.value
        val originalVoiceAssistant = preferences.voiceAssistantTriggerEnabled.value

        // Test multiple transitions
        preferences.quickTileTriggerEnabled.save(false)
        assertEquals(
            "Quick Settings tile should transition to false",
            false,
            preferences.quickTileTriggerEnabled.value,
        )

        preferences.quickTileTriggerEnabled.save(true)
        assertEquals(
            "Quick Settings tile should transition back to true",
            true,
            preferences.quickTileTriggerEnabled.value,
        )

        preferences.hardwareButtonTriggerEnabled.save(false)
        assertEquals(
            "Hardware button trigger should transition to false",
            false,
            preferences.hardwareButtonTriggerEnabled.value,
        )

        preferences.hardwareButtonTriggerEnabled.save(true)
        assertEquals(
            "Hardware button trigger should transition back to true",
            true,
            preferences.hardwareButtonTriggerEnabled.value,
        )

        preferences.voiceAssistantTriggerEnabled.save(false)
        assertEquals(
            "Voice assistant trigger should transition to false",
            false,
            preferences.voiceAssistantTriggerEnabled.value,
        )

        preferences.voiceAssistantTriggerEnabled.save(true)
        assertEquals(
            "Voice assistant trigger should transition back to true",
            true,
            preferences.voiceAssistantTriggerEnabled.value,
        )

        // Restore original values
        preferences.quickTileTriggerEnabled.save(originalQuickTile)
        preferences.hardwareButtonTriggerEnabled.save(originalHardwareButton)
        preferences.voiceAssistantTriggerEnabled.save(originalVoiceAssistant)
    }
}
