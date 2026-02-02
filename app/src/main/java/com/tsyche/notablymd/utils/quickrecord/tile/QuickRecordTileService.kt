package com.tsyche.notablymd.utils.quickrecord.tile

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.tsyche.notablymd.presentation.service.VoiceRecordingService
import com.tsyche.notablymd.utils.quickrecord.QuickRecordTriggerManager

/**
 * Quick Settings Tile for instant voice recording access Provides one-tap voice recording from the
 * quick settings panel
 */
@TargetApi(Build.VERSION_CODES.N)
class QuickRecordTileService : TileService() {

    companion object {
        private const val TAG = "QuickRecordTile"
    }

    private lateinit var triggerManager: QuickRecordTriggerManager

    override fun onCreate() {
        super.onCreate()
        triggerManager = QuickRecordTriggerManager.getInstance(this)
        Log.d(TAG, "Quick record tile service created")
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
        Log.d(TAG, "Quick record tile started listening")
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "Quick record tile stopped listening")
    }

    override fun onClick() {
        super.onClick()

        if (!triggerManager.isQuickTileTriggerEnabled) {
            Log.d(TAG, "Quick tile trigger disabled")
            showToast("Quick record tile is disabled in settings")
            return
        }

        Log.d(TAG, "Quick record tile clicked")
        triggerVoiceRecording()
    }

    /** Update the tile state based on current settings */
    private fun updateTileState() {
        val tile = qsTile ?: return

        if (triggerManager.isQuickTileTriggerEnabled) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Voice Record"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Tap to record"
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Voice Record"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Disabled"
            }
        }

        tile.updateTile()
    }

    /** Start voice recording service */
    private fun triggerVoiceRecording() {
        val intent =
            Intent(this, VoiceRecordingService::class.java).apply {
                action = VoiceRecordingService.ACTION_START_RECORDING
                putExtra(VoiceRecordingService.EXTRA_TRIGGER_SOURCE, "QUICK_TILE")
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        // Provide haptic feedback
        triggerHapticFeedback()

        // Show brief feedback
        showToast("Starting voice recording...")
    }

    /** Provide haptic feedback when tile is clicked */
    private fun triggerHapticFeedback() {
        try {
            // TileService doesn't have direct access to views, so we'll skip haptic feedback
            // for now. Could be implemented with a different approach later.
            Log.d(TAG, "Haptic feedback requested")
        } catch (e: Exception) {
            Log.w(TAG, "Could not provide haptic feedback", e)
        }
    }

    /** Show toast message to user */
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    /**
     * Update tile when settings change This can be called from settings when user enables/disables
     * the tile
     */
    fun updateTileFromSettings() {
        updateTileState()
    }
}
