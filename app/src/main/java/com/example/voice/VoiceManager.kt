package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

enum class VoiceProfile {
    AYESHA_FEMALE,
    ALTERNATIVE_MALE
}

enum class SpeechState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING
}

class VoiceManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "AyeshaVoiceManager"

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _speechState = MutableStateFlow(SpeechState.IDLE)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _amplitude = MutableStateFlow(0.1f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Configurable voice parameters
    var activeProfile: VoiceProfile = VoiceProfile.AYESHA_FEMALE
    var speechRate: Float = 1.0f
    var speechPitch: Float = 1.05f
    var activeLanguageCode: String = "en" // "en", "hi", "ur"

    private var speechSimulationJob: Job? = null

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                applyVoiceSettings()
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _speechState.value = SpeechState.SPEAKING
                        startSpeakingAmplitudeSimulation()
                    }

                    override fun onDone(utteranceId: String?) {
                        stopSpeakingAmplitudeSimulation()
                        _speechState.value = SpeechState.IDLE
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        stopSpeakingAmplitudeSimulation()
                        _speechState.value = SpeechState.IDLE
                    }
                })
            } else {
                Log.e(tag, "TTS initialization failed: status=$status")
            }
        }
    }

    fun applyVoiceSettings() {
        textToSpeech?.let { tts ->
            val targetLocale = when (activeLanguageCode) {
                "hi" -> Locale("hi", "IN")
                "ur" -> Locale("ur", "PK")
                else -> Locale.US
            }
            val result = tts.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.language = Locale.US
            }

            val pitchMultiplier = when (activeProfile) {
                VoiceProfile.AYESHA_FEMALE -> speechPitch
                VoiceProfile.ALTERNATIVE_MALE -> (speechPitch * 0.8f).coerceAtLeast(0.5f)
            }
            tts.setPitch(pitchMultiplier)
            tts.setSpeechRate(speechRate)
        }
    }

    fun startListening(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _errorMessage.value = "Speech recognition is not available on this device."
            return
        }

        // Stop any active speech
        stopSpeaking()

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _speechState.value = SpeechState.LISTENING
                    _errorMessage.value = null
                }

                override fun onBeginningOfSpeech() {
                    _speechState.value = SpeechState.LISTENING
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Map rmsdB (-2 to 10 typical) to 0.1 .. 1.0 range
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
                    _amplitude.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _amplitude.value = 0.15f
                    _speechState.value = SpeechState.THINKING
                }

                override fun onError(error: Int) {
                    _amplitude.value = 0.1f
                    _speechState.value = SpeechState.IDLE
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap mic to retry."
                        SpeechRecognizer.ERROR_NETWORK -> "Network issue while listening."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
                        else -> "Speech recognition error ($error)"
                    }
                    if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                        _errorMessage.value = msg
                    }
                }

                override fun onResults(results: Bundle?) {
                    _speechState.value = SpeechState.THINKING
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim()
                    if (!text.isNullOrBlank()) {
                        _recognizedText.value = text
                        onResult(text)
                    } else {
                        _speechState.value = SpeechState.IDLE
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.firstOrNull()?.let {
                        _recognizedText.value = it
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                val targetLang = when (activeLanguageCode) {
                    "hi" -> "hi-IN"
                    "ur" -> "ur-PK"
                    else -> "en-US"
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, targetLang)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(tag, "Failed to start speech recognition", e)
            _speechState.value = SpeechState.IDLE
            _errorMessage.value = "Microphone session error: ${e.localizedMessage}"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(tag, "Error stopping speech recognizer", e)
        }
        _speechState.value = SpeechState.IDLE
        _amplitude.value = 0.1f
    }

    fun speak(text: String, onDoneCallback: (() -> Unit)? = null) {
        if (!isTtsReady) {
            Log.w(tag, "TTS is not ready yet.")
            return
        }
        applyVoiceSettings()
        _speechState.value = SpeechState.SPEAKING
        val utteranceId = "AyeshaUtterance_${System.currentTimeMillis()}"
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        stopSpeakingAmplitudeSimulation()
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e(tag, "Error stopping TTS", e)
        }
        if (_speechState.value == SpeechState.SPEAKING) {
            _speechState.value = SpeechState.IDLE
        }
        _amplitude.value = 0.1f
    }

    fun setSpeechState(state: SpeechState) {
        _speechState.value = state
        if (state != SpeechState.SPEAKING && state != SpeechState.LISTENING) {
            _amplitude.value = 0.1f
        }
    }

    private fun startSpeakingAmplitudeSimulation() {
        speechSimulationJob?.cancel()
        speechSimulationJob = scope.launch(Dispatchers.Default) {
            var phase = 0f
            while (isActive && _speechState.value == SpeechState.SPEAKING) {
                phase += 0.35f
                val dynamicAmp = 0.35f + (kotlin.math.sin(phase) * 0.3f).toFloat() +
                        (kotlin.math.cos(phase * 1.7f) * 0.15f).toFloat()
                _amplitude.value = dynamicAmp.coerceIn(0.15f, 0.95f)
                delay(70)
            }
            _amplitude.value = 0.1f
        }
    }

    private fun stopSpeakingAmplitudeSimulation() {
        speechSimulationJob?.cancel()
        speechSimulationJob = null
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e(tag, "Error destroying VoiceManager", e)
        }
    }
}
