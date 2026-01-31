package com.tsyche.notablymd.utils.performance

import com.tsyche.notablymd.data.model.BaseNote
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Smart preloading system for better UX */
open class SmartPreloader {

    private val preloaderScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Usage tracking
    private val accessFrequency = ConcurrentHashMap<Long, AtomicLong>()
    private val lastAccessTime = ConcurrentHashMap<Long, AtomicLong>()
    private val accessPatterns = ConcurrentHashMap<String, MutableList<Long>>()

    // Preloading state
    private val _isPreloading = MutableStateFlow(false)
    val isPreloading: StateFlow<Boolean> = _isPreloading.asStateFlow()

    private val _preloadingProgress = MutableStateFlow(0f)
    val preloadingProgress: StateFlow<Float> = _preloadingProgress.asStateFlow()

    private val isPreloadingActive = AtomicBoolean(false)

    // Preloading statistics
    private var totalPreloads = 0
    private var successfulPreloads = 0
    private var preloadHits = 0

    companion object {
        private const val PRELOAD_DELAY_MS = 500L
        private const val PRELOAD_BATCH_SIZE = 20
        private const val FREQUENCY_THRESHOLD = 3
        private const val RECENT_ACCESS_THRESHOLD_MS = 24 * 60 * 60 * 1000L // 24 hours
        private const val PRELOAD_MEMORY_LIMIT_MB = 50
    }

    /** Record note access for pattern analysis */
    fun recordAccess(noteId: Long, accessType: String = "general") {
        val currentTime = System.currentTimeMillis()

        // Update frequency
        accessFrequency.getOrPut(noteId) { AtomicLong(0) }.incrementAndGet()

        // Update last access time
        lastAccessTime[noteId] = AtomicLong(currentTime)

        // Update access patterns
        val pattern = accessPatterns.getOrPut(accessType) { mutableListOf() }
        pattern.add(noteId)

        // Keep pattern size manageable
        if (pattern.size > 100) {
            pattern.removeAt(0)
        }

        // Trigger smart preloading if needed
        triggerSmartPreload(noteId)
    }

    /** Preload notes based on usage patterns */
    suspend fun preloadSmartNotes(allNotes: List<BaseNote>, performanceCache: PerformanceCache) {
        if (isPreloadingActive.compareAndSet(false, true)) {
            try {
                _isPreloading.value = true
                _preloadingProgress.value = 0f

                val preloadCandidates = identifyPreloadCandidates(allNotes)
                val totalCandidates = preloadCandidates.size

                preloadCandidates.chunked(PRELOAD_BATCH_SIZE).forEachIndexed { index, batch ->
                    batch.forEach { note ->
                        if (shouldPreload(note)) {
                            performanceCache.updateNote(note)
                            successfulPreloads++
                        }
                        totalPreloads++
                    }

                    // Update progress
                    val progress = ((index + 1) * PRELOAD_BATCH_SIZE).toFloat() / totalCandidates
                    _preloadingProgress.value = progress.coerceAtMost(1f)

                    // Small delay to avoid blocking UI
                    delay(PRELOAD_DELAY_MS)
                }

                _preloadingProgress.value = 1f
                log("Smart preloading completed: $successfulPreloads/$totalPreloads notes")
            } finally {
                _isPreloading.value = false
                isPreloadingActive.set(false)
            }
        }
    }

    /** Preload recently accessed notes */
    suspend fun preloadRecentNotes(allNotes: List<BaseNote>, performanceCache: PerformanceCache) {
        val currentTime = System.currentTimeMillis()
        val recentNotes =
            allNotes
                .filter { note ->
                    val lastAccess = lastAccessTime[note.id]?.get() ?: 0L
                    (currentTime - lastAccess) < RECENT_ACCESS_THRESHOLD_MS
                }
                .sortedByDescending { note -> lastAccessTime[note.id]?.get() ?: 0L }
                .take(PRELOAD_BATCH_SIZE)

        recentNotes.forEach { note ->
            performanceCache.updateNote(note)
            totalPreloads++
            successfulPreloads++
        }

        log("Preloaded ${recentNotes.size} recently accessed notes")
    }

    /** Preload pinned notes */
    suspend fun preloadPinnedNotes(allNotes: List<BaseNote>, performanceCache: PerformanceCache) {
        val pinnedNotes = allNotes.filter { it.pinned }

        pinnedNotes.forEach { note ->
            performanceCache.updateNote(note)
            totalPreloads++
            successfulPreloads++
        }

        log("Preloaded ${pinnedNotes.size} pinned notes")
    }

    /** Preload frequently accessed notes */
    suspend fun preloadFrequentNotes(allNotes: List<BaseNote>, performanceCache: PerformanceCache) {
        val frequentNotes =
            allNotes
                .filter { note ->
                    val frequency = accessFrequency[note.id]?.get() ?: 0L
                    frequency >= FREQUENCY_THRESHOLD
                }
                .sortedByDescending { note -> accessFrequency[note.id]?.get() ?: 0L }
                .take(PRELOAD_BATCH_SIZE)

        frequentNotes.forEach { note ->
            performanceCache.updateNote(note)
            totalPreloads++
            successfulPreloads++
        }

        log("Preloaded ${frequentNotes.size} frequently accessed notes")
    }

    /** Preload related notes based on patterns */
    suspend fun preloadRelatedNotes(
        noteId: Long,
        allNotes: List<BaseNote>,
        performanceCache: PerformanceCache,
    ) {
        val note = allNotes.find { it.id == noteId } ?: return

        val relatedNotes = mutableListOf<BaseNote>()

        // Add notes with same labels
        note.labels.forEach { label ->
            relatedNotes.addAll(
                allNotes.filter { other -> other.id != noteId && other.labels.contains(label) }
            )
        }

        // Add notes from same folder
        relatedNotes.addAll(
            allNotes.filter { other -> other.id != noteId && other.folder == note.folder }
        )

        // Add notes with similar titles (simple heuristic)
        val titleWords = note.title.lowercase().split(" ")
        titleWords.forEach { word ->
            if (word.length > 3) {
                relatedNotes.addAll(
                    allNotes.filter { other ->
                        other.id != noteId && other.title.lowercase().contains(word)
                    }
                )
            }
        }

        // Remove duplicates and limit
        val uniqueRelatedNotes = relatedNotes.distinctBy { it.id }.take(PRELOAD_BATCH_SIZE)

        uniqueRelatedNotes.forEach { relatedNote ->
            performanceCache.updateNote(relatedNote)
            totalPreloads++
            successfulPreloads++
        }

        log("Preloaded ${uniqueRelatedNotes.size} related notes for note $noteId")
    }

    /** Check if a note should be preloaded */
    private fun shouldPreload(note: BaseNote): Boolean {
        val frequency = accessFrequency[note.id]?.get() ?: 0L
        val lastAccess = lastAccessTime[note.id]?.get() ?: 0L
        val currentTime = System.currentTimeMillis()

        // Don't preload if recently accessed (already in cache)
        if ((currentTime - lastAccess) < PRELOAD_DELAY_MS) {
            return false
        }

        // Preload if frequently accessed
        if (frequency >= FREQUENCY_THRESHOLD) {
            return true
        }

        // Preload if recently created or modified
        if ((currentTime - note.timestamp) < RECENT_ACCESS_THRESHOLD_MS) {
            return true
        }

        // Preload pinned notes
        if (note.pinned) {
            return true
        }

        return false
    }

    /** Identify candidates for preloading */
    private fun identifyPreloadCandidates(allNotes: List<BaseNote>): List<BaseNote> {
        val candidates = mutableListOf<BaseNote>()
        val currentTime = System.currentTimeMillis()

        allNotes.forEach { note ->
            val frequency = accessFrequency[note.id]?.get() ?: 0L
            val lastAccess = lastAccessTime[note.id]?.get() ?: 0L

            // High priority: frequently accessed
            if (frequency >= FREQUENCY_THRESHOLD) {
                candidates.add(note)
                return@forEach
            }

            // Medium priority: recently accessed
            if ((currentTime - lastAccess) < RECENT_ACCESS_THRESHOLD_MS) {
                candidates.add(note)
                return@forEach
            }

            // Low priority: pinned or recently modified
            if (note.pinned || (currentTime - note.timestamp) < RECENT_ACCESS_THRESHOLD_MS) {
                candidates.add(note)
                return@forEach
            }
        }

        // Sort by priority and limit
        return candidates
            .distinctBy { it.id }
            .sortedWith(
                compareByDescending<BaseNote> { accessFrequency[it.id]?.get() ?: 0L }
                    .thenByDescending { lastAccessTime[it.id]?.get() ?: 0L }
            )
            .take(PRELOAD_BATCH_SIZE * 2)
    }

    /** Trigger smart preloading based on access patterns */
    private fun triggerSmartPreload(noteId: Long) {
        preloaderScope.launch {
            delay(PRELOAD_DELAY_MS) // Delay to avoid immediate preloading

            // This would be connected to the actual cache system
            // For now, we just log the trigger
            log("Smart preload triggered for note $noteId")
        }
    }

    /** Get preloading statistics */
    fun getPreloaderStats(): PreloaderStats {
        return PreloaderStats(
            totalPreloads = totalPreloads,
            successfulPreloads = successfulPreloads,
            preloadHits = preloadHits,
            successRate =
                if (totalPreloads > 0) (successfulPreloads.toFloat() / totalPreloads) * 100 else 0f,
            trackedNotes = accessFrequency.size,
            activePatterns = accessPatterns.size,
        )
    }

    /** Clear usage tracking data */
    fun clearTrackingData() {
        accessFrequency.clear()
        lastAccessTime.clear()
        accessPatterns.clear()
        totalPreloads = 0
        successfulPreloads = 0
        preloadHits = 0
        log("Preloader tracking data cleared")
    }

    /** Get access patterns for analytics */
    fun getAccessPatterns(): Map<String, List<Long>> {
        return accessPatterns.mapValues { it.value.toList() }
    }

    private fun log(message: String) {
        android.util.Log.d("SmartPreloader", message)
    }
}

/** Preloader statistics data class */
data class PreloaderStats(
    val totalPreloads: Int,
    val successfulPreloads: Int,
    val preloadHits: Int,
    val successRate: Float,
    val trackedNotes: Int,
    val activePatterns: Int,
)

/** Global smart preloader instance */
object GlobalSmartPreloader : SmartPreloader()
