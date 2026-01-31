package com.tsyche.notablymd.presentation.service

import android.content.Context
import android.content.ContextWrapper
import com.tsyche.notablymd.data.NotallyDatabase
import com.tsyche.notablymd.data.model.BaseNote
import com.tsyche.notablymd.data.model.Folder
import com.tsyche.notablymd.data.model.NoteViewMode
import com.tsyche.notablymd.data.model.Type
import com.tsyche.notablymd.presentation.viewmodel.preference.NotablyMDPreferences
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Voice Note Creator
 *
 * Handles the creation of notes from voice transcriptions. Integrates with the existing NotablyMD
 * database and preference system.
 */
class VoiceNoteCreator(private val context: Context) {

    private val database = NotallyDatabase.getDatabase(context as ContextWrapper, false).value
    private val preferences = NotablyMDPreferences.getInstance(context)

    /** Creates a new note from voice transcription */
    suspend fun createNoteFromTranscription(transcription: String, autoSave: Boolean = true): Long =
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()

            // Generate title from first few words of transcription
            val title = generateTitleFromTranscription(transcription)

            // Create BaseNote object
            val note =
                BaseNote(
                    id = 0L, // Database will generate ID
                    type = Type.NOTE,
                    folder = Folder.NOTES,
                    color = BaseNote.COLOR_DEFAULT,
                    title = title,
                    pinned = false,
                    timestamp = timestamp,
                    modifiedTimestamp = timestamp,
                    labels = emptyList(),
                    body = formatTranscription(transcription),
                    spans = emptyList(), // Could add formatting later
                    items = emptyList(),
                    images = emptyList(),
                    files = emptyList(),
                    audios = emptyList(),
                    reminders = emptyList(),
                    viewMode = NoteViewMode.EDIT,
                )

            // Save to database
            val noteId = database.getBaseNoteDao().insert(note)

            // Update last note preference for widget
            setLastVoiceNote(context, noteId, transcription)

            noteId
        }

    /** Gets the last created voice note for widget preview */
    suspend fun getLastVoiceNote(): Pair<Long, String>? =
        withContext(Dispatchers.IO) {
            try {
                // Get the most recent note that was created via voice
                // For now, we'll just get the most recent note
                // In a real implementation, you might add a flag to track voice-created notes
                val recentNotes = database.getBaseNoteDao().getAllNotes().take(5)

                recentNotes.firstOrNull()?.let { note -> Pair(note.id, note.body ?: "") }
            } catch (e: Exception) {
                null
            }
        }

    /** Updates an existing note with new transcription */
    suspend fun updateNoteWithTranscription(noteId: Long, transcription: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val existingNote = database.getBaseNoteDao().getAllNotes().find { it.id == noteId }
                existingNote?.let { note ->
                    // Delete the old note and create a new one with updated transcription
                    database.getBaseNoteDao().delete(noteId)
                    val updatedNote =
                        note.copy(
                            body = transcription,
                            modifiedTimestamp = System.currentTimeMillis(),
                        )
                    database.getBaseNoteDao().insert(updatedNote)
                    true
                } ?: false
            } catch (e: Exception) {
                false
            }
        }

    /** Deletes a voice note */
    suspend fun deleteVoiceNote(noteId: Long): Boolean =
        withContext(Dispatchers.IO) {
            try {
                database.getBaseNoteDao().delete(noteId)
                true
            } catch (e: Exception) {
                false
            }
        }

    /** Gets all voice notes (for management purposes) */
    suspend fun getAllVoiceNotes(limit: Int = 50): List<BaseNote> =
        withContext(Dispatchers.IO) {
            try {
                database.getBaseNoteDao().getAllNotes().take(limit)
            } catch (e: Exception) {
                emptyList()
            }
        }

    /** Generates a title from transcription text */
    internal fun generateTitleFromTranscription(transcription: String): String {
        val words = transcription.trim().split("\\s+".toRegex())

        return when {
            words.isEmpty() -> "Voice Note"
            words.size <= 5 -> transcription.trim()
            else -> words.take(5).joinToString(" ") + "..."
        }
    }

    /** Formats transcription with basic punctuation enhancement */
    fun formatTranscription(transcription: String): String {
        return transcription
            .trim()
            .replace(Regex("\\b(i|we|you|they|he|she|it)\\b")) { it.value.uppercase() }
            .replace(Regex("\\.$")) { "" } // Remove trailing period if exists
            .let { text ->
                if (!text.endsWith('.')) {
                    "$text."
                } else {
                    text
                }
            }
    }

    /** Checks if transcription is meaningful (not just silence or noise) */
    fun isValidTranscription(transcription: String): Boolean {
        val cleaned = transcription.trim()

        return when {
            cleaned.isEmpty() -> false
            cleaned.length < 3 -> false
            cleaned.matches(Regex("^(uh|um|ah)+\\s*$")) -> false
            cleaned.matches(Regex("^[^\\w]*$")) -> false // Only punctuation
            else -> true
        }
    }

    /** Gets transcription confidence score (simplified implementation) */
    fun getTranscriptionConfidence(transcription: String): Float {
        val cleaned = transcription.trim()

        return when {
            cleaned.isEmpty() -> 0.0f
            cleaned.length < 5 -> 0.3f
            cleaned.contains("?") || cleaned.contains("!") -> 0.8f
            cleaned.length > 20 -> 0.9f
            else -> 0.7f
        }
    }

    /** Suggests labels for the transcription based on content */
    suspend fun suggestLabels(transcription: String): List<String> =
        withContext(Dispatchers.IO) {
            val existingLabels = database.getLabelDao().getAll().value ?: emptyList()
            val transcriptionLower = transcription.lowercase()

            existingLabels
                .filter { label -> transcriptionLower.contains(label.lowercase()) }
                .take(3) // Limit to 3 suggested labels
        }
}

/** Extension function for preferences */
/** Sets the last voice note for widget preview */
internal fun setLastVoiceNote(context: Context, noteId: Long, transcription: String) {
    val prefs = context.getSharedPreferences("voice_widget_prefs", Context.MODE_PRIVATE)
    prefs
        .edit()
        .putLong("last_voice_note_id", noteId)
        .putString("last_voice_note_text", transcription.take(100))
        .apply()
}

/** Extension function to get last voice note */
fun getLastVoiceNote(context: Context): Pair<Long, String>? {
    val prefs = context.getSharedPreferences("voice_widget_prefs", Context.MODE_PRIVATE)
    val noteId = prefs.getLong("last_voice_note_id", -1L)
    val text = prefs.getString("last_voice_note_text", "")

    return if (noteId != -1L && !text.isNullOrEmpty()) {
        Pair(noteId, text)
    } else {
        null
    }
}
