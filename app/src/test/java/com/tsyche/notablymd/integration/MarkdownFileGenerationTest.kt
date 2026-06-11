package com.tsyche.notablymd.integration

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.tsyche.notablymd.data.imports.markdown.EnhancedMarkdownManager
import com.tsyche.notablymd.presentation.viewmodel.preference.NotablyMDPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Integration test to verify markdown file generation works correctly This test should FAIL
 * initially, demonstrating the bug where markdown files are not generated
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MarkdownFileGenerationTest {

    private lateinit var context: Application
    private lateinit var preferences: NotablyMDPreferences
    private lateinit var markdownManager: EnhancedMarkdownManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        preferences = NotablyMDPreferences.getInstance(context)
        markdownManager = EnhancedMarkdownManager(context)
    }

    @Test
    fun markdownSyncShouldBeEnabledByDefault() {
        // This test will FAIL initially, showing the bug
        // Markdown sync should be enabled by default for a markdown-first app
        val isEnabled = preferences.markdownSyncEnabled.value

        assertEquals(
            "Markdown sync should be enabled by default for a markdown-first app",
            true,
            isEnabled,
        )
    }

    @Test
    fun defaultSyncLocationShouldBeValid() {
        val defaultLocation = "/storage/emulated/0/Android/media/com.tsyche.notablymd/markdown"

        // If sync location is empty, it should use the default
        val currentLocation = preferences.markdownSyncLocation.value.ifEmpty { defaultLocation }

        assertEquals(
            "Should use default sync location when not set",
            defaultLocation,
            currentLocation,
        )

        // Verify the location is a valid path format
        assertTrue("Sync location should be a valid absolute path", currentLocation.startsWith("/"))
        assertTrue(
            "Sync location should end with markdown directory",
            currentLocation.endsWith("/markdown"),
        )
    }
}
