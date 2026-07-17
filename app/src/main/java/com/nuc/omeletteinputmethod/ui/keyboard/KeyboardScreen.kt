package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.nuc.omeletteinputmethod.ui.multimodal.HandwritingPanel
import com.nuc.omeletteinputmethod.ui.multimodal.HandwritingViewModel
import com.nuc.omeletteinputmethod.ui.multimodal.StrokeInputPanel
import com.nuc.omeletteinputmethod.ui.multimodal.StrokeInputViewModel
import com.nuc.omeletteinputmethod.ui.multimodal.VoiceInputPanel
import com.nuc.omeletteinputmethod.ui.multimodal.VoiceInputViewModel
import com.nuc.omeletteinputmethod.ui.theme.KeyboardColors
import com.nuc.omeletteinputmethod.ui.theme.ThemeManager
import com.nuc.omeletteinputmethod.ui.clipboard.ClipboardPanel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun KeyboardScreen(
    viewModel: KeyboardViewModel,
    handwritingViewModel: HandwritingViewModel,
    voiceViewModel: VoiceInputViewModel,
    strokeViewModel: StrokeInputViewModel,
    themeManager: ThemeManager,
) {
    val state by viewModel.state.collectAsState()
    val keyColors = MaterialTheme.colorScheme

    // Added by Agent B — global key position map for swipe path calculation
    val keyPositionMap = remember { mutableStateMapOf<Char, Rect>() }

    // Added by Agent B — swipe trajectory state
    var swipeActive by remember { mutableStateOf(false) }
    val swipePath = remember { mutableStateListOf<Offset>() }
    val traversedKeys = remember { mutableStateListOf<Char>() }
    val highlightedKeys = remember { mutableStateListOf<Char>() }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(KeyboardColors.Background),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        // ── 顶栏 ──
        ToolBar(
            modifier = Modifier.fillMaxWidth(),
            onKeyboardSwitch = { viewModel.showSwitchPanel() },
            onAiAction = { /* placeholder — AI feature WIP */ },
            onSettings = { viewModel.onOpenSettings() },
            onTextEdit = { viewModel.setEditToolMode() },
            onClipboard = { viewModel.setClipboardMode() },
            onHideKeyboard = { viewModel.onHideKeyboard() },
        )

        if (!state.isSecureMode) {
            CandidateBar(
                candidates = state.candidates,
                candidatesMeta = state.candidatesMeta,
                inputBuffer = state.inputBuffer,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                onSelect = { viewModel.onCandidateSelected(it) },
                onTogglePin = { viewModel.togglePinCandidate(it) },
                onDelete = { viewModel.deleteCandidate(it) },
                onExpand = { viewModel.setExpandedCandidatesGrid(true) },
            )
        }

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
                    // Added by Agent H — new bottom-row callbacks
                    onToggleLanguageMode = viewModel::toggleLanguageMode,
                    onSearch = viewModel::onSearch,
                    onSwipeUp = viewModel::onSwipeUp,
                    onLongPressActionVoice = {
                        // Step 6: space bar long-press → voice input
                        if (voiceViewModel.state.value.hasPermission || voiceViewModel.checkPermission()) {
                            voiceViewModel.startListening()
                        }
                    },
                    onPressReleaseVoice = {
                        // Step 6: finger release → stop listening and commit recognized text
                        voiceViewModel.stopListening()
                        val recognized = voiceViewModel.state.value.recognizedText
                        if (recognized.isNotEmpty()) {
                            voiceViewModel.commitText()
                        }
                    },
                )

            // Added by Agent F — emoji panel mode
            KeyboardMode.EMOJI ->
                EmojiPanel(
                    themeManager = themeManager,
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

            // Text editing arrows panel
            KeyboardMode.ARROWS ->
                ArrowsPanel(
                    viewModel = viewModel,
                    onBackToAlpha = { viewModel.setMode(KeyboardMode.ALPHA) },
                )

            // Clipboard history panel
            KeyboardMode.CLIPBOARD ->
                ClipboardPanel(
                    onNavigateBack = { viewModel.setMode(KeyboardMode.ALPHA) },
                )

            // 文本编辑工具面板（摇杆 + 操作网格）
            KeyboardMode.EDIT_TOOL ->
                EditToolPanel(
                    viewModel = viewModel,
                    onBackToAlpha = { viewModel.setMode(KeyboardMode.ALPHA) },
                )

            // 五笔输入模式
            KeyboardMode.WUBI ->
                WubiKeyboard(
                    state = state,
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
                    onToggleLanguageMode = viewModel::toggleLanguageMode,
                    onSearch = viewModel::onSearch,
                    onSwipeUp = viewModel::onSwipeUp,
                    onLongPressActionVoice = {
                        if (voiceViewModel.state.value.hasPermission || voiceViewModel.checkPermission()) {
                            voiceViewModel.startListening()
                        }
                    },
                    onPressReleaseVoice = {
                        voiceViewModel.stopListening()
                        val recognized = voiceViewModel.state.value.recognizedText
                        if (recognized.isNotEmpty()) {
                            voiceViewModel.commitText()
                        }
                    },
                )
        }

        // Added by Agent A — keyboard height drag slider
        HeightSlider(
            currentPercent = state.keyboardHeightPercent,
            onHeightChange = viewModel::setKeyboardHeight,
        )
        }  // end Column

        // ── 键盘切换面板（浮层覆盖在键盘上方）──
        KeyboardSwitchPanel(
            visible = state.switchPanelVisible,
            onDismiss = { viewModel.hideSwitchPanel() },
            onKeyboardSelect = { typeId ->
                viewModel.switchKeyboardType(typeId)
                viewModel.hideSwitchPanel()
            },
            preferenceManager = viewModel.preferenceManager,
            onPersonalCenter = {
                viewModel.hideSwitchPanel()
                viewModel.onOpenSettings()
            },
            onThemeStore = {
                viewModel.hideSwitchPanel()
                viewModel.onOpenSettings()
            },
            onAllApps = {
                viewModel.hideSwitchPanel()
                viewModel.onOpenSettings()
            },
        )

        if (state.expandedCandidatesGrid) {
            ExpandedCandidatesGrid(
                candidatesMeta = state.candidatesMeta,
                onSelect = { viewModel.onCandidateSelected(it) },
                onDismiss = { viewModel.setExpandedCandidatesGrid(false) }
            )
        }
    }  // end Box
}

// ── 赛博：候选栏 — 毛玻璃深色底 ──
@Composable
fun CandidateBar(
    candidates: List<String>,
    candidatesMeta: List<CandidateItem>,
    inputBuffer: String = "",
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onDelete: (String) -> Unit,
    onExpand: () -> Unit,
) {

    Box(
        modifier = modifier.background(KeyboardColors.CandidateBackground),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (candidates.isEmpty()) {
            Text(
                text = inputBuffer.ifEmpty { "" },
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = KeyboardColors.TextSecondary.copy(alpha = 0.5f),
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = inputBuffer,
                    style = MaterialTheme.typography.labelSmall,
                    color = KeyboardColors.TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val displayCandidates = candidatesMeta.take(5)
                    itemsIndexed(displayCandidates) { index, item ->
                        val isFirst = index == 0
                        var offsetX by remember { mutableFloatStateOf(0f) }
                        var showContextMenu by remember { mutableStateOf(false) }

                        Box {
                            Box(
                                modifier = Modifier
                                    .offset(x = offsetX.dp)
                                    .pointerInput(item.word) {
                                        detectHorizontalDragGestures(
                                            onDragEnd = {
                                                if (offsetX < -50) {
                                                    onDelete(item.word)
                                                }
                                                offsetX = 0f
                                            },
                                            onHorizontalDrag = { change, dragAmount ->
                                                change.consume()
                                                if (dragAmount < 0 && offsetX > -150) {
                                                    offsetX += dragAmount / density
                                                }
                                            }
                                        )
                                    }
                                    .pointerInput(item.word) {
                                        detectTapGestures(
                                            onTap = { onSelect(item.word) },
                                            onLongPress = { showContextMenu = true }
                                        )
                                    }
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isFirst) KeyboardColors.CyberBlue.copy(alpha = 0.2f) else KeyboardColors.DarkGray800,
                                    shadowElevation = if (isFirst) 1.dp else 0.dp,
                                ) {
                                    Text(
                                        text = item.word,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        color = if (isFirst) KeyboardColors.CyberBlue else KeyboardColors.TextPrimary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            if (offsetX < -10) {
                                Row(
                                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(onClick = { onTogglePin(item.word) }, modifier = Modifier.size(20.dp)) {
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = "Pin",
                                            tint = if (item.isPinned) KeyboardColors.CyberBlue else KeyboardColors.TextSecondary
                                        )
                                    }
                                    IconButton(onClick = { onDelete(item.word) }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Filled.Close, contentDescription = "Delete", tint = KeyboardColors.Error)
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = showContextMenu,
                                onDismissRequest = { showContextMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (item.isPinned) "取消置顶" else "置顶") },
                                    onClick = {
                                        onTogglePin(item.word)
                                        showContextMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("删除") },
                                    onClick = {
                                        onDelete(item.word)
                                        showContextMenu = false
                                    }
                                )
                            }
                        }
                    }

                    if (candidatesMeta.size > 5) {
                        item {
                            Surface(
                                onClick = onExpand,
                                shape = RoundedCornerShape(12.dp),
                                color = KeyboardColors.DarkGray800,
                            ) {
                                Text(
                                    text = "☁️更多",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = KeyboardColors.TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandedCandidatesGrid(
    candidatesMeta: List<CandidateItem>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(KeyboardColors.Background.copy(alpha = 0.95f))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("更多候选词", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "收起")
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(candidatesMeta.size) { index ->
                    Surface(
                        onClick = { onSelect(candidatesMeta[index].word) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (candidatesMeta[index].isPinned) KeyboardColors.CandidateSelected else KeyboardColors.KeyBackgroundPressed,
                    ) {
                        Text(
                            text = candidatesMeta[index].word,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
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
    oneHandMode: OneHandMode,
    keyPositionMap: MutableMap<Char, Rect>,
    onLongPressAlt: (baseChar: Char, selectedAlt: Char) -> Unit,
    onDeleteRepeat: () -> Unit,
    swipeActive: Boolean,
    onSwipeActiveChange: (Boolean) -> Unit,
    swipePath: MutableList<Offset>,
    traversedKeys: MutableList<Char>,
    highlightedKeys: MutableList<Char>,
    onSwipeEnd: () -> Unit,
    onClipboardPasteText: (String) -> Unit = {},
    // Added by Agent H — new bottom-row actions
    onToggleLanguageMode: () -> Unit = {},
    onSearch: () -> Unit = {},
    onSwipeUp: (Char) -> Unit = {},
    // voice input trigger from space bar long-press
    onLongPressActionVoice: (() -> Unit)? = null,
    // voice input stop+commit on finger release
    onPressReleaseVoice: (() -> Unit)? = null,
) {
    val keys =
        remember(state.isShifted) {
            val alpha = if (state.isShifted) "QWERTYUIOPASDFGHJKLZXCVBNM" else "qwertyuiopasdfghjklzxcvbnm"
            listOf(
                alpha.substring(0, 10).toList(),  // row 0: 10 keys
                alpha.substring(10, 19).toList(), // row 1: 9 keys
                alpha.substring(19, 26).toList(), // row 2: 7 keys
            )
        }

    // ── UI 重构：按键高度 48dp，行间距 5dp ──
    val keyHeight = 48.dp
    val spacerHeight = 5.dp

    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    val swipeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    // Popup menu state for "符" key long-press
    var showModeMenu by remember { mutableStateOf(false) }

    // Added by Agent H — voice recording state for space bar press-and-hold
    var voiceRecording by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(handModifier)
                .padding(horizontal = 5.dp)
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
        // Half a key width for row 2 indentation: fullWidth / 10keys / 2
        val halfKeyWidth = maxWidth / 10 / 2

        // ── UI 重构：行间距 5dp，键缝 5dp ──
        Column(
            verticalArrangement = Arrangement.spacedBy(spacerHeight * scale),
        ) {
            // Row 0: Q W E R T Y U I O P — 10 keys, equal width via weight(1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
            ) {
                keys[0].forEach { char ->
                    val isHighlighted = highlightedKeys.contains(char)
                    val swipeSym = SWIPE_UP_SYMBOLS[char]
                    KeyButton(
                        label = char.toString(),
                        modifier = Modifier.weight(1f),
                        height = keyHeight * scale,
                        action = { onKey(char) },
                        keyChar = char,
                        keyPositionMap = keyPositionMap,
                        highlighted = isHighlighted,
                        onLongPressAlt = { alt -> onLongPressAlt(char, alt) },
                        onDeleteRepeat = null,
                        longPressAlts = LONG_PRESS_ALTERNATES[char],
                        sizeScale = scale,
                        swipeUpSymbol = swipeSym,
                        onSwipeUp = { onSwipeUp(char) },
                        isAlphaKey = true,
                    )
                }
            }

            // Row 1: A S D F G H J K L — 9 keys, equal width, indented left+right by halfKeyWidth
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = halfKeyWidth),
                horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
            ) {
                keys[1].forEach { char ->
                    val isHighlighted = highlightedKeys.contains(char)
                    val swipeSym = SWIPE_UP_SYMBOLS[char]
                    KeyButton(
                        label = char.toString(),
                        modifier = Modifier.weight(1f),
                        height = keyHeight * scale,
                        action = { onKey(char) },
                        keyChar = char,
                        keyPositionMap = keyPositionMap,
                        highlighted = isHighlighted,
                        onLongPressAlt = { alt -> onLongPressAlt(char, alt) },
                        onDeleteRepeat = null,
                        longPressAlts = LONG_PRESS_ALTERNATES[char],
                        sizeScale = scale,
                        swipeUpSymbol = swipeSym,
                        onSwipeUp = { onSwipeUp(char) },
                        isAlphaKey = true,
                    )
                }
            }

            // Row 2: ⇧ Z X C V B N M ⌫ — shift 1.2x, 7 letter keys 1x, delete 1.2x
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
            ) {
                KeyButton(
                    label = "⇧",
                    modifier = Modifier.weight(1.2f),
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
                keys[2].forEach { char ->
                    val isHighlighted = highlightedKeys.contains(char)
                    val swipeSym = SWIPE_UP_SYMBOLS[char]
                    KeyButton(
                        label = char.toString(),
                        modifier = Modifier.weight(1f),
                        height = keyHeight * scale,
                        action = { onKey(char) },
                        keyChar = char,
                        keyPositionMap = keyPositionMap,
                        highlighted = isHighlighted,
                        onLongPressAlt = { alt -> onLongPressAlt(char, alt) },
                        onDeleteRepeat = null,
                        longPressAlts = LONG_PRESS_ALTERNATES[char],
                        sizeScale = scale,
                        swipeUpSymbol = swipeSym,
                        onSwipeUp = { onSwipeUp(char) },
                        isAlphaKey = true,
                    )
                }
                KeyButton(
                    label = "⌫",
                    modifier = Modifier.weight(1.2f),
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

            // ── UI 重构：底部功能行 — 胶囊形按键 + 薄荷绿搜索 ──
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp * scale),
                horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 1. "符" — 胶囊形，tap → 符号模式，long-press → 模式菜单
                Box {
                    KeyButton(
                        label = "符",
                        width = 44.dp * scale,
                        height = 40.dp * scale,
                        action = { onSetMode(KeyboardMode.SYMBOL) },
                        keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                        onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                        onLongPressAction = { showModeMenu = true },
                        sizeScale = scale,
                        capsuleShape = true,
                    )
                    DropdownMenu(
                        expanded = showModeMenu,
                        onDismissRequest = { showModeMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("😄 Emoji") },
                            onClick = { showModeMenu = false; onSetMode(KeyboardMode.EMOJI) },
                        )
                        DropdownMenuItem(
                            text = { Text("123 数字") },
                            onClick = { showModeMenu = false; onSetMode(KeyboardMode.NUMBER) },
                        )
                        DropdownMenuItem(
                            text = { Text("✍ 手写") },
                            onClick = { showModeMenu = false; onSetMode(KeyboardMode.HANDWRITING) },
                        )
                        DropdownMenuItem(
                            text = { Text("🎤 语音") },
                            onClick = { showModeMenu = false; onSetMode(KeyboardMode.VOICE) },
                        )
                        DropdownMenuItem(
                            text = { Text("笔画") },
                            onClick = { showModeMenu = false; onSetMode(KeyboardMode.STROKE) },
                        )
                        DropdownMenuItem(
                            text = { Text("九宫格") },
                            onClick = { showModeMenu = false; onSetMode(KeyboardMode.T9) },
                        )
                    }
                }

                // 2. 中/En — 胶囊形语言切换
                KeyButton(
                    label = if (state.isEnglishMode) "En" else "中",
                    width = 44.dp * scale,
                    height = 40.dp * scale,
                    action = { onToggleLanguageMode() },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                    capsuleShape = true,
                    customBgColor = if (state.isEnglishMode)
                        KeyboardColors.PrimaryAction.copy(alpha = 0.15f)
                    else
                        null,
                )

                // 3. ! , — 胶囊形标点
                KeyButton(
                    label = "!,",
                    width = 40.dp * scale,
                    height = 40.dp * scale,
                    action = { onKey(',') },
                    keyChar = ',', keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = { alt -> onLongPressAlt(',', alt) },
                    onDeleteRepeat = null,
                    longPressAlts = listOf('!'),
                    sizeScale = scale,
                    capsuleShape = true,
                )

                // 4. 空格键 — 加宽，浅灰底 + 居中横杠指示
                KeyButton(
                    label = "───",
                    modifier = Modifier.weight(1f),
                    height = 40.dp * scale,
                    action = { onSpace() },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null,
                    longPressAlts = null,
                    onLongPressAction = {
                        voiceRecording = true
                        onLongPressActionVoice?.invoke()
                    },
                    onPressRelease = {
                        if (voiceRecording) {
                            voiceRecording = false
                            onPressReleaseVoice?.invoke()
                        }
                    },
                    sizeScale = scale,
                    customBgColor = if (voiceRecording)
                        KeyboardColors.PrimaryAction.copy(alpha = 0.2f)
                    else
                        KeyboardColors.KeyBackgroundPressed.copy(alpha = 0.6f),
                )

                // 5. ? 。 — 胶囊形标点
                KeyButton(
                    label = "?。",
                    width = 40.dp * scale,
                    height = 40.dp * scale,
                    action = { onKey('.') },
                    keyChar = '.', keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = { alt -> onLongPressAlt('.', alt) },
                    onDeleteRepeat = null,
                    longPressAlts = listOf('?'),
                    sizeScale = scale,
                    capsuleShape = true,
                )

                // 6. 搜索 — 主色青蓝胶囊按钮
                KeyButton(
                    label = "搜索",
                    width = 56.dp * scale,
                    height = 40.dp * scale,
                    action = { onSearch() },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                    capsuleShape = true,
                    customBgColor = KeyboardColors.PrimaryAction,
                    customContentColor = KeyboardColors.PrimaryActionText,
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
    oneHandMode: OneHandMode,
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
    val keyHeight = 48.dp

    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(handModifier)
                .padding(horizontal = 5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp * scale),
    ) {
        // Row 0: 1 2 3 4 5 6 7 8 9 0 — weight(1f) equal width
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
        ) {
            symbols[0].forEach { char ->
                KeyButton(
                    label = char.toString(),
                    modifier = Modifier.weight(1f),
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

        // Row 1: @ # $ % & * - + ( ) — weight(1f) equal width
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
        ) {
            symbols[1].forEach { char ->
                KeyButton(
                    label = char.toString(),
                    modifier = Modifier.weight(1f),
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

        // Row 2: ⇧ ! " ' : ; / ? . , ⌫ — shift weight(1f), 9 symbols weight(1f), delete weight(1f)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
        ) {
            KeyButton(
                label = "⇧",
                modifier = Modifier.weight(1f),
                height = keyHeight * scale,
                action = {},
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
            )
            symbols[2].forEach { char ->
                KeyButton(
                    label = char.toString(),
                    modifier = Modifier.weight(1f),
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
            KeyButton(
                label = "⌫",
                modifier = Modifier.weight(1f),
                height = keyHeight * scale,
                action = { onDelete() },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = onDeleteRepeat, longPressAlts = null,
                sizeScale = scale,
            )
        }

        // Bottom: ABC — space(weight) — ↵
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyButton(
                "ABC", width = 48.dp * scale, height = 40.dp * scale,
                action = { onSetMode(KeyboardMode.ALPHA) },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
                capsuleShape = true,
            )
            KeyButton(
                "───", modifier = Modifier.weight(1f), height = 40.dp * scale,
                action = { onSpace() },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
                customBgColor = KeyboardColors.KeyBackgroundPressed.copy(alpha = 0.6f),
            )
            KeyButton(
                "↵", width = 48.dp * scale, height = 40.dp * scale,
                action = { onEnter() },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
                capsuleShape = true,
                customBgColor = KeyboardColors.PrimaryAction,
                customContentColor = KeyboardColors.PrimaryActionText,
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
    oneHandMode: OneHandMode,
    keyPositionMap: MutableMap<Char, Rect>,
    onLongPressAlt: (baseChar: Char, selectedAlt: Char) -> Unit,
    onDeleteRepeat: () -> Unit,
) {
    val nums =
        listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf(null, '0', null),  // null slots filled as spacers
        )
    val keyHeight = 48.dp

    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(handModifier)
                .padding(horizontal = 5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp * scale),
    ) {
        // Rows 0-2: pure 3×3 digit grid — all weight(1f) equal width
        repeat(3) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
            ) {
                nums[rowIndex].forEach { char ->
                    if (char != null) {
                        KeyButton(
                            char.toString(),
                            modifier = Modifier.weight(1f),
                            height = keyHeight * scale,
                            action = { onKey(char) },
                            keyChar = char, keyPositionMap = keyPositionMap, highlighted = false,
                            onLongPressAlt = { alt -> onLongPressAlt(char, alt) },
                            onDeleteRepeat = null,
                            longPressAlts = LONG_PRESS_ALTERNATES[char],
                            sizeScale = scale,
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Row 3: 符 — 0 — ⌫
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
        ) {
            KeyButton(
                "符", modifier = Modifier.weight(1f), height = keyHeight * scale,
                action = { onSetMode(KeyboardMode.SYMBOL) },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
            )
            KeyButton(
                "0", modifier = Modifier.weight(1f), height = keyHeight * scale,
                action = { onKey('0') },
                keyChar = '0', keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = { alt -> onLongPressAlt('0', alt) },
                onDeleteRepeat = null,
                longPressAlts = LONG_PRESS_ALTERNATES['0'],
                sizeScale = scale,
            )
            KeyButton(
                "⌫", modifier = Modifier.weight(1f), height = keyHeight * scale,
                action = { onDelete() },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = onDeleteRepeat, longPressAlts = null,
                sizeScale = scale,
            )
        }

        // Bottom: ABC — space(weight)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyButton(
                "ABC", width = 48.dp * scale, height = 40.dp * scale,
                action = { onSetMode(KeyboardMode.ALPHA) },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
                capsuleShape = true,
            )
            KeyButton(
                "───", modifier = Modifier.weight(1f), height = 40.dp * scale,
                action = { onSpace() },
                keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                sizeScale = scale,
                customBgColor = KeyboardColors.KeyBackgroundPressed.copy(alpha = 0.6f),
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

// ════════════════════════════════════════════════════════════════
// 赛博朋克 3D 凸起按键 Modifier 扩展
// 参照 doc/新设计文档/03_按键设计.md
// ════════════════════════════════════════════════════════════════

/** 按键交互状态 */
enum class KeyState { IDLE, PRESSED, LONG_PRESSED, DISABLED }

/** 获取指定 dp 值的 pixels（需要在 density 上下文中调用） */
private fun androidx.compose.ui.unit.Density.toPxOrZero(dp: Dp): Float =
    if (dp.value <= 0f) 0f else dp.toPx()

/**
 * 多层阴影模拟 3D 凸起效果
 *
 * 默认态：3 层阴影（基础黑色阴影 + 扩散阴影 + 霓虹光晕）
 * 按压态：阴影减小，模拟按键"压下去"
 */
fun Modifier.keyShadow(
    elevation: Dp = 4.dp,
    shapeRadius: Float = 16f,
    isPressed: Boolean = false,
    accentColor: Color = KeyboardColors.CyberBlue,
): Modifier = this.drawBehind {
    if (!isPressed) {
        val elevPx = elevation.toPx()
        // 第一层：基础阴影
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.30f),
            topLeft = Offset(0f, elevPx),
            size = size.copy(width = size.width, height = size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(shapeRadius, shapeRadius),
        )
        // 第二层：扩散阴影
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.15f),
            topLeft = Offset(-2.dp.toPx(), elevPx + 2.dp.toPx()),
            size = size.copy(width = size.width + 4.dp.toPx(), height = size.height + 4.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(shapeRadius + 2.dp.toPx(), shapeRadius + 2.dp.toPx()),
        )
        // 第三层：霓虹光晕（可选）
        drawRoundRect(
            color = accentColor.copy(alpha = 0.10f),
            topLeft = Offset(-4.dp.toPx(), elevPx + 4.dp.toPx()),
            size = size.copy(width = size.width + 8.dp.toPx(), height = size.height + 8.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(shapeRadius + 4.dp.toPx(), shapeRadius + 4.dp.toPx()),
        )
    }
}

/**
 * 顶部/左侧高光条 — 模拟光源照射的凸起表面
 *
 * 按压态高光减弱
 */
fun Modifier.keyHighlight(
    alpha: Float = 0.15f,
    shapeRadius: Float = 16f,
): Modifier = this.drawWithContent {
    drawContent()
    if (alpha <= 0f) return@drawWithContent

    // 顶部高光
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha),
                Color.White.copy(alpha = 0f),
            ),
            startY = 0f,
            endY = 8.dp.toPx(),
        ),
        size = size.copy(height = 8.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(shapeRadius),
    )
    // 左侧高光
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha * 0.67f),
                Color.White.copy(alpha = 0f),
            ),
            startX = 0f,
            endX = 4.dp.toPx(),
        ),
        size = size.copy(width = 4.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(shapeRadius),
    )
}

/**
 * 霓虹渐变边框
 */
fun Modifier.neonBorder(
    color: Color = KeyboardColors.CyberBlue,
    width: Dp = 1.dp,
    alpha: Float = 0.3f,
    shapeRadius: Float = 16f,
): Modifier = this.drawBehind {
    if (alpha <= 0f) return@drawBehind
    val bw = width.toPx()
    val halfW = bw / 2f
    // 顶部边框
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(
                color.copy(alpha = alpha),
                color.copy(alpha = alpha * 0.5f),
                color.copy(alpha = alpha),
            ),
        ),
        start = Offset(shapeRadius, halfW),
        end = Offset(size.width - shapeRadius, halfW),
        strokeWidth = bw,
        cap = StrokeCap.Round,
    )
    // 底部边框
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(
                color.copy(alpha = alpha * 0.5f),
                color.copy(alpha = alpha),
                color.copy(alpha = alpha * 0.5f),
            ),
        ),
        start = Offset(shapeRadius, size.height - halfW),
        end = Offset(size.width - shapeRadius, size.height - halfW),
        strokeWidth = bw,
        cap = StrokeCap.Round,
    )
    // 左侧边框
    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(color.copy(alpha = alpha * 0.5f), color.copy(alpha = alpha), color.copy(alpha = alpha * 0.5f)),
        ),
        start = Offset(halfW, shapeRadius),
        end = Offset(halfW, size.height - shapeRadius),
        strokeWidth = bw,
        cap = StrokeCap.Round,
    )
    // 右侧边框
    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(color.copy(alpha = alpha), color.copy(alpha = alpha * 0.5f), color.copy(alpha = alpha)),
        ),
        start = Offset(size.width - halfW, shapeRadius),
        end = Offset(size.width - halfW, size.height - shapeRadius),
        strokeWidth = bw,
        cap = StrokeCap.Round,
    )
}

/**
 * KeyButton — supports tap, long-press (alt chars / action), swipe-up, delete repeat,
 * key position registration, and press animation.
 *
 * Added by Agent H: swipe-up gesture, press scale animation, floating symbol overlay.
 * Added by Agent Z: 赛博朋克 3D 凸起效果 (keyShadow + keyHighlight + neonBorder).
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
    // Added by Agent H — swipe-up gesture support
    swipeUpSymbol: Char? = null,
    onSwipeUp: ((Char) -> Unit)? = null,
    // Added by Agent H — custom background color override
    customBgColor: Color? = null,
    customContentColor: Color? = null,
    // Added by Agent H — callback when press is released (for voice input press-and-hold)
    onPressRelease: (() -> Unit)? = null,
    // ── UI 重构：新增参数 ──
    /** 是否为字母键（影响字号） */
    isAlphaKey: Boolean = false,
    /** 是否使用胶囊形（底部功能键） */
    capsuleShape: Boolean = false,
) {
    var pressed by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    // Added by Agent B — long-press popup state
    var showLongPressPopup by remember { mutableStateOf(false) }
    var popupHoveredIndex by remember { mutableStateOf(-1) }
    val isDeleteKey = label == "⌫"

    // ── 赛博朋克：0.95 倍缩放 + 100ms 弹性回弹（对齐 05_动效设计.md）──
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow,
        ),
        label = "pressScale",
    )

    // ── 赛博朋克配色方案 ──
    val stateKey = when {
        showLongPressPopup -> KeyState.LONG_PRESSED
        pressed -> KeyState.PRESSED
        else -> KeyState.IDLE
    }
    // 按键背景（按压反转渐变）＋ 霓虹边框 alpha
    val keyBg =
        if (stateKey == KeyState.PRESSED || stateKey == KeyState.LONG_PRESSED)
            KeyboardColors.KeyPressedGradient
        else
            (customBgColor?.let { Brush.verticalGradient(listOf(it, it)) } ?: KeyboardColors.KeyNormalGradient)

    val contentColor = customContentColor ?: KeyboardColors.TextPrimary
    val borderAlpha = when (stateKey) {
        KeyState.PRESSED, KeyState.LONG_PRESSED -> 0.8f
        else -> 0.3f
    }
    val highlightAlpha = when (stateKey) {
        KeyState.PRESSED -> 0.05f
        KeyState.LONG_PRESSED -> 0.1f
        else -> 0.15f
    }
    // ── 大圆角 16dp（设计规范），或胶囊形 ──
    val shape = if (capsuleShape)
        RoundedCornerShape(percent = 50)
    else
        RoundedCornerShape(16.dp)

    // Added by Agent H — swipe-up threshold in pixels (approx 12dp)
    val density = androidx.compose.ui.platform.LocalDensity.current
    val swipeUpThresholdPx = with(density) { 18.dp.toPx() }
    // Long-press popup item dimensions in pixels (captured in composable context)
    val popupItemWidthPx = with(density) { 30.dp.toPx() }
    val popupSpacingPx = with(density) { 2.dp.toPx() }
    val popupPaddingPx = with(density) { 4.dp.toPx() }

    Box(
        modifier =
            modifier
                .width(width)
                .height(height)
                // ── 赛博朋克 3D：keyShadow + keyHighlight + neonBorder 替代旧的 shadow+ripple ──
                .keyShadow(
                    elevation = if (pressed) 1.dp else 4.dp,
                    shapeRadius = density.run { 16.dp.toPx() },
                    isPressed = pressed,
                )
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                }
                .clip(shape)
                .background(brush = keyBg, shape = shape)
                .keyHighlight(alpha = highlightAlpha, shapeRadius = density.run { 16.dp.toPx() })
                .neonBorder(
                    color = KeyboardColors.CyberBlue,
                    width = 1.dp,
                    alpha = borderAlpha,
                    shapeRadius = density.run { 16.dp.toPx() },
                )
                .onGloballyPositioned { coords ->
                    if (keyChar != null && keyPositionMap != null) {
                        val rect = coords.boundsInParent()
                        keyPositionMap[keyChar] = rect
                    }
                }
                .pointerInput(keyChar, isDeleteKey, longPressAlts, onLongPressAction, swipeUpSymbol, onSwipeUp) {
                    awaitPointerEventScope {
                        while (true) {
                            val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                            val down = downEvent.changes.firstOrNull() ?: continue
                            val downPos = down.position

                            // consume down to take ownership
                            down.consume()
                            pressed = true
                            showLongPressPopup = false
                            popupHoveredIndex = -1
                            // Material 3：发射水波纹 Press 交互
                            val pressInteraction = PressInteraction.Press(downPos)
                            interactionSource.tryEmit(pressInteraction)

                            var isLongPressTriggered = false
                            var isSwipeUpTriggered = false
                            val longPressTimeoutMs = if (isDeleteKey) 150L else 350L
                            val startTime = System.currentTimeMillis()
                            var fingerX = downPos.x

                            // Wait for up, drag, or long-press timeout
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val elapsed = System.currentTimeMillis() - startTime

                                // Check if all pointers are up
                                if (event.changes.all { !it.pressed }) {
                                    event.changes.forEach { it.consume() }
                                    // Determine what happened
                                    if (showLongPressPopup && longPressAlts != null && onLongPressAlt != null && popupHoveredIndex >= 0 && popupHoveredIndex < longPressAlts.size) {
                                        // Long-press popup: select the hovered item
                                        onLongPressAlt(longPressAlts[popupHoveredIndex])
                                    } else if (!isSwipeUpTriggered && !isLongPressTriggered && elapsed < longPressTimeoutMs) {
                                        // Normal tap
                                        action()
                                    }
                                    break
                                }

                                // Check for drag (swipe up detection)
                                val change = event.changes.firstOrNull() ?: break
                                val currentPos = change.position
                                fingerX = currentPos.x
                                val dy = downPos.y - currentPos.y // positive = upward

                                if (dy > swipeUpThresholdPx && !isSwipeUpTriggered && !isLongPressTriggered) {
                                    // Swipe-up detected
                                    isSwipeUpTriggered = true
                                    if (swipeUpSymbol != null && onSwipeUp != null) {
                                        onSwipeUp(swipeUpSymbol)
                                    } else if (swipeUpSymbol != null && keyChar != null && onSwipeUp != null) {
                                        onSwipeUp(keyChar)
                                    }
                                    change.consume()
                                }

                                // Track finger position over long-press popup items
                                if (showLongPressPopup && longPressAlts != null) {
                                    val popupTotalWidth = longPressAlts.size * popupItemWidthPx + (longPressAlts.size - 1) * popupSpacingPx
                                    val keyCenterX = downPos.x // finger started at key center
                                    val popupLeft = keyCenterX - popupTotalWidth / 2 - popupPaddingPx
                                    val relX = fingerX - popupLeft
                                    val idx = ((relX - popupPaddingPx) / (popupItemWidthPx + popupSpacingPx)).toInt()
                                    popupHoveredIndex = if (idx in longPressAlts.indices) idx else -1
                                }

                                // Check long-press timeout
                                if (elapsed >= longPressTimeoutMs && !isLongPressTriggered && !isSwipeUpTriggered) {
                                    isLongPressTriggered = true
                                    if (isDeleteKey && onDeleteRepeat != null) {
                                        scope.launch {
                                            onDeleteRepeat()
                                            delay(50L)
                                            interactionSource.tryEmit(PressInteraction.Release(pressInteraction))
                                            pressed = false
                                        }
                                    } else if (onLongPressAction != null) {
                                        onLongPressAction()
                                        interactionSource.tryEmit(PressInteraction.Release(pressInteraction))
                                        pressed = false
                                    } else if (longPressAlts != null && onLongPressAlt != null) {
                                        showLongPressPopup = true
                                        popupHoveredIndex = -1
                                    } else {
                                        interactionSource.tryEmit(PressInteraction.Release(pressInteraction))
                                        pressed = false
                                    }
                                    // Don't break — continue tracking for release
                                }

                                change.consume()
                            }

                            interactionSource.tryEmit(PressInteraction.Release(pressInteraction))
                            pressed = false
                            showLongPressPopup = false
                            popupHoveredIndex = -1
                            // Added by Agent H — notify press release (for voice input hold-to-talk)
                            onPressRelease?.invoke()
                        }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        // ── UI 重构：字母键用 20sp SemiBold，功能键用 labelLarge ──
        val textStyle = if (isAlphaKey) {
            MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
            )
        } else {
            MaterialTheme.typography.labelLarge
        }

        // Main label
        Text(
            text = label,
            style = textStyle,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )

        // Added by Agent H — floating swipe-up symbol overlay at top of key
        if (swipeUpSymbol != null && !pressed) {
            Text(
                text = swipeUpSymbol.toString(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 7.sp,
                color = contentColor.copy(alpha = 0.40f),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }

        // Added by Agent B — long-press alternate characters popup
        if (showLongPressPopup && longPressAlts != null && longPressAlts.isNotEmpty()) {
            Box(
                modifier =
                    Modifier
                        .offset(y = -(height + 8.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(KeyboardColors.CyberBlue.copy(alpha = 0.25f))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .align(Alignment.TopCenter),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    longPressAlts.forEachIndexed { index, alt ->
                        val isHovered = index == popupHoveredIndex
                        Box(
                            modifier =
                                Modifier
                                    .size(width = 32.dp, height = 32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isHovered) KeyboardColors.NeonPurple.copy(alpha = 0.3f)
                                        else KeyboardColors.CyberBlue.copy(alpha = 0.15f)
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = alt.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isHovered) KeyboardColors.TextPrimary else KeyboardColors.TextSecondary,
                                fontSize = 16.sp,
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
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(KeyboardColors.DarkGray900),
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
                tint = if (currentMode == OneHandMode.LEFT) KeyboardColors.CyberBlue else KeyboardColors.TextSecondary,
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
                tint = if (currentMode == OneHandMode.OFF) KeyboardColors.CyberBlue else KeyboardColors.TextSecondary,
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
                tint = if (currentMode == OneHandMode.RIGHT) KeyboardColors.CyberBlue else KeyboardColors.TextSecondary,
                modifier = Modifier.height(20.dp),
            )
        }
    }
}

// ── Material 3 工具栏 — 48dp 高、透明背景、32dp 圆形图标底 ──
@Composable
fun ToolBar(
    modifier: Modifier = Modifier,
    onKeyboardSwitch: () -> Unit,
    onAiAction: () -> Unit,
    onSettings: () -> Unit,
    onTextEdit: () -> Unit,
    onClipboard: () -> Unit,
    onHideKeyboard: () -> Unit,
) {
    val view = LocalView.current

    Column(modifier = modifier) {
        // ── 顶栏主体 ──
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(KeyboardColors.ToolbarBackground)
                    .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 1. 键盘切换
            ToolBarIcon(
                icon = Icons.Filled.Keyboard,
                contentDescription = "切换键盘",
                onClick = onKeyboardSwitch,
            )

            // 2. AI
            ToolBarIcon(
                icon = Icons.Filled.AutoAwesome,
                contentDescription = "AI",
                onClick = onAiAction,
            )

            // 3. 设置
            ToolBarIcon(
                icon = Icons.Filled.Settings,
                contentDescription = "设置",
                onClick = onSettings,
            )

            // 4. 文本编辑
            ToolBarIcon(
                icon = Icons.Filled.SelectAll,
                contentDescription = "文本编辑",
                onClick = onTextEdit,
            )

            // 5. 剪贴板
            ToolBarIcon(
                icon = Icons.Filled.ContentPaste,
                contentDescription = "剪贴板",
                onClick = onClipboard,
            )

            // 6. 收起键盘 — 强调色
            ToolBarIcon(
                icon = Icons.Filled.KeyboardArrowDown,
                contentDescription = "收起键盘",
                onClick = onHideKeyboard,
                iconTint = KeyboardColors.CyberBlue,
            )
        }

        // ── 分隔线 ──
        HorizontalDivider(
            color = KeyboardColors.DarkGray700,
            thickness = 0.5.dp,
        )
    }
}

/**
 * 顶栏图标按钮 — 32dp 圆形深色底，霓虹按压态
 */
@Composable
private fun ToolBarIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    iconTint: Color? = null,
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier =
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (pressed) KeyboardColors.CyberBlue.copy(alpha = 0.2f)
                    else Color.Transparent
                )
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitPointerEvent(PointerEventPass.Initial)
                            if (down.changes.isNotEmpty()) {
                                pressed = true
                                down.changes.first().consume()
                            }
                            val up = awaitPointerEvent(PointerEventPass.Main)
                            pressed = false
                            if (up.changes.isNotEmpty()) {
                                up.changes.first().consume()
                                onClick()
                            }
                        }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint ?: KeyboardColors.TextSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ── UI 重构：高度滑块 — 更精致纤细 ──
@Composable
fun HeightSlider(
    currentPercent: Float,
    onHeightChange: (Float) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var dragFraction by remember { mutableFloatStateOf(currentPercent) }
    val trackWidth = 160.dp

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(KeyboardColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .width(trackWidth)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(KeyboardColors.Divider)
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
            val filledWidth = ((dragFraction - 0.3f) / 0.3f) * trackWidth.value
            val filledWidthDp = Dp(filledWidth)
            Box(
                modifier =
                    Modifier
                        .width(filledWidthDp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(KeyboardColors.PrimaryAction),
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
    val keyHeight = 52.dp
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
                .padding(horizontal = 5.dp)
                .then(handModifier),
        verticalArrangement = Arrangement.spacedBy(5.dp),
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
                            else -> ({})
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
            horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyButton(
                "全键",
                width = 48.dp * scale,
                height = 40.dp * scale,
                action = { onSetMode(KeyboardMode.ALPHA) },
                sizeScale = scale,
                capsuleShape = true,
            )
            KeyButton(
                "───",
                modifier = Modifier.weight(1f),
                height = 40.dp * scale,
                action = { onSpace() },
                sizeScale = scale,
                customBgColor = KeyboardColors.KeyBackgroundPressed.copy(alpha = 0.6f),
            )
            KeyButton(
                "↵",
                width = 48.dp * scale,
                height = 40.dp * scale,
                action = { onEnter() },
                sizeScale = scale,
                capsuleShape = true,
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

// Text editing arrows panel — cursor navigation + clipboard operations
@Composable
fun ArrowsPanel(
    viewModel: KeyboardViewModel,
    onBackToAlpha: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Back to alpha button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(onClick = onBackToAlpha, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ArrowBack, "返回键盘", tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            Text(
                "文本编辑",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface,
                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp),
            )
        }

        Spacer(Modifier.height(4.dp))

        // Arrow keys — Row 1: ← ↑ →
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArrowKey("←", colors) { viewModel.onCursorLeft() }
            ArrowKey("↑", colors) { viewModel.onCursorUp() }
            ArrowKey("→", colors) { viewModel.onCursorRight() }
        }

        Spacer(Modifier.height(4.dp))

        // Row 2: ↓ and clipboard ops
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArrowKey("↓", colors) { viewModel.onCursorDown() }
            ActionKey("复制", colors, Icons.Filled.ContentPaste) { viewModel.onCopy() }
            ActionKey("粘贴", colors, Icons.Filled.ContentPaste) { viewModel.onPaste() }
        }

        Spacer(Modifier.height(4.dp))

        // Row 3: Cut / Select All / Home / End
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionKey("剪切", colors, Icons.Filled.ContentPaste) { viewModel.onCut() }
            ActionKey("全选", colors, Icons.Filled.SelectAll) { viewModel.onSelectAll() }
            ArrowKey("⇤", colors) { viewModel.onHome() }
            ArrowKey("⇥", colors) { viewModel.onEnd() }
        }

        Spacer(Modifier.height(8.dp))

        // Switch back hint
        Text(
            "点击 ↖ 返回输入键盘",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun ArrowKey(
    label: String,
    colors: androidx.compose.material3.ColorScheme,
    action: () -> Unit,
) {
    Surface(
        onClick = action,
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceVariant,
        modifier = Modifier.size(48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionKey(
    label: String,
    colors: androidx.compose.material3.ColorScheme,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    action: () -> Unit,
) {
    Surface(
        onClick = action,
        shape = RoundedCornerShape(8.dp),
        color = colors.primaryContainer,
        modifier = Modifier.height(48.dp).width(64.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = label, tint = colors.onPrimaryContainer, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = colors.onPrimaryContainer)
        }
    }
}

// ════════════════════════════════════════════════════════════════
// 文本编辑工具面板 — 摇杆光标控制 + 操作网格
// ════════════════════════════════════════════════════════════════

/**
 * 文本编辑工具面板 — 悬浮卡片式布局
 *
 * 左侧：圆形控制盘（上下左右箭头 + 中心"选择"按钮）+ 行首/行尾胶囊按钮
 * 右侧：3×2 操作网格（全选/退格/复制/撤销/粘贴/剪贴板）
 */
@Composable
fun EditToolPanel(
    viewModel: KeyboardViewModel,
    onBackToAlpha: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── 顶部：返回按钮 + 标题 ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(onClick = onBackToAlpha, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.ArrowBack, "返回键盘", tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            Text(
                "文本编辑",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface,
                modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp),
            )
        }

        Spacer(Modifier.height(6.dp))

        // ── 主体：左右两栏布局 ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── 左侧：光标导航与控制区 ──
            CursorControlPad(
                viewModel = viewModel,
                colors = colors,
            )

            // ── 右侧：文本操作面板 ──
            ActionGrid(
                viewModel = viewModel,
                colors = colors,
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── 底部提示 ──
        Text(
            "点击 ↖ 返回输入键盘",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

/**
 * 左侧：圆形控制盘 + 行首/行尾胶囊按钮
 *
 * 中心是圆形控制盘（浅灰色背景），上下左右各有一个箭头，
 * 中心是"选择"圆底按钮。下方左右并排两个胶囊按钮（行首/行尾）。
 */
@Composable
private fun CursorControlPad(
    viewModel: KeyboardViewModel,
    colors: androidx.compose.material3.ColorScheme,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // ── 圆形控制盘 ──
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(colors.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            // 上下左右箭头 — 使用 Column + Row 组合布局
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // 上箭头
                ControlPadArrow(
                    icon = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "光标上移",
                    colors = colors,
                    onClick = { viewModel.onCursorUp() },
                )
                // 中排：左箭头 + 中心选择 + 右箭头
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    ControlPadArrow(
                        icon = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "光标左移",
                        colors = colors,
                        onClick = { viewModel.onCursorLeft() },
                    )
                    // 中心"选择"按钮
                    Surface(
                        onClick = { viewModel.onSelectAll() },
                        shape = CircleShape,
                        color = colors.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "选择",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onPrimaryContainer,
                            )
                        }
                    }
                    ControlPadArrow(
                        icon = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "光标右移",
                        colors = colors,
                        onClick = { viewModel.onCursorRight() },
                    )
                }
                // 下箭头
                ControlPadArrow(
                    icon = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "光标下移",
                    colors = colors,
                    onClick = { viewModel.onCursorDown() },
                )
            }
        }

        // ── 行首/行尾胶囊按钮 ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 行首按钮 |<
            Surface(
                onClick = { viewModel.onHome() },
                shape = RoundedCornerShape(50),
                color = colors.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.height(32.dp).width(60.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "|<",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                }
            }
            // 行尾按钮 >|
            Surface(
                onClick = { viewModel.onEnd() },
                shape = RoundedCornerShape(50),
                color = colors.surface,
                shadowElevation = 1.dp,
                modifier = Modifier.height(32.dp).width(60.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        ">|",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                }
            }
        }
    }
}

/**
 * 控制盘箭头按钮 — 40dp 透明圆形，按压反馈
 */
@Composable
private fun ControlPadArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    colors: androidx.compose.material3.ColorScheme,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
    }
}

/**
 * 右侧：3×2 操作网格
 *
 * 第一行：全选 / 退格删除
 * 第二行：复制 / 撤销重做
 * 第三行：粘贴 / 剪贴板
 */
@Composable
private fun ActionGrid(
    viewModel: KeyboardViewModel,
    colors: androidx.compose.material3.ColorScheme,
) {
    Column(
        modifier = Modifier.width(168.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // 第一行：全选 / 退格删除
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GridActionCell(
                label = "全选",
                icon = Icons.Filled.SelectAll,
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onSelectAll() },
            )
            GridActionCell(
                label = "退格",
                icon = null,
                colors = colors,
                modifier = Modifier.weight(1f),
                customIcon = "⌫",
                onClick = { viewModel.onDelete() },
            )
        }

        // 第二行：复制 / 撤销重做
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GridActionCell(
                label = "复制",
                icon = Icons.Filled.ContentCopy,
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onCopy() },
            )
            GridActionCell(
                label = "撤销",
                icon = Icons.Filled.Undo,
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onUndo() },
            )
        }

        // 第三行：粘贴 / 剪贴板
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GridActionCell(
                label = "粘贴",
                icon = Icons.Filled.ContentPaste,
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onPaste() },
            )
            GridActionCell(
                label = "剪贴板",
                icon = Icons.Filled.ContentCut,
                colors = colors,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setClipboardMode() },
            )
        }
    }
}

/**
 * 操作网格单元格 — 白色卡片，圆角 8dp-12dp，微弱阴影
 */
@Composable
private fun GridActionCell(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    colors: androidx.compose.material3.ColorScheme,
    modifier: Modifier = Modifier,
    customIcon: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = colors.surface,
        shadowElevation = 1.5.dp,
        modifier = Modifier.height(44.dp).then(modifier),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            // 图标区域
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = colors.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            } else if (customIcon != null) {
                Text(
                    customIcon,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
            }
            Spacer(Modifier.width(3.dp))
            // 文字标签
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
