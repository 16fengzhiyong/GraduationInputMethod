package com.nuc.omeletteinputmethod.ui.floating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.nuc.omeletteinputmethod.ui.settings.Routes

@Composable
fun FloatingWindowContent(
    onDrag: (deltaX: Float, deltaY: Float) -> Unit,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            Row(
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FloatingMenuIcon(Icons.Filled.NoteAlt, "记事本") {
                    onNavigate(Routes.NOTEPAD_LIST)
                    isExpanded = false
                }
                FloatingMenuIcon(Icons.Filled.TextSnippet, "快捷短语") {
                    onNavigate(Routes.SHORTCUT_LIST)
                    isExpanded = false
                }
                FloatingMenuIcon(Icons.Filled.ContentPaste, "剪贴板") {
                    onNavigate(Routes.CLIPBOARD_LIST)
                    isExpanded = false
                }
                FloatingMenuIcon(Icons.Filled.TrendingUp, "输入统计") {
                    onNavigate(Routes.INPUT_STATS)
                    isExpanded = false
                }
                Spacer(modifier = Modifier.width(4.dp))
                FloatingMenuIcon(Icons.Filled.Close, "关闭", highlight = true) {
                    onClose()
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    }.clickable { isExpanded = !isExpanded },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Add,
                contentDescription = "展开菜单",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
fun FloatingMenuIcon(
    icon: ImageVector,
    desc: String,
    highlight: Boolean = false,
    onClick: () -> Unit = {},
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = if (highlight) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
    }
}
