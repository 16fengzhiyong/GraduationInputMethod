package com.nuc.omeletteinputmethod.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    startDestination: String = "dashboard",
    onCheckPermission: () -> Unit
) {
    var checkPermission by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(startDestination) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentScreen == "dashboard") "Omelette IME" else currentScreen) },
                navigationIcon = {
                    if (currentScreen != "dashboard") {
                        IconButton(onClick = { currentScreen = "dashboard" }) {
                            Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "dashboard" -> Dashboard(
                    onCheckPermission = onCheckPermission,
                    onNavigate = { screen -> currentScreen = screen }
                )
                "Notepad" -> com.nuc.omeletteinputmethod.ui.notepad.NotepadScreen()
                "Schedule" -> com.nuc.omeletteinputmethod.ui.schedule.ScheduleScreen()
                "Shortcuts" -> com.nuc.omeletteinputmethod.ui.shortcut.ShortcutScreen()
                "Translate" -> com.nuc.omeletteinputmethod.ui.translate.TranslateScreen()
                else -> Text("Coming Soon")
            }
        }
    }
}

@Composable
fun Dashboard(
    onCheckPermission: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Welcome to Omelette IME",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Button(
            onClick = onCheckPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Check Overlay Permissions")
        }

        DashboardItem("Notepad", "Quick notes and memos", androidx.compose.material.icons.Icons.Default.Edit) { onNavigate("Notepad") }
        DashboardItem("Schedule", "Manage your daily tasks", androidx.compose.material.icons.Icons.Default.DateRange) { onNavigate("Schedule") }
        DashboardItem("Shortcuts", "Customize input shortcuts", androidx.compose.material.icons.Icons.Default.List) { onNavigate("Shortcuts") }
        DashboardItem("Translate", "English <-> Chinese Translation", androidx.compose.material.icons.Icons.Default.Send) { onNavigate("Translate") }
    }
}

@Composable
fun DashboardItem(
    title: String, 
    subtitle: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector = androidx.compose.material.icons.Icons.Default.Info,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
