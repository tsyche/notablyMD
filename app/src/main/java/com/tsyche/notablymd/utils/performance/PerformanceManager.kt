package com.tsyche.notablymd.utils.performance

import com.tsyche.notablymd.data.dao.BaseNoteDao
import com.tsyche.notablymd.data.model.BaseNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Performance manager that coordinates cache, indexing, and preloading */
class PerformanceManager(private val baseNoteDao: BaseNoteDao) {

    private val performanceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Performance components
    private val performanceCache = GlobalPerformanceCache
    private val incrementalIndexer = GlobalIncrementalIndexer
    private val smartPreloader = GlobalSmartPreloader

    // Performance state
    private val _isPerformanceReady = MutableStateFlow(false)
    val isPerformanceReady: StateFlow<Boolean> = _isPerformanceReady.asStateFlow()

    private val _performanceStats = MutableStateFlow<PerformanceStats?>(null)
    val performanceStats: StateFlow<PerformanceStats?> = _performanceStats.asStateFlow()

    // Initialization state
    private var isInitialized = false

    companion object {
        private const val PERFORMANCE_UPDATE_INTERVAL_MS = 5000L
    }

    /** Initialize performance systems */
    fun initialize() {
        if (isInitialized) return

        performanceScope.launch {
            try {
                // Load all notes once
                val allNotes = withContext(Dispatchers.IO) { baseNoteDao.getAll() }

                // Initialize cache
                performanceCache.initialize(allNotes)

                // Build incremental index
                incrementalIndexer.buildIndex(allNotes)

                // Initial smart preloading
                smartPreloader.preloadRecentNotes(allNotes, performanceCache)
                smartPreloader.preloadPinnedNotes(allNotes, performanceCache)

                // Wait for systems to be ready
                performanceCache.isCacheReady.collect { cacheReady ->
                    incrementalIndexer.isIndexReady.collect { indexReady ->
                        if (cacheReady && indexReady) {
                            _isPerformanceReady.value = true
                            isInitialized = true

                            // Start performance monitoring
                            startPerformanceMonitoring()

                            log("Performance systems initialized successfully")
                        }
                    }
                }
            } catch (e: Exception) {
                log("Error initializing performance systems: ${e.message}")
            }
        }
    }

    /** Get note with performance optimization */
    suspend fun getNote(id: Long): BaseNote? {
        // Try cache first
        performanceCache.getNote(id)?.let {
            return it
        }

        // Fallback to database
        val note = withContext(Dispatchers.IO) { baseNoteDao.get(id) }

        // Update cache and index
        note?.let {
            performanceCache.updateNote(it)
            incrementalIndexer.addToIndex(it)
            smartPreloader.recordAccess(it.id, "direct_access")
        }

        return note
    }

    /** Search notes with performance optimization */
    suspend fun searchNotes(query: String): List<BaseNote> {
        // Try incremental index first
        val indexedResults = incrementalIndexer.search(query)
        if (indexedResults.isNotEmpty()) {
            return indexedResults.mapNotNull { performanceCache.getNote(it) }
        }

        // Fallback to database search with caching
        val dbResults =
            runCatching { baseNoteDao.getAll().filter { matchesKeyword(it, query) } }
                .getOrDefault(emptyList())

        // Cache search results
        performanceCache.searchNotes(query) { dbResults }

        return dbResults
    }

    /** Get notes by folder with performance optimization */
    suspend fun getNotesByFolder(folder: com.tsyche.notablymd.data.model.Folder): List<BaseNote> {
        // Try cache first
        performanceCache.getNotesByFolder(folder)?.let {
            return it
        }

        // Try index next
        val indexedResults = incrementalIndexer.getNotesByFolder(folder)
        if (indexedResults.isNotEmpty()) {
            return indexedResults.mapNotNull { performanceCache.getNote(it) }
        }

        // Fallback to database
        val dbResults =
            withContext(Dispatchers.IO) { baseNoteDao.getFrom(folder).value ?: emptyList() }

        // Update cache
        dbResults.forEach { performanceCache.updateNote(it) }

        return dbResults
    }

    /** Get notes by label with performance optimization */
    suspend fun getNotesByLabel(label: String): List<BaseNote> {
        // Try cache first
        performanceCache.getNotesByLabel(label)?.let {
            return it
        }

        // Try index next
        val indexedResults = incrementalIndexer.getNotesByLabel(label)
        if (indexedResults.isNotEmpty()) {
            return indexedResults.mapNotNull { performanceCache.getNote(it) }
        }

        // Fallback to database
        val dbResults =
            withContext(Dispatchers.IO) {
                baseNoteDao.getBaseNotesByLabel(label).value ?: emptyList()
            }

        // Update cache
        dbResults.forEach { performanceCache.updateNote(it) }

        return dbResults
    }

    /** Update note in all performance systems */
    suspend fun updateNote(note: BaseNote) {
        performanceCache.updateNote(note)
        incrementalIndexer.updateInIndex(note)
        smartPreloader.recordAccess(note.id, "update")

        // Trigger related preloading
        smartPreloader.preloadRelatedNotes(note.id, baseNoteDao.getAll(), performanceCache)
    }

    /** Delete note from all performance systems */
    suspend fun deleteNote(noteId: Long) {
        performanceCache.removeNote(noteId)
        incrementalIndexer.removeFromIndex(noteId)

        log("Note $noteId removed from performance systems")
    }

    /** Trigger smart preloading */
    suspend fun triggerSmartPreloading() {
        val allNotes = withContext(Dispatchers.IO) { baseNoteDao.getAll() }
        smartPreloader.preloadSmartNotes(allNotes, performanceCache)
    }

    /** Get comprehensive performance statistics */
    suspend fun getPerformanceStats(): PerformanceStats {
        val cacheStats = performanceCache.getCacheStats()
        val indexStats = incrementalIndexer.getIndexStats()
        val preloaderStats = smartPreloader.getPreloaderStats()

        return PerformanceStats(
            cacheStats = cacheStats,
            indexStats = indexStats,
            preloaderStats = preloaderStats,
            isReady = _isPerformanceReady.value,
            lastUpdateTime = System.currentTimeMillis(),
        )
    }

    /** Start performance monitoring */
    private fun startPerformanceMonitoring() {
        performanceScope.launch {
            while (true) {
                kotlinx.coroutines.delay(PERFORMANCE_UPDATE_INTERVAL_MS)

                val stats = getPerformanceStats()
                _performanceStats.value = stats

                // Log performance metrics periodically
                logPerformanceMetrics(stats)
            }
        }
    }

    /** Log performance metrics */
    private fun logPerformanceMetrics(stats: PerformanceStats) {
        log(
            "Performance Stats - Cache: ${stats.cacheStats.hitRate}% hit rate, " +
                "Index: ${stats.indexStats.totalIndexedNotes} notes, " +
                "Preloader: ${stats.preloaderStats.successRate}% success rate"
        )
    }

    /** Check if keyword matches note (simplified version) */
    private fun matchesKeyword(note: BaseNote, keyword: String): Boolean {
        val query = keyword.lowercase()
        return note.title.lowercase().contains(query) ||
            note.body.lowercase().contains(query) ||
            note.labels.any { it.lowercase().contains(query) }
    }

    private fun log(message: String) {
        android.util.Log.d("PerformanceManager", message)
    }
}

/** Comprehensive performance statistics */
data class PerformanceStats(
    val cacheStats: CacheStats,
    val indexStats: IndexStats,
    val preloaderStats: PreloaderStats,
    val isReady: Boolean,
    val lastUpdateTime: Long,
)

/** Global performance manager instance */
object GlobalPerformanceManager {
    private var instance: PerformanceManager? = null

    fun getInstance(baseNoteDao: BaseNoteDao): PerformanceManager {
        return instance
            ?: synchronized(this) {
                instance ?: PerformanceManager(baseNoteDao).also { instance = it }
            }
    }

    fun reset() {
        instance = null
    }
}
