package com.nuc.omeletteinputmethod.ui.keyboard

enum class KeyboardMode {
    ALPHA,
    SYMBOL,
    NUMBER,
    T9,

    // Added by Agent F — emoji panel mode
    EMOJI,

    // Added by Agent D — multimodal input modes
    HANDWRITING,
    VOICE,
    STROKE,
}

// Added by Agent A
enum class OneHandMode {
    OFF,
    LEFT,
    RIGHT,
    SPLIT,
}

// Added by Agent B — key type for haptic feedback intensity
enum class KeyType {
    NORMAL,
    SPECIAL,
    LONG_PRESS,
}

data class KeyboardState(
    val inputBuffer: String = "",
    val candidates: List<String> = emptyList(),
    val mode: KeyboardMode = KeyboardMode.ALPHA,
    val isShifted: Boolean = false,
    val currentPage: Int = 0,
    // Added by Agent A
    val oneHandMode: OneHandMode = OneHandMode.OFF,
    // Added by Agent A — keyboard height as fraction of screen (0.3f ~ 0.6f)
    val keyboardHeightPercent: Float = 0.4f,
    // Added by Agent B — haptic feedback toggle
    val hapticEnabled: Boolean = true,
    // Added by Agent B — key click sound toggle
    val soundEnabled: Boolean = true,
    // Added by Agent F — candidate bar font size in sp
    val candidateFontSize: Int = 14,
    // Added by Agent C — Pinyin settings
    val fuzzyEnabled: Boolean = true,
    val fuzzyRules: Set<String> = setOf("zh↔z", "ch↔c", "sh↔s", "n↔l", "ang↔an", "eng↔en", "ing↔in"),
    val autoCorrect: Boolean = true,
    val doublePinyin: Boolean = false,
    val doubleScheme: String = "xiaohe",
    val mixedInput: Boolean = true,
    val doubleBuffer: String = "",
    // Added by Agent E — last committed word for user bigram learning context
    val lastCommittedWord: String = "",
)
