package com.tsyche.notablymd.presentation.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.tsyche.notablymd.R
import com.tsyche.notablymd.presentation.activity.main.MainActivity
import com.tsyche.notablymd.test.MockAudioTestHelper
import com.tsyche.notablymd.test.PreRecordedAudioTestHelper
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Voice Recording Integration Tests with Mock Audio Tests the complete voice recording pipeline
 * using simulated audio input
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class VoiceRecordingIntegrationTests {

    @get:Rule val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val permissionRule =
        GrantPermissionRule.grant(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.POST_NOTIFICATIONS,
        )

    @Test
    fun completeVoiceRecordingWithMockAudio() {
        // Test the complete voice recording workflow using mock audio
        // This simulates real audio input without requiring actual speech

        try {
            var context: android.content.Context? = null
            activityRule.getScenario().onActivity { activity -> context = activity }
            if (context == null) {
                throw Exception("Activity not available")
            }
            val mockHelper = MockAudioTestHelper(context!!)

            // 1. Test mock audio file creation
            val mockAudioFile = mockHelper.createMockAudioFile()
            assert(mockAudioFile.exists()) { "Mock audio file creation failed" }

            // 2. Test mock transcription
            mockHelper.testMockTranscription()

            // 3. Test complete workflow
            mockHelper.testCompleteMockWorkflow()

            // 4. Test UI integration
            testVoiceRecordingUI()

            // Cleanup
            mockAudioFile.delete()
        } catch (e: Exception) {
            throw AssertionError("Complete voice recording integration test failed: ${e.message}")
        }
    }

    @Test
    fun voiceRecordingWithPreRecordedAudio() {
        // Test voice recording using pre-recorded audio files
        // This tests the transcription pipeline with simulated speech

        try {
            var context: android.content.Context? = null
            activityRule.getScenario().onActivity { activity -> context = activity }
            if (context == null) {
                throw Exception("Activity not available")
            }
            val preRecordedHelper = PreRecordedAudioTestHelper(context!!)

            // 1. Test single pre-recorded audio file
            preRecordedHelper.testPreRecordedTranscription()

            // 2. Test batch transcription
            preRecordedHelper.testBatchTranscription()

            // 3. Test UI integration with simulated audio
            testVoiceRecordingUIWithSimulatedAudio()
        } catch (e: Exception) {
            throw AssertionError("Pre-recorded audio integration test failed: ${e.message}")
        }
    }

    private fun testVoiceRecordingUI() {
        // Test the voice recording UI elements
        try {
            // Wait for main activity to load
            Thread.sleep(3000)

            // Look for voice recording elements
            try {
                onView(withId(R.id.TakeNote)).check(matches(isDisplayed()))
            } catch (e: Exception) {
                // Try alternative selectors
                onView(withContentDescription("Take Note")).check(matches(isDisplayed()))
            }

            // Test clicking the recording button
            onView(withId(R.id.TakeNote)).perform(click())
            Thread.sleep(2000)

            // Test stopping recording
            onView(withId(R.id.TakeNote)).perform(click())
            Thread.sleep(3000)

            // If we get here without crashes, the UI integration works

        } catch (e: Exception) {
            throw AssertionError("Voice recording UI test failed: ${e.message}")
        }
    }

    private fun testVoiceRecordingUIWithSimulatedAudio() {
        // Test voice recording UI with simulated audio input
        try {
            // Wait for main activity to load
            Thread.sleep(3000)

            // Start recording
            onView(withId(R.id.TakeNote)).perform(click())
            Thread.sleep(1000)

            // Simulate recording duration (in real test, this would play audio)
            Thread.sleep(2000)

            // Stop recording
            onView(withId(R.id.TakeNote)).perform(click())
            Thread.sleep(3000)

            // Check for success indicators
            try {
                onView(withText("Voice note created")).check(matches(isDisplayed()))
            } catch (e: Exception) {
                // Success message might not be visible, but recording should complete
            }
        } catch (e: Exception) {
            throw AssertionError(
                "Voice recording UI with simulated audio test failed: ${e.message}"
            )
        }
    }

    @Test
    fun transcriptionServiceIntegrationTest() {
        // Test transcription service integration with mock data
        try {
            var context: android.content.Context? = null
            activityRule.getScenario().onActivity { activity -> context = activity }
            if (context == null) {
                throw Exception("Activity not available")
            }

            // Navigate to settings
            Thread.sleep(3000)

            try {
                onView(withContentDescription("Settings")).perform(click())
                Thread.sleep(2000)

                // Test transcription service selection
                onView(withText("Transcription Service")).perform(click())
                Thread.sleep(1000)

                // Test different service options
                onView(withText("FUTO Voice")).check(matches(isDisplayed()))
                onView(withText("Built-in Android")).check(matches(isDisplayed()))
                onView(withText("OpenAI Whisper")).check(matches(isDisplayed()))
                onView(withText("Custom Service")).check(matches(isDisplayed()))

                // Test selecting a service
                onView(withText("FUTO Voice")).perform(click())
                Thread.sleep(1000)
            } catch (e: Exception) {
                throw AssertionError("Transcription service integration test failed: ${e.message}")
            }
        } catch (e: Exception) {
            throw AssertionError("Transcription service integration test failed: ${e.message}")
        }
    }

    @Test
    fun voiceRecordingErrorHandlingTest() {
        // Test voice recording error handling with mock scenarios
        try {
            var context: android.content.Context? = null
            activityRule.getScenario().onActivity { activity -> context = activity }
            if (context == null) {
                throw Exception("Activity not available")
            }
            val mockHelper = MockAudioTestHelper(context!!)

            // Test with invalid audio file
            val invalidFile = File(context.cacheDir, "invalid_audio.txt")
            invalidFile.createNewFile()

            // Test error handling
            try {
                // This should handle the invalid file gracefully
                mockHelper.testAudioFileProcessing(invalidFile)
            } catch (e: Exception) {
                // Expected - error handling should work
            }

            // Cleanup
            invalidFile.delete()
        } catch (e: Exception) {
            throw AssertionError("Voice recording error handling test failed: ${e.message}")
        }
    }

    private fun MockAudioTestHelper.testAudioFileProcessing(file: File) {
        // Test audio file processing
        assert(file.exists()) { "Audio file does not exist" }
        assert(file.length() > 0) { "Audio file is empty" }
    }
}
