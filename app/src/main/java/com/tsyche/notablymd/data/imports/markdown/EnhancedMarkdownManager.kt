package com.tsyche.notablymd.data.imports.markdown

import android.content.Context
import com.tsyche.notablymd.data.model.BaseNote
import com.tsyche.notablymd.data.model.Folder
import com.tsyche.notablymd.data.model.NoteViewMode
import com.tsyche.notablymd.data.model.Type
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.parser.Parser

/**
 * Enhanced Markdown Manager for high-level markdown operations Provides clean API with Result types
 * for better error handling
 */
class EnhancedMarkdownManager(private val context: Context) {

    private val parser =
        Parser.builder().extensions(listOf(StrikethroughExtension.create())).build()

    /** Read a markdown file and convert to BaseNote */
    suspend fun readNote(file: File): Result<BaseNote> =
        withContext(Dispatchers.IO) {
            try {
                if (!file.exists()) {
                    return@withContext Result.failure(
                        IOException("File does not exist: ${file.path}")
                    )
                }

                val content = file.readText()
                val note = parseMarkdownToNote(content, file)
                Result.success(note)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Write a BaseNote to markdown file */
    suspend fun writeNote(note: BaseNote, file: File): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                // Create parent directories if they don't exist
                file.parentFile?.mkdirs()

                val markdownContent = convertNoteToMarkdown(note)
                file.writeText(markdownContent)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Generate YAML frontmatter for a BaseNote */
    suspend fun generateYAMLFrontmatter(note: BaseNote): String =
        withContext(Dispatchers.Default) {
            buildString {
                appendLine("---")
                appendLine("title: \"${note.title}\"")
                appendLine("created: ${note.timestamp}")
                appendLine("modified: ${note.modifiedTimestamp}")
                appendLine("type: ${note.type}")
                appendLine("folder: ${note.folder}")

                if (note.color != BaseNote.COLOR_DEFAULT) {
                    appendLine("color: ${note.color}")
                }

                if (note.pinned) {
                    appendLine("pinned: true")
                }

                if (note.labels.isNotEmpty()) {
                    appendLine("labels: [${note.labels.joinToString(", ") { "\"$it\"" }}]")
                }

                if (note.items.isNotEmpty()) {
                    appendLine("items: ${note.items.size}")
                }

                if (note.images.isNotEmpty()) {
                    appendLine("images: ${note.images.size}")
                }

                if (note.audios.isNotEmpty()) {
                    appendLine("audios: ${note.audios.size}")
                }

                if (note.files.isNotEmpty()) {
                    appendLine("files: ${note.files.size}")
                }

                if (note.reminders.isNotEmpty()) {
                    appendLine("reminders: ${note.reminders.size}")
                }

                appendLine("viewMode: ${note.viewMode}")
                appendLine("---")
            }
        }

    /** Parse YAML frontmatter from markdown content */
    suspend fun parseYAMLFrontmatter(content: String): Result<NoteMetadata> =
        withContext(Dispatchers.Default) {
            try {
                if (!content.startsWith("---")) {
                    return@withContext Result.success(NoteMetadata())
                }

                val endIndex = content.indexOf("---", 3)
                if (endIndex == -1) {
                    return@withContext Result.success(NoteMetadata())
                }

                val frontMatterText = content.substring(3, endIndex).trim()
                val metadata = parseFrontMatterLines(frontMatterText)
                Result.success(metadata)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Parse markdown content to BaseNote (internal implementation) */
    private suspend fun parseMarkdownToNote(content: String, file: File): BaseNote {
        val frontMatterResult = parseYAMLFrontmatter(content)
        val metadata = frontMatterResult.getOrDefault(NoteMetadata())

        // Extract body content (remove frontmatter)
        val bodyContent =
            if (content.startsWith("---")) {
                val endIndex = content.indexOf("---", 3)
                if (endIndex > 0) {
                    content.substring(endIndex + 3).trim()
                } else {
                    content
                }
            } else {
                content
            }

        // Parse body and spans using existing MarkdownUtils
        // TODO: Re-enable when MarkdownUtils.convertToMarkdown is available
        val parseResult =
            Pair(bodyContent, emptyList<com.tsyche.notablymd.data.model.SpanRepresentation>())
        // val parseResult = MarkdownUtils.parseBodyAndSpansFromMarkdown(bodyContent)
        val body = parseResult.first
        val spans = parseResult.second

        return BaseNote(
            id = metadata.id ?: file.nameWithoutExtension.toLongOrNull() ?: 0L,
            type = metadata.type ?: Type.NOTE,
            folder = metadata.folder ?: Folder.NOTES,
            color = metadata.color ?: BaseNote.COLOR_DEFAULT,
            title = metadata.title ?: file.nameWithoutExtension,
            pinned = metadata.pinned ?: false,
            timestamp = metadata.created ?: file.lastModified(),
            modifiedTimestamp = metadata.modified ?: file.lastModified(),
            labels = metadata.labels ?: emptyList(),
            body = body,
            spans = spans,
            items = emptyList(), // TODO: Parse from markdown if needed
            images = emptyList(), // TODO: Parse from markdown if needed
            files = emptyList(), // TODO: Parse from markdown if needed
            audios = emptyList(), // TODO: Parse from markdown if needed
            reminders = emptyList(), // TODO: Parse from markdown if needed
            viewMode = metadata.viewMode ?: NoteViewMode.EDIT,
        )
    }

    /** Convert BaseNote to markdown (internal implementation) */
    private suspend fun convertNoteToMarkdown(note: BaseNote): String {
        val frontMatter = generateYAMLFrontmatter(note)
        // TODO: Re-enable when MarkdownUtils.convertToMarkdown is available
        val bodyContent = note.body // MarkdownUtils.convertToMarkdown(note.body, note.spans)

        return "$frontMatter\n$bodyContent"
    }

    /** Parse individual frontmatter lines */
    private fun parseFrontMatterLines(frontMatterText: String): NoteMetadata {
        val metadata = NoteMetadata()

        frontMatterText.lines().forEach { line ->
            if (line.contains(":")) {
                val parts = line.split(":", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim().removeSurrounding("\"")

                    when (key) {
                        "title" -> metadata.title = value
                        "created" -> metadata.created = value.toLongOrNull()
                        "modified" -> metadata.modified = value.toLongOrNull()
                        "type" -> metadata.type = Type.valueOf(value)
                        "folder" -> metadata.folder = Folder.valueOf(value)
                        "color" -> metadata.color = value
                        "pinned" -> metadata.pinned = value.toBooleanStrictOrNull()
                        "labels" -> metadata.labels = parseLabels(value)
                        "viewMode" -> metadata.viewMode = NoteViewMode.valueOf(value)
                    }
                }
            }
        }

        return metadata
    }

    /** Parse labels from string representation */
    private fun parseLabels(value: String): List<String>? {
        return try {
            if (value.startsWith("[") && value.endsWith("]")) {
                val content = value.substring(1, value.length - 1)
                content
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            null
        }
    }
}

/** Data class to hold parsed frontmatter metadata */
data class NoteMetadata(
    var id: Long? = null,
    var title: String? = null,
    var created: Long? = null,
    var modified: Long? = null,
    var type: Type? = null,
    var folder: Folder? = null,
    var color: String? = null,
    var pinned: Boolean? = null,
    var labels: List<String>? = null,
    var viewMode: NoteViewMode? = null,
)
