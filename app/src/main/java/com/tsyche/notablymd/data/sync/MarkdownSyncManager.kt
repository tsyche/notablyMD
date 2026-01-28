package com.tsyche.notablymd.data.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main coordinator for markdown synchronization system Handles file watching, bidirectional sync,
 * and conflict resolution
 */
class MarkdownSyncManager(private val context: Context) {

    private val fileWatcher = FileWatcher(context)
    private val bidirectionalSync = BidirectionalSync(context)
    private val conflictResolver = ConflictResolver()

    private val scope = CoroutineScope(Dispatchers.IO)

    /** Initialize the markdown sync system */
    fun initialize() {
        fileWatcher.startWatching()
        schedulePeriodicSync()
    }

    /** Stop the markdown sync system */
    fun stop() {
        fileWatcher.stopWatching()
        WorkManager.getInstance(context).cancelUniqueWork(WATCHER_WORK_NAME)
    }

    /** Manually trigger a sync operation */
    fun triggerSync() {
        scope.launch { bidirectionalSync.performFullSync() }
    }

    /** Schedule periodic background sync */
    private fun schedulePeriodicSync() {
        val syncRequest =
            PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WATCHER_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest,
            )
    }

    /** Handle file change detected by file watcher */
    fun onFileChanged(filePath: String, changeType: FileWatcher.ChangeType) {
        scope.launch {
            when (changeType) {
                FileWatcher.ChangeType.MODIFIED -> {
                    bidirectionalSync.syncFromFileToDatabase(filePath)
                }
                FileWatcher.ChangeType.DELETED -> {
                    bidirectionalSync.handleFileDeleted(filePath)
                }
                FileWatcher.ChangeType.CREATED -> {
                    bidirectionalSync.syncFromFileToDatabase(filePath)
                }
            }
        }
    }

    companion object {
        const val WATCHER_WORK_NAME = "markdown_file_watcher"
    }
}
