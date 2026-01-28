package com.tsyche.notablymd.data.sync

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** Simplified file watcher for markdown files */
class FileWatcher(private val context: Context) {

    enum class ChangeType {
        CREATED,
        MODIFIED,
        DELETED,
    }

    data class FileChangeEvent(
        val path: String,
        val type: ChangeType,
        val timestamp: Long = System.currentTimeMillis(),
    )

    private val _fileChanges = MutableSharedFlow<FileChangeEvent>()
    val fileChanges: SharedFlow<FileChangeEvent> = _fileChanges

    private var isWatching = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val markdownSyncManager by lazy { MarkdownSyncManager(context) }

    /** Start watching markdown directories */
    fun startWatching() {
        if (isWatching) return

        isWatching = true

        // Start collecting file changes
        scope.launch {
            fileChanges.collect { event ->
                markdownSyncManager.onFileChanged(event.path, event.type)
            }
        }
    }

    /** Stop watching all directories */
    fun stopWatching() {
        if (!isWatching) return

        isWatching = false
    }

    /** Simulate file change for testing */
    fun simulateFileChange(path: String, type: ChangeType) {
        scope.launch { _fileChanges.emit(FileChangeEvent(path, type)) }
    }

    companion object {
        const val WATCHER_WORK_NAME = "markdown_file_watcher"
    }
}
