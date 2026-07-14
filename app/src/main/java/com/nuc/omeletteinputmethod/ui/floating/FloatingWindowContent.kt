package com.nuc.omeletteinputmethod.ui.floating

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nuc.omeletteinputmethod.R

@Composable
fun FloatingWindowContent(
    onDrag: (deltaX: Float, deltaY: Float) -> Unit,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    if (isExpanded) {
        // Expanded Menu
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MenuIcon(Icons.Default.DateRange, "Schedule") { onNavigate("Schedule"); isExpanded = false }
            MenuIcon(Icons.Default.Edit, "Notepad") { onNavigate("Notepad"); isExpanded = false }
            MenuIcon(Icons.Default.List, "Shortcuts") { onNavigate("Shortcuts"); isExpanded = false }
            MenuIcon(Icons.Default.Close, "Close") {
                isExpanded = false
            }
        }
    } else {
        // Collapsed Ball
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
                .clickable { isExpanded = !isExpanded },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun MenuIcon(icon: ImageVector, desc: String, onClick: () -> Unit = {}) {
    IconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = desc)
    }
}
