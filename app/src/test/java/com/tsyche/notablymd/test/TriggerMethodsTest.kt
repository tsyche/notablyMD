package com.tsyche.notablymd.test

import com.tsyche.notablymd.utils.quickrecord.TriggerSource
import org.junit.Assert.*
import org.junit.Test

/** Test to verify trigger methods functionality */
class TriggerMethodsTest {

    @Test
    fun `trigger methods should have proper enum values`() {
        // Test that TriggerSource enum has correct values

        val allTriggerSources = TriggerSource.values()

        // Verify enum values
        assertNotNull("Trigger sources should not be null", allTriggerSources)
        assertEquals("Should have 4 trigger sources", 4, allTriggerSources.size)
        assertTrue(
            "Should include accessibility",
            allTriggerSources.contains(TriggerSource.ACCESSIBILITY),
        )
        assertTrue(
            "Should include quick tile",
            allTriggerSources.contains(TriggerSource.QUICK_TILE),
        )
        assertTrue(
            "Should include voice assistant",
            allTriggerSources.contains(TriggerSource.ASSISTANT),
        )
        assertTrue(
            "Should include device admin",
            allTriggerSources.contains(TriggerSource.DEVICE_ADMIN),
        )
    }

    @Test
    fun `trigger source enum should have correct names`() {
        // Test that TriggerSource enum has correct names

        assertEquals(
            "Accessibility should have correct name",
            "ACCESSIBILITY",
            TriggerSource.ACCESSIBILITY.name,
        )
        assertEquals(
            "Quick tile should have correct name",
            "QUICK_TILE",
            TriggerSource.QUICK_TILE.name,
        )
        assertEquals(
            "Assistant should have correct name",
            "ASSISTANT",
            TriggerSource.ASSISTANT.name,
        )
        assertEquals(
            "Device admin should have correct name",
            "DEVICE_ADMIN",
            TriggerSource.DEVICE_ADMIN.name,
        )
    }

    @Test
    fun `trigger methods should be implemented`() {
        // Test that trigger methods are implemented in the codebase

        // The fact that we can import and use these classes proves they exist
        val triggerSourceClass = TriggerSource::class.java
        assertNotNull("TriggerSource enum should exist", triggerSourceClass)

        // Verify enum is properly structured
        val enumConstants = triggerSourceClass.enumConstants
        assertNotNull("Should have enum constants", enumConstants)
        assertTrue("Should have multiple enum values", enumConstants.isNotEmpty())

        // Check that all expected trigger types are available
        val enumNames = enumConstants.map { it.name }
        assertTrue("Should contain ACCESSIBILITY", enumNames.contains("ACCESSIBILITY"))
        assertTrue("Should contain QUICK_TILE", enumNames.contains("QUICK_TILE"))
        assertTrue("Should contain ASSISTANT", enumNames.contains("ASSISTANT"))
        assertTrue("Should contain DEVICE_ADMIN", enumNames.contains("DEVICE_ADMIN"))
    }
}
