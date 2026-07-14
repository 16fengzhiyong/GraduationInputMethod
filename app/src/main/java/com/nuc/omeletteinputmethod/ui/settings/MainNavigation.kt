package com.nuc.omeletteinputmethod.ui.settings

object Routes {
    const val DASHBOARD = "dashboard"
    const val NOTEPAD_LIST = "notepad_list"
    const val NOTEPAD_EDIT = "notepad_edit/{noteId}"
    const val SHORTCUT_LIST = "shortcut_list"
    const val SHORTCUT_EDIT = "shortcut_edit/{shortcutId}"
    const val TRANSLATE = "translate"
    const val PINYIN_SETTINGS = "pinyin_settings"
    const val CLIPBOARD_LIST = "clipboard_list"
    const val INPUT_STATS = "input_stats"

    fun notepadEdit(noteId: Long = -1L) = "notepad_edit/$noteId"
    fun shortcutEdit(shortcutId: Long = -1L) = "shortcut_edit/$shortcutId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
)