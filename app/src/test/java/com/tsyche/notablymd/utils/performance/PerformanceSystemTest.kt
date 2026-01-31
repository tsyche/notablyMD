package com.tsyche.notablymd.utils.performance

import com.tsyche.notablymd.data.model.BaseNote
import com.tsyche.notablymd.data.model.Folder
import com.tsyche.notablymd.data.model.NoteViewMode
import com.tsyche.notablymd.data.model.Type
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PerformanceSystemTest {

    private lateinit var performanceCache: PerformanceCache
    private lateinit var incrementalIndexer: IncrementalIndexer
    private lateinit var smartPreloader: SmartPreloader

    @Before
    fun setUp() {
        performanceCache = PerformanceCache()
        incrementalIndexer = IncrementalIndexer()
        smartPreloader = SmartPreloader()
    }

    @Test
    fun `performance cache should store and retrieve notes`() = runBlocking {
        // Create test notes
        val notes = createTestNotes(10)

        // Initialize cache
        performanceCache.initialize(notes)

        // Wait for cache to be ready
        while (!performanceCache.isCacheReady.value) {
            kotlinx.coroutines.delay(10)
        }

        // Test retrieval
        val note = performanceCache.getNote(notes[0].id)
        assertNotNull("Should retrieve note from cache", note)
        assertEquals("Should retrieve correct note", notes[0].title, note?.title)

        // Test cache stats
        val stats = performanceCache.getCacheStats()
        assertTrue("Cache should have notes", stats.cacheSize > 0)
        assertEquals("Should have preloaded items", notes.size, stats.preloadedItems)
    }

    @Test
    fun `incremental indexer should search notes efficiently`() = runBlocking {
        // Create test notes
        val notes = createTestNotes(20)

        // Build index
        incrementalIndexer.buildIndex(notes)

        // Wait for index to be ready
        while (!incrementalIndexer.isIndexReady.value) {
            kotlinx.coroutines.delay(10)
        }

        // Test search
        val searchResults = incrementalIndexer.search("test")
        assertTrue("Should find search results", searchResults.isNotEmpty())

        // Test folder search
        val folderResults = incrementalIndexer.getNotesByFolder(Folder.NOTES)
        assertTrue("Should find notes by folder", folderResults.isNotEmpty())

        // Test index stats
        val stats = incrementalIndexer.getIndexStats()
        assertTrue("Index should be ready", stats.isReady)
        assertEquals("Should have indexed notes", notes.size.toLong(), stats.totalIndexedNotes)
    }

    @Test
    fun `smart preloader should track access patterns`() = runBlocking {
        // Create test notes
        val notes = createTestNotes(15)

        // Simulate access patterns
        repeat(5) { smartPreloader.recordAccess(notes[0].id, "test") }

        repeat(3) { smartPreloader.recordAccess(notes[1].id, "test") }

        // Test preloading
        smartPreloader.preloadFrequentNotes(notes, performanceCache)

        // Test stats
        val stats = smartPreloader.getPreloaderStats()
        assertTrue("Should track notes", stats.trackedNotes > 0)
        assertEquals(
            "Should have successful preloads",
            stats.totalPreloads,
            stats.successfulPreloads,
        )
    }

    @Test
    fun `performance systems should work together`() = runBlocking {
        // Create test notes
        val notes = createTestNotes(30)

        // Initialize all systems
        performanceCache.initialize(notes)
        incrementalIndexer.buildIndex(notes)

        // Wait for systems to be ready
        while (!performanceCache.isCacheReady.value || !incrementalIndexer.isIndexReady.value) {
            kotlinx.coroutines.delay(10)
        }

        // Simulate usage
        val targetNote = notes[0]
        smartPreloader.recordAccess(targetNote.id, "integration_test")

        // Update note in all systems
        val updatedNote = targetNote.copy(title = "Updated Title")
        performanceCache.updateNote(updatedNote)

        // Update index (now synchronous)
        incrementalIndexer.updateInIndex(updatedNote)

        // Verify updates
        val cachedNote = performanceCache.getNote(updatedNote.id)
        assertEquals("Cache should have updated note", "Updated Title", cachedNote?.title)

        val searchResults = incrementalIndexer.search("Updated")
        assertTrue("Index should find updated note", searchResults.contains(updatedNote.id))
    }

    @Test
    fun `performance cache should handle large datasets`() = runBlocking {
        // Create large dataset
        val notes = createTestNotes(1000)

        // Initialize cache
        performanceCache.initialize(notes)

        // Wait for cache to be ready
        while (!performanceCache.isCacheReady.value) {
            kotlinx.coroutines.delay(10)
        }

        // Test performance
        val startTime = System.currentTimeMillis()

        // Perform multiple operations
        repeat(100) { i ->
            val note = performanceCache.getNote(notes[i % notes.size].id)
            assertNotNull("Should retrieve note quickly", note)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Should complete within reasonable time (less than 1 second for 100 operations)
        assertTrue("Operations should be fast", duration < 1000)

        // Test cache stats
        val stats = performanceCache.getCacheStats()
        assertTrue("Should have good hit rate", stats.hitRate > 90f)
    }

    @Test
    fun `incremental indexer should handle incremental updates`() = runBlocking {
        // Create initial notes
        val initialNotes = createTestNotes(10)
        incrementalIndexer.buildIndex(initialNotes)

        // Wait for index to be ready
        while (!incrementalIndexer.isIndexReady.value) {
            kotlinx.coroutines.delay(10)
        }

        // Add new note
        val newNote = createTestNotes(1)[0]
        incrementalIndexer.addToIndex(newNote)

        // Search for new note
        val searchResults = incrementalIndexer.search(newNote.title.split(" ").first())
        assertTrue("Should find newly added note", searchResults.contains(newNote.id))

        // Update note
        val updatedNote = newNote.copy(title = "Modified Title")
        incrementalIndexer.updateInIndex(updatedNote)

        // Search for updated content
        val updatedResults = incrementalIndexer.search("Modified")
        assertTrue("Should find updated note", updatedResults.contains(updatedNote.id))

        // Remove note
        incrementalIndexer.removeFromIndex(updatedNote.id)

        // Should not find removed note
        val finalResults = incrementalIndexer.search("Modified")
        assertFalse("Should not find removed note", finalResults.contains(updatedNote.id))
    }

    private fun createTestNotes(count: Int): List<BaseNote> {
        return (1..count).map { i ->
            BaseNote(
                id = i.toLong(),
                title = "Test Note $i",
                body = "This is the body of test note $i with some content for searching.",
                folder = Folder.NOTES,
                type = Type.NOTE,
                pinned = i % 5 == 0, // Every 5th note is pinned
                timestamp = System.currentTimeMillis() - (i * 1000),
                modifiedTimestamp = System.currentTimeMillis() - (i * 500),
                labels = if (i % 3 == 0) listOf("label${i % 3}") else emptyList(),
                color = if (i % 4 == 0) "#FF0000" else BaseNote.COLOR_DEFAULT,
                items = emptyList(),
                images = emptyList(),
                files = emptyList(),
                audios = emptyList(),
                reminders = emptyList(),
                spans = emptyList(),
                viewMode = NoteViewMode.EDIT,
            )
        }
    }
}
