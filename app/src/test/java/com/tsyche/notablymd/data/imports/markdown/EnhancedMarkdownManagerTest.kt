package com.tsyche.notablymd.data.imports.markdown

import com.tsyche.notablymd.data.model.BaseNote
import com.tsyche.notablymd.data.model.Folder
import com.tsyche.notablymd.data.model.ListItem
import com.tsyche.notablymd.data.model.NoteViewMode
import com.tsyche.notablymd.data.model.SpanRepresentation
import com.tsyche.notablymd.data.model.Type
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EnhancedMarkdownManagerTest {

    @get:Rule val tmpDir = TemporaryFolder()

    private val manager by lazy {
        EnhancedMarkdownManager(RuntimeEnvironment.getApplication())
    }

    // --- Type.NOTE roundtrip ---

    @Test
    fun noteRoundtrip_plainBody() = runBlocking {
        val note = baseNote(body = "Hello world", spans = emptyList())
        val file = tmpDir.newFile("${note.id}.md")

        manager.writeNote(note, file)
        val result = manager.readNote(file).getOrThrow()

        assertEquals(note.title, result.title)
        assertEquals(note.body, result.body)
        assertEquals(note.type, result.type)
        assertEquals(note.folder, result.folder)
    }

    @Test
    fun noteRoundtrip_withBoldSpan() = runBlocking {
        val body = "Hello world"
        val spans = listOf(SpanRepresentation(0, 5, bold = true))
        val note = baseNote(body = body, spans = spans)
        val file = tmpDir.newFile("${note.id}.md")

        manager.writeNote(note, file)
        val raw = file.readText()

        // Markdown body should contain bold syntax
        assertTrue("Expected bold markdown", raw.contains("**Hello**"))

        val result = manager.readNote(file).getOrThrow()
        assertEquals(body, result.body)
        assertTrue("Expected bold span to survive roundtrip", result.spans.any { it.bold })
    }

    @Test
    fun noteRoundtrip_frontmatterPreservesMetadata() = runBlocking {
        val note = baseNote(title = "My Note", pinned = true)
        val file = tmpDir.newFile("${note.id}.md")

        manager.writeNote(note, file)
        val result = manager.readNote(file).getOrThrow()

        assertEquals("My Note", result.title)
        assertTrue(result.pinned)
        assertEquals(Folder.NOTES, result.folder)
    }

    // --- Type.LIST roundtrip ---

    @Test
    fun listRoundtrip_basicItems() = runBlocking {
        val items =
            listOf(
                ListItem("Buy milk", false, false, 0, mutableListOf()),
                ListItem("Buy eggs", true, false, 1, mutableListOf()),
            )
        val note = baseNote(type = Type.LIST, items = items)
        val file = tmpDir.newFile("${note.id}.md")

        manager.writeNote(note, file)
        val raw = file.readText()

        assertTrue("Expected unchecked item", raw.contains("- [ ] Buy milk"))
        assertTrue("Expected checked item", raw.contains("- [x] Buy eggs"))

        val result = manager.readNote(file).getOrThrow()
        assertEquals(Type.LIST, result.type)
        assertEquals(2, result.items.size)
        assertEquals("Buy milk", result.items[0].body)
        assertFalse(result.items[0].checked)
        assertEquals("Buy eggs", result.items[1].body)
        assertTrue(result.items[1].checked)
    }

    @Test
    fun listRoundtrip_withChildren() = runBlocking {
        val child = ListItem("Sub-task", true, true, null, mutableListOf())
        val parent = ListItem("Parent task", false, false, 0, mutableListOf(child))
        val note = baseNote(type = Type.LIST, items = listOf(parent))
        val file = tmpDir.newFile("${note.id}.md")

        manager.writeNote(note, file)
        val raw = file.readText()

        assertTrue("Expected parent item", raw.contains("- [ ] Parent task"))
        assertTrue("Expected indented child", raw.contains("  - [x] Sub-task"))

        val result = manager.readNote(file).getOrThrow()
        assertEquals(1, result.items.size)
        assertEquals(1, result.items[0].children.size)
        assertEquals("Sub-task", result.items[0].children[0].body)
        assertTrue(result.items[0].children[0].checked)
    }

    @Test
    fun listNote_bodyIsEmpty() = runBlocking {
        val note = baseNote(type = Type.LIST, items = listOf(ListItem("item", false, false, 0, mutableListOf())))
        val file = tmpDir.newFile("${note.id}.md")

        manager.writeNote(note, file)
        val result = manager.readNote(file).getOrThrow()

        assertEquals("", result.body)
        assertTrue(result.spans.isEmpty())
    }

    // --- File not found ---

    @Test
    fun readNote_missingFile_returnsFailure() = runBlocking {
        val missing = File(tmpDir.root, "nonexistent.md")
        val result = manager.readNote(missing)
        assertTrue(result.isFailure)
    }

    // --- Helpers ---

    private fun baseNote(
        id: Long = 42L,
        type: Type = Type.NOTE,
        title: String = "Test Note",
        body: String = "",
        spans: List<SpanRepresentation> = emptyList(),
        items: List<ListItem> = emptyList(),
        pinned: Boolean = false,
    ) =
        BaseNote(
            id = id,
            type = type,
            folder = Folder.NOTES,
            color = BaseNote.COLOR_DEFAULT,
            title = title,
            pinned = pinned,
            timestamp = 1000L,
            modifiedTimestamp = 2000L,
            labels = emptyList(),
            body = body,
            spans = spans,
            items = items,
            images = emptyList(),
            files = emptyList(),
            audios = emptyList(),
            reminders = emptyList(),
            viewMode = NoteViewMode.EDIT,
        )
}
