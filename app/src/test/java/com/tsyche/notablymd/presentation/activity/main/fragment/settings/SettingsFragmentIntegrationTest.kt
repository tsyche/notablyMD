package com.tsyche.notablymd.presentation.activity.main.fragment.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tsyche.notablymd.presentation.viewmodel.preference.NotablyMDPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SettingsFragmentIntegrationTest {

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
    fun markdownSyncPreferences_shouldHaveRequiredProperties() {
        // This test would have caught the missing titleResId crash!
        // The setupMarkdownSync method calls PreferenceBinding.setup() which requires titleResId

        val enabledPref = preferences.markdownSyncEnabled
        val locationPref = preferences.markdownSyncLocation

        // CRITICAL: These assertions would fail if titleResId is null/missing
        // This is exactly what caused the crash in SettingsFragment.setupMarkdownSync()
        assert(enabledPref.titleResId != null) {
            "markdownSyncEnabled must have titleResId - this would cause SettingsFragment crash!"
        }
        assert(locationPref.titleResId != null) {
            "markdownSyncLocation must have titleResId - this would cause SettingsFragment crash!"
        }

        // Verify the titleResId points to actual string resources (not invalid IDs)
        val enabledTitle = context.getString(enabledPref.titleResId!!)
        val locationTitle = context.getString(locationPref.titleResId!!)

        assert(enabledTitle.isNotEmpty()) {
            "markdownSyncEnabled title should not be empty - invalid resource ID!"
        }
        assert(locationTitle.isNotEmpty()) {
            "markdownSyncLocation title should not be empty - invalid resource ID!"
        }

        // Verify they're the correct strings
        assert(enabledTitle == "Save notes as Markdown files") {
            "markdownSyncEnabled should have correct title"
        }
        assert(locationTitle == "Markdown folder") {
            "markdownSyncLocation should have correct title"
        }
    }

    @Test
    fun markdownSyncPreferences_shouldHaveCorrectDefaultValues() {
        // Test that preferences have correct default values
        val enabledPref = preferences.markdownSyncEnabled
        val locationPref = preferences.markdownSyncLocation

        // These should not crash and should return expected defaults
        val isEnabled = enabledPref.value
        val location = locationPref.value

        println("DEBUG: isEnabled = $isEnabled, location = $location")

        assert(isEnabled == true) {
            "markdownSyncEnabled should default to true, but was $isEnabled"
        }
        assert(location == "") { "markdownSyncLocation should default to empty string" }
    }

    @Test
    fun markdownSyncPreferences_shouldBeSettable() {
        // Test that preferences can be set without crashing
        val enabledPref = preferences.markdownSyncEnabled
        val locationPref = preferences.markdownSyncLocation

        // These operations should not crash
        enabledPref.save(true)
        locationPref.save("/test/path")

        assert(enabledPref.value == true) { "markdownSyncEnabled should be settable" }
        assert(locationPref.value == "/test/path") { "markdownSyncLocation should be settable" }

        // Clean up
        enabledPref.save(false)
        locationPref.save("")
    }

    @Test
    fun markdownSyncPreferences_setupSimulation() {
        // Simulate what SettingsFragment.setupMarkdownSync() does
        // This would have caught the crash before it reached production

        val enabledPref = preferences.markdownSyncEnabled
        val locationPref = preferences.markdownSyncLocation

        try {
            // This simulates the PreferenceBinding.setup() call that was crashing
            // The setup method internally calls preference.titleResId!!
            val titleResId = enabledPref.titleResId!!
            val locationTitleResId = locationPref.titleResId!!

            // These would crash if titleResId was null (which was the original bug)
            context.getString(titleResId)
            context.getString(locationTitleResId)

            // If we get here, the preferences are properly configured
            assert(true) {
                "Markdown sync preferences are properly configured for SettingsFragment"
            }
        } catch (e: NullPointerException) {
            throw AssertionError(
                "SettingsFragment would crash! Missing titleResId in markdown sync preferences. " +
                    "This is the exact bug that caused the production crash.",
                e,
            )
        } catch (e: Exception) {
            throw AssertionError(
                "SettingsFragment would crash! Invalid titleResId in markdown sync preferences.",
                e,
            )
        }
    }
}
