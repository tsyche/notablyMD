package com.tsyche.notablymd.data.sync

import com.tsyche.notablymd.data.model.toText
import java.util.Date

/** Represents the current synchronization status */
data class SyncStatus(
    val state: SyncState,
    val lastSyncTime: Long = -1,
    val conflictCount: Int = 0,
    val errorMessage: String? = null,
    val progress: Float = 0f,
) {
    enum class SyncState {
        DISABLED, // Sync is disabled in settings
        IDLE, // Sync is enabled but not actively syncing
        SYNCING, // Currently syncing
        SYNCED, // Successfully synced
        ERROR, // Sync failed with error
        CONFLICT, // Conflicts detected
    }

    companion object {
        fun disabled() = SyncStatus(SyncState.DISABLED)

        fun idle() = SyncStatus(SyncState.IDLE)

        fun syncing(progress: Float = 0f) = SyncStatus(SyncState.SYNCING, progress = progress)

        fun synced(lastSyncTime: Long) = SyncStatus(SyncState.SYNCED, lastSyncTime = lastSyncTime)

        fun error(message: String) = SyncStatus(SyncState.ERROR, errorMessage = message)

        fun conflict(conflictCount: Int) =
            SyncStatus(SyncState.CONFLICT, conflictCount = conflictCount)
    }

    fun isActive(): Boolean = state == SyncState.SYNCING

    fun hasError(): Boolean = state == SyncState.ERROR

    fun hasConflicts(): Boolean = state == SyncState.CONFLICT && conflictCount > 0

    fun isHealthy(): Boolean = state in listOf(SyncState.IDLE, SyncState.SYNCED)

    fun getDisplayText(): String {
        return when (state) {
            SyncState.DISABLED -> "Sync Disabled"
            SyncState.IDLE ->
                if (lastSyncTime > 0) "Last sync: ${Date(lastSyncTime).toText()}"
                else "Ready to sync"
            SyncState.SYNCING ->
                "Syncing... ${if (progress > 0) "${(progress * 100).toInt()}%" else ""}"
            SyncState.SYNCED -> "Synced"
            SyncState.ERROR -> errorMessage ?: "Sync Error"
            SyncState.CONFLICT -> "$conflictCount conflicts"
        }
    }
}
