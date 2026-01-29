package com.tsyche.notablymd.data.sync

import com.tsyche.notablymd.data.model.BaseNote
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ConflictResolverTest {

    @Mock private lateinit var mockNote1: BaseNote

    @Mock private lateinit var mockNote2: BaseNote

    private lateinit var conflictResolver: ConflictResolver

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        conflictResolver = ConflictResolver()
    }

    @Test
    fun conflictResolverShouldInitializeSuccessfully() {
        assertNotNull("ConflictResolver should be created", conflictResolver)
    }

    @Test
    fun detectConflictShouldReturnConflictWhenTimestampsDiffer() = runBlocking {
        whenever(mockNote1.id).thenReturn(1L)
        whenever(mockNote2.id).thenReturn(1L)
        whenever(mockNote1.modifiedTimestamp).thenReturn(1000L)
        whenever(mockNote2.modifiedTimestamp).thenReturn(2000L)

        val conflict = conflictResolver.detectConflict(mockNote1, mockNote2)

        assertEquals("Should have same ID", 1L, conflict.databaseNote.id)
        assertEquals("Should have same ID", 1L, conflict.fileNote.id)
    }

    @Test
    fun detectConflictShouldReturnConflictWhenTimestampsAreSame() = runBlocking {
        whenever(mockNote1.id).thenReturn(1L)
        whenever(mockNote2.id).thenReturn(1L)
        whenever(mockNote1.modifiedTimestamp).thenReturn(1000L)
        whenever(mockNote2.modifiedTimestamp).thenReturn(1000L)

        val conflict = conflictResolver.detectConflict(mockNote1, mockNote2)

        assertEquals("Should have same ID", 1L, conflict.databaseNote.id)
        assertEquals("Should have same ID", 1L, conflict.fileNote.id)
    }
}
