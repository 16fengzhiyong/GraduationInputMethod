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
    const val THEME_SETTINGS = "theme_settings"
    const val ACCOUNT_LOGIN = "account_login"
    const val ACCOUNT_REGISTER = "account_register"
    const val ACCOUNT_PROFILE = "account_profile"
    const val THEME_STORE = "theme_store"
    const val CLOUD_SYNC = "cloud_sync"

    fun notepadEdit(noteId: Long = -1L) = "notepad_edit/$noteId"
    fun shortcutEdit(shortcutId: Long = -1L) = "shortcut_edit/$shortcutId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
)