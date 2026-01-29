package com.tsyche.notablymd.presentation.viewmodel.preference

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MarkdownSyncPreferencesTest {

    @Mock private lateinit var mockSharedPreferences: SharedPreferences

    @Mock private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var context: Context
    private lateinit var preferences: NotablyMDPreferences

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = RuntimeEnvironment.getApplication()

        // Use real SharedPreferences for more realistic testing
        preferences = NotablyMDPreferences.getInstance(context)
    }

    @Test
    fun markdownSyncEnabledShouldDefaultToFalse() {
        // Test that markdown sync enabled defaults to false
        val isEnabled = preferences.markdownSyncEnabled.value

        assertEquals("Markdown sync should be disabled by default", false, isEnabled)
    }

    @Test
    fun markdownSyncEnabledShouldBeSettable() {
        // Test that markdown sync enabled can be set to true
        preferences.markdownSyncEnabled.save(true)

        val isEnabled = preferences.markdownSyncEnabled.value

        assertEquals("Markdown sync should be enabled when set", true, isEnabled)
    }

    @Test
    fun markdownSyncLocationShouldBeSettable() {
        // Test that markdown sync location can be set
        val testLocation = "/storage/emulated/0/Documents/Sync"
        preferences.markdownSyncLocation.save(testLocation)

        val location = preferences.markdownSyncLocation.value

        assertEquals("Markdown sync location should be settable", testLocation, location)
    }

    @Test
    fun markdownSyncLocationShouldAcceptUriString() {
        // Test that markdown sync location can accept URI strings
        val testUri =
            "content://com.android.externalstorage.documents/tree/primary%3ADocuments%2FSync"
        preferences.markdownSyncLocation.save(testUri)

        val location = preferences.markdownSyncLocation.value

        assertEquals("Markdown sync location should accept URI strings", testUri, location)
    }

    @Test
    fun markdownSyncPreferencesShouldPersist() {
        // Test that preferences persist across instances
        val testLocation = "/storage/emulated/0/Documents/TestSync"

        // Set value
        preferences.markdownSyncLocation.save(testLocation)
        preferences.markdownSyncEnabled.save(true)

        // Create new instance to test persistence
        val newPreferences = NotablyMDPreferences.getInstance(context)

        assertEquals(
            "Location should persist",
            testLocation,
            newPreferences.markdownSyncLocation.value,
        )
        assertEquals("Enabled state should persist", true, newPreferences.markdownSyncEnabled.value)
    }

    @Test
    fun markdownSyncPreferencesShouldAcceptEmptyString() {
        // Test that empty string is handled correctly
        preferences.markdownSyncLocation.save("")

        val location = preferences.markdownSyncLocation.value

        assertEquals("Should accept empty string", "", location)
    }

    @Test
    fun markdownSyncPreferencesShouldHandleSpecialCharacters() {
        // Test that special characters in paths are handled correctly
        val specialPath = "/storage/emulated/0/Documents/Sync-Test_123"
        preferences.markdownSyncLocation.save(specialPath)

        val location = preferences.markdownSyncLocation.value

        assertEquals("Should handle special characters", specialPath, location)
    }
}
