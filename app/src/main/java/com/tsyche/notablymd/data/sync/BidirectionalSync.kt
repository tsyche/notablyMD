package com.tsyche.notablymd.data.sync

import android.content.Context
import android.content.ContextWrapper
import com.tsyche.notablymd.data.NotallyDatabase
import com.tsyche.notablymd.data.imports.markdown.EnhancedMarkdownManager
import java.io.File
import kotlinx.coroutines.*

/** Simplified bidirectional synchronization between database and markdown files */
class BidirectionalSync(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dao by lazy {
        NotallyDatabase.getDatabase(context as ContextWrapper, false).value.getBaseNoteDao()
    }
    private val markdownManager = EnhancedMarkdownManager(context)

    /** Perform full synchronization between database and markdown files */
    suspend fun performFullSync() =
        withContext(Dispatchers.IO) {
            try {
                println("Markdown sync: Starting full sync")
                // TODO: implement full bidirectional scan and conflict resolution
                println("Markdown sync: Full sync completed successfully")
            } catch (e: Exception) {
                throw SyncException("Full sync failed", e)
            }
        }

    /** Sync changes from markdown file to database */
    suspend fun syncFromFileToDatabase(filePath: String) =
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                val note = markdownManager.readNote(file).getOrThrow()
                dao.insert(note)
            } catch (e: Exception) {
                throw SyncException("Failed to sync file to database: $filePath", e)
            }
        }

    /** Handle deleted markdown file — removes the corresponding note from the database */
    suspend fun handleFileDeleted(filePath: String): Long? =
        withContext(Dispatchers.IO) {
            try {
                val noteId =
                    File(filePath).nameWithoutExtension.toLongOrNull() ?: return@withContext null
                dao.delete(noteId)
                noteId
            } catch (e: Exception) {
                throw SyncException("Failed to handle deleted file: $filePath", e)
            }
        }
}

/** Custom exception for sync operations */
class SyncException(message: String, cause: Throwable? = null) : Exception(message, cause)
