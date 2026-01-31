package com.tsyche.notablymd.data.sync

import android.os.Parcel
import android.os.Parcelable

/** Represents a sync conflict between different versions of a note */
data class SyncConflict(
    val noteId: Long,
    val noteTitle: String,
    val localVersion: NoteVersion,
    val remoteVersion: NoteVersion,
    val conflictType: ConflictType,
    val detectedAt: Long = System.currentTimeMillis(),
) : Parcelable {

    enum class ConflictType {
        CONTENT_MODIFIED, // Both local and remote have different content
        DELETED_MODIFIED, // One side deleted, other modified
        METADATA_MODIFIED, // Only metadata (title, tags, etc.) differs
        STRUCTURE_CHANGED, // File structure changed
    }

    data class NoteVersion(
        val content: String,
        val lastModified: Long,
        val checksum: String,
        val size: Int,
        val source: String, // "local", "remote", "backup"
    ) : Parcelable {

        companion object CREATOR : Parcelable.Creator<NoteVersion> {
            override fun createFromParcel(parcel: Parcel): NoteVersion {
                return NoteVersion(
                    content = parcel.readString() ?: "",
                    lastModified = parcel.readLong(),
                    checksum = parcel.readString() ?: "",
                    size = parcel.readInt(),
                    source = parcel.readString() ?: "",
                )
            }

            override fun newArray(size: Int): Array<NoteVersion?> {
                return arrayOfNulls(size)
            }
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(content)
            parcel.writeLong(lastModified)
            parcel.writeString(checksum)
            parcel.writeInt(size)
            parcel.writeString(source)
        }

        override fun describeContents(): Int = 0
    }

    /** Get a human-readable description of the conflict */
    fun getDescription(): String {
        return when (conflictType) {
            ConflictType.CONTENT_MODIFIED -> "Content differs between local and remote versions"
            ConflictType.DELETED_MODIFIED ->
                "Note was deleted on one side but modified on the other"
            ConflictType.METADATA_MODIFIED -> "Metadata (title, tags) differs between versions"
            ConflictType.STRUCTURE_CHANGED -> "File structure or format changed between versions"
        }
    }

    /** Get the severity of this conflict */
    fun getSeverity(): ConflictSeverity {
        return when (conflictType) {
            ConflictType.CONTENT_MODIFIED -> ConflictSeverity.HIGH
            ConflictType.DELETED_MODIFIED -> ConflictSeverity.CRITICAL
            ConflictType.METADATA_MODIFIED -> ConflictSeverity.LOW
            ConflictType.STRUCTURE_CHANGED -> ConflictSeverity.MEDIUM
        }
    }

    enum class ConflictSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL,
    }

    companion object CREATOR : Parcelable.Creator<SyncConflict> {
        override fun createFromParcel(parcel: Parcel): SyncConflict {
            return SyncConflict(
                noteId = parcel.readLong(),
                noteTitle = parcel.readString() ?: "",
                localVersion =
                    parcel.readParcelable(NoteVersion::class.java.classLoader)
                        ?: NoteVersion("", 0, "", 0, ""),
                remoteVersion =
                    parcel.readParcelable(NoteVersion::class.java.classLoader)
                        ?: NoteVersion("", 0, "", 0, ""),
                conflictType = ConflictType.values()[parcel.readInt()],
                detectedAt = parcel.readLong(),
            )
        }

        override fun newArray(size: Int): Array<SyncConflict?> {
            return arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(noteId)
        parcel.writeString(noteTitle)
        parcel.writeParcelable(localVersion, flags)
        parcel.writeParcelable(remoteVersion, flags)
        parcel.writeInt(conflictType.ordinal)
        parcel.writeLong(detectedAt)
    }

    override fun describeContents(): Int = 0
}

/** Represents the resolution options for a conflict */
data class ConflictResolution(
    val conflictId: Long,
    val action: ResolutionAction,
    val customContent: String? = null,
    val preserveMetadata: Boolean = true,
) {

    enum class ResolutionAction {
        KEEP_LOCAL, // Use local version
        KEEP_REMOTE, // Use remote version
        MERGE_MANUAL, // Manual merge with custom content
        KEEP_BOTH, // Create two separate notes
        DELETE_NOTE, // Delete the note entirely
        AUTO_MERGE, // Attempt automatic merge
    }
}

/** Result of a conflict resolution operation */
data class ConflictResolutionResult(
    val conflictId: Long,
    val success: Boolean,
    val action: ConflictResolution.ResolutionAction,
    val errorMessage: String? = null,
    val resultingNoteId: Long? = null,
)
