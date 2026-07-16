package com.nuc.omeletteinputmethod.ui.settings

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nuc.omeletteinputmethod.ui.notepad.NotepadEditScreen
import com.nuc.omeletteinputmethod.ui.notepad.NotepadListScreen
import com.nuc.omeletteinputmethod.ui.shortcut.ShortcutEditScreen
import com.nuc.omeletteinputmethod.ui.shortcut.ShortcutListScreen
import com.nuc.omeletteinputmethod.ui.translate.TranslateScreen
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardViewModel
import com.nuc.omeletteinputmethod.ui.clipboard.ClipboardPanel
import com.nuc.omeletteinputmethod.ui.stats.InputStatsScreen

private val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD, "首页", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Routes.NOTEPAD_LIST, "记事本", Icons.Filled.NoteAlt, Icons.Outlined.NoteAlt),
    BottomNavItem(Routes.SHORTCUT_LIST, "快捷短语", Icons.Filled.TextSnippet, Icons.Outlined.TextSnippet),
    BottomNavItem(Routes.CLIPBOARD_LIST, "剪贴板", Icons.Filled.ContentPaste, Icons.Outlined.ContentPaste)
)

@Composable
fun SettingsScreen(
    startDestination: String = Routes.DASHBOARD,
    onCheckPermission: () -> Unit,
    // Added by Agent B — for haptic/sound toggle access
    keyboardViewModel: KeyboardViewModel? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showBottomBar by rememberSaveable { mutableStateOf(true) }

    val bottomBarRoutes = bottomNavItems.map { it.route }
    showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 2.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.icon else item.selectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNavigateNotepad = { navController.navigate(Routes.NOTEPAD_LIST) },
                    onNavigateShortcut = { navController.navigate(Routes.SHORTCUT_LIST) },
                    onNavigateTranslate = { navController.navigate(Routes.TRANSLATE) },
                    onNavigateClipboard = { navController.navigate(Routes.CLIPBOARD_LIST) },
                    onNavigatePinyinSettings = { navController.navigate(Routes.PINYIN_SETTINGS) },
                    onNavigateInputStats = { navController.navigate(Routes.INPUT_STATS) },
                    onCheckPermission = onCheckPermission,
                    keyboardViewModel = keyboardViewModel
                )
            }
            composable(Routes.NOTEPAD_LIST) {
                NotepadListScreen(
                    onEditNote = { noteId ->
                        navController.navigate(Routes.notepadEdit(noteId))
                    }
                )
            }
            composable(
                route = Routes.NOTEPAD_EDIT,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) { entry ->
                val noteId = entry.arguments?.getLong("noteId") ?: -1L
                NotepadEditScreen(
                    noteId = noteId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SHORTCUT_LIST) {
                ShortcutListScreen(
                    onEditShortcut = { shortcutId ->
                        navController.navigate(Routes.shortcutEdit(shortcutId))
                    }
                )
            }
            composable(
                route = Routes.SHORTCUT_EDIT,
                arguments = listOf(navArgument("shortcutId") { type = NavType.LongType })
            ) { entry ->
                val shortcutId = entry.arguments?.getLong("shortcutId") ?: -1L
                ShortcutEditScreen(
                    shortcutId = shortcutId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.TRANSLATE) {
                TranslateScreen()
            }
            composable(Routes.CLIPBOARD_LIST) {
                ClipboardPanel()
            }
            // Added by Agent C — Pinyin settings page
            composable(Routes.PINYIN_SETTINGS) {
                val vm = keyboardViewModel
                if (vm != null) {
                    PinyinSettingsScreen(
                        viewModel = vm,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
            composable(Routes.INPUT_STATS) {
                InputStatsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}