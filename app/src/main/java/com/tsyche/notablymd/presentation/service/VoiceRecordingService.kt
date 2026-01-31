package com.tsyche.notablymd.presentation.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.tsyche.notablymd.R
import com.tsyche.notablymd.presentation.widget.VoiceNoteWidget
import java.io.File
import java.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Voice Recording Service
 *
 * Handles audio recording and speech-to-text conversion for the voice widget. Features:
 * - Real-time audio recording with silence detection
 * - Voice level monitoring and visualization
 * - Speech-to-text integration
 * - Automatic note creation from transcription
 * - Background operation with notification
 */
class VoiceRecordingService : Service() {

    companion object {
        internal const val NOTIFICATION_ID = 1001
        internal const val CHANNEL_ID = "voice_recording_channel"
        internal const val CHANNEL_NAME = "Voice Recording"

        const val ACTION_START_RECORDING = "com.tsyche.notablymd.service.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.tsyche.notablymd.service.STOP_RECORDING"

        // Audio recording parameters
        internal const val SAMPLE_RATE = 44100
        internal const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        internal const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        internal const val BUFFER_SIZE_MULTIPLIER = 2

        // Silence detection
        internal const val SILENCE_THRESHOLD = 2000 // 2 seconds of silence
        internal const val VOICE_THRESHOLD = 1000 // Minimum voice level
    }

    private var serviceJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isRecording = false
    private var audioFile: File? = null
    private lateinit var voiceNoteCreator: VoiceNoteCreator

    private val voiceLevelChannel = Channel<Int>(Channel.UNLIMITED)
    private var silenceTimer: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        voiceNoteCreator = VoiceNoteCreator(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_START_RECORDING -> {
                startRecording()
                START_STICKY
            }
            ACTION_STOP_RECORDING -> {
                stopRecording()
                START_NOT_STICKY
            }
            else -> START_NOT_STICKY
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
                    .apply {
                        description = "Voice recording for notes"
                        setShowBadge(false)
                        enableVibration(false)
                        setSound(null, null)
                    }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createRecordingNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Recording")
            .setContentText("Recording voice note...")
            .setSmallIcon(R.drawable.ic_mic)
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                R.drawable.ic_stop,
                "Stop",
                PendingIntent.getService(
                    this,
                    0,
                    Intent(this, VoiceRecordingService::class.java).apply {
                        action = ACTION_STOP_RECORDING
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .build()
    }

    private fun startRecording() {
        if (isRecording) return

        if (!checkAudioPermission()) {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }

        serviceJob =
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    isRecording = true
                    startForeground(NOTIFICATION_ID, createRecordingNotification())

                    // Update widget UI
                    val intent =
                        Intent(this@VoiceRecordingService, VoiceNoteWidget::class.java).apply {
                            action = VoiceNoteWidget.ACTION_UPDATE_STATUS
                            putExtra("state", VoiceNoteWidget.STATE_RECORDING)
                            putExtra("status", "Recording...")
                        }
                    sendBroadcast(intent)

                    // Start audio recording
                    startAudioRecording()

                    // Start speech recognition
                    startSpeechRecognition()

                    // Monitor voice levels
                    monitorVoiceLevels()
                } catch (e: Exception) {
                    e.printStackTrace()
                    handleRecordingError("Failed to start recording: ${e.message}")
                }
            }
    }

    private fun stopRecording() {
        if (!isRecording) return

        serviceJob?.cancel()

        try {
            // Update widget UI
            val intent =
                Intent(this, VoiceNoteWidget::class.java).apply {
                    action = VoiceNoteWidget.ACTION_UPDATE_STATUS
                    putExtra("state", VoiceNoteWidget.STATE_PROCESSING)
                    putExtra("status", "Processing voice...")
                }
            sendBroadcast(intent)

            // Stop audio recording
            stopAudioRecording()

            // Stop speech recognition
            stopSpeechRecognition()

            // Process the recorded audio
            processRecording()
        } catch (e: Exception) {
            e.printStackTrace()
            handleRecordingError("Failed to stop recording: ${e.message}")
        } finally {
            isRecording = false
            stopForeground(true)
            stopSelf()
        }
    }

    private fun checkAudioPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun startAudioRecording() {
        val bufferSize =
            AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) *
                BUFFER_SIZE_MULTIPLIER

        // Check for audio recording permission (API 23+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (
                checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                stopSelf()
                return
            }
        }

        audioRecord =
            AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize,
                )
                .apply { startRecording() }

        // Create temporary audio file
        audioFile = File(cacheDir, "voice_recording_${System.currentTimeMillis()}.wav")
    }

    private fun stopAudioRecording() {
        audioRecord?.let { recorder ->
            if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                recorder.stop()
            }
            recorder.release()
        }
        audioRecord = null
    }

    private fun startSpeechRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            handleRecordingError("Speech recognition not available")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {
                    // Send voice level to widget for visualization
                    val level = (rmsdB + 100).toInt().coerceIn(0, 100)
                    voiceLevelChannel.trySend(level)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    handleRecordingError("Speech recognition error: $error")
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        processTranscription(matches[0])
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches =
                        partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        // Update widget with partial transcription
                        val intent =
                            Intent(this@VoiceRecordingService, VoiceNoteWidget::class.java).apply {
                                action = VoiceNoteWidget.ACTION_UPDATE_STATUS
                                putExtra("state", VoiceNoteWidget.STATE_RECORDING)
                                putExtra("status", "Recording: ${matches[0].take(50)}...")
                            }
                        sendBroadcast(intent)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
        )

        speechRecognizer?.startListening(intent)
    }

    private fun stopSpeechRecognition() {
        speechRecognizer?.let { recognizer ->
            recognizer.stopListening()
            recognizer.destroy()
        }
        speechRecognizer = null
    }

    private suspend fun monitorVoiceLevels() {
        voiceLevelChannel.receiveAsFlow().collect { level ->
            // Update widget with voice level for visualization
            // This would be handled by a separate mechanism in the widget
            if (level < VOICE_THRESHOLD) {
                // Start silence detection timer
                silenceTimer?.cancel()
                silenceTimer =
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(SILENCE_THRESHOLD.toLong())
                        if (isRecording) {
                            // Auto-stop recording on silence
                            stopRecording()
                        }
                    }
            } else {
                // Cancel silence timer if voice is detected
                silenceTimer?.cancel()
            }
        }
    }

    private fun processRecording() {
        serviceJob =
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Save audio file if needed
                    audioFile?.let { file ->
                        if (file.exists()) {
                            // Process the audio file or use speech recognition results
                            // For now, we'll rely on the speech recognition results
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    handleRecordingError("Failed to process recording: ${e.message}")
                }
            }
    }

    private fun processTranscription(transcription: String) {
        serviceJob =
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Validate and format transcription
                    if (!voiceNoteCreator.isValidTranscription(transcription)) {
                        handleRecordingError("Invalid transcription: $transcription")
                        return@launch
                    }

                    val formattedTranscription = voiceNoteCreator.formatTranscription(transcription)

                    // Create note from transcription
                    val noteId =
                        voiceNoteCreator.createNoteFromTranscription(formattedTranscription)

                    // Update widget with success
                    val intent =
                        Intent(this@VoiceRecordingService, VoiceNoteWidget::class.java).apply {
                            action = VoiceNoteWidget.ACTION_UPDATE_STATUS
                            putExtra("state", VoiceNoteWidget.STATE_IDLE)
                            putExtra("status", "Note created successfully")
                        }
                    sendBroadcast(intent)

                    // Show success toast
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                                this@VoiceRecordingService,
                                "Voice note created: ${formattedTranscription.take(30)}...",
                                Toast.LENGTH_LONG,
                            )
                            .show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    handleRecordingError("Failed to create note: ${e.message}")
                }
            }
    }

    private fun handleRecordingError(error: String) {
        // Update widget with error state
        val intent =
            Intent(this, VoiceNoteWidget::class.java).apply {
                action = VoiceNoteWidget.ACTION_UPDATE_STATUS
                putExtra("state", VoiceNoteWidget.STATE_ERROR)
                putExtra("status", "Error: $error")
            }
        sendBroadcast(intent)

        // Show error toast
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(applicationContext, error, Toast.LENGTH_LONG).show()
        }

        // Stop service
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
        silenceTimer?.cancel()
        stopAudioRecording()
        stopSpeechRecognition()
        voiceLevelChannel.close()
    }
}
