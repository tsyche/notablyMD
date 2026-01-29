package com.tsyche.notablymd.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MarkdownSyncManagerTest {

    private lateinit var context: Context
    private lateinit var syncManager: MarkdownSyncManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        syncManager = MarkdownSyncManager(context)
    }

    @Test
    fun markdownSyncManagerShouldInitializeSuccessfully() {
        assertNotNull("MarkdownSyncManager should be created", syncManager)
    }
}
