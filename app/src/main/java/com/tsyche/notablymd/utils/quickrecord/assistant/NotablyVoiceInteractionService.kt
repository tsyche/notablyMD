package com.tsyche.notablymd.utils.quickrecord.assistant

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log
import com.tsyche.notablymd.presentation.service.VoiceRecordingService
import com.tsyche.notablymd.utils.quickrecord.QuickRecordTriggerManager

/**
 * Voice Interaction Service for Assistant Integration Allows hands-free voice recording activation
 * through voice commands
 */
class NotablyVoiceInteractionService : VoiceInteractionService() {

    companion object {
        private const val TAG = "NotablyVoiceInteraction"
        const val ACTION_VOICE_RECORD = "com.tsyche.notablymd.action.VOICE_RECORD"
        const val EXTRA_WAKE_PHRASE = "wake_phrase"
    }

    override fun onReady() {
        super.onReady()
        Log.d(TAG, "Voice interaction service ready")
    }

    override fun onShutdown() {
        super.onShutdown()
        Log.d(TAG, "Voice interaction service shutdown")
    }

    /** Handle voice commands and trigger recording if appropriate */
    private fun handleVoiceCommand(command: String) {
        val triggerManager = QuickRecordTriggerManager.getInstance(this)

        if (!triggerManager.isAssistantTriggerEnabled) {
            Log.d(TAG, "Assistant trigger disabled")
            return
        }

        val wakePhrase = triggerManager.customWakePhrase.lowercase()
        if (command.lowercase().contains(wakePhrase)) {
            Log.d(TAG, "Wake phrase detected: $wakePhrase")
            triggerVoiceRecording()
        }
    }

    /** Start voice recording */
    private fun triggerVoiceRecording() {
        val intent =
            Intent(this, VoiceRecordingService::class.java).apply {
                action = VoiceRecordingService.ACTION_START_RECORDING
                putExtra(VoiceRecordingService.EXTRA_TRIGGER_SOURCE, "ASSISTANT")
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

/** Voice Interaction Session for handling voice interactions */
class NotablyVoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {

    companion object {
        private const val TAG = "NotablyVoiceSession"
    }

    private val sessionContext: Context = context

    /** Trigger voice recording from session */
    private fun triggerVoiceRecording() {
        val triggerManager = QuickRecordTriggerManager.getInstance(sessionContext)

        if (!triggerManager.isAssistantTriggerEnabled) {
            return
        }

        val intent =
            Intent(sessionContext, VoiceRecordingService::class.java).apply {
                action = VoiceRecordingService.ACTION_START_RECORDING
                putExtra(VoiceRecordingService.EXTRA_TRIGGER_SOURCE, "ASSISTANT")
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sessionContext.startForegroundService(intent)
        } else {
            sessionContext.startService(intent)
        }
        showVoiceRecordingStarted()
    }

    /** Show feedback that voice recording has started */
    private fun showVoiceRecordingStarted() {
        // Could show a toast, notification, or voice feedback
        Log.d(TAG, "Voice recording started via assistant")
    }
}

/** Service to manage Voice Interaction Session */
class NotablyVoiceInteractionSessionService : VoiceInteractionSessionService() {

    override fun onNewSession(bundle: Bundle?): VoiceInteractionSession {
        return NotablyVoiceInteractionSession(this)
    }
}
