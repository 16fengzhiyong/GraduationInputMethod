package com.nuc.omeletteinputmethod.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nuc.omeletteinputmethod.ui.theme.KeyShapeConfig
import com.nuc.omeletteinputmethod.ui.theme.KeyShapeType
import com.nuc.omeletteinputmethod.ui.theme.NightModeStrategy
import com.nuc.omeletteinputmethod.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onNavigateBack: () -> Unit,
    themeManager: ThemeManager = hiltViewModel()
) {
    val currentSkin by themeManager.currentSkin.collectAsState()
    val nightStrategy by themeManager.nightStrategy.collectAsState()
    val autoNightMode by themeManager.autoNightMode.collectAsState()
    val keyShape by themeManager.keyShape.collectAsState()
    val keySpacing by themeManager.keySpacingDp.collectAsState()

    val builtinSkins = remember { themeManager.getAllBuiltinSkins() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("皮肤与主题") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Skin Selection
            Text("选择皮肤", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                builtinSkins.forEach { skin ->
                    val isSelected = currentSkin.skinId == skin.skinId
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { themeManager.setSkin(skin) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(skin.colors["primary"] ?: Color.Gray)
                                .then(
                                    if (isSelected) Modifier.padding(2.dp).background(Color.White, CircleShape)
                                    else Modifier
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = skin.skinName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    }
                }
            }

            HorizontalDivider()

            // Night Mode Strategy
            Text("夜间模式", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = nightStrategy == NightModeStrategy.SYSTEM,
                    onClick = { themeManager.setNightStrategy(NightModeStrategy.SYSTEM) },
                    label = { Text("跟随系统") }
                )
                FilterChip(
                    selected = nightStrategy == NightModeStrategy.TIME,
                    onClick = { themeManager.setNightStrategy(NightModeStrategy.TIME) },
                    label = { Text("定时") }
                )
                FilterChip(
                    selected = nightStrategy == NightModeStrategy.LIGHT_SENSOR,
                    onClick = { themeManager.setNightStrategy(NightModeStrategy.LIGHT_SENSOR) },
                    label = { Text("光线感应") }
                )
            }

            HorizontalDivider()

            // Key Shape
            Text("按键形状", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
FilterChip(
            selected = keyShape.shapeType == KeyShapeType.ROUNDED,
            onClick = { themeManager.setKeyShape(KeyShapeConfig(KeyShapeType.ROUNDED, 16f)) },
            label = { Text("圆角") }
        )
        FilterChip(
            selected = keyShape.shapeType == KeyShapeType.CIRCLE,
            onClick = { themeManager.setKeyShape(KeyShapeConfig(KeyShapeType.CIRCLE, 20f)) },
            label = { Text("胶囊") }
        )
        FilterChip(
            selected = keyShape.shapeType == KeyShapeType.SHARP,
            onClick = { themeManager.setKeyShape(KeyShapeConfig(KeyShapeType.SHARP, 8f)) },
            label = { Text("方形") }
        )
            }

            HorizontalDivider()

            // Key Spacing
            Text("按键间距: ${keySpacing}dp", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = keySpacing.toFloat(),
                onValueChange = { themeManager.setKeySpacing(it.toInt()) },
                valueRange = 0f..8f,
                steps = 7
            )
        }
    }
}
