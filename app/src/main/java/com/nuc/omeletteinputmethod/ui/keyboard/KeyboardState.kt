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

    // Text editing / arrow keys panel
    ARROWS,
    // Clipboard history panel
    CLIPBOARD,
    // 文本编辑工具面板（摇杆 + 操作网格）
    EDIT_TOOL,
    // 五笔输入模式
    WUBI,
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
    // Added by Agent H — swipe-up gesture feedback
    SWIPE,
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
    // Added by Agent H — English input mode toggle
    val isEnglishMode: Boolean = false,
    // Candidate bar expanded mode — shows all candidates when true, paginated when false
    val expandedCandidates: Boolean = false,
    // Whether the dictionary engine has been initialized successfully
    val dictionaryReady: Boolean = false,
    // 键盘切换面板是否可见
    val switchPanelVisible: Boolean = false,
    // Updated candidates list with metadata for gestures and pinning
    val candidatesMeta: List<CandidateItem> = emptyList(),
    // Whether to show all candidates in a grid overlay
    val expandedCandidatesGrid: Boolean = false,
    // Whether the current input field is a password field
    val isSecureMode: Boolean = false,
    // 五笔：当前已输入的字根编码缓冲区（纯字母）
    val wubiBuffer: String = "",
    // 五笔：当前编码对应的候选字词列表
    val wubiCandidates: List<String> = emptyList(),
    // 续词联想：上屏后通过字前缀联想出的下文候选词
    val associatedCandidates: List<String> = emptyList(),
)

data class CandidateItem(
    val word: String,
    val isPinned: Boolean = false,
)
