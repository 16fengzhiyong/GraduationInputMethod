package com.nuc.omeletteinputmethod.kernel.keyboard

class KeyboardState private constructor() {

    companion object {
        var witchKeyboardNow = 1
        var witchKeyboardLast = 0
        val SETTING_KEYBOARD = 0
        val PINYIN_26_KEY_KEYBOARD = 1
        val PINYIN_9_KEY_KEYBOARD = 2
        val ENGLISH_26_KEY_KEYBOARD = 3
        val ENGLISH_9_KEY_KEYBOARD = 4
        val ARROWS_KEYBOARD = 5
//        val instance = KeyboardState()
    }
}
