package com.tsyche.notablymd.utils.quickrecord

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Unit tests for QuickRecordTriggerManager */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class QuickRecordTriggerManagerTest {

    private lateinit var context: Context
    private lateinit var triggerManager: QuickRecordTriggerManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        triggerManager = QuickRecordTriggerManager.getInstance(context)
        triggerManager.resetToDefaults()
    }

    @Test
    fun `test default trigger settings`() {
        // Test default values
        assertFalse(
            "Assistant trigger should be disabled by default",
            triggerManager.isAssistantTriggerEnabled,
        )
        assertTrue(
            "Quick tile trigger should be enabled by default",
            triggerManager.isQuickTileTriggerEnabled,
        )
        assertFalse(
            "Accessibility trigger should be disabled by default",
            triggerManager.isAccessibilityTriggerEnabled,
        )
        assertFalse(
            "Device admin trigger should be disabled by default",
            triggerManager.isDeviceAdminTriggerEnabled,
        )
    }

    @Test
    fun `test enabling and disabling triggers`() {
        // Test assistant trigger
        triggerManager.isAssistantTriggerEnabled = true
        assertTrue("Assistant trigger should be enabled", triggerManager.isAssistantTriggerEnabled)

        triggerManager.isAssistantTriggerEnabled = false
        assertFalse(
            "Assistant trigger should be disabled",
            triggerManager.isAssistantTriggerEnabled,
        )

        // Test quick tile trigger
        triggerManager.isQuickTileTriggerEnabled = false
        assertFalse(
            "Quick tile trigger should be disabled",
            triggerManager.isQuickTileTriggerEnabled,
        )

        triggerManager.isQuickTileTriggerEnabled = true
        assertTrue("Quick tile trigger should be enabled", triggerManager.isQuickTileTriggerEnabled)

        // Test accessibility trigger
        triggerManager.isAccessibilityTriggerEnabled = true
        assertTrue(
            "Accessibility trigger should be enabled",
            triggerManager.isAccessibilityTriggerEnabled,
        )

        // Test device admin trigger
        triggerManager.isDeviceAdminTriggerEnabled = true
        assertTrue(
            "Device admin trigger should be enabled",
            triggerManager.isDeviceAdminTriggerEnabled,
        )
    }

    @Test
    fun `test custom wake phrase`() {
        val defaultPhrase = "Hey Notably"
        assertEquals(
            "Default wake phrase should be correct",
            defaultPhrase,
            triggerManager.customWakePhrase,
        )

        val customPhrase = "Hello Notably"
        triggerManager.customWakePhrase = customPhrase
        assertEquals(
            "Custom wake phrase should be set",
            customPhrase,
            triggerManager.customWakePhrase,
        )
    }

    @Test
    fun `test hardware button combo`() {
        val defaultCombo = "power_volume_up"
        assertEquals(
            "Default button combo should be correct",
            defaultCombo,
            triggerManager.hardwareButtonCombo,
        )

        val customCombo = "triple_power"
        triggerManager.hardwareButtonCombo = customCombo
        assertEquals(
            "Custom button combo should be set",
            customCombo,
            triggerManager.hardwareButtonCombo,
        )
    }

    @Test
    fun `test isAnyTriggerEnabled`() {
        // With default settings (only quick tile enabled)
        assertTrue(
            "Should have at least one trigger enabled by default",
            triggerManager.isAnyTriggerEnabled(),
        )

        // Disable all triggers
        triggerManager.isQuickTileTriggerEnabled = false
        assertFalse("Should have no triggers enabled", triggerManager.isAnyTriggerEnabled())

        // Enable one trigger
        triggerManager.isAssistantTriggerEnabled = true
        assertTrue("Should have at least one trigger enabled", triggerManager.isAnyTriggerEnabled())
    }

    @Test
    fun `test isTriggerSourceEnabled`() {
        // Enable assistant trigger
        triggerManager.isAssistantTriggerEnabled = true

        assertTrue(
            "Assistant trigger should be enabled",
            triggerManager.isTriggerSourceEnabled(TriggerSource.ASSISTANT),
        )
        assertTrue(
            "Quick tile trigger should be enabled by default",
            triggerManager.isTriggerSourceEnabled(TriggerSource.QUICK_TILE),
        )
        assertFalse(
            "Accessibility trigger should be disabled",
            triggerManager.isTriggerSourceEnabled(TriggerSource.ACCESSIBILITY),
        )
        assertFalse(
            "Device admin trigger should be disabled",
            triggerManager.isTriggerSourceEnabled(TriggerSource.DEVICE_ADMIN),
        )
    }

    @Test
    fun `test getEnabledTriggerSources`() {
        // With default settings (only quick tile enabled)
        val enabledSources = triggerManager.getEnabledTriggerSources()
        assertEquals("Should have exactly one enabled trigger", 1, enabledSources.size)
        assertEquals("Quick tile should be enabled", TriggerSource.QUICK_TILE, enabledSources[0])

        // Enable more triggers
        triggerManager.isAssistantTriggerEnabled = true
        triggerManager.isAccessibilityTriggerEnabled = true

        val allEnabledSources = triggerManager.getEnabledTriggerSources()
        assertEquals("Should have three enabled triggers", 3, allEnabledSources.size)
        assertTrue(
            "Should contain assistant trigger",
            allEnabledSources.contains(TriggerSource.ASSISTANT),
        )
        assertTrue(
            "Should contain quick tile trigger",
            allEnabledSources.contains(TriggerSource.QUICK_TILE),
        )
        assertTrue(
            "Should contain accessibility trigger",
            allEnabledSources.contains(TriggerSource.ACCESSIBILITY),
        )
    }

    @Test
    fun `test getTriggerSummary`() {
        // With default settings
        val defaultSummary = triggerManager.getTriggerSummary()
        assertEquals(
            "Default summary should show one trigger",
            "1 trigger(s) enabled",
            defaultSummary,
        )

        // Enable more triggers
        triggerManager.isAssistantTriggerEnabled = true
        triggerManager.isAccessibilityTriggerEnabled = true

        val multipleSummary = triggerManager.getTriggerSummary()
        assertEquals(
            "Multiple summary should show three triggers",
            "3 trigger(s) enabled",
            multipleSummary,
        )

        // Disable all triggers
        triggerManager.isQuickTileTriggerEnabled = false
        triggerManager.isAssistantTriggerEnabled = false
        triggerManager.isAccessibilityTriggerEnabled = false

        val noneSummary = triggerManager.getTriggerSummary()
        assertEquals("No triggers summary should show zero", "0 trigger(s) enabled", noneSummary)
    }

    @Test
    fun `test resetToDefaults`() {
        // Change all settings
        triggerManager.isAssistantTriggerEnabled = true
        triggerManager.isQuickTileTriggerEnabled = false
        triggerManager.isAccessibilityTriggerEnabled = true
        triggerManager.isDeviceAdminTriggerEnabled = true
        triggerManager.customWakePhrase = "Custom Phrase"
        triggerManager.hardwareButtonCombo = "triple_power"

        // Reset to defaults
        triggerManager.resetToDefaults()

        // Verify defaults are restored
        assertFalse(
            "Assistant trigger should be reset to default",
            triggerManager.isAssistantTriggerEnabled,
        )
        assertTrue(
            "Quick tile trigger should be reset to default",
            triggerManager.isQuickTileTriggerEnabled,
        )
        assertFalse(
            "Accessibility trigger should be reset to default",
            triggerManager.isAccessibilityTriggerEnabled,
        )
        assertFalse(
            "Device admin trigger should be reset to default",
            triggerManager.isDeviceAdminTriggerEnabled,
        )
        assertEquals(
            "Wake phrase should be reset to default",
            "Hey Notably",
            triggerManager.customWakePhrase,
        )
        assertEquals(
            "Button combo should be reset to default",
            "power_volume_up",
            triggerManager.hardwareButtonCombo,
        )
    }

    @Test
    fun `test singleton pattern`() {
        val instance1 = QuickRecordTriggerManager.getInstance(context)
        val instance2 = QuickRecordTriggerManager.getInstance(context)

        assertSame("Should return the same instance", instance1, instance2)
    }
}
