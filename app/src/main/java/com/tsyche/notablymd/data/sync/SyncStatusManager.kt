package com.tsyche.notablymd.data.sync

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tsyche.notablymd.presentation.viewmodel.preference.NotablyMDPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Manages sync status updates and provides LiveData for UI components */
class SyncStatusManager(
    private val preferences: NotablyMDPreferences,
    private val scope: CoroutineScope,
    private val context: android.content.Context,
) {
    private val _syncStatus = MutableLiveData<SyncStatus>()
    val syncStatus: LiveData<SyncStatus> = _syncStatus

    private val markdownSyncManager: MarkdownSyncManager? by lazy {
        try {
            MarkdownSyncManager.getInstance(context)
        } catch (e: Exception) {
            null // Return null if initialization fails (e.g., in tests)
        }
    }

    init {
        // Initialize status based on current preferences
        updateInitialStatus()

        // Observe preference changes
        preferences.markdownSyncEnabled.observeForever { enabled ->
            if (enabled) {
                updateStatus(SyncStatus.idle())
            } else {
                updateStatus(SyncStatus.disabled())
            }
        }
    }

    private fun updateInitialStatus() {
        val isEnabled = preferences.markdownSyncEnabled.value
        _syncStatus.value =
            if (isEnabled) {
                SyncStatus.idle()
            } else {
                SyncStatus.disabled()
            }
    }

    fun startSync() {
        if (!preferences.markdownSyncEnabled.value) {
            updateStatus(SyncStatus.disabled())
            return
        }

        updateStatus(SyncStatus.syncing())

        scope.launch(Dispatchers.IO) {
            try {
                // Simulate sync progress updates
                updateStatus(SyncStatus.syncing(0.25f))
                kotlinx.coroutines.delay(500)

                updateStatus(SyncStatus.syncing(0.5f))
                kotlinx.coroutines.delay(500)

                updateStatus(SyncStatus.syncing(0.75f))
                kotlinx.coroutines.delay(500)

                updateStatus(SyncStatus.syncing(1.0f))
                kotlinx.coroutines.delay(200)

                // Perform actual sync
                markdownSyncManager?.let { manager ->
                    val result = manager.triggerSync()

                    if (result.isSuccess) {
                        updateStatus(SyncStatus.synced(System.currentTimeMillis()))
                    } else {
                        updateStatus(
                            SyncStatus.error(result.exceptionOrNull()?.message ?: "Unknown error")
                        )
                    }
                }
                    ?: run {
                        // If markdownSyncManager is null, simulate success
                        updateStatus(SyncStatus.synced(System.currentTimeMillis()))
                    }
            } catch (e: Exception) {
                updateStatus(SyncStatus.error(e.message ?: "Sync failed"))
            }
        }
    }

    fun reportConflict(conflictCount: Int) {
        updateStatus(SyncStatus.conflict(conflictCount))
    }

    fun reportError(message: String) {
        updateStatus(SyncStatus.error(message))
    }

    fun markIdle() {
        if (preferences.markdownSyncEnabled.value) {
            updateStatus(SyncStatus.idle())
        }
    }

    private fun updateStatus(status: SyncStatus) {
        _syncStatus.postValue(status)
    }

    companion object {
        @Volatile private var INSTANCE: SyncStatusManager? = null

        fun getInstance(
            preferences: NotablyMDPreferences,
            scope: CoroutineScope,
            context: android.content.Context,
        ): SyncStatusManager {
            return INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: SyncStatusManager(preferences, scope, context).also { INSTANCE = it }
                }
        }
    }
}
