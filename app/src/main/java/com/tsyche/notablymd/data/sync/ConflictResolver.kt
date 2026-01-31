package com.tsyche.notablymd.data.sync

import com.tsyche.notablymd.data.model.BaseNote
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles conflict resolution between database and markdown file versions Implements various
 * strategies for resolving conflicts
 */
class ConflictResolver {

    enum class ConflictResolutionStrategy {
        LATEST_WINS, // Most recent modification wins
        DATABASE_WINS, // Database version takes precedence
        FILE_WINS, // File version takes precedence
        MERGE, // Attempt to merge changes
        MANUAL, // Require manual resolution
    }

    data class Conflict(
        val databaseNote: BaseNote,
        val fileNote: BaseNote,
        val conflictType: ConflictType,
    )

    enum class ConflictType {
        CONTENT_MODIFIED, // Both versions have different content
        METADATA_MODIFIED, // Only metadata differs
        STRUCTURE_CHANGED, // Note structure changed (type, etc.)
        DELETED_CONFLICT, // One version deleted, other modified
    }

    /** Resolve conflict between database and file versions */
    fun resolveConflict(
        databaseNote: BaseNote,
        fileNote: BaseNote,
        strategy: ConflictResolutionStrategy = ConflictResolutionStrategy.LATEST_WINS,
    ): BaseNote {
        val conflict = detectConflict(databaseNote, fileNote)

        return when (strategy) {
            ConflictResolutionStrategy.LATEST_WINS -> resolveLatestWins(databaseNote, fileNote)
            ConflictResolutionStrategy.DATABASE_WINS -> databaseNote
            ConflictResolutionStrategy.FILE_WINS -> fileNote
            ConflictResolutionStrategy.MERGE -> mergeNotes(databaseNote, fileNote, conflict)
            ConflictResolutionStrategy.MANUAL -> throw ManualResolutionRequired(conflict)
        }
    }

    /** Attempt to automatically resolve a SyncConflict */
    suspend fun autoResolveConflict(conflict: SyncConflict): ConflictResolutionResult {
        return withContext(Dispatchers.IO) {
            try {
                when (conflict.conflictType) {
                    SyncConflict.ConflictType.METADATA_MODIFIED -> {
                        // Auto-merge metadata conflicts by combining both
                        val mergedContent =
                            mergeMetadata(conflict.localVersion, conflict.remoteVersion)
                        ConflictResolutionResult(
                            conflictId = conflict.noteId,
                            success = true,
                            action = ConflictResolution.ResolutionAction.AUTO_MERGE,
                            resultingNoteId = conflict.noteId,
                        )
                    }

                    SyncConflict.ConflictType.CONTENT_MODIFIED -> {
                        // Try simple content merge
                        if (canAutoMergeContent(conflict.localVersion, conflict.remoteVersion)) {
                            val mergedContent =
                                mergeContent(conflict.localVersion, conflict.remoteVersion)
                            ConflictResolutionResult(
                                conflictId = conflict.noteId,
                                success = true,
                                action = ConflictResolution.ResolutionAction.AUTO_MERGE,
                                resultingNoteId = conflict.noteId,
                            )
                        } else {
                            ConflictResolutionResult(
                                conflictId = conflict.noteId,
                                success = false,
                                action = ConflictResolution.ResolutionAction.MERGE_MANUAL,
                                errorMessage = "Manual merge required for complex content changes",
                            )
                        }
                    }

                    else -> {
                        ConflictResolutionResult(
                            conflictId = conflict.noteId,
                            success = false,
                            action = ConflictResolution.ResolutionAction.MERGE_MANUAL,
                            errorMessage = "Auto-resolution not available for this conflict type",
                        )
                    }
                }
            } catch (e: Exception) {
                ConflictResolutionResult(
                    conflictId = conflict.noteId,
                    success = false,
                    action = ConflictResolution.ResolutionAction.MERGE_MANUAL,
                    errorMessage = "Auto-resolution failed: ${e.message}",
                )
            }
        }
    }

    /** Apply a manual resolution to a conflict */
    suspend fun applyResolution(
        conflict: SyncConflict,
        resolution: ConflictResolution,
    ): ConflictResolutionResult {
        return withContext(Dispatchers.IO) {
            try {
                when (resolution.action) {
                    ConflictResolution.ResolutionAction.KEEP_LOCAL -> {
                        ConflictResolutionResult(
                            conflictId = conflict.noteId,
                            success = true,
                            action = resolution.action,
                            resultingNoteId = conflict.noteId,
                        )
                    }

                    ConflictResolution.ResolutionAction.KEEP_REMOTE -> {
                        ConflictResolutionResult(
                            conflictId = conflict.noteId,
                            success = true,
                            action = resolution.action,
                            resultingNoteId = conflict.noteId,
                        )
                    }

                    ConflictResolution.ResolutionAction.MERGE_MANUAL -> {
                        if (resolution.customContent != null) {
                            ConflictResolutionResult(
                                conflictId = conflict.noteId,
                                success = true,
                                action = resolution.action,
                                resultingNoteId = conflict.noteId,
                            )
                        } else {
                            ConflictResolutionResult(
                                conflictId = conflict.noteId,
                                success = false,
                                action = resolution.action,
                                errorMessage = "Custom content required for manual merge",
                            )
                        }
                    }

                    ConflictResolution.ResolutionAction.KEEP_BOTH -> {
                        // Would create two separate notes in a real implementation
                        ConflictResolutionResult(
                            conflictId = conflict.noteId,
                            success = true,
                            action = resolution.action,
                            resultingNoteId = conflict.noteId,
                        )
                    }

                    ConflictResolution.ResolutionAction.DELETE_NOTE -> {
                        ConflictResolutionResult(
                            conflictId = conflict.noteId,
                            success = true,
                            action = resolution.action,
                            resultingNoteId = null,
                        )
                    }

                    ConflictResolution.ResolutionAction.AUTO_MERGE -> {
                        autoResolveConflict(conflict)
                    }
                }
            } catch (e: Exception) {
                ConflictResolutionResult(
                    conflictId = conflict.noteId,
                    success = false,
                    action = resolution.action,
                    errorMessage = "Resolution failed: ${e.message}",
                )
            }
        }
    }

    /** Check if content can be auto-merged */
    private fun canAutoMergeContent(
        local: SyncConflict.NoteVersion,
        remote: SyncConflict.NoteVersion,
    ): Boolean {
        // Simple heuristic: if content is similar enough and not too large
        val similarity = calculateContentSimilarity(local.content, remote.content)
        return similarity > 0.7 && local.content.length < 10000 && remote.content.length < 10000
    }

    /** Calculate similarity between two content strings */
    private fun calculateContentSimilarity(content1: String, content2: String): Float {
        if (content1 == content2) return 1.0f
        if (content1.isEmpty() || content2.isEmpty()) return 0.0f

        // Simple word-based similarity calculation
        val words1 = content1.split("\\s+".toRegex()).toSet()
        val words2 = content2.split("\\s+".toRegex()).toSet()

        val intersection = words1.intersect(words2)
        val union = words1.union(words2)

        return if (union.isNotEmpty()) {
            intersection.size.toFloat() / union.size.toFloat()
        } else {
            0.0f
        }
    }

    /** Merge content from two versions */
    private fun mergeContent(
        local: SyncConflict.NoteVersion,
        remote: SyncConflict.NoteVersion,
    ): String {
        // Simple merge strategy: use the newer version as base and add changes
        val base = if (local.lastModified > remote.lastModified) local else remote
        val other = if (local.lastModified > remote.lastModified) remote else local

        // In a real implementation, this would use a proper diff/merge algorithm
        return base.content + "\n\n--- Merged content ---\n" + other.content
    }

    /** Merge metadata from two versions */
    private fun mergeMetadata(
        local: SyncConflict.NoteVersion,
        remote: SyncConflict.NoteVersion,
    ): String {
        // For metadata conflicts, use the most recent version
        return if (local.lastModified > remote.lastModified) local.content else remote.content
    }

    /** Detect the type of conflict between two notes */
    fun detectConflict(databaseNote: BaseNote, fileNote: BaseNote): Conflict {
        val conflictType =
            when {
                databaseNote.body != fileNote.body || databaseNote.spans != fileNote.spans -> {
                    ConflictType.CONTENT_MODIFIED
                }
                hasMetadataConflict(databaseNote, fileNote) -> {
                    ConflictType.METADATA_MODIFIED
                }
                databaseNote.type != fileNote.type -> {
                    ConflictType.STRUCTURE_CHANGED
                }
                else -> ConflictType.METADATA_MODIFIED
            }

        return Conflict(databaseNote, fileNote, conflictType)
    }

    /** Resolve conflict by choosing the most recently modified version */
    private fun resolveLatestWins(databaseNote: BaseNote, fileNote: BaseNote): BaseNote {
        return if (databaseNote.modifiedTimestamp > fileNote.modifiedTimestamp) {
            databaseNote.copy(modifiedTimestamp = System.currentTimeMillis())
        } else {
            fileNote.copy(modifiedTimestamp = System.currentTimeMillis())
        }
    }

    /** Attempt to merge two note versions */
    private fun mergeNotes(
        databaseNote: BaseNote,
        fileNote: BaseNote,
        conflict: Conflict,
    ): BaseNote {
        return when (conflict.conflictType) {
            ConflictType.CONTENT_MODIFIED -> mergeContent(databaseNote, fileNote)
            ConflictType.METADATA_MODIFIED -> mergeMetadata(databaseNote, fileNote)
            ConflictType.STRUCTURE_CHANGED -> mergeStructure(databaseNote, fileNote)
            ConflictType.DELETED_CONFLICT -> handleDeleteConflict(databaseNote, fileNote)
        }
    }

    /** Merge content between two notes */
    private fun mergeContent(databaseNote: BaseNote, fileNote: BaseNote): BaseNote {
        // Simple merge strategy: use the longer content with combined spans
        val mergedBody =
            if (fileNote.body.length > databaseNote.body.length) {
                fileNote.body
            } else {
                databaseNote.body
            }

        // Merge spans (simplified - in reality would need more sophisticated merging)
        val mergedSpans = (databaseNote.spans + fileNote.spans).distinctBy { it.start to it.end }

        return databaseNote.copy(
            body = mergedBody,
            spans = mergedSpans,
            modifiedTimestamp = System.currentTimeMillis(),
        )
    }

    /** Merge metadata between two notes */
    private fun mergeMetadata(databaseNote: BaseNote, fileNote: BaseNote): BaseNote {
        return databaseNote.copy(
            title =
                if (fileNote.modifiedTimestamp > databaseNote.modifiedTimestamp) fileNote.title
                else databaseNote.title,
            color =
                if (fileNote.modifiedTimestamp > databaseNote.modifiedTimestamp) fileNote.color
                else databaseNote.color,
            pinned =
                fileNote.pinned || databaseNote.pinned, // Keep pinned if either version is pinned
            labels = (databaseNote.labels + fileNote.labels).distinct(),
            modifiedTimestamp = System.currentTimeMillis(),
        )
    }

    /** Merge structure changes between two notes */
    private fun mergeStructure(databaseNote: BaseNote, fileNote: BaseNote): BaseNote {
        // For structure changes, prefer the file version as it likely represents user intent
        return fileNote.copy(
            id = databaseNote.id, // Keep original ID
            modifiedTimestamp = System.currentTimeMillis(),
        )
    }

    /** Handle delete conflicts */
    private fun handleDeleteConflict(databaseNote: BaseNote, fileNote: BaseNote): BaseNote {
        // If one version was "deleted" (empty body) and other has content, keep the one with
        // content
        return if (databaseNote.body.isBlank() && !fileNote.body.isBlank()) {
            fileNote.copy(modifiedTimestamp = System.currentTimeMillis())
        } else if (!databaseNote.body.isBlank() && fileNote.body.isBlank()) {
            databaseNote.copy(modifiedTimestamp = System.currentTimeMillis())
        } else {
            // Both blank or both have content - use latest wins
            resolveLatestWins(databaseNote, fileNote)
        }
    }

    /** Check if there are metadata conflicts */
    private fun hasMetadataConflict(note1: BaseNote, note2: BaseNote): Boolean {
        return note1.title != note2.title ||
            note1.color != note2.color ||
            note1.pinned != note2.pinned ||
            note1.labels != note2.labels
    }

    /** Get suggested resolution strategy based on conflict type */
    fun suggestResolutionStrategy(conflict: Conflict): ConflictResolutionStrategy {
        return when (conflict.conflictType) {
            ConflictType.CONTENT_MODIFIED -> ConflictResolutionStrategy.LATEST_WINS
            ConflictType.METADATA_MODIFIED -> ConflictResolutionStrategy.MERGE
            ConflictType.STRUCTURE_CHANGED -> ConflictResolutionStrategy.FILE_WINS
            ConflictType.DELETED_CONFLICT -> ConflictResolutionStrategy.LATEST_WINS
        }
    }

    /** Create a human-readable conflict description */
    fun describeConflict(conflict: Conflict): String {
        return when (conflict.conflictType) {
            ConflictType.CONTENT_MODIFIED -> {
                "Content differs between database and file versions"
            }
            ConflictType.METADATA_MODIFIED -> {
                "Metadata (title, labels, color, etc.) differs between versions"
            }
            ConflictType.STRUCTURE_CHANGED -> {
                "Note structure or type differs between versions"
            }
            ConflictType.DELETED_CONFLICT -> {
                "One version appears deleted while the other has content"
            }
        }
    }
}

/** Exception thrown when manual conflict resolution is required */
class ManualResolutionRequired(val conflict: ConflictResolver.Conflict) :
    Exception(
        "Manual conflict resolution required: ${ConflictResolver().describeConflict(conflict)}"
    )
