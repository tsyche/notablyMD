package com.tsyche.notablymd.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.tsyche.notablymd.R
import com.tsyche.notablymd.presentation.activity.main.MainActivity
import com.tsyche.notablymd.presentation.service.VoiceRecordingService

/**
 * Voice Note Widget Provider
 *
 * Provides a home screen widget for quick voice recording and note creation. Features:
 * - One-tap voice recording
 * - Real-time voice level visualization
 * - Recording status indicators
 * - Quick access to last note
 * - Settings access
 */
class VoiceNoteWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_RECORD_START = "com.tsyche.notablymd.widget.RECORD_START"
        const val ACTION_RECORD_STOP = "com.tsyche.notablymd.widget.RECORD_STOP"
        const val ACTION_SETTINGS = "com.tsyche.notablymd.widget.SETTINGS"
        const val ACTION_UPDATE_STATUS = "com.tsyche.notablymd.widget.UPDATE_STATUS"

        // Widget states
        const val STATE_IDLE = 0
        const val STATE_RECORDING = 1
        const val STATE_PROCESSING = 2
        const val STATE_ERROR = 3

        /** Public method to update widget status from services */
        fun updateWidgetStatus(context: Context, state: Int, status: String) {
            val intent =
                Intent(context, VoiceNoteWidget::class.java).apply {
                    action = ACTION_UPDATE_STATUS
                    putExtra("state", state)
                    putExtra("status", status)
                }
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, STATE_IDLE)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds =
            appWidgetManager.getAppWidgetIds(ComponentName(context, VoiceNoteWidget::class.java))

        when (intent.action) {
            ACTION_RECORD_START -> {
                handleRecordStart(context, appWidgetManager, appWidgetIds)
            }
            ACTION_RECORD_STOP -> {
                handleRecordStop(context, appWidgetManager, appWidgetIds)
            }
            ACTION_SETTINGS -> {
                handleSettings(context)
            }
            ACTION_UPDATE_STATUS -> {
                val state = intent.getIntExtra("state", STATE_IDLE)
                val status = intent.getStringExtra("status") ?: ""
                updateWidgetStatus(context, appWidgetManager, appWidgetIds, state, status)
            }
        }
    }

    private fun handleRecordStart(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // Update UI to recording state
        updateWidgetStatus(context, appWidgetManager, appWidgetIds, STATE_RECORDING, "Recording...")

        // Start recording service
        val serviceIntent =
            Intent(context, VoiceRecordingService::class.java).apply {
                action = VoiceRecordingService.ACTION_START_RECORDING
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun handleRecordStop(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // Update UI to processing state
        updateWidgetStatus(
            context,
            appWidgetManager,
            appWidgetIds,
            STATE_PROCESSING,
            "Processing...",
        )

        // Stop recording service
        val serviceIntent =
            Intent(context, VoiceRecordingService::class.java).apply {
                action = VoiceRecordingService.ACTION_STOP_RECORDING
            }
        context.startService(serviceIntent)
    }

    private fun handleSettings(context: Context) {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_FRAGMENT_TO_OPEN, R.id.Settings)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        context.startActivity(intent)
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        state: Int,
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_voice_note)

        // Setup click handlers
        setupClickHandlers(context, views)

        // Update UI based on state
        updateWidgetUI(views, state)

        // Update last note preview
        updateLastNotePreview(context, views)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun setupClickHandlers(context: Context, views: RemoteViews) {
        // Record button
        val recordIntent =
            Intent(context, VoiceNoteWidget::class.java).apply { action = ACTION_RECORD_START }
        val recordPendingIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                recordIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        views.setOnClickPendingIntent(R.id.recordButton, recordPendingIntent)

        // Stop button
        val stopIntent =
            Intent(context, VoiceNoteWidget::class.java).apply { action = ACTION_RECORD_STOP }
        val stopPendingIntent =
            PendingIntent.getBroadcast(
                context,
                1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        views.setOnClickPendingIntent(R.id.stopButton, stopPendingIntent)

        // Settings button
        val settingsIntent =
            Intent(context, VoiceNoteWidget::class.java).apply { action = ACTION_SETTINGS }
        val settingsPendingIntent =
            PendingIntent.getBroadcast(
                context,
                2,
                settingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        views.setOnClickPendingIntent(R.id.settingsButton, settingsPendingIntent)
    }

    private fun updateWidgetUI(views: RemoteViews, state: Int) {
        when (state) {
            STATE_IDLE -> {
                views.setViewVisibility(R.id.recordButton, View.VISIBLE)
                views.setViewVisibility(R.id.stopButton, View.GONE)
                views.setTextViewText(R.id.statusText, "Tap to start recording")
                views.setInt(
                    R.id.recordButton,
                    "setBackgroundColor",
                    0xFFFF5722.toInt(),
                ) // Orange color
            }
            STATE_RECORDING -> {
                views.setViewVisibility(R.id.recordButton, View.GONE)
                views.setViewVisibility(R.id.stopButton, View.VISIBLE)
                views.setTextViewText(R.id.statusText, "Recording... Tap to stop")
                // Animate voice bars
                animateVoiceBars(views, true)
            }
            STATE_PROCESSING -> {
                views.setViewVisibility(R.id.recordButton, View.GONE)
                views.setViewVisibility(R.id.stopButton, View.GONE)
                views.setTextViewText(R.id.statusText, "Processing voice...")
                animateVoiceBars(views, false)
            }
            STATE_ERROR -> {
                views.setViewVisibility(R.id.recordButton, View.VISIBLE)
                views.setViewVisibility(R.id.stopButton, View.GONE)
                views.setTextViewText(R.id.statusText, "Error. Tap to retry")
                animateVoiceBars(views, false)
            }
        }
    }

    private fun animateVoiceBars(views: RemoteViews, isRecording: Boolean) {
        val alpha = if (isRecording) 1.0f else 0.3f
        val barIds =
            intArrayOf(
                R.id.voiceBar1,
                R.id.voiceBar2,
                R.id.voiceBar3,
                R.id.voiceBar4,
                R.id.voiceBar5,
                R.id.voiceBar6,
            )

        barIds.forEach { barId -> views.setFloat(barId, "setAlpha", alpha) }
    }

    private fun updateLastNotePreview(context: Context, views: RemoteViews) {
        // Get last voice note from preferences
        val prefs = context.getSharedPreferences("voice_widget_prefs", Context.MODE_PRIVATE)
        val noteId = prefs.getLong("last_voice_note_id", -1L)
        val noteText = prefs.getString("last_voice_note_text", "")

        if (noteId != -1L && !noteText.isNullOrEmpty()) {
            views.setViewVisibility(R.id.lastNotePreview, View.VISIBLE)
            views.setTextViewText(R.id.lastNoteText, noteText)
        } else {
            views.setViewVisibility(R.id.lastNotePreview, View.GONE)
        }
    }

    private fun updateWidgetStatus(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        state: Int,
        status: String,
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_voice_note)
            views.setTextViewText(R.id.statusText, status)
            updateWidgetUI(views, state)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
