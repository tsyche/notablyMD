package com.tsyche.notablymd.presentation.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class VoiceNoteCreatorTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `setLastVoiceNote should store note in preferences`() = runBlocking {
        val noteId = 123L
        val transcription = "Test voice note"

        setLastVoiceNote(context, noteId, transcription)

        val result = getLastVoiceNote(context)
        assertNotNull("Should store and retrieve note", result)
        assertEquals("Should store correct note ID", noteId, result?.first)
        assertEquals(
            "Should store truncated transcription",
            transcription.take(100),
            result?.second,
        )
    }

    @Test
    fun `getLastVoiceNote should return null when no notes exist`() = runBlocking {
        // Clear any existing notes
        context
            .getSharedPreferences("voice_widget_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        val result = getLastVoiceNote(context)

        assertNull("Should return null when no notes exist", result)
    }

    @Test
    fun `setLastVoiceNote should truncate long transcriptions`() = runBlocking {
        val noteId = 123L
        val longTranscription =
            "This is a very long transcription that exceeds the 100 character limit and should be truncated when stored in preferences for the widget preview"

        setLastVoiceNote(context, noteId, longTranscription)

        val result = getLastVoiceNote(context)
        assertNotNull("Should store truncated note", result)
        assertTrue("Should truncate to 100 characters", result?.second?.length!! <= 100)
    }
}
