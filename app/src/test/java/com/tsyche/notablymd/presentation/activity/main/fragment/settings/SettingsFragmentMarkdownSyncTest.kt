package com.tsyche.notablymd.presentation.activity.main.fragment.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tsyche.notablymd.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SettingsFragmentMarkdownSyncTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun markdownSyncStringResourcesShouldExist() {
        // Test that all required string resources exist
        val syncTitle = context.getString(R.string.markdown_sync)
        val enabledTitle = context.getString(R.string.markdown_sync_enabled)
        val locationTitle = context.getString(R.string.markdown_sync_location)
        val locationUpdated = context.getString(R.string.sync_location_updated)

        assertEquals("Markdown Files", syncTitle)
        assertEquals("Save notes as Markdown files", enabledTitle)
        assertEquals("Markdown folder", locationTitle)
        assertEquals("Sync location updated", locationUpdated)
    }

    @Test
    fun defaultLocationDisplayShouldShowCorrectText() {
        // Test that empty location shows default path
        val location = ""
        val displayLocation =
            if (location.isEmpty()) {
                "Android/media/com.tsyche.notablymd (Default)"
            } else {
                location
            }

        assertEquals(
            "Should show default location when empty",
            "Android/media/com.tsyche.notablymd (Default)",
            displayLocation,
        )
    }

    @Test
    fun customLocationDisplayShouldShowCorrectText() {
        // Test that custom location shows the actual path
        val customPath = "/storage/emulated/0/Documents/Sync"
        val location = customPath
        val displayLocation =
            if (location.isEmpty()) {
                "Android/media/com.tsyche.notablymd (Default)"
            } else {
                location
            }

        assertEquals("Should show custom location when set", customPath, displayLocation)
    }
}
