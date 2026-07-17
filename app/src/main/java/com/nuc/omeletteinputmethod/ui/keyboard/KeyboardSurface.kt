package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════
// KeyboardSurface — 统一 Canvas 按键绘制 + 统一 pointer 手势处理
// 替代原来的 N 个 KeyButton Box
// ═══════════════════════════════════════════════════════════════

/**
 * 长按 Popup 显示状态
 */
data class LongPressPopupState(
    val keyIndex: Int,
    val keyDef: KeyDef,
    val hoveredIndex: Int = -1,
)

/**
 * @param layout 预计算的按键布局
 * @param renderParams 皮肤派生绘制参数
 * @param onGesture 手势事件回调
 * @param modifier 外层 modifier (单手模式 padding 等)
 * @param enableSwipeInput 是否启用键盘级滑行输入 (拼音/五笔开，符号/数字关)
 * @param onVoiceLongPress 空格长按语音触发 (可选)
 * @param onVoiceRelease 空格松开语音停止 (可选)
 */
@Composable
fun KeyboardSurface(
    layout: ComputedLayout,
    renderParams: KeyRenderParams,
    onGesture: (GestureEvent) -> Unit,
    modifier: Modifier = Modifier,
    enableSwipeInput: Boolean = true,
    onVoiceLongPress: (() -> Unit)? = null,
    onVoiceRelease: (() -> Unit)? = null,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()

    // ── 手势状态 (纯数据，不触发重组 — 用 remember + set 读给 Canvas) ──
    val gestureState = remember { mutableStateOf(GestureState()) }

    // ── 长按 Popup 状态 ──
    val popupState = remember { mutableStateOf<LongPressPopupState?>(null) }

    // ── 按压动画 scale ──
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) renderParams.pressScaleMin else 1f,
        animationSpec = tween(durationMillis = renderParams.pressAnimDurationMs),
        label = "pressScale",
    )

    // ── 语音录制状态 ──
    var voiceRecording by remember { mutableStateOf(false) }

    // ── 预测量所有 label 的文字尺寸 (避免 Canvas 每帧重新排版) ──
    val measuredLabels = remember(layout, renderParams) {
        layout.keys.map { pk ->
            val style = when {
                pk.keyDef.arrowLabel != null -> renderParams.textStyleArrow
                pk.keyDef.isAlpha -> renderParams.textStyleAlpha
                else -> renderParams.textStyleFunc
            }
            val displayLabel = pk.keyDef.arrowLabel ?: pk.keyDef.label
            val result = textMeasurer.measure(
                text = displayLabel,
                style = style,
                maxLines = 2,
            )
            result
        }
    }

    // ── 预测量上滑符号 ──
    val measuredSwipeUp = remember(layout, renderParams) {
        layout.keys.map { pk ->
            val sym = pk.keyDef.swipeUpSymbol
            if (sym != null) {
                textMeasurer.measure(
                    text = sym.toString(),
                    style = TextStyle(
                        fontSize = renderParams.swipeUpTextSize,
                        color = renderParams.swipeUpTextColor,
                    ),
                )
            } else null
        }
    }

    // ── 预测量五笔字根提示 ──
    val measuredRootHints = remember(layout, renderParams) {
        layout.keys.map { pk ->
            val hint = pk.keyDef.rootHint
            if (hint != null) {
                textMeasurer.measure(
                    text = hint,
                    style = TextStyle(
                        fontSize = renderParams.rootHintSize,
                        color = renderParams.rootHintColor,
                    ),
                )
            } else null
        }
    }

    val contentHeightDp = with(density) {
        layout.totalHeightPx.toDp()
    }

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .height(contentHeightDp)
    ) {
        // 若键盘内容超出 IME 窗口可用高度，等比缩放使其适配
        val availableHeight = maxHeight
        val fitScale = if (contentHeightDp > availableHeight) {
            (availableHeight / contentHeightDp)
        } else 1f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeightDp)
                .graphicsLayer {
                    scaleX = fitScale * pressScale
                    scaleY = fitScale * pressScale
                    transformOrigin = TransformOrigin(0.5f, 0f) // 从顶部缩放
                }
                // ── 统一 Canvas 绘制 ──
                .drawBehind {
                    drawKeyboard(
                        layout = layout,
                        params = renderParams,
                        pressedIndex = gestureState.value.pressedIndex,
                        highlightedIndices = gestureState.value.highlightedIndices,
                        swipePath = gestureState.value.swipePath,
                        popupState = popupState.value,
                        measuredLabels = measuredLabels,
                        measuredSwipeUp = measuredSwipeUp,
                        measuredRootHints = measuredRootHints,
                        textMeasurer = textMeasurer,
                        realDensity = density.density,
                    )
                }
                // ── 统一 pointer 手势处理 ──
                .pointerInput(layout) {
                    awaitPointerEventScope {
                        while (true) {
                            // 等待手指按下
                            val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                            val down = downEvent.changes.firstOrNull() ?: continue
                            down.consume()
                            val downPos = down.position
                            val downTime = System.currentTimeMillis()

                            // 命中检测
                            val hitIndex = hitTest(downPos, layout)
                            if (hitIndex < 0) continue

                            val hitKey = layout.keys[hitIndex]
                            pressed = true

                            // 更新手势状态
                            val newState = GestureState(
                                pressedIndex = hitIndex,
                                downPos = downPos,
                                downTimeMs = downTime,
                            )
                            gestureState.value = newState
                            popupState.value = null

                            onGesture(GestureEvent.PressChanged(hitIndex))

                            var isLongPress = false
                            var isSwipeUp = false
                            var isSwipeMode = false

                            // 等待后续事件
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val currentTime = System.currentTimeMillis()

                                // 所有手指抬起
                                if (event.changes.all { !it.pressed }) {
                                    event.changes.forEach { it.consume() }

                                    val gs = gestureState.value
                                    val popup = popupState.value

                                    when {
                                        // 长按 Popup 选中了替代字符
                                        popup != null && popup.hoveredIndex >= 0 &&
                                            popup.keyDef.longPressAlts != null &&
                                            popup.hoveredIndex < popup.keyDef.longPressAlts.size -> {
                                            val baseChar = popup.keyDef.keyChar ?: popup.keyDef.swipeUpTargetChar
                                            if (baseChar != null) {
                                                onGesture(
                                                    GestureEvent.LongPressAlt(
                                                        baseChar,
                                                        popup.keyDef.longPressAlts[popup.hoveredIndex]
                                                    )
                                                )
                                            }
                                        }
                                        // 滑行输入
                                        isSwipeMode && gs.traversedKeys.isNotEmpty() -> {
                                            onGesture(GestureEvent.SwipeInput(gs.traversedKeys.toList()))
                                        }
                                        // 上滑
                                        isSwipeUp -> {
                                            onGesture(GestureEvent.SwipeUp(hitKey.keyDef))
                                        }
                                        // 长按 (无 Popup 或已触发 action)
                                        isLongPress -> {
                                            // 长按 action 或退格连删已在 timeout 时处理
                                        }
                                        // 普通点击
                                        else -> {
                                            onGesture(GestureEvent.Tap(hitKey.keyDef))
                                        }
                                    }

                                    // 重置
                                    gestureState.value = gs.reset()
                                    popupState.value = null
                                    onGesture(GestureEvent.DismissLongPressPopup)
                                    onGesture(GestureEvent.PressChanged(-1))

                                    // 语音松开
                                    if (voiceRecording) {
                                        voiceRecording = false
                                        onVoiceRelease?.invoke()
                                    }

                                    break // 回到外层 awaitPointerEventScope 循环
                                }

                                // 手指还在屏幕上 (移动或静止)
                                val change = event.changes.firstOrNull() ?: break
                                val currentPos = change.position
                                change.consume()

                                val elapsed = currentTime - downTime
                                val gs = gestureState.value

                                // ── 上滑检测 ──
                                if (!isSwipeUp && !isLongPress && !isSwipeMode) {
                                    if (detectSwipeUp(downPos.y, currentPos.y, swipeUpThresholdPx(density))) {
                                        isSwipeUp = true
                                        gestureState.value = gs.copy(swipeUpTriggered = true)
                                        onGesture(GestureEvent.SwipeUp(hitKey.keyDef))
                                        break // 上滑触发后中断本次手势 (手指抬起时不再处理其他)
                                    }
                                }

                                // ── 滑行模式检测 (手指移出原键) ──
                                if (enableSwipeInput && !isSwipeMode && !isSwipeUp && !isLongPress) {
                                    if (detectExitKey(currentPos, hitKey.rect)) {
                                        isSwipeMode = true
                                        val sp = mutableListOf<Offset>()
                                        sp.add(downPos)
                                        gestureState.value = gs.copy(isSwipeMode = true, swipePath = sp)
                                        onGesture(GestureEvent.SwipePathPoint(downPos))
                                    }
                                }

                                // ── 滑行模式中: 收集轨迹和经过的键 ──
                                if (isSwipeMode) {
                                    val sp = gs.swipePath
                                    sp.add(currentPos)
                                    onGesture(GestureEvent.SwipePathPoint(currentPos))

                                    val hitIdx = hitTest(currentPos, layout)
                                    if (hitIdx >= 0) {
                                        val hitKeyDef = layout.keys[hitIdx].keyDef
                                        val ch = hitKeyDef.keyChar ?: hitKeyDef.swipeUpTargetChar
                                        if (ch != null) {
                                            val tk = gs.traversedKeys
                                            if (tk.isEmpty() || tk.last() != ch) {
                                                tk.add(ch)
                                            }
                                        }
                                        updateSwipeHighlight(hitIdx, gs.highlightedIndices)
                                        onGesture(GestureEvent.SwipeHighlightChanged(gs.highlightedIndices.toSet()))
                                    }
                                    gestureState.value = gs
                                    continue
                                }

                                // ── Popup hover 追踪 ──
                                if (gs.popupVisible && popupState.value != null) {
                                    val pkv = popupState.value!!
                                    val alts = pkv.keyDef.longPressAlts ?: emptyList()
                                    val hoverIdx = computePopupHoverIndex(
                                        fingerX = currentPos.x,
                                        downX = downPos.x,
                                        altCount = alts.size,
                                        itemWidthPx = renderParams.popupItemSizePx,
                                        spacingPx = 3f * density.density,
                                        paddingPx = 6f * density.density,
                                    )
                                    popupState.value = pkv.copy(hoveredIndex = hoverIdx)
                                }

                                // ── 长按超时检测 ──
                                if (!isLongPress && !isSwipeUp && !isSwipeMode) {
                                    if (isLongPressTimeout(downTime, currentTime, hitKey.keyDef.isDelete)) {
                                        isLongPress = true
                                        gestureState.value = gs.copy(longPressTriggered = true)

                                        when {
                                            // 退格连删
                                            hitKey.keyDef.isDelete -> {
                                                onGesture(GestureEvent.DeleteRepeat)
                                                // 启动连删协程
                                                scope.launch {
                                                    while (isActive) {
                                                        onGesture(GestureEvent.DeleteRepeat)
                                                        delay(50L)
                                                    }
                                                }
                                                break // 退格连删中断手势
                                            }
                                            // longPressAction (符键等)
                                            hitKey.keyDef.longPressAction -> {
                                                onGesture(GestureEvent.LongPressAction(hitKey.keyDef))
                                                break
                                            }
                                            // 替代字符 Popup
                                            hitKey.keyDef.longPressAlts != null &&
                                                hitKey.keyDef.longPressAlts.isNotEmpty() -> {
                                                gestureState.value = gs.copy(
                                                    longPressTriggered = true,
                                                    popupVisible = true,
                                                )
                                                popupState.value = LongPressPopupState(
                                                    keyIndex = hitIndex,
                                                    keyDef = hitKey.keyDef,
                                                    hoveredIndex = -1,
                                                )
                                                onGesture(
                                                    GestureEvent.ShowLongPressPopup(hitIndex, hitKey.keyDef)
                                                )
                                                // 继续等待手指移动/抬起 (不 break)
                                            }
                                            // 无替代字符 → 就是普通长按，抬起时不触发 tap
                                            else -> {
                                                // 长按只是标记，抬起时不处理 tap
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
        )
    }
}

/**
 * dp 转 px 工具 (在 Composable 上下文中使用)
 */
private fun Dp.toPxOrZero(density: androidx.compose.ui.unit.Density): Float =
    if (this.value <= 0f) 0f else this.value * density.density

/**
 * 上滑阈值 (px)
 */
private fun swipeUpThresholdPx(density: androidx.compose.ui.unit.Density): Float =
    with(density) { 18.dp.toPx() }

// ═══════════════════════════════════════════════════════════════
// 统一 Canvas 绘制函数
// ═══════════════════════════════════════════════════════════════

private fun DrawScope.drawKeyboard(
    layout: ComputedLayout,
    params: KeyRenderParams,
    pressedIndex: Int,
    highlightedIndices: Set<Int>,
    swipePath: List<Offset>,
    popupState: LongPressPopupState?,
    measuredLabels: List<androidx.compose.ui.text.TextLayoutResult>,
    measuredSwipeUp: List<androidx.compose.ui.text.TextLayoutResult?>,
    measuredRootHints: List<androidx.compose.ui.text.TextLayoutResult?>,
    textMeasurer: TextMeasurer,
    realDensity: Float,
) {
    // ── 1. 按键背景 ──
    for (pk in layout.keys) {
        val isPressed = pk.index == pressedIndex
        val isHighlighted = pk.index in highlightedIndices
        val bg = when {
            isPressed -> params.pressedBg
            isHighlighted -> params.swipeTrailColor.copy(alpha = 0.15f).run {
                Brush.verticalGradient(listOf(this, this))
            }
            pk.keyDef.customBgColor != null ->
                Brush.verticalGradient(listOf(pk.keyDef.customBgColor, pk.keyDef.customBgColor))
            pk.keyDef.isCapsule -> params.functionKeyBg
            else -> params.normalBg
        }
        drawRoundRect(
            brush = bg,
            topLeft = Offset(pk.x, pk.y),
            size = Size(pk.width, pk.height),
            cornerRadius = CornerRadius(pk.cornerRadiusPx, pk.cornerRadiusPx),
        )
    }

    // ── 2. 阴影 (非按压键) ──
    if (params.shadowColor != Color.Transparent && params.shadowOffsetYPx > 0f) {
        for (pk in layout.keys) {
            if (pk.index == pressedIndex) continue
            drawRoundRect(
                color = params.shadowColor,
                topLeft = Offset(pk.x, pk.y + params.shadowOffsetYPx),
                size = Size(pk.width, pk.height),
                cornerRadius = CornerRadius(pk.cornerRadiusPx, pk.cornerRadiusPx),
            )
        }
    }

    // ── 3. 高光 (非按压键，顶部渐变条) ──
    if (params.highlightAlpha > 0f && params.highlightHeightPx > 0f) {
        for (pk in layout.keys) {
            if (pk.index == pressedIndex) continue
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = params.highlightAlpha),
                        Color.White.copy(alpha = 0f),
                    ),
                    startY = pk.y,
                    endY = pk.y + params.highlightHeightPx,
                ),
                topLeft = Offset(pk.x, pk.y),
                size = Size(pk.width, minOf(params.highlightHeightPx, pk.height)),
                cornerRadius = CornerRadius(pk.cornerRadiusPx, pk.cornerRadiusPx),
            )
        }
    }

    // ── 4. 边框 ──
    if (params.borderColor != Color.Transparent && params.borderWidthPx > 0f) {
        for (pk in layout.keys) {
            drawRoundRect(
                color = params.borderColor,
                topLeft = Offset(pk.x, pk.y),
                size = Size(pk.width, pk.height),
                cornerRadius = CornerRadius(pk.cornerRadiusPx, pk.cornerRadiusPx),
                style = Stroke(width = params.borderWidthPx),
            )
        }
    }

    // ── 5. 文字 ──
    for ((i, pk) in layout.keys.withIndex()) {
        val measured = measuredLabels.getOrNull(i) ?: continue
        val hasArrow = pk.keyDef.arrowLabel != null
        val textColor = when {
            pk.keyDef.customContentColor != null -> pk.keyDef.customContentColor
            else -> params.textColor
        }
        val style = when {
            hasArrow -> params.textStyleArrow
            pk.keyDef.isAlpha -> params.textStyleAlpha
            else -> params.textStyleFunc
        }
        val displayLabel = pk.keyDef.arrowLabel ?: pk.keyDef.label
        // 顶层带 arrowLabel 的键也显示 label 作为副标题
        val coloredResult = textMeasurer.measure(
            text = displayLabel,
            style = style.copy(color = textColor),
            maxLines = 2,
        )
        val textWidth = coloredResult.size.width.toFloat()
        val textHeight = coloredResult.size.height.toFloat()
        val textX = pk.x + (pk.width - textWidth) / 2f
        val textY = pk.y + (pk.height - textHeight) / 2f
        drawText(
            textLayoutResult = coloredResult,
            topLeft = Offset(textX, textY),
        )
    }

    // ── 6. 上滑符号 (非按压键，顶部居中) ──
    for ((i, pk) in layout.keys.withIndex()) {
        if (pk.index == pressedIndex) continue
        val symMeasure = measuredSwipeUp.getOrNull(i) ?: continue ?: continue
        val sym = pk.keyDef.swipeUpSymbol ?: continue
        val coloredSym = textMeasurer.measure(
            text = sym.toString(),
            style = TextStyle(
                fontSize = params.swipeUpTextSize,
                color = params.swipeUpTextColor,
            ),
            maxLines = 1,
        )
        val sw = coloredSym.size.width.toFloat()
        val sx = pk.x + (pk.width - sw) / 2f
        val sy = pk.y + 2f * realDensity  // 2dp 偏移
        drawText(coloredSym, topLeft = Offset(sx, sy))
    }

    // ── 7. 五笔字根提示 (左上角) ──
    for ((i, pk) in layout.keys.withIndex()) {
        val hintText = measuredRootHints.getOrNull(i) ?: continue ?: continue
        val hint = pk.keyDef.rootHint ?: continue
        val coloredHint = textMeasurer.measure(
            text = hint,
            style = TextStyle(
                fontSize = params.rootHintSize,
                color = params.rootHintColor,
            ),
            maxLines = 1,
        )
        val hx = pk.x + 4f * realDensity
        val hy = pk.y + 2f * realDensity
        drawText(coloredHint, topLeft = Offset(hx, hy))
    }

    // ── 8. 滑行轨迹 ──
    if (swipePath.size >= 2) {
        val path = Path()
        path.moveTo(swipePath[0].x, swipePath[0].y)
        for (i in 1 until swipePath.size) {
            path.lineTo(swipePath[i].x, swipePath[i].y)
        }
        drawPath(
            path = path,
            color = params.swipeTrailColor,
            style = Stroke(
                width = params.swipeTrailWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }

    // ── 9. 长按 Popup ──
    if (popupState != null) {
        val pk = layout.keys.getOrNull(popupState.keyIndex) ?: return
        val alts = popupState.keyDef.longPressAlts ?: return
        if (alts.isEmpty()) return

        val itemW = params.popupItemSizePx
        val spacingPx = 3f * realDensity
        val paddingPx = 6f * realDensity
        val totalW = alts.size * itemW + (alts.size - 1) * spacingPx + 2f * paddingPx
        val popupH = itemW + 8f * realDensity

        // Popup 位于按键上方
        val popupX = pk.x + (pk.width - totalW) / 2f
        val popupY = pk.y - popupH - 8f * realDensity

        // Popup 背景
        drawRoundRect(
            color = params.popupBgColor,
            topLeft = Offset(popupX, popupY),
            size = Size(totalW, popupH),
            cornerRadius = CornerRadius(12f * realDensity, 12f * density),
        )

        // Popup 内替代字符
        for ((j, alt) in alts.withIndex()) {
            val isHover = j == popupState.hoveredIndex
            val itemX = popupX + paddingPx + j * (itemW + spacingPx)
            val itemY = popupY + 4f * realDensity
            val itemBg = if (isHover) params.popupHoverBgColor else Color.Transparent
            if (itemBg != Color.Transparent) {
                drawRoundRect(
                    color = itemBg,
                    topLeft = Offset(itemX, itemY),
                    size = Size(itemW, itemW),
                    cornerRadius = CornerRadius(8f * realDensity, 8f * density),
                )
            }
            val altText = textMeasurer.measure(
                text = alt.toString(),
                style = TextStyle(
                    fontSize = 16.sp,
                    color = if (isHover) params.popupTextColor else params.popupTextColor.copy(alpha = 0.8f),
                ),
                maxLines = 1,
            )
            val atw = altText.size.width.toFloat()
            val ath = altText.size.height.toFloat()
            val atx = itemX + (itemW - atw) / 2f
            val aty = itemY + (itemW - ath) / 2f
            drawText(altText, topLeft = Offset(atx, aty))
        }
    }
}

// local density accessor for Canvas lambdas
private val DrawScope.density: Float get() = drawContext.size.width / 360f // approximate