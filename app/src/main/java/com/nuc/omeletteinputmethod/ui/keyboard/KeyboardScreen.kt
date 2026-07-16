package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
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
import com.nuc.omeletteinputmethod.ui.theme.ThemeManager
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

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(keyColors.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Added by Agent H — top toolbar (replaces OneHandModeBar)
        ToolBar(
            modifier = Modifier.fillMaxWidth().height(36.dp),
            onKeyboardSwitch = { viewModel.onSwitchIME() },
            onAiAction = { /* placeholder — AI feature WIP */ },
            onSettings = { viewModel.onOpenSettings() },
            onTextEdit = { /* placeholder — text selection mode */ },
            onClipboard = { /* handled by ClipboardQuickButton in bottom row */ },
            onHideKeyboard = { viewModel.onHideKeyboard() },
        )

        CandidateBar(
            candidates = state.candidates,
            inputBuffer = state.inputBuffer,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(60.dp),
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
    inputBuffer: String = "",
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier.background(colors.surfaceVariant),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (candidates.isEmpty()) {
            // 空状态：显示用户输入的原始拼音作为灰色提示
            Text(
                text = inputBuffer.ifEmpty { "" },
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.5f),
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                // 拼音显示行
                Text(
                    text = inputBuffer,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 1.dp),
                )
                // 候选词行
                LazyRow(
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

    val keyHeight = 44.dp
    val spacerHeight = 4.dp

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
        // Half a key width for row 2 indentation: fullWidth / 10keys / 2
        val halfKeyWidth = maxWidth / 10 / 2

        Column(
            verticalArrangement = Arrangement.spacedBy(spacerHeight * scale),
        ) {
            // Row 0: Q W E R T Y U I O P — 10 keys, equal width via weight(1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp * scale),
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
                    )
                }
            }

            // Row 1: A S D F G H J K L — 9 keys, equal width, indented left+right by halfKeyWidth
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = halfKeyWidth),
                horizontalArrangement = Arrangement.spacedBy(2.dp * scale),
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
                    )
                }
            }

            // Row 2: ⇧ Z X C V B N M ⌫ — shift 1.2x, 7 letter keys 1x, delete 1.2x
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp * scale),
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

            // Bottom row: 符  中/En  ! ,  ─── 空格(语音) ───  ? 。  🔵搜索
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp * scale),
                horizontalArrangement = Arrangement.spacedBy(3.dp * scale),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 1. "符" key — tap → symbol mode, long-press → mode menu
                Box {
                    KeyButton(
                        label = "符",
                        width = 42.dp * scale,
                        height = 36.dp * scale,
                        action = { onSetMode(KeyboardMode.SYMBOL) },
                        keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                        onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                        onLongPressAction = { showModeMenu = true },
                        sizeScale = scale,
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

                // 2. 中/En — language toggle
                KeyButton(
                    label = if (state.isEnglishMode) "En" else "中",
                    width = 42.dp * scale,
                    height = 36.dp * scale,
                    action = { onToggleLanguageMode() },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                    customBgColor = if (state.isEnglishMode)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        null,
                )

                // 3. ! , — tap comma, long-press exclamation
                KeyButton(
                    label = "! ,",
                    width = 32.dp * scale,
                    height = 36.dp * scale,
                    action = { onKey(',') },
                    keyChar = ',', keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = { alt -> onLongPressAlt(',', alt) },
                    onDeleteRepeat = null,
                    longPressAlts = listOf('!'),
                    sizeScale = scale,
                )

                // 4. Space bar — tap = space/candidate, long-press = voice input (press-and-hold to talk)
                KeyButton(
                    label = if (voiceRecording) "🎤 录音中…" else "🎤 空格",
                    modifier = Modifier.weight(1f),
                    height = 36.dp * scale,
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
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    else
                        null,
                )

                // 5. ? . — tap period, long-press question mark
                KeyButton(
                    label = "? 。",
                    width = 32.dp * scale,
                    height = 36.dp * scale,
                    action = { onKey('.') },
                    keyChar = '.', keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = { alt -> onLongPressAlt('.', alt) },
                    onDeleteRepeat = null,
                    longPressAlts = listOf('?'),
                    sizeScale = scale,
                )

                // 6. 🔵 Search — blue action button
                KeyButton(
                    label = "搜索",
                    width = 52.dp * scale,
                    height = 36.dp * scale,
                    action = { onSearch() },
                    keyChar = null, keyPositionMap = keyPositionMap, highlighted = false,
                    onLongPressAlt = null, onDeleteRepeat = null, longPressAlts = null,
                    sizeScale = scale,
                    customBgColor = MaterialTheme.colorScheme.primary,
                    customContentColor = MaterialTheme.colorScheme.onPrimary,
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
    val keyHeight = 44.dp

    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(handModifier)
                .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp * scale),
    ) {
        // Row 0: 1 2 3 4 5 6 7 8 9 0 — weight(1f) equal width
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp * scale),
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
            horizontalArrangement = Arrangement.spacedBy(2.dp * scale),
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
            horizontalArrangement = Arrangement.spacedBy(2.dp * scale),
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
            horizontalArrangement = Arrangement.spacedBy(4.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyButton(
                "ABC", width = 48.dp * scale, height = 36.dp * scale,
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
            KeyButton(
                "↵", width = 48.dp * scale, height = 36.dp * scale,
                action = { onEnter() },
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
    val keyHeight = 44.dp

    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(handModifier)
                .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp * scale),
    ) {
        // Rows 0-2: pure 3×3 digit grid — all weight(1f) equal width
        repeat(3) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp * scale),
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
            horizontalArrangement = Arrangement.spacedBy(4.dp * scale),
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
            horizontalArrangement = Arrangement.spacedBy(4.dp * scale),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyButton(
                "ABC", width = 48.dp * scale, height = 36.dp * scale,
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
 * KeyButton — supports tap, long-press (alt chars / action), swipe-up, delete repeat,
 * key position registration, and press animation.
 *
 * Added by Agent H: swipe-up gesture, press scale animation, floating symbol overlay.
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
) {
    var pressed by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    // Added by Agent B — long-press popup state
    var showLongPressPopup by remember { mutableStateOf(false) }
    var popupHoveredIndex by remember { mutableStateOf(-1) }
    val isDeleteKey = label == "⌫"

    // Added by Agent H — press scale animation
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "pressScale",
    )

    val bgColor =
        when {
            showLongPressPopup -> colors.tertiaryContainer
            highlighted -> colors.primaryContainer
            pressed -> colors.tertiaryContainer
            else -> customBgColor ?: colors.surfaceVariant
        }
    val contentColor = customContentColor ?: colors.onSurfaceVariant
    val shape = RoundedCornerShape(6.dp * sizeScale)

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
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                    translationY = if (pressed) 2.dp.toPx() else 0f
                }
                .clip(shape)
                .background(bgColor)
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
                                            pressed = false
                                        }
                                    } else if (onLongPressAction != null) {
                                        onLongPressAction()
                                        pressed = false
                                    } else if (longPressAlts != null && onLongPressAlt != null) {
                                        showLongPressPopup = true
                                        popupHoveredIndex = -1
                                    } else {
                                        pressed = false
                                    }
                                    // Don't break — continue tracking for release
                                }

                                change.consume()
                            }

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
        // Main label
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
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
                color = contentColor.copy(alpha = 0.45f),
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

// Added by Agent H — top toolbar with circular icon buttons
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
    val colors = MaterialTheme.colorScheme
    val view = LocalView.current

    Row(
        modifier =
            modifier
                .background(colors.surface)
                .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 1. Keyboard switch → system IME switcher
        IconButton(
            onClick = onKeyboardSwitch,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Keyboard,
                contentDescription = "切换键盘",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }

        // 2. AI magic tool
        IconButton(
            onClick = {
                Toast.makeText(view.context, "AI功能开发中，敬请期待 ✨", Toast.LENGTH_SHORT).show()
                onAiAction()
            },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "AI 辅助",
                tint = colors.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp),
            )
        }

        // 3. Settings
        IconButton(
            onClick = onSettings,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "设置",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }

        // 4. Text edit / selection mode
        IconButton(
            onClick = {
                Toast.makeText(view.context, "文本编辑模式开发中", Toast.LENGTH_SHORT).show()
                onTextEdit()
            },
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.SelectAll,
                contentDescription = "文本编辑",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }

        // 5. Clipboard quick access
        IconButton(
            onClick = onClipboard,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.ContentPaste,
                contentDescription = "剪贴板",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }

        // 6. Hide keyboard — emphasized with primary color
        IconButton(
            onClick = onHideKeyboard,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "收起键盘",
                tint = colors.primary,
                modifier = Modifier.size(20.dp),
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
            val filledWidth = ((dragFraction - 0.3f) / 0.3f) * trackWidth.value
            val filledWidthDp = Dp(filledWidth)
            Box(
                modifier =
                    Modifier
                        .width(filledWidthDp)
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
            horizontalArrangement = Arrangement.spacedBy(4.dp * scale),
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
