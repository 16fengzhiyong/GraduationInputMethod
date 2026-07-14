package com.nuc.omeletteinputmethod.ui.keyboard

import com.nuc.omeletteinputmethod.data.repository.DictionaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class KeyboardEffect {
    data class CommitText(val text: String) : KeyboardEffect()
    object DeleteBackward : KeyboardEffect()
}

@Singleton
class KeyboardViewModel @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) {
    private val _state = MutableStateFlow(KeyboardState())
    val state: StateFlow<KeyboardState> = _state.asStateFlow()

    private val _effects = kotlinx.coroutines.flow.MutableSharedFlow<KeyboardEffect>()
    val effects = _effects.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        scope.launch {
            dictionaryRepository.initializeDictionary()
        }
    }

    fun onKeyChar(char: Char) {
        val currentBuffer = _state.value.inputBuffer
        if (_state.value.mode == KeyboardMode.ALPHA) {
            val newBuffer = currentBuffer + char
            updateCandidates(newBuffer)
        } else {
             // Direct commit for numbers/symbols
             scope.launch { _effects.emit(KeyboardEffect.CommitText(char.toString())) }
        }
    }

    fun onDelete() {
        val currentBuffer = _state.value.inputBuffer
        if (currentBuffer.isNotEmpty()) {
            val newBuffer = currentBuffer.dropLast(1)
            updateCandidates(newBuffer)
        } else {
            // Send delete to editor
            scope.launch { _effects.emit(KeyboardEffect.DeleteBackward) }
        }
    }

    private fun updateCandidates(buffer: String) {
        if (buffer.isEmpty()) {
            _state.update { it.copy(inputBuffer = "", candidates = emptyList()) }
            return
        }

        scope.launch {
            val candidates = dictionaryRepository.getInitialCandidates(buffer)
            _state.update { 
                it.copy(
                    inputBuffer = buffer, 
                    candidates = candidates
                ) 
            }
        }
    }

    fun setMode(mode: KeyboardMode) {
        _state.update { it.copy(mode = mode) }
    }

    fun toggleShift() {
        _state.update { it.copy(isShifted = !it.isShifted) }
    }

    fun onSpace() {
        val state = _state.value
        if (state.candidates.isNotEmpty()) {
            // Commit first candidate
            onCandidateSelected(state.candidates[0])
        } else {
             // Commit space
             _state.update { it.copy(inputBuffer = "") }
             scope.launch { _effects.emit(KeyboardEffect.CommitText(" ")) }
        }
    }

    fun onCandidateSelected(candidate: String) {
        scope.launch {
            dictionaryRepository.recordSelection(candidate) // Save habit
            _state.update { 
                it.copy(
                    inputBuffer = "", 
                    candidates = emptyList()
                ) 
            }
            _state.update { 
                it.copy(
                    inputBuffer = "", 
                    candidates = emptyList()
                ) 
            }
            _effects.emit(KeyboardEffect.CommitText(candidate))
        }
    }
}
