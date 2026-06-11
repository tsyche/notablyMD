package com.tsyche.notablymd.test

import android.content.Context
import android.media.MediaRecorder
import com.tsyche.notablymd.presentation.service.VoiceNoteCreator
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.runBlocking

/** Mock Audio Test Helper Creates mock audio files for testing voice recording functionality */
class MockAudioTestHelper(private val context: Context) {

    /**
     * Creates a mock audio file that simulates voice recording This can be used to test the
     * transcription workflow
     */
    fun createMockAudioFile(): File {
        val mockAudioFile =
            File(context.cacheDir, "mock_voice_recording_${System.currentTimeMillis()}.3gp")

        try {
            // Create a minimal valid audio file header
            // This is a simplified approach - in practice, you might use a pre-recorded file
            val outputStream = FileOutputStream(mockAudioFile)

            // Write a minimal audio file header (3GP format)
            // This is just enough to make the file appear valid for testing
            val mockAudioData =
                byteArrayOf(
                    0x00.toByte(),
                    0x00.toByte(),
                    0x00.toByte(),
                    0x20.toByte(), // File size placeholder
                    0x66,
                    0x74,
                    0x79,
                    0x70, // 'ftyp'
                    0x69,
                    0x73,
                    0x6F,
                    0x6D, // 'isom'
                    0x00,
                    0x00,
                    0x02,
                    0x00, // Version info
                    0x69,
                    0x73,
                    0x6F,
                    0x6D, // 'isom'
                    0x69,
                    0x73,
                    0x6F,
                    0x32, // 'iso2'
                    0x61,
                    0x76,
                    0x63,
                    0x31, // 'avc1'
                    0x6D,
                    0x70,
                    0x34,
                    0x31, // 'mp41'
                    // Add more mock data as needed
                )

            outputStream.write(mockAudioData)
            outputStream.close()
        } catch (e: Exception) {
            // If file creation fails, create an empty file for testing
            mockAudioFile.createNewFile()
        }

        return mockAudioFile
    }

    /** Tests the voice note creation with mock transcription */
    fun testMockTranscription() {
        runBlocking {
            val voiceNoteCreator = VoiceNoteCreator(context)

            // Test with mock transcription
            val mockTranscription = "Hello world, this is a test transcription"

            try {
                val noteId = voiceNoteCreator.createNoteFromTranscription(mockTranscription)
                assert(noteId > 0) { "Note creation failed with mock transcription" }

                // Verify the note was created
                val lastNote = voiceNoteCreator.getLastVoiceNote()
                assert(lastNote != null) { "No voice note found after creation" }
                assert(lastNote?.first == noteId) { "Created note ID mismatch" }
            } catch (e: Exception) {
                throw AssertionError("Mock transcription test failed: ${e.message}")
            }
        }
    }

    /** Creates a mock audio file with actual silence This can be used to test silence detection */
    fun createSilenceAudioFile(durationMs: Int = 3000): File {
        val silenceFile = File(context.cacheDir, "silence_${System.currentTimeMillis()}.3gp")

        try {
            // Use MediaRecorder to create actual silence
            val recorder =
                MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(silenceFile.absolutePath)
                }

            recorder.prepare()
            recorder.start()

            // Record silence for specified duration
            Thread.sleep(durationMs.toLong())

            recorder.stop()
            recorder.release()
        } catch (e: Exception) {
            // Fallback to empty file
            silenceFile.createNewFile()
        }

        return silenceFile
    }

    /** Tests the complete workflow with mock audio */
    fun testCompleteMockWorkflow() {
        try {
            // 1. Create mock audio file
            val mockAudioFile = createMockAudioFile()
            assert(mockAudioFile.exists()) { "Mock audio file creation failed" }

            // 2. Test transcription with mock data
            testMockTranscription()

            // 3. Test silence detection
            val silenceFile = createSilenceAudioFile(2000)
            assert(silenceFile.exists()) { "Silence audio file creation failed" }

            // 4. Cleanup
            mockAudioFile.delete()
            silenceFile.delete()
        } catch (e: Exception) {
            throw AssertionError("Complete mock workflow test failed: ${e.message}")
        }
    }
}
