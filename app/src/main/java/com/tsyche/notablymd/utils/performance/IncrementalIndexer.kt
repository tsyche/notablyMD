package com.tsyche.notablymd.utils.performance

import com.tsyche.notablymd.data.model.BaseNote
import com.tsyche.notablymd.data.model.Folder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Incremental indexing system for fast search and filtering */
open class IncrementalIndexer {

    private val indexerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Index structures
    private val titleIndex = ConcurrentHashMap<String, MutableSet<Long>>()
    private val bodyIndex = ConcurrentHashMap<String, MutableSet<Long>>()
    private val labelIndex = ConcurrentHashMap<String, MutableSet<Long>>()
    private val folderIndex = ConcurrentHashMap<Folder, MutableSet<Long>>()
    private val fullTextIndex = ConcurrentHashMap<String, MutableSet<Long>>()

    // Index state tracking
    private val _isIndexReady = MutableStateFlow(false)
    val isIndexReady: StateFlow<Boolean> = _isIndexReady.asStateFlow()

    private val _indexProgress = MutableStateFlow(0f)
    val indexProgress: StateFlow<Float> = _indexProgress.asStateFlow()

    private val _lastIndexTime = MutableStateFlow(0L)
    val lastIndexTime: StateFlow<Long> = _lastIndexTime.asStateFlow()

    // Indexing state
    private val isIndexing = AtomicBoolean(false)
    private val totalNotes = AtomicLong(0)
    private val indexedNotes = AtomicLong(0)

    companion object {
        private const val MIN_WORD_LENGTH = 2
        private const val MAX_WORD_LENGTH = 50
        private val STOP_WORDS =
            setOf(
                "the",
                "a",
                "an",
                "and",
                "or",
                "but",
                "in",
                "on",
                "at",
                "to",
                "for",
                "of",
                "with",
                "by",
                "is",
                "are",
                "was",
                "were",
                "be",
                "been",
                "have",
                "has",
                "had",
                "do",
                "does",
                "did",
                "will",
                "would",
                "could",
                "should",
                "may",
                "might",
                "can",
                "this",
                "that",
                "these",
                "those",
                "i",
                "you",
                "he",
                "she",
                "it",
                "we",
                "they",
            )
    }

    /** Build initial index for all notes */
    fun buildIndex(notes: List<BaseNote>) {
        if (isIndexing.compareAndSet(false, true)) {
            indexerScope.launch {
                try {
                    clearIndex()
                    totalNotes.set(notes.size.toLong())
                    indexedNotes.set(0)

                    notes.forEachIndexed { index, note ->
                        indexNote(note)
                        indexedNotes.incrementAndGet()

                        // Update progress
                        val progress = (index + 1).toFloat() / notes.size
                        _indexProgress.value = progress
                    }

                    _lastIndexTime.value = System.currentTimeMillis()
                    _isIndexReady.value = true
                    _indexProgress.value = 1f

                    log("Index built successfully with ${notes.size} notes")
                } finally {
                    isIndexing.set(false)
                }
            }
        }
    }

    /** Add note to index incrementally */
    fun addToIndex(note: BaseNote) {
        indexerScope.launch {
            indexNote(note)
            _lastIndexTime.value = System.currentTimeMillis()
            log("Added note ${note.id} to index")
        }
    }

    /** Remove note from index */
    fun removeFromIndex(noteId: Long) {
        // Remove from all indices synchronously
        titleIndex.values.forEach { it.remove(noteId) }
        bodyIndex.values.forEach { it.remove(noteId) }
        labelIndex.values.forEach { it.remove(noteId) }
        folderIndex.values.forEach { it.remove(noteId) }
        fullTextIndex.values.forEach { it.remove(noteId) }

        // Clean up empty entries
        val emptyTitleKeys = titleIndex.entries.filter { it.value.isEmpty() }.map { it.key }
        emptyTitleKeys.forEach { titleIndex.remove(it) }

        val emptyBodyKeys = bodyIndex.entries.filter { it.value.isEmpty() }.map { it.key }
        emptyBodyKeys.forEach { bodyIndex.remove(it) }

        val emptyLabelKeys = labelIndex.entries.filter { it.value.isEmpty() }.map { it.key }
        emptyLabelKeys.forEach { labelIndex.remove(it) }

        val emptyFolderKeys = folderIndex.entries.filter { it.value.isEmpty() }.map { it.key }
        emptyFolderKeys.forEach { folderIndex.remove(it) }

        val emptyFullTextKeys = fullTextIndex.entries.filter { it.value.isEmpty() }.map { it.key }
        emptyFullTextKeys.forEach { fullTextIndex.remove(it) }

        _lastIndexTime.value = System.currentTimeMillis()
        log("Removed note $noteId from index")
    }

    /** Update note in index */
    fun updateInIndex(note: BaseNote) {
        // Remove from all indices synchronously
        titleIndex.values.forEach { it.remove(note.id) }
        bodyIndex.values.forEach { it.remove(note.id) }
        labelIndex.values.forEach { it.remove(note.id) }
        folderIndex.values.forEach { it.remove(note.id) }
        fullTextIndex.values.forEach { it.remove(note.id) }

        // Clean up empty entries
        val emptyTitleKeys = titleIndex.entries.filter { it.value.isEmpty() }.map { it.key }
        emptyTitleKeys.forEach { titleIndex.remove(it) }

        val emptyBodyKeys = bodyIndex.entries.filter { it.value.isEmpty() }.map { it.key }
        emptyBodyKeys.forEach { bodyIndex.remove(it) }

        val emptyLabelKeys = labelIndex.entries.filter { it.value.isEmpty() }.map { it.key }
        emptyLabelKeys.forEach { labelIndex.remove(it) }

        val emptyFolderKeys = folderIndex.entries.filter { it.value.isEmpty() }.map { it.key }
        emptyFolderKeys.forEach { folderIndex.remove(it) }

        val emptyFullTextKeys = fullTextIndex.entries.filter { it.value.isEmpty() }.map { it.key }
        emptyFullTextKeys.forEach { fullTextIndex.remove(it) }

        // Re-index the note
        indexNote(note)
        _lastIndexTime.value = System.currentTimeMillis()
        log("Updated note ${note.id} in index")
    }

    /** Search using incremental index */
    fun search(query: String): Set<Long> {
        if (!_isIndexReady.value) return emptySet()

        val terms = tokenizeQuery(query)
        if (terms.isEmpty()) return emptySet()

        var result: Set<Long>? = null

        for (term in terms) {
            val termResults = searchTerm(term)
            result =
                if (result == null) {
                    termResults
                } else {
                    result.intersect(termResults)
                }
        }

        return result ?: emptySet()
    }

    /** Get notes by folder using index */
    fun getNotesByFolder(folder: Folder): Set<Long> {
        return folderIndex[folder] ?: emptySet()
    }

    /** Get notes by label using index */
    fun getNotesByLabel(label: String): Set<Long> {
        return labelIndex[label.lowercase()] ?: emptySet()
    }

    /** Get index statistics */
    fun getIndexStats(): IndexStats {
        return IndexStats(
            totalIndexedNotes = totalNotes.get(),
            titleIndexSize = titleIndex.size,
            bodyIndexSize = bodyIndex.size,
            labelIndexSize = labelIndex.size,
            folderIndexSize = folderIndex.size,
            fullTextIndexSize = fullTextIndex.size,
            lastIndexTime = _lastIndexTime.value,
            isReady = _isIndexReady.value,
        )
    }

    /** Index a single note */
    private fun indexNote(note: BaseNote) {
        // Index title
        tokenizeText(note.title).forEach { word ->
            titleIndex.getOrPut(word) { mutableSetOf() }.add(note.id)
            fullTextIndex.getOrPut(word) { mutableSetOf() }.add(note.id)
        }

        // Index body
        tokenizeText(note.body).forEach { word ->
            bodyIndex.getOrPut(word) { mutableSetOf() }.add(note.id)
            fullTextIndex.getOrPut(word) { mutableSetOf() }.add(note.id)
        }

        // Index labels
        note.labels.forEach { label ->
            val normalizedLabel = label.lowercase()
            labelIndex.getOrPut(normalizedLabel) { mutableSetOf() }.add(note.id)
            fullTextIndex.getOrPut(normalizedLabel) { mutableSetOf() }.add(note.id)
        }

        // Index folder
        folderIndex.getOrPut(note.folder) { mutableSetOf() }.add(note.id)
    }

    /** Search for a single term */
    private fun searchTerm(term: String): Set<Long> {
        val normalizedTerm = term.lowercase()

        // Search in all indices
        val results = mutableSetOf<Long>()

        titleIndex[normalizedTerm]?.let { results.addAll(it) }
        bodyIndex[normalizedTerm]?.let { results.addAll(it) }
        labelIndex[normalizedTerm]?.let { results.addAll(it) }

        // Also try partial matches
        fullTextIndex.keys.forEach { key ->
            if (key.contains(normalizedTerm)) {
                fullTextIndex[key]?.let { results.addAll(it) }
            }
        }

        return results
    }

    /** Tokenize text into searchable terms */
    private fun tokenizeText(text: String): List<String> {
        return text
            .lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { word ->
                word.length in MIN_WORD_LENGTH..MAX_WORD_LENGTH && word !in STOP_WORDS
            }
    }

    /** Tokenize search query */
    private fun tokenizeQuery(query: String): List<String> {
        return query
            .lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
    }

    /** Clear all indices */
    private fun clearIndex() {
        titleIndex.clear()
        bodyIndex.clear()
        labelIndex.clear()
        folderIndex.clear()
        fullTextIndex.clear()
        _isIndexReady.value = false
        _indexProgress.value = 0f
    }

    private fun log(message: String) {
        android.util.Log.d("IncrementalIndexer", message)
    }
}

/** Index statistics data class */
data class IndexStats(
    val totalIndexedNotes: Long,
    val titleIndexSize: Int,
    val bodyIndexSize: Int,
    val labelIndexSize: Int,
    val folderIndexSize: Int,
    val fullTextIndexSize: Int,
    val lastIndexTime: Long,
    val isReady: Boolean,
)

/** Global incremental indexer instance */
object GlobalIncrementalIndexer : IncrementalIndexer()
