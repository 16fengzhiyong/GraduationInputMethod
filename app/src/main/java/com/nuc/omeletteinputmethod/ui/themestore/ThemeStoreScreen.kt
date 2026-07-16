package com.nuc.omeletteinputmethod.ui.themestore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nuc.omeletteinputmethod.data.remote.dto.StoreThemeDto
import com.nuc.omeletteinputmethod.ui.theme.KeyboardColors

/**
 * 主题商店界面 — 浏览、分类筛选、下载主题
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeStoreScreen(
    onNavigateBack: () -> Unit,
    viewModel: ThemeStoreViewModel = hiltViewModel()
) {
    val themes by viewModel.themes.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val downloadingThemeId by viewModel.downloadingThemeId.collectAsState()
    val downloadedThemeIds by viewModel.downloadedThemeIds.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 错误提示
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // 成功提示
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("主题商店") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KeyboardColors.DarkGray900,
                    titleContentColor = KeyboardColors.TextPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = KeyboardColors.Background
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(KeyboardColors.Background)
        ) {
            // ── 分类 Tab 栏 ──
            ScrollableCategoryTabs(
                categories = STORE_CATEGORIES,
                selected = selectedCategory,
                onSelect = { viewModel.fetchThemes(it) }
            )

            // ── 主题网格 ──
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = KeyboardColors.CyberBlue)
                }
            } else if (themes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无主题",
                        style = MaterialTheme.typography.bodyLarge,
                        color = KeyboardColors.TextSecondary
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(themes, key = { it.themeId }) { theme ->
                        ThemeCard(
                            theme = theme,
                            isDownloading = downloadingThemeId == theme.themeId,
                            isDownloaded = theme.themeId in downloadedThemeIds,
                            onDownload = { viewModel.downloadTheme(theme.themeId) },
                            onApply = { viewModel.applyTheme(theme.themeId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrollableCategoryTabs(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KeyboardColors.CyberBlue.copy(alpha = 0.2f),
                    selectedLabelColor = KeyboardColors.CyberBlue
                )
            )
        }
    }
}

@Composable
private fun ThemeCard(
    theme: StoreThemeDto,
    isDownloading: Boolean,
    isDownloaded: Boolean = false,
    onDownload: () -> Unit,
    onApply: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().then(
            if (isDownloaded) Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onApply
            ) else Modifier
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = KeyboardColors.DarkGray900
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // 预览色块区域
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(
                            brush = createPreviewGradient(theme.previewColors)
                        ),
                contentAlignment = Alignment.Center
            ) {
                // 显示主题名首字
                Text(
                    text = theme.themeName.take(2),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                // 主题名
                Text(
                    text = theme.themeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = KeyboardColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 作者
                Text(
                    text = "by ${theme.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = KeyboardColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 评分 + 下载量
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = KeyboardColors.Warning
                    )
                    Text(
                        text = String.format("%.1f", theme.rating),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 2.dp),
                        color = KeyboardColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = formatDownloadCount(theme.downloadCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = KeyboardColors.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 下载按钮
                Button(
                    onClick = onDownload,
                    enabled = !isDownloading && !isDownloaded,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDownloaded) KeyboardColors.DarkGray700 else KeyboardColors.CyberBlue,
                        disabledContainerColor = KeyboardColors.DarkGray700
                    )
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else if (isDownloaded) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = KeyboardColors.CyberBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "已下载",
                            style = MaterialTheme.typography.labelSmall,
                            color = KeyboardColors.CyberBlue
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "下载",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

/** 创建预览渐变 */
private fun createPreviewGradient(colors: List<String>): Brush {
    val parsed =
        colors.mapNotNull { hex ->
            runCatching {
                Color(hex.removePrefix("#").toLong(16) or 0xFF000000)
            }.getOrNull()
        }
    return if (parsed.size >= 2) {
        Brush.verticalGradient(parsed.take(4))
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFF60A5FA),
                Color(0xFF7C3AED)
            )
        )
    }
}

/** 格式化下载量：12580 → 1.2万 */
private fun formatDownloadCount(count: Int): String =
    when {
        count >= 10000 -> "${count / 10000}万+"
        count >= 1000 -> "${count / 1000}k+"
        else -> "$count+"
    }
