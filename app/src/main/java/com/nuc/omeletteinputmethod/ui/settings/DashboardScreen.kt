package com.nuc.omeletteinputmethod.ui.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nuc.omeletteinputmethod.ui.clipboard.ClipboardViewModel
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardViewModel
import com.nuc.omeletteinputmethod.ui.notepad.NotepadViewModel
import com.nuc.omeletteinputmethod.ui.shortcut.ShortcutViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateNotepad: () -> Unit,
    onNavigateShortcut: () -> Unit,
    onNavigateTranslate: () -> Unit,
    onNavigateClipboard: () -> Unit,
    onNavigatePinyinSettings: () -> Unit,
    onNavigateInputStats: () -> Unit,
    onCheckPermission: () -> Unit,
    keyboardViewModel: KeyboardViewModel? = null,
    clipboardViewModel: ClipboardViewModel = viewModel(),
    notepadViewModel: NotepadViewModel = viewModel(),
    shortcutViewModel: ShortcutViewModel = viewModel()
) {
    val notes by notepadViewModel.notes.collectAsState()
    val shortcuts by shortcutViewModel.shortcuts.collectAsState()
    val clipboardItems by clipboardViewModel.items.collectAsState()
    val maxItems by clipboardViewModel.maxItems.collectAsState()
    val kbState by keyboardViewModel?.state?.collectAsState() ?: remember { mutableStateOf(null) }
    val hapticEnabled = kbState?.hapticEnabled
    val soundEnabled = kbState?.soundEnabled
    var showAboutDialog by remember { mutableStateOf(false) }
    var showMaxItemsDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顶部欢迎区域
        Text(
            text = dateFormat.format(Date()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "煎蛋输入法",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "更简洁，更高效",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 功能卡片区域
        Text(
            text = "功能",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        FeatureCard(
            icon = Icons.Filled.NoteAlt,
            title = "记事本",
            subtitle = "记录点滴，随手即得",
            badge = "${notes.size} 条",
            gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE8F0FE),
                    Color(0xFFF5F8FD)
                )
            ),
            onClick = onNavigateNotepad
        )

        FeatureCard(
            icon = Icons.Filled.TextSnippet,
            title = "快捷短语",
            subtitle = "常用文本，一键输入",
            badge = "${shortcuts.size} 条",
            gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFEF3E8),
                    Color(0xFFFEFBF7)
                )
            ),
            onClick = onNavigateShortcut
        )

        FeatureCard(
            icon = Icons.Filled.Share,
            title = "翻译工具",
            subtitle = "英汉互译，快速便捷",
            gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF0F0F8),
                    Color(0xFFF8F7FC)
                )
            ),
            onClick = onNavigateTranslate
        )

        FeatureCard(
            icon = Icons.Filled.ContentPaste,
            title = "剪贴板",
            subtitle = "历史记录，随时粘贴",
            badge = "${clipboardItems.size} 条",
            gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF0FEF0),
                    Color(0xFFF8FFF8)
                )
            ),
            onClick = onNavigateClipboard
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 设置区域
        Text(
            text = "设置",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        SettingRow(
            label = "悬浮窗权限",
            description = "管理在其他应用上显示的权限",
            onClick = onCheckPermission
        )

        // Added by Agent C — Pinyin settings entry
        SettingRow(
            label = "拼音设置",
            description = "模糊音、拼音纠错、双拼、中英文混输",
            onClick = onNavigatePinyinSettings
        )

        SettingRow(
            label = "输入统计",
            description = "查看每日输入字数、热力图与趋势",
            onClick = onNavigateInputStats
        )

        SettingRow(
            label = "剪贴板历史上限",
            description = "当前上限：${maxItems} 条（超出时自动清理）",
            onClick = { showMaxItemsDialog = true }
        )

        // Added by Agent B — haptic feedback toggle
        if (hapticEnabled != null) {
            ToggleRow(
                label = "按键震动反馈",
                description = if (hapticEnabled) "已开启" else "已关闭",
                checked = hapticEnabled,
                onToggle = { keyboardViewModel?.toggleHaptic() }
            )
        }

        // Added by Agent B — key click sound toggle
        if (soundEnabled != null) {
            ToggleRow(
                label = "按键音效",
                description = if (soundEnabled) "已开启" else "已关闭",
                checked = soundEnabled,
                onToggle = { keyboardViewModel?.toggleSound() }
            )
        }

        SettingRow(
            label = "关于煎蛋输入法",
            description = "版本信息与帮助",
            onClick = { showAboutDialog = true }
        )
    }

    if (showAboutDialog) {
        BasicAlertDialog(
            onDismissRequest = { showAboutDialog = false }
        ) {
            Box(
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "煎蛋输入法",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "版本 1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "基于 Kotlin + Jetpack Compose 构建",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "集成记事本、快捷短语、翻译等实用工具\n致力于提供简洁高效的输入体验",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAboutDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("关闭", color = Color.White)
                    }
                }
            }
        }
    }

    if (showMaxItemsDialog) {
        val options = listOf(50, 100, 200, 500, 1000, 1500, 2000)
        BasicAlertDialog(
            onDismissRequest = { showMaxItemsDialog = false }
        ) {
            Box(
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("剪贴板历史上限", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "超出上限时将自动清理最旧的未置顶项",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options.forEach { option ->
                            Button(
                                onClick = {
                                    clipboardViewModel.setMaxItems(option)
                                    showMaxItemsDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (option == maxItems)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = 4.dp,
                                    vertical = 8.dp
                                )
                            ) {
                                Text(
                                    "$option",
                                    style = MaterialTheme.typography.labelSmall,
color = if (option == maxItems) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    gradientBrush: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(gradientBrush)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .animateContentSize(spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingRow(
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}