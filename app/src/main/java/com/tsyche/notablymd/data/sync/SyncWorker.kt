package com.tsyche.notablymd.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Background worker for periodic markdown synchronization */
class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val syncManager = MarkdownSyncManager(appContext)

    override suspend fun doWork(): Result {
        return try {
            // For now, always enable sync
            // In real implementation, this would check preferences
            syncManager.triggerSync()
            Result.success()
        } catch (e: Exception) {
            // Log error but don't fail the work
            e.printStackTrace()
            Result.retry()
        }
    }
}
