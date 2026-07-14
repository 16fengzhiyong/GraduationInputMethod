package com.nuc.omeletteinputmethod.ui.keyboard

enum class KeyboardMode {
    ALPHA, SYMBOL, NUMBER
}

data class KeyboardState(
    val inputBuffer: String = "", // Current Pinyin buffer
    val candidates: List<String> = emptyList(),
    val mode: KeyboardMode = KeyboardMode.ALPHA,
    val isShifted: Boolean = false,
    val currentPage: Int = 0 // Candidate page
)
