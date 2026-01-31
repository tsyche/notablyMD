package com.tsyche.notablymd.data.migration

/** Represents the progress of a migration operation */
data class MigrationProgress(
    val totalNotes: Int,
    val processedNotes: Int,
    val currentNoteTitle: String?,
    val status: MigrationStatus,
    val startTime: Long,
    val estimatedTimeRemaining: Long = -1,
    val errors: List<MigrationError> = emptyList(),
) {

    enum class MigrationStatus {
        NOT_STARTED,
        PREPARING,
        IN_PROGRESS,
        PAUSED,
        COMPLETED,
        FAILED,
        CANCELLED,
    }

    /** Get progress as a percentage (0-100) */
    fun getProgressPercentage(): Int {
        return if (totalNotes == 0) 0 else (processedNotes * 100) / totalNotes
    }

    /** Check if migration is active */
    fun isActive(): Boolean =
        status in
            listOf(MigrationStatus.PREPARING, MigrationStatus.IN_PROGRESS, MigrationStatus.PAUSED)

    /** Check if migration is finished */
    fun isFinished(): Boolean =
        status in
            listOf(MigrationStatus.COMPLETED, MigrationStatus.FAILED, MigrationStatus.CANCELLED)
}

/** Represents an error that occurred during migration */
data class MigrationError(
    val noteId: Long,
    val noteTitle: String,
    val errorType: ErrorType,
    val errorMessage: String,
    val timestamp: Long = System.currentTimeMillis(),
) {

    enum class ErrorType {
        CONVERSION_FAILED, // Failed to convert note to markdown
        FILE_WRITE_FAILED, // Failed to write markdown file
        VALIDATION_FAILED, // Converted content failed validation
        PERMISSION_DENIED, // Permission denied for file operations
        STORAGE_FULL, // Insufficient storage space
        UNKNOWN_ERROR, // Unclassified error
    }
}

/** Result of a migration operation */
data class MigrationResult(
    val success: Boolean,
    val totalNotes: Int,
    val successfulMigrations: Int,
    val failedMigrations: Int,
    val skippedMigrations: Int,
    val duration: Long,
    val errors: List<MigrationError> = emptyList(),
) {
    /** Get success rate as percentage */
    fun getSuccessRate(): Int {
        return if (totalNotes == 0) 0 else (successfulMigrations * 100) / totalNotes
    }

    /** Get human-readable summary */
    fun getSummary(): String {
        return buildString {
            append("Migration ")
            append(if (success) "completed successfully" else "failed")
            append(". ")
            append("$successfulMigrations/$totalNotes notes migrated")
            if (failedMigrations > 0) {
                append(", $failedMigrations failed")
            }
            if (skippedMigrations > 0) {
                append(", $skippedMigrations skipped")
            }
            append(". Duration: ${duration / 1000}s")
        }
    }
}

/** Configuration for migration process */
data class MigrationConfig(
    val targetDirectory: String,
    val includeDeletedNotes: Boolean = false,
    val preserveOriginalFormat: Boolean = true,
    val createBackup: Boolean = true,
    val batchSize: Int = 50,
    val skipExistingFiles: Boolean = true,
    val validateOutput: Boolean = true,
    val customFileNameTemplate: String? = null,
)
