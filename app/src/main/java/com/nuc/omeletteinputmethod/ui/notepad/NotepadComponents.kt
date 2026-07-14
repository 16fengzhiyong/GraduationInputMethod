package com.nuc.omeletteinputmethod.ui.notepad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nuc.omeletteinputmethod.data.model.Note
import kotlin.math.roundToInt

val noteCardColors =
    listOf(
        Brush.verticalGradient(listOf(Color(0xFFF5F0EB), Color(0xFFFBF9F7))),
        Brush.verticalGradient(listOf(Color(0xFFEBF3F5), Color(0xFFF7FAFB))),
        Brush.verticalGradient(listOf(Color(0xFFF2F0F5), Color(0xFFFAF9FC))),
        Brush.verticalGradient(listOf(Color(0xFFF0F5F2), Color(0xFFF7FCF9))),
        Brush.verticalGradient(listOf(Color(0xFFF5F3EB), Color(0xFFFDFCF8))),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadTopBar(
    title: String,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    showSearch: Boolean = false,
    onSearchToggle: () -> Unit = {},
    onDone: () -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Delete, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        actions = {
            if (showSearch) {
                IconButton(onClick = onSearchToggle) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
            ),
    )
}

@Composable
fun NoteCard(
    note: Note,
    index: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var showDelete by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (showDelete) 1f else 0f,
        animationSpec = tween(200),
    )

    val bgBrush = noteCardColors[index % noteCardColors.size]

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -120) {
                                showDelete = true
                                offsetX = -80f
                            } else if (showDelete && offsetX > -40) {
                                showDelete = false
                                offsetX = 0f
                            } else {
                                offsetX = 0f
                                showDelete = false
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!showDelete) {
                                offsetX = (offsetX + dragAmount).coerceIn(-160f, 0f)
                            } else {
                                offsetX = (-80f + dragAmount).coerceIn(-80f, 0f)
                            }
                        },
                    )
                },
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgBrush),
            ) {
                Card(
                    onClick = onClick,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        if (note.title.isNotBlank()) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatTimestamp(note.updatedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showDelete,
                enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                exit = fadeOut(tween(200)) + scaleOut(tween(200)),
                modifier =
                    Modifier
                        .width(80.dp)
                        .align(Alignment.CenterVertically),
            ) {
                IconButton(
                    onClick = {
                        onDelete()
                        showDelete = false
                        offsetX = 0f
                    },
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    hint: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .padding(32.dp)
                .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val fmt =
        java.text.SimpleDateFormat(
            when {
                diff < 60_000 -> "刚刚"
                diff < 3600_000 -> "mm分钟前"
                diff < 86_400_000 -> "HH:mm"
                diff < 172_800_000 -> "昨天 HH:mm"
                diff < 604_800_000 -> "MM-dd HH:mm"
                else -> "yyyy-MM-dd"
            },
            java.util.Locale.CHINESE,
        )
    if (diff < 60_000 || diff < 3600_000) return fmt.format(date)
    return fmt.format(date)
}
