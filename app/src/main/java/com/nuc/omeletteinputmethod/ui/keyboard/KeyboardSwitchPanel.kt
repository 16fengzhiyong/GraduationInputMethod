package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 键盘切换面板 — 在键盘上方弹出的浮层
 *
 * 布局结构：
 *   1. 顶部工具栏（网格/皮肤/个人中心/钱包 图标）
 *   2. 主体内容区（3x4 网格键盘选项列表）
 *   3. 选中高亮 + 点击关闭
 *
 * @param visible          面板是否可见
 * @param onDismiss        点击外部区域或完成切换后的关闭回调
 * @param onKeyboardSelect 选中某个键盘项的回调（传入 typeId）
 * @param preferenceManager 键盘偏好管理器，读取当前选中项
 */
@Composable
fun KeyboardSwitchPanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    onKeyboardSelect: (String) -> Unit,
    preferenceManager: KeyboardPreferenceManager,
    onPersonalCenter: () -> Unit = {},
    onThemeStore: () -> Unit = {},
    onAllApps: () -> Unit = {},
) {
    val currentTypeId by preferenceManager.currentTypeId.collectAsState()

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },  // 从下方滑入
            animationSpec = tween(250),
        ) + fadeIn(animationSpec = tween(250)),
        exit = slideOutVertically(
            targetOffsetY = { it },   // 向下滑出
            animationSpec = tween(200),
        ) + fadeOut(animationSpec = tween(200)),
    ) {
        // ── 半透明遮罩 + 面板容器 ──
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.Transparent),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(bottom = 8.dp),
            ) {
                // ── 顶部小三角指示器 ──
                TriangleIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )

                // ── 顶部工具栏 ──
                PanelToolBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    onPersonalCenter = onPersonalCenter,
                    onThemeStore = onThemeStore,
                    onAllApps = onAllApps,
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── 键盘选项网格 ──
                KeyboardOptionGrid(
                    currentTypeId = currentTypeId,
                    onItemClick = { typeId ->
                        onKeyboardSelect(typeId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * 三角指示器 — 面板顶部的小三角
 */
@Composable
private fun TriangleIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,  // 用小三角图标代替，或使用 Canvas 绘制
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

/**
 * 面板顶部工具栏 — 4 个功能入口图标
 */
@Composable
private fun PanelToolBar(
    modifier: Modifier = Modifier,
    onPersonalCenter: () -> Unit,
    onThemeStore: () -> Unit,
    onAllApps: () -> Unit,
) {
    Row(
        modifier = modifier.height(48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PanelToolBarIcon(
            icon = Icons.Filled.GridView,
            label = "全部应用",
            onClick = onAllApps,
        )
        PanelToolBarIcon(
            icon = Icons.Filled.TravelExplore,
            label = "皮肤主题",
            onClick = onThemeStore,
        )
        PanelToolBarIcon(
            icon = Icons.Filled.AccountCircle,
            label = "个人中心",
            onClick = onPersonalCenter,
        )
        PanelToolBarIcon(
            icon = Icons.Filled.ShoppingBag,
            label = "钱包商城",
            onClick = { /* TODO: 钱包商城 */ },
        )
    }
}

@Composable
private fun PanelToolBarIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
        )
    }
}

/**
 * 键盘选项网格 — 3 列自适应网格布局
 */
@Composable
private fun KeyboardOptionGrid(
    currentTypeId: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.height(280.dp),  // 限制高度，3 行可见
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(
            items = KeyboardRegistry.all,
            key = { it.typeId },
        ) { keyboardType ->
            KeyboardOptionItem(
                keyboardType = keyboardType,
                isSelected = keyboardType.typeId == currentTypeId,
                onClick = {
                    if (keyboardType.enabled) {
                        onItemClick(keyboardType.typeId)
                    }
                },
            )
        }
    }
}

/**
 * 单个键盘选项卡片
 */
@Composable
private fun KeyboardOptionItem(
    keyboardType: KeyboardType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    // 选中态：主题色高亮；未选中：灰色；禁用：半透明灰色
    val bgColor =
        when {
            !keyboardType.enabled -> colors.surfaceVariant.copy(alpha = 0.3f)
            isSelected -> colors.primary.copy(alpha = 0.12f)
            else -> colors.surfaceVariant.copy(alpha = 0.5f)
        }
    val borderColor =
        when {
            !keyboardType.enabled -> Color.Transparent
            isSelected -> colors.primary
            else -> Color.Transparent
        }
    val iconTint =
        when {
            !keyboardType.enabled -> colors.onSurface.copy(alpha = 0.3f)
            isSelected -> colors.primary
            else -> colors.onSurfaceVariant
        }
    val textColor =
        when {
            !keyboardType.enabled -> colors.onSurface.copy(alpha = 0.3f)
            isSelected -> colors.primary
            else -> colors.onSurfaceVariant
        }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = keyboardType.enabled,
                    onClick = onClick,
                ),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp),
            ) {
                // 图标
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = keyboardType.icon,
                        contentDescription = keyboardType.displayName,
                        modifier = Modifier.size(26.dp),
                        tint = iconTint,
                    )
                    // "更多语言" 图标叠加 + 号
                    if (keyboardType.typeId == KeyboardRegistry.ID_MORE_LANG) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.BottomEnd),
                            tint = iconTint,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 显示名称
                Text(
                    text = keyboardType.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // 选中状态的右下角勾选标记
            if (isSelected && keyboardType.enabled) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "当前选中",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    tint = colors.primary,
                )
            }
        }
    }
}
