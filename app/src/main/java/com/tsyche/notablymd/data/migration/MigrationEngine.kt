package com.tsyche.notablymd.data.migration

import android.content.Context
import com.tsyche.notablymd.data.NotallyDatabase
import com.tsyche.notablymd.data.model.BaseNote
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/** Handles migration of notes from internal format to markdown files */
class MigrationEngine(private val context: Context, private val database: NotallyDatabase) {

    private val _progress: MutableStateFlow<MigrationProgress?> = MutableStateFlow(null)
    val progress: Flow<MigrationProgress?> = _progress.asStateFlow()

    private var isMigrationActive = false
    private var migrationJob: kotlinx.coroutines.Job? = null

    /** Start migration process */
    suspend fun startMigration(config: MigrationConfig): MigrationResult {
        return withContext(Dispatchers.IO) {
            if (isMigrationActive) {
                throw IllegalStateException("Migration is already in progress")
            }

            isMigrationActive = true
            val startTime = System.currentTimeMillis()

            try {
                // Get all notes to migrate
                val notes = getNotesForMigration(config)
                val totalNotes = notes.size

                // Initialize progress
                _progress.value =
                    MigrationProgress(
                        totalNotes = totalNotes,
                        processedNotes = 0,
                        currentNoteTitle = null,
                        status = MigrationProgress.MigrationStatus.PREPARING,
                        startTime = startTime,
                    )

                // Create target directory if needed
                val targetDir = File(config.targetDirectory)
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                // Create backup if requested
                if (config.createBackup) {
                    createBackup(targetDir)
                }

                // Process notes in batches
                var successfulMigrations = 0
                var failedMigrations = 0
                var skippedMigrations = 0
                val errors = mutableListOf<MigrationError>()

                _progress.value =
                    _progress.value?.copy(status = MigrationProgress.MigrationStatus.IN_PROGRESS)

                notes.chunked(config.batchSize).forEach { batch ->
                    batch.forEach { note ->
                        try {
                            val result = migrateNote(note, config)
                            when (result) {
                                is NoteResult.SUCCESS -> successfulMigrations++
                                is NoteResult.SKIPPED -> skippedMigrations++
                                is NoteResult.FAILED -> {
                                    failedMigrations++
                                    errors.add(result.error)
                                }
                            }

                            // Update progress
                            _progress.value =
                                _progress.value?.copy(
                                    processedNotes =
                                        successfulMigrations + failedMigrations + skippedMigrations,
                                    currentNoteTitle = note.title,
                                    errors = errors.toList(),
                                )
                        } catch (e: Exception) {
                            failedMigrations++
                            val error =
                                MigrationError(
                                    noteId = note.id,
                                    noteTitle = note.title ?: "Untitled",
                                    errorType = MigrationError.ErrorType.UNKNOWN_ERROR,
                                    errorMessage = e.message ?: "Unknown error",
                                )
                            errors.add(error)

                            _progress.value =
                                _progress.value?.copy(
                                    processedNotes =
                                        successfulMigrations + failedMigrations + skippedMigrations,
                                    currentNoteTitle = note.title,
                                    errors = errors.toList(),
                                )
                        }
                    }
                }

                val duration = System.currentTimeMillis() - startTime
                val success = failedMigrations == 0

                _progress.value =
                    _progress.value?.copy(
                        status =
                            if (success) MigrationProgress.MigrationStatus.COMPLETED
                            else MigrationProgress.MigrationStatus.FAILED,
                        processedNotes = totalNotes,
                        errors = errors.toList(),
                    )

                MigrationResult(
                    success = success,
                    totalNotes = totalNotes,
                    successfulMigrations = successfulMigrations,
                    failedMigrations = failedMigrations,
                    skippedMigrations = skippedMigrations,
                    duration = duration,
                    errors = errors.toList(),
                )
            } catch (e: Exception) {
                _progress.value =
                    _progress.value?.copy(
                        status = MigrationProgress.MigrationStatus.FAILED,
                        errors =
                            listOf(
                                MigrationError(
                                    noteId = -1,
                                    noteTitle = "Migration Error",
                                    errorType = MigrationError.ErrorType.UNKNOWN_ERROR,
                                    errorMessage = e.message ?: "Migration failed",
                                )
                            ),
                    )

                MigrationResult(
                    success = false,
                    totalNotes = 0,
                    successfulMigrations = 0,
                    failedMigrations = 0,
                    skippedMigrations = 0,
                    duration = System.currentTimeMillis() - startTime,
                    errors =
                        listOf(
                            MigrationError(
                                noteId = -1,
                                noteTitle = "Migration Error",
                                errorType = MigrationError.ErrorType.UNKNOWN_ERROR,
                                errorMessage = e.message ?: "Migration failed",
                            )
                        ),
                )
            } finally {
                isMigrationActive = false
            }
        }
    }

    /** Cancel migration process */
    suspend fun cancelMigration() {
        migrationJob?.cancel()
        _progress.value =
            _progress.value?.copy(status = MigrationProgress.MigrationStatus.CANCELLED)
        isMigrationActive = false
    }

    /** Pause migration process */
    suspend fun pauseMigration() {
        _progress.value = _progress.value?.copy(status = MigrationProgress.MigrationStatus.PAUSED)
    }

    /** Resume migration process */
    suspend fun resumeMigration() {
        _progress.value =
            _progress.value?.copy(status = MigrationProgress.MigrationStatus.IN_PROGRESS)
    }

    /** Get notes that need to be migrated */
    private suspend fun getNotesForMigration(config: MigrationConfig): List<BaseNote> {
        return withContext(Dispatchers.IO) {
            // Get all notes from database
            val allNotes = database.getBaseNoteDao().getAllNotes()

            // Filter based on configuration
            allNotes.filter { note ->

                // Skip existing files if configured to do so
                if (config.skipExistingFiles) {
                    val fileName = generateFileName(note, config)
                    val targetFile = File(config.targetDirectory, fileName)
                    if (targetFile.exists()) {
                        return@filter false
                    }
                }

                true
            }
        }
    }

    /** Migrate a single note */
    private suspend fun migrateNote(note: BaseNote, config: MigrationConfig): NoteResult {
        return withContext(Dispatchers.IO) {
            try {
                // Convert note to markdown
                val markdownContent = convertNoteToMarkdown(note, config)

                // Generate filename
                val fileName = generateFileName(note, config)
                val targetFile = File(config.targetDirectory, fileName)

                // Skip if file exists and configured to do so
                if (config.skipExistingFiles && targetFile.exists()) {
                    return@withContext NoteResult.SKIPPED
                }

                // Write markdown file
                targetFile.writeText(markdownContent)

                // Validate output if requested
                if (config.validateOutput) {
                    validateMarkdownFile(targetFile, note)
                }

                NoteResult.SUCCESS
            } catch (e: IOException) {
                NoteResult.FAILED(
                    MigrationError(
                        noteId = note.id,
                        noteTitle = note.title ?: "Untitled",
                        errorType = MigrationError.ErrorType.FILE_WRITE_FAILED,
                        errorMessage = e.message ?: "File write failed",
                    )
                )
            } catch (e: Exception) {
                NoteResult.FAILED(
                    MigrationError(
                        noteId = note.id,
                        noteTitle = note.title ?: "Untitled",
                        errorType = MigrationError.ErrorType.CONVERSION_FAILED,
                        errorMessage = e.message ?: "Conversion failed",
                    )
                )
            }
        }
    }

    /** Convert note to markdown format */
    private fun convertNoteToMarkdown(note: BaseNote, config: MigrationConfig): String {
        val markdown = StringBuilder()

        // Add title
        if (!note.title.isNullOrEmpty()) {
            markdown.append("# ${note.title}\n\n")
        }

        // Add metadata
        if (config.preserveOriginalFormat) {
            markdown.append("<!-- Migration Metadata -->\n")
            markdown.append("<!-- Original ID: ${note.id} -->\n")
            markdown.append("<!-- Created: ${Date(note.timestamp)} -->\n")
            markdown.append("<!-- Modified: ${Date(note.modifiedTimestamp)} -->\n")
            if (note.pinned) markdown.append("<!-- Pinned: true -->\n")
            markdown.append("\n")
        }

        // Add tags
        if (!note.labels.isNullOrEmpty()) {
            markdown.append("Tags: ${note.labels.joinToString(", ") { "#$it" }}\n\n")
        }

        // Add content
        if (!note.body.isNullOrEmpty()) {
            // For now, just use the body as-is without span conversion
            // TODO: Implement proper span to markdown conversion
            markdown.append(note.body)
        }

        return markdown.toString()
    }

    /** Generate filename for note */
    private fun generateFileName(note: BaseNote, config: MigrationConfig): String {
        val template = config.customFileNameTemplate ?: "{title}_{id}.md"
        val timestamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date(note.modifiedTimestamp))

        return template
            .replace("{title}", (note.title ?: "Untitled").sanitizeFilename())
            .replace("{id}", note.id.toString())
            .replace("{date}", timestamp)
            .replace(
                "{created}",
                SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(note.timestamp)),
            )
    }

    /** Sanitize filename */
    private fun String.sanitizeFilename(): String {
        return this.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    /** Validate markdown file */
    private fun validateMarkdownFile(file: File, originalNote: BaseNote) {
        if (!file.exists()) {
            throw IOException("Markdown file was not created")
        }

        if (file.length() == 0L) {
            throw IOException("Markdown file is empty")
        }

        // Additional validation can be added here
    }

    /** Create backup of existing files */
    private suspend fun createBackup(targetDir: File) {
        val backupDir = File(targetDir.parent, "${targetDir.name}_backup_${Date().time}")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }

        // Copy existing files to backup
        targetDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                val backupFile = File(backupDir, file.name)
                file.copyTo(backupFile, overwrite = true)
            }
        }
    }
}

/** Sealed class for individual note migration results */
sealed class NoteResult {
    object SUCCESS : NoteResult()

    object SKIPPED : NoteResult()

    data class FAILED(val error: MigrationError) : NoteResult()
}
