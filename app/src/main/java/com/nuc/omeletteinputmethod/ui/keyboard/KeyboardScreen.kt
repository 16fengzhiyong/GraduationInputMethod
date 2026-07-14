package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nuc.omeletteinputmethod.ui.multimodal.HandwritingPanel
import com.nuc.omeletteinputmethod.ui.multimodal.HandwritingViewModel
import com.nuc.omeletteinputmethod.ui.multimodal.StrokeInputPanel
import com.nuc.omeletteinputmethod.ui.multimodal.StrokeInputViewModel
import com.nuc.omeletteinputmethod.ui.multimodal.VoiceInputPanel
import com.nuc.omeletteinputmethod.ui.multimodal.VoiceInputViewModel
import kotlinx.coroutines.launch

@Composable
fun KeyboardScreen(viewModel: KeyboardViewModel) {
    val state by viewModel.state.collectAsState()
    val keyColors = MaterialTheme.colorScheme

    val handwritingViewModel: HandwritingViewModel = hiltViewModel()
    val voiceViewModel: VoiceInputViewModel = hiltViewModel()
    val strokeViewModel: StrokeInputViewModel = hiltViewModel()

    // Added by Agent B — global key position map for swipe path calculation
    val keyPositionMap = remember { mutableStateMapOf<Char, Rect>() }

    // Added by Agent B — swipe trajectory state
    var swipeActive by remember { mutableStateOf(false) }
    val swipePath = remember { mutableStateListOf<Offset>() }
    val traversedKeys = remember { mutableStateListOf<Char>() }
    val highlightedKeys = remember { mutableStateListOf<Char>() }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(state.keyboardHeightPercent)
                .background(keyColors.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Added by Agent A — one-hand mode switch bar
        OneHandModeBar(
            currentMode = state.oneHandMode,
            onSetMode = viewModel::setOneHandMode,
        )

        CandidateBar(
            candidates = state.candidates,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            onSelect = { viewModel.onCandidateSelected(it) },
        )

        Spacer(modifier = Modifier.height(2.dp))

        when (state.mode) {
            KeyboardMode.ALPHA ->
                PinyinKeyboard(
                    state = state,
                    onKey = viewModel::onKeyChar,
                    onDelete = viewModel::onDelete,
                    onSpace = viewModel::onSpace,
                    onEnter = viewModel::onEnter,
                    onToggleShift = viewModel::toggleShift,
                    onSetMode = viewModel::setMode,
                    oneHandMode = state.oneHandMode,
                    keyPositionMap = keyPositionMap,
                    onLongPressAlt = { baseChar, selectedAlt ->
                        viewModel.onKeyLongPress(baseChar, selectedAlt)
                    },
                    onDeleteRepeat = viewModel::onDeleteRepeat,
                    swipeActive = swipeActive,
                    onSwipeActiveChange = { swipeActive = it },
                    swipePath = swipePath,
                    traversedKeys = traversedKeys,
                    highlightedKeys = highlightedKeys,
                    onSwipeEnd = {
                        if (traversedKeys.isNotEmpty()) {
                            viewModel.onSwipeInput(traversedKeys.toList())
                        }
                        swipeActive = false
                        swipePath.clear()
                        traversedKeys.clear()
                        highlightedKeys.clear()
                    },
                    onClipboardPasteText = viewModel::onPasteText,
                )

            // Added by Agent F — emoji panel mode
            KeyboardMode.EMOJI ->
                EmojiPanel(
                    themeManager = hiltViewModel(),
                    onEmojiSelected = { emoji ->
                        viewModel.onCandidateSelected(emoji)
                    },
                    onBack = { viewModel.setMode(KeyboardMode.ALPHA) },
                )

            KeyboardMode.SYMBOL ->
                SymbolKeyboard(
                    onKey = viewModel::onKeyChar,
                    onDelete = viewModel::onDelete,
                    onSpace = viewModel::onSpace,
                    onEnter = viewModel::onEnter,
                    onSetMode = viewModel::setMode,
                    oneHandMode = state.oneHandMode,
                    keyPositionMap = keyPositionMap,
                    onLongPressAlt = { baseChar, selectedAlt ->
                        viewModel.onKeyLongPress(baseChar, selectedAlt)
                    },
                    onDeleteRepeat = viewModel::onDeleteRepeat,
                )

            KeyboardMode.NUMBER ->
                NumberKeyboard(
                    onKey = viewModel::onKeyChar,
                    onDelete = viewModel::onDelete,
                    onEnter = viewModel::onEnter,
                    onSpace = viewModel::onSpace,
                    onSetMode = viewModel::setMode,
                    oneHandMode = state.oneHandMode,
                    keyPositionMap = keyPositionMap,
                    onLongPressAlt = { baseChar, selectedAlt ->
                        viewModel.onKeyLongPress(baseChar, selectedAlt)
                    },
                    onDeleteRepeat = viewModel::onDeleteRepeat,
                )

            // Added by Agent A — T9 nine-grid keyboard
            KeyboardMode.T9 ->
                T9Keyboard(
                    state = state,
                    onKey = viewModel::onT9Key,
                    onDelete = viewModel::onT9Delete,
                    onSpace = viewModel::onT9Space,
                    onEnter = viewModel::onEnter,
                    onSetMode = viewModel::setMode,
                    oneHandMode = state.oneHandMode,
                )

            // Added by Agent D — multimodal input panels
            KeyboardMode.HANDWRITING ->
                HandwritingPanel(
                    viewModel = handwritingViewModel,
                    onBackToAlpha = { viewModel.setMode(KeyboardMode.ALPHA) },
                )

            KeyboardMode.VOICE ->
                VoiceInputPanel(
                    viewModel = voiceViewModel,
                    onBackToAlpha = { viewModel.setMode(KeyboardMode.ALPHA) },
                )

            KeyboardMode.STROKE ->
                StrokeInputPanel(
                    viewModel = strokeViewModel,
                    onBackToAlpha = { viewModel.setMode(KeyboardMode.ALPHA) },
                )
        }

        // Added by Agent A — keyboard height drag slider
        HeightSlider(
            currentPercent = state.keyboardHeightPercent,
            onHeightChange = viewModel::setKeyboardHeight,
        )
    }
}

@Composable
fun CandidateBar(
    candidates: List<String>,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier =
            modifier
                .background(colors.surfaceVariant),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (candidates.isEmpty()) {
            Text(
                text = "Omelette IME",
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        } else {
            LazyRow(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(candidates) { index, candidate ->
                    val isFirst = index == 0
                    Surface(
                        onClick = { onSelect(candidate) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isFirst) colors.primaryContainer else colors.surface,
                        tonalElevation = if (isFirst) 2.dp else 0.dp,
                    ) {
                        Text(
                            text = candidate,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (isFirst) colors.onPrimaryContainer else colors.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
    onEnter: () -> Unit,
    onToggleShift: () -> Unit,
    onSetMode: (KeyboardMode) -> Unit,
    // Added by Agent A
    oneHandMode: OneHandMode,
    // Added by Agent B — swipe/glide parameters
    keyPositionMap: MutableMap<Char, Rect>,
    onLongPressAlt: (baseChar: Char, selectedAlt: Char) -> Unit,
    onDeleteRepeat: () -> Unit,
    swipeActive: Boolean,
    onSwipeActiveChange: (Boolean) -> Unit,
    swipePath: MutableList<Offset>,
    traversedKeys: MutableList<Char>,
    highlightedKeys: MutableList<Char>,
    onSwipeEnd: () -> Unit,
    // Added by Agent G — clipboard quick paste from keyboard
    onClipboardPasteText: (String) -> Unit = {},
) {
    val keys =
        remember(state.isShifted) {
            val alpha = if (state.isShifted) "QWERTYUIOPASDFGHJKLZXCVBNM" else "qwertyuiopasdfghjklzxcvbnm"
            listOf(
                alpha.substring(0, 10).toList(),
                alpha.substring(10, 19).toList(),
                alpha.substring(19, 26).toList(),
            )
        }

    val keyHeight = 44.dp
    val spacerHeight = 4.dp

    // Added by Agent A
    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    val swipeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(handModifier)
                .padding(horizontal = 4.dp)
                .drawWithContent {
                    drawContent()
                    if (swipeActive && swipePath.size >= 2) {
                        val path = Path()
                        path.moveTo(swipePath[0].x, swipePath[0].y)
                        for (i in 1 until swipePath.size) {
                            path.lineTo(swipePath[i].x, swipePath[i].y)
                        }
                        drawPath(
                            path = path,
                            color = swipeColor,
                            style =
                                Stroke(
                                    width = 8.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round,
                                ),
                        )
                    }
                }.pointerInput(state.mode == KeyboardMode.ALPHA) {
                    if (state.mode != KeyboardMode.ALPHA) return@pointerInput
                    detectDragGestures(
                        onDragStart = { offset ->
                            onSwipeActiveChange(true)
                            swipePath.clear()
                            traversedKeys.clear()
                            highlightedKeys.clear()
                            swipePath.add(offset)
                            highlightedKeys.clear()
                            resolveKeyForPoint(offset, keyPositionMap, traversedKeys, highlightedKeys)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val pos = change.position
                            swipePath.add(pos)
                            resolveKeyForPoint(pos, keyPositionMap, traversedKeys, highlightedKeys)
                        },
                        onDragEnd = { onSwipeEnd() },
                        onDragCancel = {
                            onSwipeActiveChange(false)
                            swipePath.clear()
                            traversedKeys.clear()
                            highlightedKeys.clear()
                        },
                    )
                },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(spacerHeight),
        ) {
            repeat(3) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        when (rowIndex) {
                            0 -> Arrangement.SpaceEvenly
                            1 -> Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                            2 -> Arrangement.SpaceEvenly
                            else -> Arrangement.SpaceEvenly
                        },
                ) {
                    if (rowIndex == 2) {
                        KeyButton(
                            label = "⇧",
                            width = 40.dp * scale,
                            height = keyHeight * scale,
                            action = { onToggleShift() },
                            keyChar = null,
                            keyPositionMap = keyPositionMap,
                            highlighted = false,
                            onLongPressAlt = null,
                            onDeleteRepeat = null,
                            longPressAlts = null,
                            sizeScale = scale,
                        )
                    }
                    keys[rowIndex].forEach { char ->
                        val isHighlighted = highlightedKeys.contains(char)
                        KeyButton(
                            label = char.toString(),
                            width = (
                                (
                                    (
                                        if (rowIndex == 0) {
                                            8.6f
                                        } else if (rowIndex == 1) {
                                            10.0f
                                        } else {
                                            7.5f
                                        }
                                    ) / keys[rowIndex].size
                                ).dp * scale
                            ),
                            height = keyHeight * scale,
                            action = { onKey(char) },
                            keyChar = char,
                            keyPositionMap = keyPositionMap,
                            highlighted = isHighlighted,
                            onLongPressAlt = { alt -> onLongPressAlt(char, alt) },
                            onDeleteRepeat = null,
                            longPressAlts = LONG_PRESS_ALTERNATES[char],
                            sizeScale = scale,
                        )
                    }
                    if (rowIndex == 2) {
                        KeyButton(
                            label = "⌫",
                            width = 40.dp * scale,
                            height = keyHeight * scale,
                            action = { onDelete() },
                            keyChar = null,
                            keyPositionMap = keyPositionMap,
                            highlighted = false,
                            onLongPressAlt = null,
                            onDeleteRepeat = onDeleteRepeat,
                            longPressAlts = null,
                            sizeScale = scale,
                        )
                    }
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Added by Agent F — emoji mode button
                KeyButton(
                    label = "\uD83D\uDE04", width = 36.dp * scale, height = 36.dp * scale,
                    action = { onSetMode(KeyboardMode.EMOJI) },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                )
                KeyButton(
                    label = "符", width = 40.dp * scale, height = 36.dp * scale,
                    action = { onSetMode(KeyboardMode.SYMBOL) },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                )
                // Added by Agent D — handwriting mode button
                KeyButton(
                    label = "手写", width = 40.dp * scale, height = 36.dp * scale,
                    action = { onSetMode(KeyboardMode.HANDWRITING) },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                )
                // Added by Agent D — voice input mode button
                KeyButton(
                    label = "\uD83C\uDF99", width = 36.dp * scale, height = 36.dp * scale,
                    action = { onSetMode(KeyboardMode.VOICE) },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                )
                // Added by Agent D — stroke input mode button
                KeyButton(
                    label = "笔画", width = 40.dp * scale, height = 36.dp * scale,
                    action = { onSetMode(KeyboardMode.STROKE) },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                )
                // Added by Agent A — nine-grid mode button
                KeyButton(
                    label = "九宫", width = 40.dp * scale, height = 36.dp * scale,
                    action = { onSetMode(KeyboardMode.T9) },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                )
                // Added by Agent G — clipboard quick paste popup
                ClipboardQuickButton(
                    onPaste = onClipboardPasteText,
                    scale = scale,
                )
                KeyButton(
                    label = "123", width = 40.dp * scale, height = 36.dp * scale,
                    action = { onSetMode(KeyboardMode.NUMBER) },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                )
                KeyButton(
                    label = "空格", modifier = Modifier.weight(1f), height = 36.dp * scale,
                    action = { onSpace() },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null,
                    longPressAlts = null,
                    onLongPressAction = { onSetMode(KeyboardMode.SYMBOL) },
                    sizeScale = scale,
                )
                KeyButton(
                    label = "↵", width = 48.dp * scale, height = 36.dp * scale,
                    action = { onEnter() },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                )
            }
        }
    }
}

@Composable
fun SymbolKeyboard(
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onSetMode: (KeyboardMode) -> Unit,
    // Added by Agent A
    oneHandMode: OneHandMode,
    // Added by Agent B
    keyPositionMap: MutableMap<Char, Rect>,
    onLongPressAlt: (baseChar: Char, selectedAlt: Char) -> Unit,
    onDeleteRepeat: () -> Unit,
) {
    val symbols =
        listOf(
            listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0'),
            listOf('@', '#', '$', '%', '&', '*', '-', '+', '(', ')'),
            listOf('!', '"', '\'', ':', ';', '/', '?', '.', ','),
        )
    val keyHeight = 44.dp

    // Added by Agent A
    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(handModifier)
                .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(3) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                if (rowIndex == 2) {
                    KeyButton(
                        label = "⌫", width = 36.dp * scale, height = keyHeight * scale,
                        action = { onDelete() },
                        keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                        onLongPressAlt = null, onDeleteRepeat = onDeleteRepeat, longPressAlts = null,
                        sizeScale = scale,
                    )
                }
                symbols[rowIndex].forEach { char ->
                    KeyButton(
                        label = char.toString(),
                        width = (if (rowIndex == 0) 30.dp else 28.dp) * scale,
                        height = keyHeight * scale,
                        action = { onKey(char) },
                        keyChar = char,
                        keyPositionMap = keyPositionMap,
                        highlighted = false,
                        onLongPressAlt = { alt -> onLongPressAlt(char, alt) },
                        onDeleteRepeat = null,
                        longPressAlts = LONG_PRESS_ALTERNATES[char],
                        sizeScale = scale,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            KeyButton(
                "ABC", width = 56.dp * scale, height = keyHeight * scale, action = { onSetMode(KeyboardMode.ALPHA) },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
            )
            // Added by Agent A — nine-grid mode button
            KeyButton(
                "九宫", width = 56.dp * scale, height = keyHeight * scale, action = { onSetMode(KeyboardMode.T9) },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
            )
            KeyButton(
                "123", width = 56.dp * scale, height = keyHeight * scale, action = { onSetMode(KeyboardMode.NUMBER) },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
            )
            KeyButton(
                "空格", modifier = Modifier.weight(1f), height = keyHeight * scale, action = { onSpace() },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
            )
            KeyButton(
                "↵", width = 56.dp * scale, height = keyHeight * scale, action = { onEnter() },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
            )
        }
    }
}

@Composable
fun NumberKeyboard(
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit,
    onSpace: () -> Unit,
    onSetMode: (KeyboardMode) -> Unit,
    // Added by Agent A
    oneHandMode: OneHandMode,
    // Added by Agent B
    keyPositionMap: MutableMap<Char, Rect>,
    onLongPressAlt: (baseChar: Char, selectedAlt: Char) -> Unit,
    onDeleteRepeat: () -> Unit,
) {
    val nums =
        listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf(null, '0', null),
        )
    val keyHeight = 44.dp

    // Added by Agent A
    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(handModifier)
                .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(4) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                if (rowIndex == 3) {
                    KeyButton(
                        "符", width = 56.dp * scale, height = keyHeight * scale,
                        action = { onSetMode(KeyboardMode.SYMBOL) },
                        keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                        onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                        sizeScale = scale,
                    )
                } else {
                    KeyButton(
                        "⌫", width = 44.dp * scale, height = keyHeight * scale,
                        action = { onDelete() },
                        keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                        onLongPressAlt = null, onDeleteRepeat = onDeleteRepeat, longPressAlts = null,
                        sizeScale = scale,
                    )
                }
                nums[rowIndex].forEach { char ->
                    if (char != null) {
                        KeyButton(
                            char.toString(), width = 52.dp * scale, height = keyHeight * scale,
                            action = { onKey(char) },
                            keyChar = char, keyPositionMap = keyPositionMap, highlighted = false,
                            onLongPressAlt = { alt -> onLongPressAlt(char, alt) },
                            onDeleteRepeat = null,
                            longPressAlts = LONG_PRESS_ALTERNATES[char],
                            sizeScale = scale,
                        )
                    } else {
                        Spacer(modifier = Modifier.width(52.dp * scale))
                    }
                }
                if (rowIndex == 3) {
                    KeyButton(
                        "↵", width = 56.dp * scale, height = keyHeight * scale,
                        action = { onEnter() },
                        keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                        onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                        sizeScale = scale,
                    )
                } else {
                    Spacer(modifier = Modifier.width(44.dp * scale))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            KeyButton(
                "ABC", width = 80.dp * scale, height = 36.dp * scale,
                action = { onSetMode(KeyboardMode.ALPHA) },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
            )
            KeyButton(
                "空格", modifier = Modifier.weight(1f), height = 36.dp * scale,
                action = { onSpace() },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
            )
        }
    }
}

// Added by Agent B — resolve touch point to key char using position map
private fun resolveKeyForPoint(
    point: Offset,
    keyPositionMap: Map<Char, Rect>,
    traversedKeys: MutableList<Char>,
    highlightedKeys: MutableList<Char>,
) {
    for ((char, rect) in keyPositionMap) {
        if (rect.contains(point)) {
            if (traversedKeys.isEmpty() || traversedKeys.last() != char) {
                traversedKeys.add(char)
            }
            if (!highlightedKeys.contains(char)) {
                highlightedKeys.add(char)
            }
            return
        }
    }
}

/**
 * KeyButton — same public API signature (label, modifier, width, height, action).
 * Agent B extensions: long-press popup, delete repeat, key position registration.
 */
@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    width: Dp = 30.dp,
    height: Dp = 44.dp,
    action: () -> Unit,
    // Added by Agent B
    keyChar: Char? = null,
    keyPositionMap: MutableMap<Char, Rect>? = null,
    highlighted: Boolean = false,
    onLongPressAlt: ((Char) -> Unit)? = null,
    onDeleteRepeat: (() -> Unit)? = null,
    longPressAlts: List<Char>? = null,
    onLongPressAction: (() -> Unit)? = null,
    // Added by Agent A
    sizeScale: Float = 1f,
) {
    var pressed by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    // Added by Agent B — long-press popup state
    var showLongPressPopup by remember { mutableStateOf(false) }
    var popupHoveredIndex by remember { mutableStateOf(-1) }
    val isDeleteKey = label == "⌫"

    val bgColor =
        when {
            showLongPressPopup -> colors.tertiaryContainer
            highlighted -> colors.primaryContainer
            pressed -> colors.tertiaryContainer
            else -> colors.surfaceVariant
        }
    val shape = RoundedCornerShape(6.dp * sizeScale)

    Box(
        modifier =
            modifier
                .width(width)
                .height(height)
                .clip(shape)
                .background(bgColor)
                .onGloballyPositioned { coords ->
                    if (keyChar != null && keyPositionMap != null) {
                        val rect = coords.boundsInParent()
                        keyPositionMap[keyChar] = rect
                    }
                }.pointerInput(isDeleteKey, longPressAlts, onLongPressAction) {
                    detectTapGestures(
                        onPress = {
                            pressed = true
                            tryAwaitRelease()
                            pressed = false
                            showLongPressPopup = false
                            popupHoveredIndex = -1
                        },
                        onTap = {
                            action()
                        },
                        onLongPress = { pos ->
                            pressed = true
                            if (isDeleteKey && onDeleteRepeat != null) {
                                var keepRepeating = true
                                scope.launch {
                                    while (keepRepeating && isActive) {
                                        onDeleteRepeat()
                                        delay(50L)
                                    }
                                }
                                do {
                                    val e = awaitPointerEvent()
                                    val anyPressed = e.changes.any { it.pressed }
                                } while (anyPressed)
                                keepRepeating = false
                                pressed = false
                            } else if (onLongPressAction != null) {
                                onLongPressAction()
                                pressed = false
                            } else if (longPressAlts != null && onLongPressAlt != null) {
                                showLongPressPopup = true
                                popupHoveredIndex = -1
                                val slotWidthPx = 30.dp.toPx() + 2.dp.toPx()
                                val altCount = longPressAlts.size
                                do {
                                    val e = awaitPointerEvent()
                                    val ptr = e.changes.firstOrNull()
                                    if (ptr != null && ptr.pressed) {
                                        ptr.consume()
                                        val relX = ptr.position.x - (size.width / 2f - altCount * slotWidthPx / 2f)
                                        popupHoveredIndex = ((relX / slotWidthPx).toInt()).coerceIn(0, altCount - 1)
                                    } else {
                                        break
                                    }
                                } while (true)
                                if (popupHoveredIndex >= 0 && popupHoveredIndex < altCount) {
                                    onLongPressAlt(longPressAlts[popupHoveredIndex])
                                }
                                pressed = false
                                showLongPressPopup = false
                                popupHoveredIndex = -1
                            } else {
                                pressed = false
                            }
                        },
                    )
                },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )

        // Added by Agent B — long-press alternate characters popup
        if (showLongPressPopup && longPressAlts != null && longPressAlts.isNotEmpty()) {
            Box(
                modifier =
                    Modifier
                        .offset(y = -(height + 8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .align(Alignment.TopCenter),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    longPressAlts.forEachIndexed { index, alt ->
                        val isHovered = index == popupHoveredIndex
                        Box(
                            modifier =
                                Modifier
                                    .size(width = 30.dp, height = 30.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isHovered) colors.tertiary else colors.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = alt.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isHovered) colors.onTertiary else colors.onPrimaryContainer,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Added by Agent A — New composables and helpers
// ════════════════════════════════════════════════════════════════

@Composable
fun OneHandModeBar(
    currentMode: OneHandMode,
    onSetMode: (OneHandMode) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(colors.surfaceVariant),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { onSetMode(OneHandMode.LEFT) },
            modifier = Modifier.width(44.dp).height(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "左手模式",
                tint = if (currentMode == OneHandMode.LEFT) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.height(20.dp),
            )
        }
        IconButton(
            onClick = { onSetMode(OneHandMode.OFF) },
            modifier = Modifier.width(44.dp).height(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = "全尺寸",
                tint = if (currentMode == OneHandMode.OFF) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.height(20.dp),
            )
        }
        IconButton(
            onClick = { onSetMode(OneHandMode.RIGHT) },
            modifier = Modifier.width(44.dp).height(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "右手模式",
                tint = if (currentMode == OneHandMode.RIGHT) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.height(20.dp),
            )
        }
    }
}

@Composable
fun HeightSlider(
    currentPercent: Float,
    onHeightChange: (Float) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var dragFraction by remember { mutableFloatStateOf(currentPercent) }
    val trackWidth = 180.dp

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(colors.surface),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .width(trackWidth)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.surfaceVariant)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            val widthPx = size.width.toFloat()
                            val delta = -(dragAmount / widthPx) * 0.3f
                            dragFraction = (dragFraction + delta).coerceIn(0.3f, 0.6f)
                            onHeightChange(dragFraction)
                        }
                    },
            contentAlignment = Alignment.CenterStart,
        ) {
            val filledWidth = ((dragFraction - 0.3f) / 0.3f) * trackWidth
            Box(
                modifier =
                    Modifier
                        .width(filledWidth)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.primary),
            )
        }
    }
}

@Composable
fun T9Keyboard(
    state: KeyboardState,
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onSetMode: (KeyboardMode) -> Unit,
    oneHandMode: OneHandMode,
) {
    val keyHeight = 48.dp
    val keyWidth = 60.dp
    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    val t9Digits =
        listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf('*', '0', '#'),
        )

    val t9Labels =
        mapOf(
            '1' to "1",
            '2' to "2\nabc",
            '3' to "3\ndef",
            '4' to "4\nghi",
            '5' to "5\njkl",
            '6' to "6\nmno",
            '7' to "7\npqrs",
            '8' to "8\ntuv",
            '9' to "9\nwxyz",
            '0' to "0\n,.?!",
            '*' to "*",
            '#' to "#",
        )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .then(handModifier),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(4) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                t9Digits[rowIndex].forEach { digit ->
                    val label = t9Labels[digit] ?: digit.toString()
                    val digitAction: () -> Unit =
                        when (digit) {
                            in '2'..'9', '0' -> {
                                { onKey(digit) }
                            }
                            else -> {}
                        }
                    KeyButton(
                        label = label,
                        width = keyWidth * scale,
                        height = keyHeight * scale,
                        action = digitAction,
                        sizeScale = scale,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyButton(
                "全键",
                width = 48.dp * scale,
                height = 36.dp * scale,
                action = { onSetMode(KeyboardMode.ALPHA) },
                sizeScale = scale,
            )
            KeyButton(
                "符",
                width = 40.dp * scale,
                height = 36.dp * scale,
                action = { onSetMode(KeyboardMode.SYMBOL) },
                sizeScale = scale,
            )
            KeyButton(
                "空格",
                modifier = Modifier.weight(1f),
                height = 36.dp * scale,
                action = { onSpace() },
                sizeScale = scale,
            )
            KeyButton(
                "↵",
                width = 48.dp * scale,
                height = 36.dp * scale,
                action = { onEnter() },
                sizeScale = scale,
            )
        }
    }
}

private fun oneHandScale(mode: OneHandMode): Float =
    when (mode) {
        OneHandMode.LEFT, OneHandMode.RIGHT -> 0.85f
        else -> 1f
    }

private fun oneHandPaddingModifier(mode: OneHandMode): Modifier =
    when (mode) {
        OneHandMode.LEFT -> Modifier.padding(end = 80.dp)
        OneHandMode.RIGHT -> Modifier.padding(start = 80.dp)
        else -> Modifier
    }

// Added by Agent G — clipboard quick paste popup on keyboard bottom row
@Composable
fun ClipboardQuickButton(
    onPaste: (String) -> Unit,
    scale: Float = 1f
) {
    var expanded by remember { mutableStateOf(false) }
    var currentClip by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(expanded) {
        if (expanded) {
            currentClip = try {
                val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                    as? android.content.ClipboardManager
                cm?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString() ?: ""
            } catch (e: Exception) { "" }
        }
    }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size((36.dp * scale))
        ) {
            Icon(
                Icons.Filled.ContentPaste,
                contentDescription = "剪贴板快速粘贴",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size((20.dp * scale))
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (currentClip.isBlank()) {
                DropdownMenuItem(
                    text = { Text("暂无剪贴板内容", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = { expanded = false }
                )
            } else {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = currentClip.take(30).replace("\n", " "),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onPaste(currentClip)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}
