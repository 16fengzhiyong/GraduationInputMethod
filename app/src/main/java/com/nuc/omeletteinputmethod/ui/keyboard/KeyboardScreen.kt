package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val KeyColors = MaterialTheme.colorScheme

@Composable
fun KeyboardScreen(viewModel: KeyboardViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeyColors.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CandidateBar(
            candidates = state.candidates,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            onSelect = { viewModel.onCandidateSelected(it) }
        )

        Spacer(modifier = Modifier.height(2.dp))

        when (state.mode) {
            KeyboardMode.ALPHA -> PinyinKeyboard(state = state, onKey = viewModel::onKeyChar, onDelete = viewModel::onDelete, onSpace = viewModel::onSpace, onToggleShift = viewModel::toggleShift, onSetMode = viewModel::setMode)
            KeyboardMode.SYMBOL -> SymbolKeyboard(onKey = viewModel::onKeyChar, onDelete = viewModel::onDelete, onSpace = viewModel::onSpace, onSetMode = viewModel::setMode)
            KeyboardMode.NUMBER -> NumberKeyboard(onKey = viewModel::onKeyChar, onDelete = viewModel::onDelete, onSetMode = viewModel::setMode)
        }
    }
}

@Composable
fun CandidateBar(
    candidates: List<String>,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    Box(
        modifier = modifier
            .background(KeyColors.surfaceVariant),
        contentAlignment = Alignment.CenterStart
    ) {
        if (candidates.isEmpty()) {
            Text(
                text = "Omelette IME",
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = KeyColors.onSurfaceVariant
            )
        } else {
            LazyRow(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(candidates) { index, candidate ->
                    val isFirst = index == 0
                    Surface(
                        onClick = { onSelect(candidate) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isFirst) KeyColors.primaryContainer else KeyColors.surface,
                        tonalElevation = if (isFirst) 2.dp else 0.dp
                    ) {
                        Text(
                            text = candidate,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (isFirst) KeyColors.onPrimaryContainer else KeyColors.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PinyinKeyboard(
    state: KeyboardState,
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onToggleShift: () -> Unit,
    onSetMode: (KeyboardMode) -> Unit
) {
    val keys = remember(state.isShifted) {
        val alpha = if (state.isShifted) "QWERTYUIOPASDFGHJKLZXCVBNM" else "qwertyuiopasdfghjklzxcvbnm"
        listOf(
            alpha.substring(0, 10).toList(),
            alpha.substring(10, 19).toList(),
            alpha.substring(19, 26).toList()
        )
    }

    val keyHeight = 44.dp
    val spacerHeight = 4.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(spacerHeight)
    ) {
        repeat(3) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = when (rowIndex) {
                    0 -> Arrangement.SpaceEvenly
                    1 -> Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                    2 -> {
                        Arrangement.SpaceEvenly
                    }
                    else -> Arrangement.SpaceEvenly
                }
            ) {
                if (rowIndex == 2) {
                    KeyButton(
                        label = if (state.isShifted) "⇧" else "⇧",
                        width = 40.dp,
                        height = keyHeight,
                        action = { onToggleShift() }
                    )
                }
                keys[rowIndex].forEach { char ->
                    KeyButton(
                        label = char.toString(),
                        width = ((if (rowIndex == 0) 8.6f else if (rowIndex == 1) 10.0f else 7.5f) / keys[rowIndex].size).dp,
                        height = keyHeight,
                        action = { onKey(char) }
                    )
                }
                if (rowIndex == 2) {
                    KeyButton(
                        label = "⌫",
                        width = 40.dp,
                        height = keyHeight,
                        action = { onDelete() }
                    )
                }
            }
        }

        BottomBar(
            onSetMode = onSetMode,
            onSpace = onSpace
        )
    }
}

@Composable
fun SymbolKeyboard(
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onSetMode: (KeyboardMode) -> Unit
) {
    val symbols = listOf(
        listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0'),
        listOf('@', '#', '$', '%', '&', '*', '-', '+', '(', ')'),
        listOf('!', '"', '\'', ':', ';', '/', '?', '.', ',')
    )

    val keyHeight = 44.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (rowIndex == 2) {
                    KeyButton("⌫", width = 36.dp, height = keyHeight, action = { onDelete() })
                }
                symbols[rowIndex].forEach { char ->
                    KeyButton(char.toString(), width = if (rowIndex == 0) 30.dp else 28.dp, height = keyHeight, action = { onKey(char) })
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            KeyButton("ABC", width = 56.dp, height = keyHeight, action = { onSetMode(KeyboardMode.ALPHA) })
            KeyButton("123", width = 56.dp, height = keyHeight, action = { onSetMode(KeyboardMode.NUMBER) })
            KeyButton("空格", modifier = Modifier.weight(1f), height = keyHeight, action = { onSpace() })
            KeyButton("↵", width = 56.dp, height = keyHeight, action = { onKey('\n') })
        }
    }
}

@Composable
fun NumberKeyboard(
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onSetMode: (KeyboardMode) -> Unit
) {
    val nums = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9'),
        listOf(null, '0', null)
    )
    val keyHeight = 44.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(4) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (rowIndex == 3) {
                    KeyButton("符", width = 56.dp, height = keyHeight, action = { onSetMode(KeyboardMode.SYMBOL) })
                } else {
                    KeyButton("⌫", width = 44.dp, height = keyHeight, action = { onDelete() })
                }
                nums[rowIndex].forEach { char ->
                    if (char != null) {
                        KeyButton(char.toString(), width = 52.dp, height = keyHeight, action = { onKey(char) })
                    } else {
                        Spacer(modifier = Modifier.width(52.dp))
                    }
                }
                if (rowIndex == 3) {
                    KeyButton("ABC", width = 56.dp, height = keyHeight, action = { onSetMode(KeyboardMode.ALPHA) })
                } else {
                    Spacer(modifier = Modifier.width(44.dp))
                }
            }
        }
    }
}

@Composable
fun BottomBar(
    onSetMode: (KeyboardMode) -> Unit,
    onSpace: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KeyButton("符", width = 40.dp, height = 36.dp, action = { onSetMode(KeyboardMode.SYMBOL) })
        KeyButton("123", width = 40.dp, height = 36.dp, action = { onSetMode(KeyboardMode.NUMBER) })
        KeyButton("空格", modifier = Modifier.weight(1f), height = 36.dp, action = { onSpace() })
        KeyButton("↵", width = 48.dp, height = 36.dp, action = { })
    }
}

@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    width: Dp = 30.dp,
    height: Dp = 44.dp,
    action: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    val bgColor = if (pressed) KeyColors.tertiaryContainer else KeyColors.surfaceVariant
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(shape)
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { action() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = KeyColors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}