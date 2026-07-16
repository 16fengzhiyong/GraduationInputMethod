package com.nuc.omeletteinputmethod.ui.multimodal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class VoiceState(
    val isListening: Boolean = false,
    val recognizedText: String = "",
    val hasPermission: Boolean = false,
    val errorMessage: String? = null,
)

class VoiceInputViewModel
@Inject
constructor(
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(VoiceState())
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<KeyboardEffect>(replay = 0, extraBufferCapacity = 16)
    val effects: SharedFlow<KeyboardEffect> = _effects.asSharedFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var isRecognizerAvailable: Boolean = false

    init {
        _state.update {
            it.copy(
                hasPermission =
                    ContextCompat.checkSelfPermission(
                        appContext,
                        android.Manifest.permission.RECORD_AUDIO,
                    ) == PackageManager.PERMISSION_GRANTED,
            )
        }
        isRecognizerAvailable = SpeechRecognizer.isRecognitionAvailable(appContext)
    }

    fun checkPermission(): Boolean {
        val granted =
            ContextCompat.checkSelfPermission(
                appContext,
                android.Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
        _state.update { it.copy(hasPermission = granted) }
        if (!granted) {
            _state.update { it.copy(errorMessage = "需要录音权限") }
        }
        return granted
    }

    fun startListening() {
        if (!checkPermission()) return
        if (!isRecognizerAvailable) {
            _state.update { it.copy(errorMessage = "语音识别不可用") }
            return
        }

        _state.update { it.copy(isListening = true, recognizedText = "", errorMessage = null) }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
        }

        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _state.update { it.copy(isListening = false) }
    }

    fun cancelListening() {
        speechRecognizer?.cancel()
        _state.update { it.copy(isListening = false, recognizedText = "", errorMessage = null) }
    }

    fun commitText() {
        val text = _state.value.recognizedText
        if (text.isNotEmpty()) {
            _state.update { it.copy(recognizedText = "") }
            viewModelScope.launch { _effects.emit(KeyboardEffect.CommitText(text)) }
        }
    }

    private fun createRecognitionListener(): RecognitionListener =
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _state.update { it.copy(errorMessage = null) }
            }

            override fun onBeginningOfSpeech() {
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onEndOfSpeech() {
                _state.update { it.copy(isListening = false) }
            }

            override fun onError(error: Int) {
                val msg =
                    when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "音频错误"
                        SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                        SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                        SpeechRecognizer.ERROR_NO_MATCH -> "未识别到语音"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙碌"
                        SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音超时"
                        else -> "未知错误 ($error)"
                    }
                _state.update { it.copy(isListening = false, errorMessage = msg) }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                _state.update { it.copy(isListening = false, recognizedText = text, errorMessage = null) }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotEmpty()) {
                    _state.update { it.copy(recognizedText = text) }
                }
            }

            override fun onEvent(
                eventType: Int,
                params: Bundle?,
            ) {
            }
        }

    override fun onCleared() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        super.onCleared()
    }
}