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

    // Added by Agent H — toolbar action effects
    object SwitchIME : KeyboardEffect()
    object OpenSettings : KeyboardEffect()
    object HideKeyboard : KeyboardEffect()

    // Text editing / cursor navigation effects
    object CursorLeft : KeyboardEffect()
    object CursorRight : KeyboardEffect()
    object CursorUp : KeyboardEffect()
    object CursorDown : KeyboardEffect()
    object Home : KeyboardEffect()
    object End : KeyboardEffect()
    object Copy : KeyboardEffect()
    object Cut : KeyboardEffect()
    object PasteFromClipboard : KeyboardEffect()
    object SelectAll : KeyboardEffect()

    // 文本编辑工具面板：撤销 / 重做 / 删除光标后字符
    object Undo : KeyboardEffect()
    object Redo : KeyboardEffect()
    object DeleteForward : KeyboardEffect()
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

// Added by Agent H — swipe-up alt character map: key char -> swipe-up symbol
    val SWIPE_UP_SYMBOLS: Map<Char, Char> =
        mapOf(
            // Row 0: QWERTYUIOP → 1-9,0
            'q' to '1', 'w' to '2', 'e' to '3', 'r' to '4', 't' to '5',
            'y' to '6', 'u' to '7', 'i' to '8', 'o' to '9', 'p' to '0',
            // Row 1: ASDFGHJKL → ~@#$%&*()
            'a' to '~', 's' to '@', 'd' to '#', 'f' to '$', 'g' to '%',
            'h' to '&', 'j' to '*', 'k' to '(', 'l' to ')',
            // Row 2: ZXCVBNM → -+=|\<> 
            'z' to '-', 'x' to '+', 'c' to '=', 'v' to '|', 'b' to '\\',
            'n' to '<', 'm' to '>',
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
        val preferenceManager: KeyboardPreferenceManager,
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
                val ok = dictionaryRepository.initializeDictionary()
                if (ok) {
                    _state.update { it.copy(dictionaryReady = true) }
                    android.util.Log.i("KeyboardVM", "Dictionary init OK")
                } else {
                    _state.update { it.copy(dictionaryReady = false) }
                    android.util.Log.e("KeyboardVM", "Dictionary init FAILED — candidates will be empty!")
                }
            }
        }

        // Updated by Agent C — fuzzy/double/mixed-input support
        // Updated by Agent H — English mode: letters commit directly
        fun onKeyChar(char: Char) {
            val st = _state.value

            // 五笔模式：字母累积到 wubiBuffer，查询 WubiEngine
            if (st.mode == KeyboardMode.WUBI) {
                if (char.isLetter()) {
                    val newBuffer = st.wubiBuffer + char.lowercaseChar()
                    // 五笔最多 4 码
                    if (newBuffer.length <= 4) {
                        val candidates = WubiEngine.query(newBuffer)
                        _state.update {
                            it.copy(
                                wubiBuffer = newBuffer,
                                wubiCandidates = candidates,
                                inputBuffer = newBuffer,
                                candidates = candidates,
                            )
                        }
                    }
                    emitHaptic(KeyType.NORMAL)
                    emitSound()
                } else {
                    // 非字母字符直接上屏
                    scope.launch { _effects.emit(KeyboardEffect.CommitText(char.toString())) }
                    emitHaptic(KeyType.NORMAL)
                    emitSound()
                }
                return
            }

            // Added by Agent H — English mode: letters directly commit to editor
            if (st.isEnglishMode && st.mode == KeyboardMode.ALPHA && char.isLetter()) {
                scope.launch { _effects.emit(KeyboardEffect.CommitText(char.toString())) }
                emitHaptic(KeyType.NORMAL)
                emitSound()
                return
            }

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

            // 五笔模式：从 wubiBuffer 删除最后一码
            if (st.mode == KeyboardMode.WUBI) {
                if (st.wubiBuffer.isNotEmpty()) {
                    val newBuffer = st.wubiBuffer.dropLast(1)
                    val candidates = if (newBuffer.isNotEmpty()) WubiEngine.query(newBuffer) else emptyList()
                    _state.update {
                        it.copy(
                            wubiBuffer = newBuffer,
                            wubiCandidates = candidates,
                            inputBuffer = newBuffer,
                            candidates = candidates,
                        )
                    }
                } else {
                    scope.launch { _effects.emit(KeyboardEffect.DeleteBackward) }
                }
                emitHaptic(KeyType.NORMAL)
                emitSound()
                return
            }

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
            if (!_state.value.dictionaryReady) {
                // Dictionary not initialized yet — keep buffer visible but show no candidates
                _state.update { it.copy(inputBuffer = buffer, candidates = emptyList()) }
                android.util.Log.w("KeyboardVM", "updateCandidates skipped: dictionary not ready")
                return
            }

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

            // 拼音音节切分：将 "nihao" → "ni hao" 等空格分隔形式，匹配 dict.db 存储格式
            // 注意：即使输入是合法单音节（如 "xian"），也要尝试多音节切分（"xi an"），
            // 因为 dict.db 中多字词的拼音是带空格的（"xi an" → 西安）
            val segmentedVariants = mutableListOf<String>()
            if (!isDouble) {
                for (variant in variants.distinct()) {
                    val segmentations = PinyinProcessor.segmentPinyin(variant)
                    for (seg in segmentations) {
                        if (seg != variant && seg.contains(" ")) {
                            segmentedVariants.add(seg)
                        }
                    }
                    // 模糊展开后的变体也需要切分
                    if (variant != buffer) {
                        val segVariants = PinyinProcessor.segmentPinyin(variant)
                        for (seg in segVariants) {
                            if (seg.contains(" ") && seg !in segmentedVariants) {
                                segmentedVariants.add(seg)
                            }
                        }
                    }
                }
            }
            val allQueryVariants = (variants + segmentedVariants).distinct()

            val allCandidates = mutableListOf<String>()
            val seenCandidates = mutableSetOf<String>()
            for (variant in allQueryVariants) {
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

                val emojiRecommendations = getEmojiRecommendations(buffer)
                val finalCandidates = (emojiRecommendations + allCandidates).distinct()

                _state.update {
                    it.copy(
                        inputBuffer = if (isDouble) it.doubleBuffer else buffer,
                        candidates = finalCandidates,
                        expandedCandidates = false,
                    )
                }
                android.util.Log.i("KeyboardVM", "updateCandidates: buffer=\"$buffer\" variants=${variants.size} segVariants=${segmentedVariants.size} finalCandidates=${finalCandidates.size} first=\"${finalCandidates.firstOrNull() ?: ""}\"")
                insertCandidatesMeta(finalCandidates)
            }

        fun setMode(mode: KeyboardMode) {
            // 切换模式时清空五笔缓冲区
            if (mode != KeyboardMode.WUBI) {
                _state.update { it.copy(mode = mode, wubiBuffer = "", wubiCandidates = emptyList()) }
            } else {
                _state.update { it.copy(mode = mode) }
            }
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

        private fun getEmojiRecommendations(input: String): List<String> {
            val emojiMap = mapOf(
                "哈哈" to "😂", "开心" to "😄", "难过" to "😢", "生气" to "😡",
                "爱" to "❤️", "心" to "❤️", "心碎" to "💔", "喜欢" to "🥰",
                "笑" to "😄", "哭" to "😭", "怒" to "😡", "怕" to "😱",
                "棒" to "👍", "赞" to "👍", "好的" to "👌", "OK" to "👌",
                "谢谢" to "🙏", "加油" to "💪", "晚安" to "😴", "早安" to "🌅",
                "生日" to "🎂", "庆祝" to "🎉", "礼物" to "🎁", "鲜花" to "💐",
                "太阳" to "☀️", "月亮" to "🌙", "星星" to "⭐", "下雨" to "🌧️",
                "吃" to "🍔", "喝" to "🥤", "饿" to "😋", "馋" to "🤤",
                "睡" to "😴", "累" to "😩", "困" to "😪", "懒" to "🦥",
                "帅" to "😎", "美" to "💃", "酷" to "😎", "牛" to "🐂",
                "冲" to "🏃", "跑" to "🏃", "走" to "🚶", "飞" to "✈️",
                "钱" to "💰", "穷" to "💸", "富" to "🤑", "买" to "🛒",
                "家" to "🏠", "回" to "🏠", "学校" to "🏫", "公司" to "🏢",
                "猫" to "🐱", "狗" to "🐶", "猪" to "🐷", "鸡" to "🐔",
                "拜拜" to "👋", "再见" to "👋", "嗨" to "👋", "你好" to "👋"
            )
            return emojiMap.entries
                .filter { input.contains(it.key) }
                .map { it.value }
                .distinct()
                .take(3)
        }

        fun onSpace() {
            val st = _state.value
            // Added by Agent H — English mode: always commit space
            if (st.isEnglishMode) {
                _state.update { it.copy(inputBuffer = "", doubleBuffer = "", lastCommittedWord = " ") }
                scope.launch { _effects.emit(KeyboardEffect.CommitText(" ")) }
                emitHaptic(KeyType.NORMAL)
                emitSound()
                return
            }
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
                // 五笔模式：选字后清空五笔缓冲区
                if (_state.value.mode == KeyboardMode.WUBI) {
                    _state.update {
                        it.copy(
                            inputBuffer = "",
                            candidates = emptyList(),
                            doubleBuffer = "",
                            wubiBuffer = "",
                            wubiCandidates = emptyList(),
                            lastCommittedWord = candidate,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            inputBuffer = "",
                            candidates = emptyList(),
                            doubleBuffer = "",
                            lastCommittedWord = candidate,
                        )
                    }
                }
                _effects.emit(KeyboardEffect.CommitText(candidate))
            }
            recordInputStat(candidate.length, 1)
            trackCommittedChars(candidate)
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
            // Track unique Chinese characters — iterate the committed text chars
            if (chars > 0) {
                // The candidate string characters will be added via trackCommittedChars
            }
        }

        /** Call this from onCandidateSelected to track unique chars from the committed word */
        private fun trackCommittedChars(word: String) {
            for (c in word) {
                sessionUniqueChars.add(c)
            }
        }

        private fun flushStats() {
            if (sessionTotalChars == 0 && sessionTotalWords == 0) return
            val today = dateFormat.format(Date())
            val elapsedMs = System.currentTimeMillis() - sessionStartTime
            val elapsedMin = (elapsedMs / 60000.0).coerceAtLeast(1.0)
            val avgSpeed = (sessionTotalChars / elapsedMin).toFloat()

            // Use a separate IO context to ensure write completes even if ViewModel scope is cancelling
            kotlinx.coroutines.runBlocking(Dispatchers.IO) {
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

        private suspend fun insertCandidatesMeta(candidates: List<String>) {
            val pinnedMap = candidates.associateWith { dictionaryRepository.isWordPinned(it) }
            val candidatesMeta = candidates.map { CandidateItem(word = it, isPinned = pinnedMap[it] == true) }
            _state.update { it.copy(candidatesMeta = candidatesMeta) }
        }

        fun togglePinCandidate(candidate: String) {
            scope.launch {
                dictionaryRepository.togglePin(candidate)
                val newMeta = _state.value.candidatesMeta.map {
                    if (it.word == candidate) it.copy(isPinned = !it.isPinned) else it
                }
                _state.update { it.copy(candidatesMeta = newMeta) }
            }
        }

        fun deleteCandidate(candidate: String) {
            val newMeta = _state.value.candidatesMeta.filter { it.word != candidate }
            val newCandidates = _state.value.candidates.filter { it != candidate }
            _state.update { it.copy(candidatesMeta = newMeta, candidates = newCandidates) }
        }

        fun setExpandedCandidatesGrid(expanded: Boolean) {
            _state.update { it.copy(expandedCandidatesGrid = expanded) }
        }

        // Added by Agent H — toggle Chinese/English input mode
        fun toggleLanguageMode() {
            val newMode = !_state.value.isEnglishMode
            _state.update { it.copy(isEnglishMode = newMode) }
            emitHaptic(KeyType.SPECIAL)
            // Toast will be shown by KeyboardScreen observing state change
        }

        // Added by Agent H — search/enter action key (blue button)
        fun onSearch() {
            val st = _state.value
            if (st.inputBuffer.isNotEmpty()) {
                // Commit first candidate if available, otherwise commit raw buffer
                val commitText = st.candidates.firstOrNull() ?: st.inputBuffer
                scope.launch { _effects.emit(KeyboardEffect.CommitText(commitText)) }
                _state.update { it.copy(inputBuffer = "", doubleBuffer = "", candidates = emptyList()) }
            } else {
                scope.launch { _effects.emit(KeyboardEffect.CommitText("\n")) }
            }
            emitHaptic(KeyType.SPECIAL)
            emitSound()
        }

        // Added by Agent H — toolbar actions forwarded to IME
        fun onSwitchIME() {
            scope.launch { _effects.emit(KeyboardEffect.SwitchIME) }
        }

        fun onOpenSettings() {
            scope.launch { _effects.emit(KeyboardEffect.OpenSettings) }
        }

        fun onHideKeyboard() {
            scope.launch { _effects.emit(KeyboardEffect.HideKeyboard) }
        }

        // Added by Agent H — handle swipe-up on a letter key
        fun onSwipeUp(baseChar: Char) {
            val symbol = SWIPE_UP_SYMBOLS[baseChar]
            if (symbol != null) {
                emitHaptic(KeyType.SWIPE)
                emitSound()
                // Commit the symbol directly — swipe-up always outputs the mapped symbol
                scope.launch { _effects.emit(KeyboardEffect.CommitText(symbol.toString())) }
            }
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

        // Text editing / cursor navigation actions
        fun onCursorLeft() { scope.launch { _effects.emit(KeyboardEffect.CursorLeft) } }
        fun onCursorRight() { scope.launch { _effects.emit(KeyboardEffect.CursorRight) } }
        fun onCursorUp() { scope.launch { _effects.emit(KeyboardEffect.CursorUp) } }
        fun onCursorDown() { scope.launch { _effects.emit(KeyboardEffect.CursorDown) } }
        fun onHome() { scope.launch { _effects.emit(KeyboardEffect.Home) } }
        fun onEnd() { scope.launch { _effects.emit(KeyboardEffect.End) } }
        fun onCopy() { scope.launch { _effects.emit(KeyboardEffect.Copy) } }
        fun onCut() { scope.launch { _effects.emit(KeyboardEffect.Cut) } }
        fun onPaste() { scope.launch { _effects.emit(KeyboardEffect.PasteFromClipboard) } }
        fun onSelectAll() { scope.launch { _effects.emit(KeyboardEffect.SelectAll) } }

        // 文本编辑工具面板：撤销 / 重做 / 删除光标后字符
        fun onUndo() { scope.launch { _effects.emit(KeyboardEffect.Undo) } }
        fun onRedo() { scope.launch { _effects.emit(KeyboardEffect.Redo) } }
        fun onDeleteForward() { scope.launch { _effects.emit(KeyboardEffect.DeleteForward) } }

        fun setArrowsMode() {
            _state.update { it.copy(mode = KeyboardMode.ARROWS) }
            emitHaptic(KeyType.SPECIAL)
        }

        fun setSecureMode(isSecure: Boolean) {
            _state.update { it.copy(isSecureMode = isSecure) }
        }

        fun setClipboardMode() {
            _state.update { it.copy(mode = KeyboardMode.CLIPBOARD) }
            emitHaptic(KeyType.SPECIAL)
        }

        // 打开文本编辑工具面板
        fun setEditToolMode() {
            _state.update { it.copy(mode = KeyboardMode.EDIT_TOOL) }
            emitHaptic(KeyType.SPECIAL)
        }

        /**
         * Cycle through available input modes: ALPHA → T9 → SYMBOL → HANDWRITING → VOICE → STROKE → EMOJI → ALPHA
         */
        fun cycleInputMode() {
            val current = _state.value.mode
            val next = when (current) {
                KeyboardMode.ALPHA -> KeyboardMode.T9
                KeyboardMode.T9 -> KeyboardMode.SYMBOL
                KeyboardMode.SYMBOL -> KeyboardMode.HANDWRITING
                KeyboardMode.HANDWRITING -> KeyboardMode.VOICE
                KeyboardMode.VOICE -> KeyboardMode.STROKE
                KeyboardMode.STROKE -> KeyboardMode.EMOJI
                KeyboardMode.EMOJI -> KeyboardMode.ALPHA
                else -> KeyboardMode.ALPHA  // NUMBER/ARROWS/CLIPBOARD fallback to ALPHA
            }
            _state.update { it.copy(mode = next) }
            emitHaptic(KeyType.SPECIAL)
        }

        /**
         * 通过 typeId 切换键盘类型 — 由 KeyboardSwitchPanel 调用
         *
         * @param typeId 目标键盘的 typeId（来自 KeyboardRegistry）
         * @return 是否成功切换
         */
        fun switchKeyboardType(typeId: String): Boolean {
            val target = KeyboardRegistry.findById(typeId) ?: return false
            if (!target.enabled) return false

            // 持久化到本地
            preferenceManager.switchTo(typeId)

            // 更新 KeyboardMode
            _state.update {
                it.copy(
                    mode = target.mode,
                    // 英文模式特殊处理：切换 isEnglishMode 标志
                    isEnglishMode = (typeId == KeyboardRegistry.ID_ENGLISH),
                    // 切换键盘时清空输入缓冲
                    inputBuffer = "",
                    candidates = emptyList(),
                    expandedCandidates = false,
                    // 五笔缓冲区清空
                    wubiBuffer = "",
                    wubiCandidates = emptyList(),
                )
            }
            emitHaptic(KeyType.SPECIAL)
            return true
        }

        /**
         * 打开键盘切换面板
         */
        fun showSwitchPanel() {
            _state.update { it.copy(switchPanelVisible = true) }
            emitHaptic(KeyType.SPECIAL)
        }

        /**
         * 关闭键盘切换面板
         */
        fun hideSwitchPanel() {
            _state.update { it.copy(switchPanelVisible = false) }
        }

        /**
         * Retry dictionary initialization. Called by IME on each onStartInput
         * so that if the first init failed (e.g. assets not yet extracted),
         * subsequent input sessions can recover.
         */
        fun retryDictionaryInit() {
            if (_state.value.dictionaryReady) return
            scope.launch {
                val ok = dictionaryRepository.initializeDictionary()
                if (ok) {
                    _state.update { it.copy(dictionaryReady = true) }
                    android.util.Log.i("KeyboardVM", "Dictionary retry init OK")
                }
            }
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
