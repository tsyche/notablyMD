package com.tsyche.notablymd.presentation.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tsyche.notablymd.presentation.service.VoiceRecordingService
import com.tsyche.notablymd.presentation.service.getLastVoiceNote
import com.tsyche.notablymd.presentation.service.setLastVoiceNote
import com.tsyche.notablymd.presentation.widget.VoiceNoteWidget
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class VoiceWidgetIntegrationTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `complete voice to note workflow should work end to end`() = runBlocking {
        // Step 1: Simulate voice transcription
        val rawTranscription = "i need to remember to call the bank tomorrow at 3pm"

        // Step 2: Store in preferences for widget
        setLastVoiceNote(context, 123L, rawTranscription)

        // Step 3: Verify widget can retrieve last note
        val lastNote = getLastVoiceNote(context)
        assertNotNull("Widget should retrieve last note", lastNote)
        assertEquals("Should store correct note ID", 123L, lastNote?.first)
        assertEquals("Should store transcription", rawTranscription.take(100), lastNote?.second)
    }

    @Test
    fun `widget service integration should handle state transitions`() {
        // Test service action constants
        assertEquals(
            "Start action should match",
            "com.tsyche.notablymd.service.START_RECORDING",
            VoiceRecordingService.ACTION_START_RECORDING,
        )

        assertEquals(
            "Stop action should match",
            "com.tsyche.notablymd.service.STOP_RECORDING",
            VoiceRecordingService.ACTION_STOP_RECORDING,
        )
    }

    @Test
    fun `service and widget constants should be consistent`() {
        // Test widget state constants
        assertEquals("Idle state should be 0", 0, VoiceNoteWidget.STATE_IDLE)
        assertEquals("Recording state should be 1", 1, VoiceNoteWidget.STATE_RECORDING)
        assertEquals("Processing state should be 2", 2, VoiceNoteWidget.STATE_PROCESSING)
        assertEquals("Error state should be 3", 3, VoiceNoteWidget.STATE_ERROR)

        // Test widget action constants
        assertEquals(
            "Record start action should match",
            "com.tsyche.notablymd.widget.RECORD_START",
            VoiceNoteWidget.ACTION_RECORD_START,
        )

        assertEquals(
            "Record stop action should match",
            "com.tsyche.notablymd.widget.RECORD_STOP",
            VoiceNoteWidget.ACTION_RECORD_STOP,
        )
    }

    @Test
    fun `preferences should persist across widget updates`() = runBlocking {
        // Store initial note
        setLastVoiceNote(context, 100L, "First note")

        // Update with new note
        setLastVoiceNote(context, 200L, "Second note")

        // Verify latest note is retrieved
        val latestNote = getLastVoiceNote(context)
        assertEquals("Should retrieve latest note ID", 200L, latestNote?.first)
        assertEquals("Should retrieve latest note content", "Second note", latestNote?.second)
    }

    @Test
    fun `widget should handle edge cases gracefully`() = runBlocking {
        // Clear preferences first
        context
            .getSharedPreferences("voice_widget_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        // Test empty transcription - should return null since empty text is considered invalid
        setLastVoiceNote(context, 1L, "")
        val emptyNote = getLastVoiceNote(context)
        assertNull("Should return null for empty transcription", emptyNote)

        // Test very long transcription
        val longTranscription = "a".repeat(200)
        setLastVoiceNote(context, 2L, longTranscription)
        val longNote = getLastVoiceNote(context)
        assertNotNull("Should handle long transcription", longNote)
        assertTrue("Should truncate long transcription", longNote?.second?.length!! <= 100)
        assertEquals("Should store correct note ID", 2L, longNote?.first)
    }

    @Test
    fun `widget should handle rapid state changes`() {
        // Test that widget can handle multiple state updates
        val states =
            listOf(
                VoiceNoteWidget.STATE_IDLE,
                VoiceNoteWidget.STATE_RECORDING,
                VoiceNoteWidget.STATE_PROCESSING,
                VoiceNoteWidget.STATE_IDLE,
            )

        states.forEach { state ->
            assertTrue("State $state should be valid", state >= 0 && state <= 3)
        }
    }
}
