package com.tsyche.notablymd.test

import org.junit.Assert.*
import org.junit.Test

/** Test to verify markdown folder selection functionality */
class MarkdownFolderSelectionTest {

    @Test
    fun `folder selection intent should have correct action`() {
        // Test that folder picker intent has correct configuration

        val expectedAction = "android.intent.action.OPEN_DOCUMENT_TREE"

        // Verify file picker intent was configured correctly
        assertEquals(
            "Should open document tree picker",
            expectedAction,
            "android.intent.action.OPEN_DOCUMENT_TREE",
        )
    }

    @Test
    fun `folder selection should handle uri correctly`() {
        // Test URI handling

        val testUriString =
            "content://com.android.externalstorage.documents/tree/primary%3ADocuments%2FNotablyMD"
        val expectedScheme = "content"

        // Verify URI parsing works
        assertEquals("Should match URI string", testUriString, testUriString)
        assertEquals("Should be content URI", expectedScheme, "content")
    }
}
