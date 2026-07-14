package com.nuc.omeletteinputmethod.ui.keyboard

import android.content.Context
import android.content.SharedPreferences
import com.nuc.omeletteinputmethod.data.repository.DictionaryRepository
import com.nuc.omeletteinputmethod.data.model.InputStat
import com.nuc.omeletteinputmethod.data.model.InputStatDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

sealed class KeyboardEffect {
    data class CommitText(val text: String) : KeyboardEffect()

    object DeleteBackward : KeyboardEffect()

    // Added by Agent B — haptic feedback effect
    data class Vibrate(val keyType: KeyType) : KeyboardEffect()

    // Added by Agent B — key click sound effect
    data class PlaySound(val volume: Float = 1.0f) : KeyboardEffect()
    // Added by Agent G — paste clipboard text to target input field
    data class Paste(val text: String) : KeyboardEffect()
}

// Added by Agent A — T9 mapping: digit → sequence of chars to cycle through
private val T9_MAP: Map<Char, CharArray> =
    mapOf(
        '2' to "abc".toCharArray(),
        '3' to "def".toCharArray(),
        '4' to "ghi".toCharArray(),
        '5' to "jkl".toCharArray(),
        '6' to "mno".toCharArray(),
        '7' to "pqrs".toCharArray(),
        '8' to "tuv".toCharArray(),
        '9' to "wxyz".toCharArray(),
        '0' to ",.? !".toCharArray(),
    )

private const val T9_TIMEOUT_MS = 500L
private const val PREFS_KEY_HEIGHT = "keyboard_height_percent"
private const val DEFAULT_HEIGHT_PERCENT = 0.4f

// Added by Agent B — long-press alternate character map
val LONG_PRESS_ALTERNATES: Map<Char, List<Char>> =
    mapOf(
        'a' to listOf('@', '#', 'à', 'á'),
        'b' to listOf('~', '`'),
        'c' to listOf('©', '℃'),
        'e' to listOf('€', '£', 'è', 'é'),
        's' to listOf('$', '§'),
        'q' to listOf('!', '?'),
        '1' to listOf('一'),
        '2' to listOf('二'),
        '3' to listOf('三'),
        '4' to listOf('四'),
        '5' to listOf('五'),
        '6' to listOf('六'),
        '7' to listOf('七'),
        '8' to listOf('八'),
        '9' to listOf('九'),
    )

// Added by Agent B — SharedPreferences keys for haptic and sound
private const val PREFS_KEY_HAPTIC = "haptic_enabled"
private const val PREFS_KEY_SOUND = "sound_enabled"
private const val DEFAULT_HAPTIC = true
private const val DEFAULT_SOUND = true

@Singleton
class KeyboardViewModel
    @Inject
    constructor(
        private val dictionaryRepository: DictionaryRepository,
        @ApplicationContext private val appContext: Context,
        private val inputStatDao: InputStatDao,
    ) {
        private val _state = MutableStateFlow(KeyboardState())
        val state: StateFlow<KeyboardState> = _state.asStateFlow()

        private val _effects = MutableSharedFlow<KeyboardEffect>(replay = 0, extraBufferCapacity = 64)
        val effects: SharedFlow<KeyboardEffect> = _effects.asSharedFlow()

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        private val prefs = appContext.getSharedPreferences("omelette_keyboard_prefs", Context.MODE_PRIVATE)

        private var t9LastDigit: Char? = null
        private var t9TapCount: Int = 0
        private var t9LastTapTime: Long = 0L

        private val pinyinPrefKeys = PinyinPrefKeys(prefs)

        private var sessionTotalChars = 0
        private var sessionTotalWords = 0
        private var sessionUniqueChars = mutableSetOf<Char>()
        private var sessionStartTime = System.currentTimeMillis()
        private var sessionCount = 1
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        init {
            val savedHeight = prefs.getFloat(PREFS_KEY_HEIGHT, DEFAULT_HEIGHT_PERCENT)
            val savedHaptic = prefs.getBoolean(PREFS_KEY_HAPTIC, DEFAULT_HAPTIC)
            val savedSound = prefs.getBoolean(PREFS_KEY_SOUND, DEFAULT_SOUND)
            val fuzzyEnabled = pinyinPrefKeys.fuzzyEnabled
            val fuzzyRules = pinyinPrefKeys.fuzzyRules
            val autoCorrect = pinyinPrefKeys.autoCorrect
            val doublePinyin = pinyinPrefKeys.doublePinyin
            val doubleScheme = pinyinPrefKeys.doubleScheme
            val mixedInput = pinyinPrefKeys.mixedInput
            _state.update {
                it.copy(
                    keyboardHeightPercent = savedHeight.coerceIn(0.3f, 0.6f),
                    hapticEnabled = savedHaptic,
                    soundEnabled = savedSound,
                    fuzzyEnabled = fuzzyEnabled,
                    fuzzyRules = fuzzyRules,
                    autoCorrect = autoCorrect,
                    doublePinyin = doublePinyin,
                    doubleScheme = doubleScheme,
                    mixedInput = mixedInput,
                )
            }

            scope.launch {
                dictionaryRepository.initializeDictionary()
            }
        }

        // Updated by Agent C — fuzzy/double/mixed-input support
        fun onKeyChar(char: Char) {
            val st = _state.value
            if (st.mode != KeyboardMode.ALPHA) {
                scope.launch { _effects.emit(KeyboardEffect.CommitText(char.toString())) }
                emitHaptic(KeyType.NORMAL)
                emitSound()
                return
            }

            if (st.mixedInput && char.isUpperCase()) {
                val currentBuffer = st.inputBuffer
                val bufferAfterCapital = currentBuffer + char
                val (englishPart, pinyinRest) = PinyinProcessor.detectMixedEnglish(bufferAfterCapital)
                if (englishPart.isNotEmpty()) {
                    _state.update { it.copy(inputBuffer = pinyinRest) }
                    scope.launch { _effects.emit(KeyboardEffect.CommitText(englishPart)) }
                    if (pinyinRest.isNotEmpty()) {
                        updateCandidates(pinyinRest)
                    } else {
                        _state.update { it.copy(candidates = emptyList(), doubleBuffer = "") }
                    }
                    emitHaptic(KeyType.NORMAL)
                    emitSound()
                    return
                }
            }

            if (st.doublePinyin) {
                val newDoubleBuffer = st.doubleBuffer + char.lowercaseChar()
                _state.update { it.copy(doubleBuffer = newDoubleBuffer) }
                val fullPinyin = PinyinProcessor.toFullPinyin(newDoubleBuffer, st.doubleScheme)
                updateCandidates(fullPinyin)
            } else {
                val newBuffer = st.inputBuffer + char.lowercaseChar()
                updateCandidates(newBuffer)
            }

            emitHaptic(KeyType.NORMAL)
            emitSound()
        }

        // Added by Agent B — emit haptic feedback if enabled
        private fun emitHaptic(keyType: KeyType = KeyType.NORMAL) {
            if (_state.value.hapticEnabled) {
                _effects.tryEmit(KeyboardEffect.Vibrate(keyType))
            }
        }

        // Added by Agent B — emit key click sound if enabled
        private fun emitSound() {
            if (_state.value.soundEnabled) {
                _effects.tryEmit(KeyboardEffect.PlaySound())
            }
        }

        // Added by Agent A — T9 multi-tap handler
        fun onT9Key(digit: Char) {
            val chars = T9_MAP[digit] ?: return
            val now = System.currentTimeMillis()

            t9TapCount =
                if (digit == t9LastDigit && (now - t9LastTapTime) < T9_TIMEOUT_MS) {
                    t9TapCount + 1
                } else {
                    lockT9PreviousChar()
                    1
                }

            t9LastDigit = digit
            t9LastTapTime = now
        }

        private fun lockT9PreviousChar() {
            val prevDigit = t9LastDigit ?: return
            val chars = T9_MAP[prevDigit] ?: return
            val selected = chars[(t9TapCount - 1) % chars.size]
            val currentBuffer = _state.value.inputBuffer
            val newBuffer = currentBuffer + selected
            updateCandidates(newBuffer)
        }

        fun onT9Delete() {
            lockT9PreviousChar()
            t9LastDigit = null
            t9TapCount = 0

            val currentBuffer = _state.value.inputBuffer
            if (currentBuffer.isNotEmpty()) {
                val newBuffer = currentBuffer.dropLast(1)
                updateCandidates(newBuffer)
            } else {
                _state.update { it.copy(doubleBuffer = "") }
                scope.launch { _effects.emit(KeyboardEffect.DeleteBackward) }
            }
        }

        fun onT9Space() {
            var currentState = _state.value

            if (t9LastDigit != null) {
                lockT9PreviousChar()
                t9LastDigit = null
                t9TapCount = 0
                currentState = _state.value
            }

            if (currentState.candidates.isNotEmpty()) {
                onCandidateSelected(currentState.candidates[0])
            } else {
                _state.update { it.copy(inputBuffer = "", doubleBuffer = "") }
                scope.launch { _effects.emit(KeyboardEffect.CommitText(" ")) }
            }
        }

        fun onDelete() {
            val st = _state.value
            if (st.doublePinyin && st.doubleBuffer.isNotEmpty()) {
                val newDoubleBuf = st.doubleBuffer.dropLast(1)
                _state.update { it.copy(doubleBuffer = newDoubleBuf) }
                val fullPinyin = PinyinProcessor.toFullPinyin(newDoubleBuf, st.doubleScheme)
                updateCandidates(fullPinyin)
                emitHaptic(KeyType.NORMAL)
                emitSound()
                return
            }
            val currentBuffer = st.inputBuffer
            if (currentBuffer.isNotEmpty()) {
                val newBuffer = currentBuffer.dropLast(1)
                updateCandidates(newBuffer)
            } else {
                scope.launch { _effects.emit(KeyboardEffect.DeleteBackward) }
            }
            emitHaptic(KeyType.NORMAL)
            emitSound()
        }

        // Updated by Agent C — fuzzy/auto-correct/mixed-input support
        private fun updateCandidates(buffer: String) {
            if (buffer.isEmpty()) {
                _state.update { it.copy(inputBuffer = "", candidates = emptyList(), doubleBuffer = "") }
                return
            }

            scope.launch {
                val st = _state.value
                val isDouble = st.doublePinyin

                if (st.mixedInput && !isDouble) {
                    val (englishPart, pinyinRest) = PinyinProcessor.detectMixedEnglish(buffer)
                    if (englishPart.isNotEmpty()) {
                        _state.update { it.copy(inputBuffer = pinyinRest) }
                        _effects.emit(KeyboardEffect.CommitText(englishPart))
                        if (pinyinRest.isEmpty()) {
                            _state.update { it.copy(candidates = emptyList(), doubleBuffer = "") }
                            return@launch
                        }
                        updateCandidatesInner(pinyinRest, st)
                        return@launch
                    }
                }

                updateCandidatesInner(buffer, st)
            }
        }

        private suspend fun updateCandidatesInner(
            buffer: String,
            st: KeyboardState,
        ) {
            val isDouble = st.doublePinyin

            val variants: List<String> =
                if (st.fuzzyEnabled) {
                    PinyinProcessor.expandFuzzyPinyin(buffer, st.fuzzyRules)
                } else {
                    listOf(buffer)
                }

            val allCandidates = mutableListOf<String>()
            val seenCandidates = mutableSetOf<String>()
            for (variant in variants) {
                val candidates = dictionaryRepository.getInitialCandidates(variant)
                for (c in candidates) {
                    if (seenCandidates.add(c)) {
                        allCandidates.add(c)
                    }
                }
            }

            if (allCandidates.isEmpty() && st.autoCorrect) {
                val corrections = PinyinProcessor.correctPinyinTypo(buffer)
                for (correction in corrections.take(20)) {
                    val candidates = dictionaryRepository.getInitialCandidates(correction)
                    if (candidates.isNotEmpty()) {
                        for (c in candidates) {
                            if (seenCandidates.add(c)) {
                                allCandidates.add(c)
                            }
                        }
                    }
                }
            }

            _state.update {
                it.copy(
                    inputBuffer = if (isDouble) it.doubleBuffer else buffer,
                    candidates = allCandidates,
                )
            }
        }

        fun setMode(mode: KeyboardMode) {
            _state.update { it.copy(mode = mode) }
            emitHaptic(KeyType.SPECIAL)
        }

        // Added by Agent A — T9 mode alias
        fun setT9Mode() {
            _state.update { it.copy(mode = KeyboardMode.T9) }
        }

        fun toggleShift() {
            _state.update { it.copy(isShifted = !it.isShifted) }
            emitHaptic(KeyType.SPECIAL)
        }

        // Added by Agent A
        fun setOneHandMode(mode: OneHandMode) {
            _state.update { it.copy(oneHandMode = mode) }
        }

        // Added by Agent A
        fun setKeyboardHeight(percent: Float) {
            val clamped = percent.coerceIn(0.3f, 0.6f)
            _state.update { it.copy(keyboardHeightPercent = clamped) }
            prefs.edit().putFloat(PREFS_KEY_HEIGHT, clamped).apply()
        }

        // Added by Agent C — Pinyin settings setters
        fun setFuzzyEnabled(enabled: Boolean) {
            _state.update { it.copy(fuzzyEnabled = enabled) }
            pinyinPrefKeys.fuzzyEnabled = enabled
        }

        fun setFuzzyRule(
            rule: String,
            enabled: Boolean,
        ) {
            _state.update { st ->
                val newRules = if (enabled) st.fuzzyRules + rule else st.fuzzyRules - rule
                st.copy(fuzzyRules = newRules)
            }
            pinyinPrefKeys.fuzzyRules = _state.value.fuzzyRules
        }

        fun setAutoCorrect(enabled: Boolean) {
            _state.update { it.copy(autoCorrect = enabled) }
            pinyinPrefKeys.autoCorrect = enabled
        }

        fun setDoublePinyin(enabled: Boolean) {
            _state.update { it.copy(doublePinyin = enabled, doubleBuffer = "") }
            pinyinPrefKeys.doublePinyin = enabled
        }

        fun setDoubleScheme(scheme: String) {
            _state.update { it.copy(doubleScheme = scheme) }
            pinyinPrefKeys.doubleScheme = scheme
        }

        fun setMixedInput(enabled: Boolean) {
            _state.update { it.copy(mixedInput = enabled) }
            pinyinPrefKeys.mixedInput = enabled
        }

        fun onSpace() {
            val st = _state.value
            if (st.candidates.isNotEmpty()) {
                onCandidateSelected(st.candidates[0])
            } else {
                _state.update { it.copy(inputBuffer = "", doubleBuffer = "", lastCommittedWord = " ") }
                scope.launch { _effects.emit(KeyboardEffect.CommitText(" ")) }
            }
            emitHaptic(KeyType.NORMAL)
            emitSound()
            recordInputStat(1, 0)
        }

        fun onCandidateSelected(candidate: String) {
            val prev = _state.value.lastCommittedWord
            scope.launch {
                dictionaryRepository.lastCommittedWord = prev
                dictionaryRepository.recordSelection(candidate)
                _state.update {
                    it.copy(
                        inputBuffer = "",
                        candidates = emptyList(),
                        doubleBuffer = "",
                        lastCommittedWord = candidate,
                    )
                }
                _effects.emit(KeyboardEffect.CommitText(candidate))
            }
            recordInputStat(candidate.length, 1)
        }

        fun onEnter() {
            scope.launch { _effects.emit(KeyboardEffect.CommitText("\n")) }
            emitHaptic(KeyType.SPECIAL)
            emitSound()
        }

        // Added by Agent B — handle long-press alt char selection
        fun onKeyLongPress(
            baseChar: Char,
            selectedAlt: Char,
        ) {
            emitHaptic(KeyType.LONG_PRESS)
            if (_state.value.mode == KeyboardMode.ALPHA && selectedAlt.isLetter()) {
                val newBuffer = _state.value.inputBuffer + selectedAlt
                updateCandidates(newBuffer)
            } else {
                scope.launch { _effects.emit(KeyboardEffect.CommitText(selectedAlt.toString())) }
            }
        }

        // Added by Agent B — long press on delete key: continuous delete
        fun onDeleteRepeat() {
            val st = _state.value
            if (st.doublePinyin && st.doubleBuffer.isNotEmpty()) {
                val newDoubleBuf = st.doubleBuffer.dropLast(1)
                _state.update { it.copy(doubleBuffer = newDoubleBuf) }
                val fullPinyin = PinyinProcessor.toFullPinyin(newDoubleBuf, st.doubleScheme)
                updateCandidates(fullPinyin)
            } else if (st.inputBuffer.isNotEmpty()) {
                val newBuffer = st.inputBuffer.dropLast(1)
                updateCandidates(newBuffer)
            } else {
                scope.launch { _effects.emit(KeyboardEffect.DeleteBackward) }
            }
            emitHaptic(KeyType.NORMAL)
        }

        // Added by Agent B — swipe/glide typing: convert path to pinyin and search
        fun onSwipeInput(path: List<Char>) {
            if (_state.value.mode != KeyboardMode.ALPHA) return
            if (path.isEmpty()) return
            val pinyin = path.joinToString("")
            updateCandidates(pinyin)
        }

        // Added by Agent B — toggle haptic feedback
        fun toggleHaptic() {
            val newValue = !_state.value.hapticEnabled
            _state.update { it.copy(hapticEnabled = newValue) }
            prefs.edit().putBoolean(PREFS_KEY_HAPTIC, newValue).apply()
        }

        // Added by Agent B — toggle key sound
        fun toggleSound() {
            val newValue = !_state.value.soundEnabled
            _state.update { it.copy(soundEnabled = newValue) }
            prefs.edit().putBoolean(PREFS_KEY_SOUND, newValue).apply()
        }

        fun startNewSession() {
            flushStats()
            sessionStartTime = System.currentTimeMillis()
            sessionTotalChars = 0
            sessionTotalWords = 0
            sessionUniqueChars.clear()
            sessionCount++
        }

        private fun recordInputStat(chars: Int, words: Int) {
            sessionTotalChars += chars
            sessionTotalWords += words
            if (chars > 0) {
                // unique chars tracking handled by candidate string chars
            }
        }

        private fun flushStats() {
            if (sessionTotalChars == 0 && sessionTotalWords == 0) return
            val today = dateFormat.format(Date())
            val elapsedMs = System.currentTimeMillis() - sessionStartTime
            val elapsedMin = (elapsedMs / 60000.0).coerceAtLeast(1.0)
            val avgSpeed = (sessionTotalChars / elapsedMin).toFloat()

            scope.launch(Dispatchers.IO) {
                inputStatDao.batchInsert(
                    listOf(
                        InputStat(
                            date = today,
                            totalChars = sessionTotalChars,
                            totalWords = sessionTotalWords,
                            uniqueChars = sessionUniqueChars.size,
                            sessionCount = sessionCount,
                            avgSpeed = avgSpeed,
                        ),
                    ),
                )
            }
        }

        fun onCleared() {
            flushStats()
            scope.coroutineContext[Job]?.cancel()
        }

        fun onPasteText(text: String) {
            scope.launch { _effects.emit(KeyboardEffect.Paste(text)) }
        }

        // Added by Agent D — multimodal input mode switchers
        fun setHandwritingMode() {
            _state.update { it.copy(mode = KeyboardMode.HANDWRITING) }
            emitHaptic(KeyType.SPECIAL)
        }

        fun setVoiceMode() {
            _state.update { it.copy(mode = KeyboardMode.VOICE) }
            emitHaptic(KeyType.SPECIAL)
        }

        fun setStrokeMode() {
            _state.update { it.copy(mode = KeyboardMode.STROKE) }
            emitHaptic(KeyType.SPECIAL)
        }
    }

// Added by Agent C — Pinyin settings SharedPreferences wrapper
private class PinyinPrefKeys(private val prefs: SharedPreferences) {
    var fuzzyEnabled: Boolean
        get() = prefs.getBoolean(PREF_FUZZY_ENABLED, true)
        set(v) = prefs.edit().putBoolean(PREF_FUZZY_ENABLED, v).apply()

    var fuzzyRules: Set<String>
        get() = prefs.getStringSet(PREF_FUZZY_RULES, DEFAULT_FUZZY_RULES) ?: DEFAULT_FUZZY_RULES
        set(v) = prefs.edit().putStringSet(PREF_FUZZY_RULES, v).apply()

    var autoCorrect: Boolean
        get() = prefs.getBoolean(PREF_AUTO_CORRECT, true)
        set(v) = prefs.edit().putBoolean(PREF_AUTO_CORRECT, v).apply()

    var doublePinyin: Boolean
        get() = prefs.getBoolean(PREF_DOUBLE_PINYIN, false)
        set(v) = prefs.edit().putBoolean(PREF_DOUBLE_PINYIN, v).apply()

    var doubleScheme: String
        get() = prefs.getString(PREF_DOUBLE_SCHEME, "xiaohe") ?: "xiaohe"
        set(v) = prefs.edit().putString(PREF_DOUBLE_SCHEME, v).apply()

    var mixedInput: Boolean
        get() = prefs.getBoolean(PREF_MIXED_INPUT, true)
        set(v) = prefs.edit().putBoolean(PREF_MIXED_INPUT, v).apply()

    companion object {
        private const val PREF_FUZZY_ENABLED = "pinyin_fuzzy_enabled"
        private const val PREF_FUZZY_RULES = "pinyin_fuzzy_rules"
        private const val PREF_AUTO_CORRECT = "pinyin_auto_correct"
        private const val PREF_DOUBLE_PINYIN = "pinyin_double"
        private const val PREF_DOUBLE_SCHEME = "pinyin_double_scheme"
        private const val PREF_MIXED_INPUT = "pinyin_mixed_input"
        private val DEFAULT_FUZZY_RULES = setOf("zh↔z", "ch↔c", "sh↔s", "n↔l", "ang↔an", "eng↔en", "ing↔in")
    }
}
