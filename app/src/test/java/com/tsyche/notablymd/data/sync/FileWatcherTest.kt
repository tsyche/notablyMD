package com.tsyche.notablymd.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FileWatcherTest {

    private lateinit var context: Context
    private lateinit var fileWatcher: FileWatcher

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        fileWatcher = FileWatcher(context)
    }

    @Test
    fun fileWatcherShouldInitializeSuccessfully() {
        assertNotNull("FileWatcher should be created", fileWatcher)
    }

    @Test
    fun startWatchingShouldNotThrow() = runBlocking {
        val testDir = File(context.cacheDir, "test_start")
        testDir.mkdirs()

        try {
            fileWatcher.startWatching()
            assertTrue("Start watching should not throw", true)
        } catch (e: Exception) {
            assertTrue("Should not throw exception: ${e.message}", false)
        } finally {
            fileWatcher.stopWatching()
            testDir.deleteRecursively()
        }
    }

    @Test
    fun stopWatchingShouldNotThrow() = runBlocking {
        val testDir = File(context.cacheDir, "test_stop")
        testDir.mkdirs()

        try {
            fileWatcher.startWatching()
            fileWatcher.stopWatching()
            assertTrue("Stop watching should not throw", true)
        } catch (e: Exception) {
            assertTrue("Should not throw exception: ${e.message}", false)
        } finally {
            testDir.deleteRecursively()
        }
    }
}
