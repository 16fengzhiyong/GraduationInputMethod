package com.nuc.omeletteinputmethod.ui.sync

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 云同步管理界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(
    onNavigateBack: () -> Unit,
    viewModel: CloudSyncViewModel = hiltViewModel()
) {
    val syncState by viewModel.syncState.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val syncResult by viewModel.syncResult.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE) }

    // 同步完成提示 — 仅显示 Snackbar，不清空状态（保留结果卡片供用户查看）
    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("云同步管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // ── 同步状态图标 ──
            SyncStateIcon(syncState)

            Spacer(modifier = Modifier.height(16.dp))

            // 状态文字
            Text(
                text =
                    when (syncState) {
                        SyncState.IDLE -> "准备同步"
                        SyncState.SYNCING -> "同步中..."
                        SyncState.SUCCESS -> "同步完成"
                        SyncState.ERROR -> "同步失败"
                    },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 上次同步时间
            Text(
                text =
                    lastSyncTime?.let { "上次同步：${dateFormat.format(Date(it))}" }
                        ?: "尚未同步",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 同步结果统计
            if (syncResult != null && syncState == SyncState.SUCCESS) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "同步结果",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("上传：${syncResult!!.uploaded} 条")
                        Text("下载：${syncResult!!.downloaded} 条")
                        if (syncResult!!.conflicts > 0) {
                            Text("冲突解决：${syncResult!!.conflicts} 条")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── 同步数据类型说明 ──
            Text(
                text = "同步内容",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            SyncDataTypeRow(icon = Icons.Filled.CloudSync, label = "用户词典", description = "已学习的词汇和频率")
            SyncDataTypeRow(icon = Icons.Filled.CloudSync, label = "记事本", description = "所有笔记内容")
            SyncDataTypeRow(icon = Icons.Filled.CloudSync, label = "快捷短语", description = "自定义快捷文本")
            SyncDataTypeRow(icon = Icons.Filled.CloudSync, label = "剪贴板", description = "剪贴板历史记录")
            SyncDataTypeRow(icon = Icons.Filled.CloudSync, label = "输入统计", description = "每日输入字数统计")

            Spacer(modifier = Modifier.height(32.dp))

            // ── 操作按钮 ──
            Button(
                onClick = { viewModel.syncAll() },
                enabled = syncState != SyncState.SYNCING,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (syncState == SyncState.SYNCING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (syncState == SyncState.SYNCING) "同步中..." else "立即全量同步",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.uploadOnly() },
                    enabled = syncState != SyncState.SYNCING,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Filled.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("仅上传")
                }

                OutlinedButton(
                    onClick = { viewModel.downloadOnly() },
                    enabled = syncState != SyncState.SYNCING,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Filled.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("仅下载")
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SyncStateIcon(syncState: SyncState) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val iconColor by animateColorAsState(
        targetValue =
            when (syncState) {
                SyncState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                SyncState.SYNCING -> MaterialTheme.colorScheme.primary
                SyncState.SUCCESS -> Color(0xFF10B981)
                SyncState.ERROR -> MaterialTheme.colorScheme.error
            },
        label = "icon_color"
    )

    Box(
        modifier =
            Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector =
                when (syncState) {
                    SyncState.IDLE -> Icons.Filled.CloudOff
                    SyncState.SYNCING -> Icons.Filled.CloudSync
                    SyncState.SUCCESS -> Icons.Filled.CloudDone
                    SyncState.ERROR -> Icons.Filled.Error
            },
            contentDescription = null,
            modifier =
                Modifier
                    .size(40.dp)
                    .then(if (syncState == SyncState.SYNCING) Modifier.rotate(rotation) else Modifier),
            tint = iconColor
        )
    }
}

@Composable
private fun SyncDataTypeRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
