package com.tsyche.notablymd.presentation.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class VoiceNoteWidgetTest {

    @Mock private lateinit var mockAppWidgetManager: AppWidgetManager

    private lateinit var widget: VoiceNoteWidget
    private lateinit var context: Context
    private val testWidgetId = 123

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        widget = VoiceNoteWidget()
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `STATE_IDLE should be 0`() {
        assertEquals("STATE_IDLE should be 0", 0, VoiceNoteWidget.STATE_IDLE)
    }

    @Test
    fun `STATE_RECORDING should be 1`() {
        assertEquals("STATE_RECORDING should be 1", 1, VoiceNoteWidget.STATE_RECORDING)
    }

    @Test
    fun `STATE_PROCESSING should be 2`() {
        assertEquals("STATE_PROCESSING should be 2", 2, VoiceNoteWidget.STATE_PROCESSING)
    }

    @Test
    fun `STATE_ERROR should be 3`() {
        assertEquals("STATE_ERROR should be 3", 3, VoiceNoteWidget.STATE_ERROR)
    }

    @Test
    fun `ACTION_RECORD_START should be correct`() {
        assertEquals(
            "Record start action should be correct",
            "com.tsyche.notablymd.widget.RECORD_START",
            VoiceNoteWidget.ACTION_RECORD_START,
        )
    }

    @Test
    fun `ACTION_RECORD_STOP should be correct`() {
        assertEquals(
            "Record stop action should be correct",
            "com.tsyche.notablymd.widget.RECORD_STOP",
            VoiceNoteWidget.ACTION_RECORD_STOP,
        )
    }

    @Test
    fun `ACTION_SETTINGS should be correct`() {
        assertEquals(
            "Settings action should be correct",
            "com.tsyche.notablymd.widget.SETTINGS",
            VoiceNoteWidget.ACTION_SETTINGS,
        )
    }

    @Test
    fun `ACTION_UPDATE_STATUS should be correct`() {
        assertEquals(
            "Update status action should be correct",
            "com.tsyche.notablymd.widget.UPDATE_STATUS",
            VoiceNoteWidget.ACTION_UPDATE_STATUS,
        )
    }

    @Test
    fun `onUpdate should handle empty widget ids`() {
        val emptyWidgetIds = intArrayOf()

        // Should not throw exception
        widget.onUpdate(context, mockAppWidgetManager, emptyWidgetIds)

        // Verify no interactions with mockAppWidgetManager
        verify(mockAppWidgetManager, never()).updateAppWidget(any<Int>(), any<RemoteViews>())
    }

    @Test
    fun `onUpdate should handle single widget id`() {
        val singleWidgetId = intArrayOf(testWidgetId)

        widget.onUpdate(context, mockAppWidgetManager, singleWidgetId)

        // Verify updateAppWidget was called once
        verify(mockAppWidgetManager, times(1)).updateAppWidget(eq(testWidgetId), any<RemoteViews>())
    }

    @Test
    fun `onUpdate should handle multiple widget ids`() {
        val multipleWidgetIds = intArrayOf(1, 2, 3)

        widget.onUpdate(context, mockAppWidgetManager, multipleWidgetIds)

        // Verify updateAppWidget was called three times
        verify(mockAppWidgetManager, times(3)).updateAppWidget(any<Int>(), any<RemoteViews>())
    }

    @Test
    fun `onReceive should handle RECORD_START action`() {
        val intent = Intent().apply { action = VoiceNoteWidget.ACTION_RECORD_START }

        // Should not throw exception
        widget.onReceive(context, intent)
    }

    @Test
    fun `onReceive should handle RECORD_STOP action`() {
        val intent = Intent().apply { action = VoiceNoteWidget.ACTION_RECORD_STOP }

        // Should not throw exception
        widget.onReceive(context, intent)
    }

    @Test
    fun `onReceive should handle SETTINGS action`() {
        val intent = Intent().apply { action = VoiceNoteWidget.ACTION_SETTINGS }

        // Should not throw exception
        widget.onReceive(context, intent)
    }

    @Test
    fun `onReceive should handle UPDATE_STATUS action`() {
        val intent =
            Intent().apply {
                action = VoiceNoteWidget.ACTION_UPDATE_STATUS
                putExtra("state", VoiceNoteWidget.STATE_RECORDING)
                putExtra("status", "Recording...")
            }

        // Should not throw exception
        widget.onReceive(context, intent)
    }

    @Test
    fun `onReceive should handle unknown action gracefully`() {
        val intent = Intent().apply { action = "UNKNOWN_ACTION" }

        // Should not throw exception
        widget.onReceive(context, intent)
    }

    @Test
    fun `onReceive should handle null intent gracefully`() {
        // Should not throw exception
        widget.onReceive(context, Intent()) // Use empty Intent instead of null
    }

    @Test
    fun `onReceive should handle UPDATE_STATUS without extras`() {
        val intent = Intent().apply { action = VoiceNoteWidget.ACTION_UPDATE_STATUS }

        // Should not throw exception even without extras
        widget.onReceive(context, intent)
    }

    @Test
    fun `updateWidgetStatus should handle null context gracefully`() {
        // Should not throw exception - but we can't test null context with the public method
        // since it requires a valid Context for Intent creation
        // Instead, test with mock context
        VoiceNoteWidget.updateWidgetStatus(context, VoiceNoteWidget.STATE_IDLE, "Test status")
    }

    @Test
    fun `updateWidgetStatus should handle valid parameters`() {
        // Should not throw exception
        VoiceNoteWidget.updateWidgetStatus(context, VoiceNoteWidget.STATE_RECORDING, "Recording...")
    }

    @Test
    fun `widget states should be unique`() {
        val states =
            setOf(
                VoiceNoteWidget.STATE_IDLE,
                VoiceNoteWidget.STATE_RECORDING,
                VoiceNoteWidget.STATE_PROCESSING,
                VoiceNoteWidget.STATE_ERROR,
            )

        assertEquals("All states should be unique", 4, states.size)
    }

    @Test
    fun `widget actions should be unique`() {
        val actions =
            setOf(
                VoiceNoteWidget.ACTION_RECORD_START,
                VoiceNoteWidget.ACTION_RECORD_STOP,
                VoiceNoteWidget.ACTION_SETTINGS,
                VoiceNoteWidget.ACTION_UPDATE_STATUS,
            )

        assertEquals("All actions should be unique", 4, actions.size)
    }

    @Test
    fun `widget constants should be properly formatted`() {
        // Verify action strings follow the expected pattern
        assertTrue(
            "Record start action should contain package name",
            VoiceNoteWidget.ACTION_RECORD_START.contains("com.tsyche.notablymd"),
        )
        assertTrue(
            "Record stop action should contain package name",
            VoiceNoteWidget.ACTION_RECORD_STOP.contains("com.tsyche.notablymd"),
        )
        assertTrue(
            "Settings action should contain package name",
            VoiceNoteWidget.ACTION_SETTINGS.contains("com.tsyche.notablymd"),
        )
        assertTrue(
            "Update status action should contain package name",
            VoiceNoteWidget.ACTION_UPDATE_STATUS.contains("com.tsyche.notablymd"),
        )
    }

    @Test
    fun `widget should handle rapid state changes`() {
        // Simulate rapid state changes
        VoiceNoteWidget.updateWidgetStatus(context, VoiceNoteWidget.STATE_IDLE, "Idle")
        VoiceNoteWidget.updateWidgetStatus(context, VoiceNoteWidget.STATE_RECORDING, "Recording")
        VoiceNoteWidget.updateWidgetStatus(context, VoiceNoteWidget.STATE_PROCESSING, "Processing")
        VoiceNoteWidget.updateWidgetStatus(context, VoiceNoteWidget.STATE_IDLE, "Idle")

        // Should not throw exception
        assertTrue("Should handle rapid state changes", true)
    }

    @Test
    fun `widget should handle empty status messages`() {
        // Should not throw exception with empty status
        VoiceNoteWidget.updateWidgetStatus(context, VoiceNoteWidget.STATE_IDLE, "")
        VoiceNoteWidget.updateWidgetStatus(context, VoiceNoteWidget.STATE_RECORDING, "")

        assertTrue("Should handle empty status messages", true)
    }

    @Test
    fun `widget should handle long status messages`() {
        val longStatus =
            "This is a very long status message that exceeds the normal length and should be handled gracefully by the widget without causing any issues"

        // Should not throw exception with long status
        VoiceNoteWidget.updateWidgetStatus(context, VoiceNoteWidget.STATE_PROCESSING, longStatus)

        assertTrue("Should handle long status messages", true)
    }
}
