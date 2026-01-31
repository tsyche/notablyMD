package com.tsyche.notablymd.utils.performance

import com.tsyche.notablymd.data.model.BaseNote
import com.tsyche.notablymd.data.model.Folder
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Enhanced performance cache with smart preloading and incremental updates */
open class PerformanceCache {

    private val cacheScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Main cache storage
    private val notesCache = ConcurrentHashMap<Long, BaseNote>()
    private val folderCache = ConcurrentHashMap<Folder, List<Long>>()
    private val labelCache = ConcurrentHashMap<String, List<Long>>()
    private val searchCache = ConcurrentHashMap<String, List<Long>>()

    // Cache state tracking
    private val _isCacheReady = MutableStateFlow(false)
    val isCacheReady: StateFlow<Boolean> = _isCacheReady.asStateFlow()

    private val _lastUpdateTime = MutableStateFlow(0L)
    val lastUpdateTime: StateFlow<Long> = _lastUpdateTime.asStateFlow()

    // Cache statistics
    private var cacheHits = 0
    private var cacheMisses = 0
    private var preloadedItems = 0

    companion object {
        private const val MAX_CACHE_SIZE = 1000
        private const val PRELOAD_BATCH_SIZE = 50
        private const val SEARCH_RESULT_CACHE_SIZE = 100
    }

    /** Initialize cache with initial data */
    fun initialize(notes: List<BaseNote>) {
        cacheScope.launch {
            clearCache()

            // Populate main cache
            notes.forEach { note ->
                notesCache[note.id] = note
                updateFolderIndex(note)
                updateLabelIndex(note)
            }

            preloadedItems = notes.size
            _lastUpdateTime.value = System.currentTimeMillis()
            _isCacheReady.value = true

            log("Cache initialized with ${notes.size} notes")
        }
    }

    /** Get note from cache with fallback */
    suspend fun getNote(id: Long): BaseNote? {
        val cached = notesCache[id]
        return if (cached != null) {
            cacheHits++
            cached
        } else {
            cacheMisses++
            null
        }
    }

    /** Add or update note in cache */
    suspend fun updateNote(note: BaseNote) {
        notesCache[note.id] = note
        updateFolderIndex(note)
        updateLabelIndex(note)
        invalidateSearchCache()
        _lastUpdateTime.value = System.currentTimeMillis()
    }

    /** Remove note from cache */
    suspend fun removeNote(id: Long) {
        val note = notesCache.remove(id)
        note?.let {
            removeFromFolderIndex(it)
            removeFromLabelIndex(it)
            invalidateSearchCache()
        }
        _lastUpdateTime.value = System.currentTimeMillis()
    }

    /** Get notes by folder from cache */
    fun getNotesByFolder(folder: Folder): List<BaseNote>? {
        val noteIds = folderCache[folder]
        return noteIds?.mapNotNull { notesCache[it] }
    }

    /** Get notes by label from cache */
    fun getNotesByLabel(label: String): List<BaseNote>? {
        val noteIds = labelCache[label]
        return noteIds?.mapNotNull { notesCache[it] }
    }

    /** Search notes with caching */
    fun searchNotes(query: String, searchFunction: (String) -> List<BaseNote>): List<BaseNote> {
        val cacheKey = query.lowercase().trim()

        return searchCache[cacheKey]?.let { cachedIds ->
            cacheHits++
            cachedIds.mapNotNull { notesCache[it] }
        }
            ?: run {
                cacheMisses++
                val results = searchFunction(query)

                // Cache search results
                if (searchCache.size >= SEARCH_RESULT_CACHE_SIZE) {
                    // Remove oldest entries (simple LRU)
                    val keysToRemove = searchCache.keys.take(10)
                    keysToRemove.forEach { searchCache.remove(it) }
                }

                searchCache[cacheKey] = results.map { it.id }
                results
            }
    }

    /** Smart preloading based on usage patterns */
    suspend fun preloadRecentNotes(allNotes: List<BaseNote>, limit: Int = PRELOAD_BATCH_SIZE) {
        val recentNotes = allNotes.sortedByDescending { it.timestamp }.take(limit)

        recentNotes.forEach { note ->
            if (!notesCache.containsKey(note.id)) {
                notesCache[note.id] = note
                updateFolderIndex(note)
                updateLabelIndex(note)
                preloadedItems++
            }
        }

        log("Preloaded ${recentNotes.size} recent notes")
    }

    /** Preload notes that are likely to be accessed */
    suspend fun preloadPinnedNotes(allNotes: List<BaseNote>) {
        val pinnedNotes = allNotes.filter { it.pinned }

        pinnedNotes.forEach { note ->
            if (!notesCache.containsKey(note.id)) {
                notesCache[note.id] = note
                updateFolderIndex(note)
                updateLabelIndex(note)
                preloadedItems++
            }
        }

        log("Preloaded ${pinnedNotes.size} pinned notes")
    }

    /** Get cache statistics */
    fun getCacheStats(): CacheStats {
        val totalRequests = cacheHits + cacheMisses
        val hitRate = if (totalRequests > 0) (cacheHits.toFloat() / totalRequests) * 100 else 0f

        return CacheStats(
            cacheSize = notesCache.size,
            hitRate = hitRate,
            totalRequests = totalRequests,
            preloadedItems = preloadedItems,
            lastUpdateTime = _lastUpdateTime.value,
        )
    }

    /** Clear all cache data */
    private fun clearCache() {
        notesCache.clear()
        folderCache.clear()
        labelCache.clear()
        searchCache.clear()
        cacheHits = 0
        cacheMisses = 0
        preloadedItems = 0
    }

    /** Update folder index */
    private fun updateFolderIndex(note: BaseNote) {
        val folderNotes = folderCache.getOrPut(note.folder) { mutableListOf() }.toMutableList()
        if (!folderNotes.contains(note.id)) {
            folderNotes.add(note.id)
            folderCache[note.folder] = folderNotes
        }
    }

    /** Update label index */
    private fun updateLabelIndex(note: BaseNote) {
        note.labels.forEach { label ->
            val labelNotes = labelCache.getOrPut(label) { mutableListOf() }.toMutableList()
            if (!labelNotes.contains(note.id)) {
                labelNotes.add(note.id)
                labelCache[label] = labelNotes
            }
        }
    }

    /** Remove from folder index */
    private fun removeFromFolderIndex(note: BaseNote) {
        folderCache[note.folder]?.let { folderNotes ->
            val updatedNotes = folderNotes.toMutableList()
            updatedNotes.remove(note.id)
            if (updatedNotes.isEmpty()) {
                folderCache.remove(note.folder)
            } else {
                folderCache[note.folder] = updatedNotes
            }
        }
    }

    /** Remove from label index */
    private fun removeFromLabelIndex(note: BaseNote) {
        note.labels.forEach { label ->
            labelCache[label]?.let { labelNotes ->
                val updatedNotes = labelNotes.toMutableList()
                updatedNotes.remove(note.id)
                if (updatedNotes.isEmpty()) {
                    labelCache.remove(label)
                } else {
                    labelCache[label] = updatedNotes
                }
            }
        }
    }

    /** Invalidate search cache when data changes */
    private fun invalidateSearchCache() {
        searchCache.clear()
    }

    private fun log(message: String) {
        android.util.Log.d("PerformanceCache", message)
    }
}

/** Cache statistics data class */
data class CacheStats(
    val cacheSize: Int,
    val hitRate: Float,
    val totalRequests: Int,
    val preloadedItems: Int,
    val lastUpdateTime: Long,
)

/** Global performance cache instance */
object GlobalPerformanceCache : PerformanceCache()
