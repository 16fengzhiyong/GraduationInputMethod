package com.nuc.omeletteinputmethod.ui.multimodal

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class StrokeState(
    val strokeSequence: String = "",
    val candidates: List<String> = emptyList(),
    val currentPage: Int = 0,
)

@HiltViewModel
class StrokeInputViewModel
@Inject
constructor(
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(StrokeState())
    val state: StateFlow<StrokeState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<KeyboardEffect>(replay = 0, extraBufferCapacity = 32)
    val effects: SharedFlow<KeyboardEffect> = _effects.asSharedFlow()

    private val strokeMap = mutableMapOf<String, List<String>>()
    private var strokeMapLoaded = false

    private val strokeLabels =
        mapOf(
            '1' to "一",
            '2' to "丨",
            '3' to "丿",
            '4' to "丶",
            '5' to "乛",
        )

    private fun loadStrokeMap() {
        if (strokeMapLoaded) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = appContext.assets.open("stroke_map.txt")
                stream.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        val parts = line.split("\t")
                        if (parts.size >= 2) {
                            val key = parts[0].trim()
                            val chars = parts[1].trim()
                            if (key.isNotEmpty() && chars.isNotEmpty()) {
                                strokeMap[key] =
                                    chars.toCharArray().map { it.toString() }.filter { it.isNotEmpty() }
                            }
                        }
                    }
                }
                strokeMapLoaded = true
            } catch (_: Exception) {
            }
        }
    }

    fun onStrokeKey(strokeId: Char) {
        if (!strokeMapLoaded) loadStrokeMap()

        val newSeq = _state.value.strokeSequence + strokeId
        _state.update { it.copy(strokeSequence = newSeq, currentPage = 0) }

        queryCandidates(newSeq)
    }

    fun onWildcardKey() {
        if (!strokeMapLoaded) loadStrokeMap()

        val newSeq = _state.value.strokeSequence + '*'
        _state.update { it.copy(strokeSequence = newSeq, currentPage = 0) }

        queryCandidates(newSeq)
    }

    fun onDeleteStroke() {
        val currentSeq = _state.value.strokeSequence
        if (currentSeq.isEmpty()) {
            viewModelScope.launch { _effects.emit(KeyboardEffect.DeleteBackward) }
            return
        }

        val newSeq = currentSeq.dropLast(1)
        _state.update { it.copy(strokeSequence = newSeq, currentPage = 0) }

        if (newSeq.isEmpty()) {
            _state.update { it.copy(candidates = emptyList()) }
        } else {
            queryCandidates(newSeq)
        }
    }

    private fun queryCandidates(seq: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val results = matchStrokeSequence(seq)
            withContext(Dispatchers.Main) {
                _state.update { it.copy(candidates = results) }
            }
        }
    }

    private fun matchStrokeSequence(seq: String): List<String> {
        val prefixMatches = mutableListOf<String>()
        val exactMatches = mutableListOf<String>()
        val wildcardMatches = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        if (seq.contains('*')) {
            for ((key, chars) in strokeMap) {
                if (matchWithWildcard(key, seq)) {
                    for (ch in chars) {
                        if (seen.add(ch)) wildcardMatches.add(ch)
                    }
                }
            }
        } else {
            strokeMap[seq]?.let { exactMatches.addAll(it.filter { seen.add(it) }) }

            for ((key, chars) in strokeMap) {
                if (key.startsWith(seq) && key != seq) {
                    for (ch in chars) {
                        if (seen.add(ch)) prefixMatches.add(ch)
                    }
                }
            }
        }

        return exactMatches + prefixMatches + wildcardMatches
    }

    private fun matchWithWildcard(key: String, pattern: String): Boolean {
        if (key.length != pattern.length) return false
        for (i in key.indices) {
            if (pattern[i] != '*' && key[i] != pattern[i]) return false
        }
        return true
    }

    fun onCandidateSelected(candidate: String) {
        val seq = _state.value.strokeSequence
        _state.update { it.copy(strokeSequence = "", candidates = emptyList()) }
        viewModelScope.launch { _effects.emit(KeyboardEffect.CommitText(candidate)) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                appContext.assets.open("stroke_map.txt").bufferedReader().use { _ ->
                }
            } catch (_: Exception) {
            }
        }
    }

    fun onSpace() {
        val candidates = _state.value.candidates
        if (candidates.isNotEmpty()) {
            onCandidateSelected(candidates[0])
        } else {
            viewModelScope.launch { _effects.emit(KeyboardEffect.CommitText(" ")) }
        }
    }

    fun commitEnter() {
        viewModelScope.launch { _effects.emit(KeyboardEffect.CommitText("\n")) }
    }

    fun clearInput() {
        _state.update { it.copy(strokeSequence = "", candidates = emptyList()) }
    }

    fun getStrokeLabel(strokeId: Char): String = strokeLabels[strokeId] ?: strokeId.toString()
}