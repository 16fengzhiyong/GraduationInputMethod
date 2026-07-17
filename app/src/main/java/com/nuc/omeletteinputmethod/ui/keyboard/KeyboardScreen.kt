package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.clip
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
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
    val skin by themeManager.currentSkin.collectAsState()
    val densityFloat = androidx.compose.ui.platform.LocalDensity.current.density

    // ── 键盘尺寸参数 (可从 SkinPack.layout 覆盖) ──
    val baseKeyHeightDp = skin?.layout?.keyHeight ?: 48.dp
    // 高度滑块: 0.3~0.6, 基准 0.4 → 缩放因子
    val heightScale = state.keyboardHeightPercent / 0.4f
    val keyHeightDp = baseKeyHeightDp * heightScale
    val spacingDp = skin?.layout?.keySpacing ?: 5.dp
    val rowSpacingDp = skin?.layout?.rowSpacing ?: 5.dp
    val cornerRadiusDp = skin?.layout?.cornerRadius ?: 16.dp

    // ── 从 SkinPack 派生绘制参数 ──
    val renderParams = remember(skin) {
        deriveRenderParams(
            skin = skin,
            density = densityFloat,
            keyHeightDp = keyHeightDp.value,
        )
    }

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
        // ── 顶栏：有输入内容时显示候选词框，否则显示工具栏 ──
        // 使用 AnimatedVisibility 替代 if/else 硬切换，消除闪白：
        // 当候选区和输入缓冲同时清空时，两个组件通过 fade 过渡平滑交换，
        // 避免同一帧内组件替换导致的 Column 重布局闪烁。
        // 续词联想存在时同样优先显示候选栏（联想阶段也算"有输入内容"）
        val hasInputContent =
            state.candidates.isNotEmpty() ||
                state.inputBuffer.isNotEmpty() ||
                state.associatedCandidates.isNotEmpty()
        val showCandidateBar = !state.isSecureMode && hasInputContent
        AnimatedVisibility(
            visible = showCandidateBar,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(100)),
        ) {
            CandidateBar(
                candidates = state.candidates,
                candidatesMeta = state.candidatesMeta,
                associatedCandidates = state.associatedCandidates,
                inputBuffer = state.inputBuffer,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                onSelect = { viewModel.onCandidateSelected(it) },
                onTogglePin = { viewModel.togglePinCandidate(it) },
                onDelete = { viewModel.deleteCandidate(it) },
                onExpand = { viewModel.setExpandedCandidatesGrid(true) },
            )
        }
        AnimatedVisibility(
            visible = !showCandidateBar,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(100)),
        ) {
            ToolBar(
                modifier = Modifier.fillMaxWidth(),
                onKeyboardSwitch = { viewModel.showSwitchPanel() },
                onAiAction = { /* placeholder — AI feature WIP */ },
                onSettings = { viewModel.onOpenSettings() },
                onTextEdit = { viewModel.setEditToolMode() },
                onClipboard = { viewModel.setClipboardMode() },
                onHideKeyboard = { viewModel.onHideKeyboard() },
            )
        }

        when (state.mode) {
            KeyboardMode.ALPHA -> {
                KeyboardSurfaceAlpha(
                    state = state,
                    skin = skin,
                    renderParams = renderParams,
                    keyHeightDp = keyHeightDp,
                    spacingDp = spacingDp,
                    rowSpacingDp = rowSpacingDp,
                    cornerRadiusDp = cornerRadiusDp,
                    oneHandMode = state.oneHandMode,
                    onTap = { keyDef ->
                        when {
                            keyDef.keyChar != null -> viewModel.onKeyChar(keyDef.keyChar)
                            keyDef.label == "⇧" -> viewModel.toggleShift()
                            keyDef.label == "⌫" -> viewModel.onDelete()
                            keyDef.label == "───" -> viewModel.onSpace()
                            keyDef.label == "中" || keyDef.label == "En" -> viewModel.toggleLanguageMode()
                            keyDef.label == "符号" || keyDef.label == "符" -> viewModel.setMode(KeyboardMode.SYMBOL)
                            keyDef.label == "搜索" -> viewModel.onSearch()
                            keyDef.label == "!," -> viewModel.onKeyChar(',')
                            keyDef.label == "?。" -> viewModel.onKeyChar('.')
                        }
                    },
                    onLongPressAction = { keyDef ->
                        when {
                            keyDef.label == "符" -> { /* show mode menu handled via DropdownMenu */ }
                            keyDef.label == "⇧" -> { /* shift long press — nothing special */ }
                            keyDef.label == "───" -> {
                                if (voiceViewModel.state.value.hasPermission || voiceViewModel.checkPermission()) {
                                    voiceViewModel.startListening()
                                }
                            }
                        }
                    },
                    onLongPressAlt = { baseChar, alt ->
                        viewModel.onKeyLongPress(baseChar, alt)
                    },
                    onDeleteRepeat = viewModel::onDeleteRepeat,
                    onSwipeUp = { keyDef ->
                        val sym = keyDef.swipeUpSymbol ?: keyDef.keyChar
                        if (sym != null) viewModel.onSwipeUp(sym)
                    },
                    onSwipeInput = { chars ->
                        viewModel.onSwipeInput(chars)
                    },
                    onVoiceRelease = {
                        voiceViewModel.stopListening()
                        val recognized = voiceViewModel.state.value.recognizedText
                        if (recognized.isNotEmpty()) {
                            voiceViewModel.commitText()
                        }
                    },
                    onShowModeMenu = { viewModel.showSwitchPanel() },
                )
            }

            // Added by Agent F — emoji panel mode
            KeyboardMode.EMOJI ->
                EmojiPanel(
                    themeManager = themeManager,
                    onEmojiSelected = { emoji ->
                        viewModel.onCandidateSelected(emoji)
                    },
                    onBack = { viewModel.setMode(KeyboardMode.ALPHA) },
                )

            KeyboardMode.SYMBOL -> {
                KeyboardSurfaceSymbol(
                    renderParams = renderParams,
                    keyHeightDp = keyHeightDp,
                    spacingDp = spacingDp,
                    rowSpacingDp = rowSpacingDp,
                    cornerRadiusDp = cornerRadiusDp,
                    oneHandMode = state.oneHandMode,
                    onTap = { keyDef ->
                        when {
                            keyDef.keyChar != null -> viewModel.onKeyChar(keyDef.keyChar)
                            keyDef.label == "⌫" -> viewModel.onDelete()
                            keyDef.label == "───" -> viewModel.onSpace()
                            keyDef.label == "ABC" -> viewModel.setMode(KeyboardMode.ALPHA)
                            keyDef.label == "↵" -> viewModel.onEnter()
                        }
                    },
                    onLongPressAlt = { baseChar, alt ->
                        viewModel.onKeyLongPress(baseChar, alt)
                    },
                    onDeleteRepeat = viewModel::onDeleteRepeat,
                )
            }

            KeyboardMode.NUMBER -> {
                KeyboardSurfaceNumber(
                    renderParams = renderParams,
                    keyHeightDp = keyHeightDp,
                    spacingDp = spacingDp,
                    rowSpacingDp = rowSpacingDp,
                    cornerRadiusDp = cornerRadiusDp,
                    oneHandMode = state.oneHandMode,
                    onTap = { keyDef ->
                        when {
                            keyDef.keyChar != null -> viewModel.onKeyChar(keyDef.keyChar)
                            keyDef.label == "⌫" -> viewModel.onDelete()
                            keyDef.label == "───" -> viewModel.onSpace()
                            keyDef.label == "ABC" -> viewModel.setMode(KeyboardMode.ALPHA)
                            keyDef.label == "符" -> viewModel.setMode(KeyboardMode.SYMBOL)
                        }
                    },
                    onLongPressAlt = { baseChar, alt ->
                        viewModel.onKeyLongPress(baseChar, alt)
                    },
                    onDeleteRepeat = viewModel::onDeleteRepeat,
                )
            }

            // Added by Agent A — T9 nine-grid keyboard (unified via KeyboardSurface)
            KeyboardMode.T9 ->
                KeyboardSurfaceT9(
                    state = state,
                    skin = skin,
                    renderParams = renderParams,
                    keyHeightDp = keyHeightDp,
                    spacingDp = spacingDp,
                    rowSpacingDp = rowSpacingDp,
                    cornerRadiusDp = cornerRadiusDp,
                    oneHandMode = state.oneHandMode,
                    onTap = { keyDef ->
                        val ch = keyDef.keyChar
                        if (ch != null && ch in '2'..'9' || ch == '0') {
                            viewModel.onT9Key(ch)
                        }
                    },
                    onSpace = { viewModel.onT9Space() },
                    onEnter = { viewModel.onEnter() },
                    onDelete = { viewModel.onT9Delete() },
                    onSetMode = viewModel::setMode,
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

            // Text editing arrows panel (unified via KeyboardSurface)
            KeyboardMode.ARROWS ->
                KeyboardSurfaceArrows(
                    state = state,
                    skin = skin,
                    renderParams = renderParams,
                    keyHeightDp = keyHeightDp,
                    spacingDp = spacingDp,
                    rowSpacingDp = rowSpacingDp,
                    cornerRadiusDp = cornerRadiusDp,
                    oneHandMode = state.oneHandMode,
                    onCursorLeft = { viewModel.onCursorLeft() },
                    onCursorUp = { viewModel.onCursorUp() },
                    onCursorRight = { viewModel.onCursorRight() },
                    onCursorDown = { viewModel.onCursorDown() },
                    onCopy = { viewModel.onCopy() },
                    onPaste = { viewModel.onPaste() },
                    onCut = { viewModel.onCut() },
                    onSelectAll = { viewModel.onSelectAll() },
                    onHome = { viewModel.onHome() },
                    onEnd = { viewModel.onEnd() },
                    onDelete = { viewModel.onDelete() },
                )

            // Clipboard history panel
            KeyboardMode.CLIPBOARD ->
                ClipboardPanel(
                    onNavigateBack = { viewModel.setMode(KeyboardMode.ALPHA) },
                )

            // 文本编辑工具面板 (unified via KeyboardSurface)
            KeyboardMode.EDIT_TOOL ->
                KeyboardSurfaceEditTool(
                    state = state,
                    skin = skin,
                    renderParams = renderParams,
                    keyHeightDp = keyHeightDp,
                    spacingDp = spacingDp,
                    rowSpacingDp = rowSpacingDp,
                    cornerRadiusDp = cornerRadiusDp,
                    oneHandMode = state.oneHandMode,
                    onCursorLeft = { viewModel.onCursorLeft() },
                    onCursorUp = { viewModel.onCursorUp() },
                    onCursorRight = { viewModel.onCursorRight() },
                    onCursorDown = { viewModel.onCursorDown() },
                    onCopy = { viewModel.onCopy() },
                    onPaste = { viewModel.onPaste() },
                    onCut = { viewModel.onCut() },
                    onSelectAll = { viewModel.onSelectAll() },
                    onHome = { viewModel.onHome() },
                    onEnd = { viewModel.onEnd() },
                    onDelete = { viewModel.onDelete() },
                    onUndo = { viewModel.onUndo() },
                    onClipboard = { viewModel.setClipboardMode() },
                )

            KeyboardMode.WUBI -> {
                KeyboardSurfaceWubi(
                    state = state,
                    renderParams = renderParams,
                    keyHeightDp = keyHeightDp,
                    spacingDp = spacingDp,
                    rowSpacingDp = rowSpacingDp,
                    cornerRadiusDp = cornerRadiusDp,
                    oneHandMode = state.oneHandMode,
                    onTap = { keyDef ->
                        when {
                            keyDef.keyChar != null -> viewModel.onKeyChar(keyDef.keyChar)
                            keyDef.label == "⇧" -> viewModel.toggleShift()
                            keyDef.label == "⌫" -> viewModel.onDelete()
                            keyDef.label == "───" -> viewModel.onSpace()
                            keyDef.label == "中" || keyDef.label == "En" -> viewModel.toggleLanguageMode()
                            keyDef.label == "符" -> viewModel.setMode(KeyboardMode.SYMBOL)
                            keyDef.label == "搜索" -> viewModel.onSearch()
                            keyDef.label == "!," -> viewModel.onKeyChar(',')
                            keyDef.label == "?。" -> viewModel.onKeyChar('.')
                        }
                    },
                    onLongPressAction = { keyDef ->
                        if (keyDef.label == "───") {
                            if (voiceViewModel.state.value.hasPermission || voiceViewModel.checkPermission()) {
                                voiceViewModel.startListening()
                            }
                        }
                    },
                    onLongPressAlt = { baseChar, alt ->
                        viewModel.onKeyLongPress(baseChar, alt)
                    },
                    onDeleteRepeat = viewModel::onDeleteRepeat,
                    onSwipeUp = { keyDef ->
                        val sym = keyDef.swipeUpSymbol ?: keyDef.keyChar
                        if (sym != null) viewModel.onSwipeUp(sym)
                    },
                    onSwipeInput = { chars ->
                        viewModel.onSwipeInput(chars)
                    },
                    onVoiceRelease = {
                        voiceViewModel.stopListening()
                        val recognized = voiceViewModel.state.value.recognizedText
                        if (recognized.isNotEmpty()) {
                            voiceViewModel.commitText()
                        }
                    },
                )
            }
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

// ═══════════════════════════════════════════════════════════════
// 4 个键盘模式包装 — 组装 KeyboardSurface + 布局 + 手势分发
// ═══════════════════════════════════════════════════════════════

@Composable
private fun KeyboardSurfaceAlpha(
    state: KeyboardState,
    skin: com.nuc.omeletteinputmethod.ui.theme.SkinPack?,
    renderParams: KeyRenderParams,
    keyHeightDp: Dp,
    spacingDp: Dp,
    rowSpacingDp: Dp,
    cornerRadiusDp: Dp,
    oneHandMode: OneHandMode,
    onTap: (KeyDef) -> Unit,
    onLongPressAction: (KeyDef) -> Unit,
    onLongPressAlt: (Char, Char) -> Unit,
    onDeleteRepeat: () -> Unit,
    onSwipeUp: (KeyDef) -> Unit,
    onSwipeInput: (List<Char>) -> Unit,
    onVoiceRelease: () -> Unit,
    onShowModeMenu: () -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    val rows = remember(state.isShifted, state.isEnglishMode) {
        buildPinyinRows(state.isShifted).mapIndexed { rowIdx, row ->
            if (rowIdx == 3) {
                // 底部行: 动态 中/En 标签
                row.map { kd ->
                    if (kd.label == "中" || kd.label == "En") {
                        kd.copy(label = if (state.isEnglishMode) "En" else "中")
                    } else kd
                }
            } else row
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(handModifier),
    ) {
        val params = LayoutParams(
            widthPx = maxWidth.value * density,
            keyHeightDp = keyHeightDp,
            spacingDp = spacingDp,
            rowSpacingDp = rowSpacingDp,
            cornerRadiusDp = cornerRadiusDp,
            horizontalPaddingPx = 5f * density,
            oneHandScale = scale,
        )
        val layout = remember(params, rows) { computeLayout(params, rows, density) }

        KeyboardSurface(
            layout = layout,
            renderParams = renderParams,
            enableSwipeInput = true,
            onGesture = { event ->
                when (event) {
                    is GestureEvent.Tap -> onTap(event.keyDef)
                    is GestureEvent.LongPressAction -> onLongPressAction(event.keyDef)
                    is GestureEvent.LongPressAlt -> onLongPressAlt(event.baseChar, event.selectedAlt)
                    is GestureEvent.DeleteRepeat -> onDeleteRepeat()
                    is GestureEvent.SwipeUp -> onSwipeUp(event.keyDef)
                    is GestureEvent.SwipeInput -> onSwipeInput(event.chars)
                    else -> { /* press/swipe state handled internally by KeyboardSurface */ }
                }
            },
            onVoiceRelease = onVoiceRelease,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun KeyboardSurfaceSymbol(
    renderParams: KeyRenderParams,
    keyHeightDp: Dp,
    spacingDp: Dp,
    rowSpacingDp: Dp,
    cornerRadiusDp: Dp,
    oneHandMode: OneHandMode,
    onTap: (KeyDef) -> Unit,
    onLongPressAlt: (Char, Char) -> Unit,
    onDeleteRepeat: () -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    val rows = remember { buildSymbolRows() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(handModifier),
    ) {
        val params = LayoutParams(
            widthPx = maxWidth.value * density,
            keyHeightDp = keyHeightDp,
            spacingDp = spacingDp,
            rowSpacingDp = rowSpacingDp,
            cornerRadiusDp = cornerRadiusDp,
            horizontalPaddingPx = 5f * density,
            oneHandScale = scale,
        )
        val layout = remember(params, rows) { computeLayout(params, rows, density) }

        KeyboardSurface(
            layout = layout,
            renderParams = renderParams,
            enableSwipeInput = false,
            onGesture = { event ->
                when (event) {
                    is GestureEvent.Tap -> onTap(event.keyDef)
                    is GestureEvent.LongPressAlt -> onLongPressAlt(event.baseChar, event.selectedAlt)
                    is GestureEvent.DeleteRepeat -> onDeleteRepeat()
                    else -> {}
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun KeyboardSurfaceNumber(
    renderParams: KeyRenderParams,
    keyHeightDp: Dp,
    spacingDp: Dp,
    rowSpacingDp: Dp,
    cornerRadiusDp: Dp,
    oneHandMode: OneHandMode,
    onTap: (KeyDef) -> Unit,
    onLongPressAlt: (Char, Char) -> Unit,
    onDeleteRepeat: () -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    val rows = remember { buildNumberRows() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(handModifier),
    ) {
        val params = LayoutParams(
            widthPx = maxWidth.value * density,
            keyHeightDp = keyHeightDp,
            spacingDp = spacingDp,
            rowSpacingDp = rowSpacingDp,
            cornerRadiusDp = cornerRadiusDp,
            horizontalPaddingPx = 5f * density,
            oneHandScale = scale,
        )
        val layout = remember(params, rows) { computeLayout(params, rows, density) }

        KeyboardSurface(
            layout = layout,
            renderParams = renderParams,
            enableSwipeInput = false,
            onGesture = { event ->
                when (event) {
                    is GestureEvent.Tap -> onTap(event.keyDef)
                    is GestureEvent.LongPressAlt -> onLongPressAlt(event.baseChar, event.selectedAlt)
                    is GestureEvent.DeleteRepeat -> onDeleteRepeat()
                    else -> {}
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun KeyboardSurfaceWubi(
    state: KeyboardState,
    renderParams: KeyRenderParams,
    keyHeightDp: Dp,
    spacingDp: Dp,
    rowSpacingDp: Dp,
    cornerRadiusDp: Dp,
    oneHandMode: OneHandMode,
    onTap: (KeyDef) -> Unit,
    onLongPressAction: (KeyDef) -> Unit,
    onLongPressAlt: (Char, Char) -> Unit,
    onDeleteRepeat: () -> Unit,
    onSwipeUp: (KeyDef) -> Unit,
    onSwipeInput: (List<Char>) -> Unit,
    onVoiceRelease: () -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    // 五笔字根提示函数 (引用 WubiEngine)
    val rootHintFn: (Char) -> String? = remember {
        { ch -> com.nuc.omeletteinputmethod.ui.keyboard.WubiEngine.getRootHint(ch) }
    }

    val rows = remember(state.isShifted, state.isEnglishMode) {
        buildWubiRows(state.isShifted, rootHintFn).mapIndexed { rowIdx, row ->
            if (rowIdx == 3) {
                row.map { kd ->
                    if (kd.label == "中" || kd.label == "En") {
                        kd.copy(label = if (state.isEnglishMode) "En" else "中")
                    } else kd
                }
            } else row
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(handModifier),
    ) {
        val params = LayoutParams(
            widthPx = maxWidth.value * density,
            keyHeightDp = keyHeightDp,
            spacingDp = spacingDp,
            rowSpacingDp = rowSpacingDp,
            cornerRadiusDp = cornerRadiusDp,
            horizontalPaddingPx = 5f * density,
            oneHandScale = scale,
        )
        val layout = remember(params, rows) { computeLayout(params, rows, density) }

        KeyboardSurface(
            layout = layout,
            renderParams = renderParams,
            enableSwipeInput = true,
            onGesture = { event ->
                when (event) {
                    is GestureEvent.Tap -> onTap(event.keyDef)
                    is GestureEvent.LongPressAction -> onLongPressAction(event.keyDef)
                    is GestureEvent.LongPressAlt -> onLongPressAlt(event.baseChar, event.selectedAlt)
                    is GestureEvent.DeleteRepeat -> onDeleteRepeat()
                    is GestureEvent.SwipeUp -> onSwipeUp(event.keyDef)
                    is GestureEvent.SwipeInput -> onSwipeInput(event.chars)
                    else -> {}
                }
            },
            onVoiceRelease = onVoiceRelease,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
@Composable
fun CandidateBar(
    candidates: List<String>,
    candidatesMeta: List<CandidateItem>,
    associatedCandidates: List<String> = emptyList(),
    inputBuffer: String = "",
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onDelete: (String) -> Unit,
    onExpand: () -> Unit,
) {

    // LazyRow 真实条目：联想候选以浅青色高亮显示在前，拼音候选在后
    // 使用 ElectricCyan 与拼音候选 CyberBlue 区分，表明这是"上屏后的续词联想"
    data class RowItem(val text: String, val isAssociated: Boolean)

    val density = androidx.compose.ui.platform.LocalDensity.current.density

    val rowItems = remember(associatedCandidates, candidatesMeta) {
        val list = mutableListOf<RowItem>()
        for (a in associatedCandidates.take(3)) {
            list.add(RowItem(a, isAssociated = true))
        }
        for (c in candidatesMeta.take(5)) {
            list.add(RowItem(c.word, isAssociated = false))
        }
        list
    }

    Box(
        modifier = modifier.background(KeyboardColors.CandidateBackground),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (candidates.isEmpty() && associatedCandidates.isEmpty()) {
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
                    itemsIndexed(rowItems) { index, item ->
                        val isFirst = index == 0
                        val isAssociated = item.isAssociated
                        var offsetX by remember { mutableFloatStateOf(0f) }
                        var showContextMenu by remember { mutableStateOf(false) }

                        Box {
                            Box(
                                modifier = Modifier
                                    .offset(x = offsetX.dp)
                                    .pointerInput(item.text) {
                                        detectHorizontalDragGestures(
                                            onDragEnd = {
                                                if (offsetX < -50) {
                                                    onDelete(item.text)
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
                                    .pointerInput(item.text) {
                                        detectTapGestures(
                                            onTap = { onSelect(item.text) },
                                            onLongPress = { showContextMenu = true }
                                        )
                                    }
                            ) {
                                // 联想候选不走置顶体系，需要从 candidatesMeta 反查是否置顶
                                val isPinned = !isAssociated &&
                                    candidatesMeta.firstOrNull { it.word == item.text }?.isPinned == true
                                val cardColor = when {
                                    isAssociated -> KeyboardColors.ElectricCyan.copy(alpha = 0.18f)
                                    isFirst -> KeyboardColors.CyberBlue.copy(alpha = 0.2f)
                                    else -> KeyboardColors.DarkGray800
                                }
                                val textColor = when {
                                    isAssociated -> KeyboardColors.ElectricCyan
                                    isFirst -> KeyboardColors.CyberBlue
                                    else -> KeyboardColors.TextPrimary
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = cardColor,
                                    shadowElevation = if (isFirst && !isAssociated) 1.dp else 0.dp,
                                ) {
                                    Text(
                                        text = item.text,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            // 联想候选不参与置顶/删除手势
                            if (!isAssociated && offsetX < -10) {
                                Row(
                                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(onClick = { onTogglePin(item.text) }, modifier = Modifier.size(20.dp)) {
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = "Pin",
                                            tint = if (candidatesMeta.firstOrNull { it.word == item.text }?.isPinned == true) KeyboardColors.CyberBlue else KeyboardColors.TextSecondary
                                        )
                                    }
                                    IconButton(onClick = { onDelete(item.text) }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Filled.Close, contentDescription = "Delete", tint = KeyboardColors.Error)
                                    }
                                }
                            }

                            if (!isAssociated) {
                                DropdownMenu(
                                    expanded = showContextMenu,
                                    onDismissRequest = { showContextMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (candidatesMeta.firstOrNull { it.word == item.text }?.isPinned == true) "取消置顶" else "置顶") },
                                        onClick = {
                                            onTogglePin(item.text)
                                            showContextMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("删除") },
                                        onClick = {
                                            onDelete(item.text)
                                            showContextMenu = false
                                        }
                                    )
                                }
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

// (PinyinKeyboard deleted — replaced by KeyboardSurfaceAlpha above)

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

// ── 工具栏 — 42dp 高、深色背景、IconButton 涟漪反馈 ──
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
    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(KeyboardColors.ToolbarBackground)
                    .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onKeyboardSwitch,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Keyboard,
                    contentDescription = "切换键盘",
                    tint = KeyboardColors.ToolbarActiveIcon,
                    modifier = Modifier.size(22.dp),
                )
            }

            IconButton(
                onClick = onAiAction,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "AI",
                    tint = KeyboardColors.ToolbarActiveIcon,
                    modifier = Modifier.size(22.dp),
                )
            }

            IconButton(
                onClick = onSettings,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "设置",
                    tint = KeyboardColors.ToolbarActiveIcon,
                    modifier = Modifier.size(22.dp),
                )
            }

            IconButton(
                onClick = onTextEdit,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.SelectAll,
                    contentDescription = "文本编辑",
                    tint = KeyboardColors.ToolbarActiveIcon,
                    modifier = Modifier.size(22.dp),
                )
            }

            IconButton(
                onClick = onClipboard,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentPaste,
                    contentDescription = "剪贴板",
                    tint = KeyboardColors.ToolbarActiveIcon,
                    modifier = Modifier.size(22.dp),
                )
            }

            IconButton(
                onClick = onHideKeyboard,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "收起键盘",
                    tint = KeyboardColors.CyberBlue,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        HorizontalDivider(
            color = KeyboardColors.ToolbarDivider,
            thickness = 0.5.dp,
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
private fun KeyboardSurfaceT9(
    state: KeyboardState,
    skin: com.nuc.omeletteinputmethod.ui.theme.SkinPack?,
    renderParams: KeyRenderParams,
    keyHeightDp: Dp,
    spacingDp: Dp,
    rowSpacingDp: Dp,
    cornerRadiusDp: Dp,
    oneHandMode: OneHandMode,
    onTap: (KeyDef) -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onDelete: () -> Unit,
    onSetMode: (KeyboardMode) -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    val rows = remember { buildT9Rows() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(handModifier),
    ) {
        val params = LayoutParams(
            widthPx = maxWidth.value * density,
            keyHeightDp = keyHeightDp,
            spacingDp = spacingDp,
            rowSpacingDp = rowSpacingDp,
            cornerRadiusDp = cornerRadiusDp,
            horizontalPaddingPx = 5f * density,
            oneHandScale = scale,
        )
        val layout = remember(params, rows) { computeLayout(params, rows, density) }

        KeyboardSurface(
            layout = layout,
            renderParams = renderParams,
            enableSwipeInput = false,
            onGesture = { event ->
                when (event) {
                    is GestureEvent.Tap -> {
                        val kd = event.keyDef
                        when (kd.label) {
                            "全键" -> onSetMode(KeyboardMode.ALPHA)
                            "───" -> onSpace()
                            "↵" -> onEnter()
                            else -> {
                                if (kd.keyChar in '1'..'9' || kd.keyChar == '0') {
                                    onTap(kd)
                                }
                            }
                        }
                    }
                    is GestureEvent.DeleteRepeat -> onDelete()
                    else -> {}
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
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

// ── 方向键 / 文本操作面板 (统一 KeyboardSurface) ──
@Composable
private fun KeyboardSurfaceArrows(
    state: KeyboardState,
    skin: com.nuc.omeletteinputmethod.ui.theme.SkinPack?,
    renderParams: KeyRenderParams,
    keyHeightDp: Dp,
    spacingDp: Dp,
    rowSpacingDp: Dp,
    cornerRadiusDp: Dp,
    oneHandMode: OneHandMode,
    onCursorLeft: () -> Unit,
    onCursorUp: () -> Unit,
    onCursorRight: () -> Unit,
    onCursorDown: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onCut: () -> Unit,
    onSelectAll: () -> Unit,
    onHome: () -> Unit,
    onEnd: () -> Unit,
    onDelete: () -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    val rows = remember { buildArrowsRows() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(handModifier),
    ) {
        val params = LayoutParams(
            widthPx = maxWidth.value * density,
            keyHeightDp = keyHeightDp,
            spacingDp = spacingDp,
            rowSpacingDp = rowSpacingDp,
            cornerRadiusDp = cornerRadiusDp,
            horizontalPaddingPx = 5f * density,
            oneHandScale = scale,
        )
        val layout = remember(params, rows) { computeLayout(params, rows, density) }

        KeyboardSurface(
            layout = layout,
            renderParams = renderParams,
            enableSwipeInput = false,
            onGesture = { event ->
                when (event) {
                    is GestureEvent.Tap -> {
                        when (event.keyDef.label) {
                            "←" -> onCursorLeft()
                            "↑" -> onCursorUp()
                            "→" -> onCursorRight()
                            "↓" -> onCursorDown()
                            "复制" -> onCopy()
                            "粘贴" -> onPaste()
                            "剪切" -> onCut()
                            "全选" -> onSelectAll()
                            "|<\n行首" -> onHome()
                            ">|\n行尾" -> onEnd()
                            "⌫" -> onDelete()
                        }
                    }
                    is GestureEvent.DeleteRepeat -> onDelete()
                    else -> {}
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun KeyboardSurfaceEditTool(
    state: KeyboardState,
    skin: com.nuc.omeletteinputmethod.ui.theme.SkinPack?,
    renderParams: KeyRenderParams,
    keyHeightDp: Dp,
    spacingDp: Dp,
    rowSpacingDp: Dp,
    cornerRadiusDp: Dp,
    oneHandMode: OneHandMode,
    onCursorLeft: () -> Unit,
    onCursorUp: () -> Unit,
    onCursorRight: () -> Unit,
    onCursorDown: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onCut: () -> Unit,
    onSelectAll: () -> Unit,
    onHome: () -> Unit,
    onEnd: () -> Unit,
    onDelete: () -> Unit,
    onUndo: () -> Unit,
    onClipboard: () -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val scale = oneHandScale(oneHandMode)
    val handModifier = oneHandPaddingModifier(oneHandMode)

    val rows = remember { buildEditToolRows() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(handModifier),
    ) {
        val params = LayoutParams(
            widthPx = maxWidth.value * density,
            keyHeightDp = keyHeightDp,
            spacingDp = spacingDp,
            rowSpacingDp = rowSpacingDp,
            cornerRadiusDp = cornerRadiusDp,
            horizontalPaddingPx = 5f * density,
            oneHandScale = scale,
        )
        val layout = remember(params, rows) { computeLayout(params, rows, density) }

        KeyboardSurface(
            layout = layout,
            renderParams = renderParams,
            enableSwipeInput = false,
            onGesture = { event ->
                when (event) {
                    is GestureEvent.Tap -> {
                        when (event.keyDef.label) {
                            "←" -> onCursorLeft()
                            "↑" -> onCursorUp()
                            "→" -> onCursorRight()
                            "↓" -> onCursorDown()
                            "复制" -> onCopy()
                            "粘贴" -> onPaste()
                            "剪切" -> onCut()
                            "全选" -> onSelectAll()
                            "|<\n行首" -> onHome()
                            ">|\n行尾" -> onEnd()
                            "撤销" -> onUndo()
                            "剪贴板" -> onClipboard()
                            "⌫" -> onDelete()
                        }
                    }
                    is GestureEvent.DeleteRepeat -> onDelete()
                    else -> {}
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
