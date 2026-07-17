package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 五笔键盘布局 — 26键 QWERTY 布局 + 字根提示
 *
 * 与拼音26键共享相同的物理布局，但：
 * 1. 每个按键上方显示该键对应的五笔字根
 * 2. 输入字母累积到 wubiBuffer，查询 WubiEngine 获取候选字
 * 3. 候选字显示在 CandidateBar 中
 *
 * @param state 键盘状态（包含 wubiBuffer 和 wubiCandidates）
 * @param onKey 字母键按下回调
 * @param onDelete 删除键回调
 * @param onSpace 空格键回调（选择第一个候选字）
 * @param onEnter 回车键回调
 * @param onSetMode 切换模式回调
 * @param oneHandMode 单手模式
 * @param keyPositionMap 按键位置映射（用于滑动输入）
 * @param onLongPressAlt 长按选备选字回调
 * @param onDeleteRepeat 长按删除连删回调
 * @param swipeActive 是否正在滑动输入
 * @param onSwipeActiveChange 滑动状态变化回调
 * @param swipePath 滑动轨迹
 * @param traversedKeys 滑动经过的键
 * @param highlightedKeys 高亮的键
 * @param onSwipeEnd 滑动结束回调
 * @param onToggleLanguageMode 切换语言模式回调
 * @param onSearch 搜索回调
 * @param onSwipeUp 上滑输入回调
 * @param onLongPressActionVoice 长按语音回调
 * @param onPressReleaseVoice 松手结束语音回调
 */
@Composable
fun WubiKeyboard(
    state: KeyboardState,
    onKey: (Char) -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onSetMode: (KeyboardMode) -> Unit,
    oneHandMode: OneHandMode,
    keyPositionMap: MutableMap<Char, androidx.compose.ui.geometry.Rect>,
    onLongPressAlt: (baseChar: Char, selectedAlt: Char) -> Unit,
    onDeleteRepeat: () -> Unit,
    swipeActive: Boolean,
    onSwipeActiveChange: (Boolean) -> Unit,
    swipePath: MutableList<androidx.compose.ui.geometry.Offset>,
    traversedKeys: MutableList<Char>,
    highlightedKeys: MutableList<Char>,
    onSwipeEnd: () -> Unit,
    onToggleLanguageMode: () -> Unit = {},
    onSearch: () -> Unit = {},
    onSwipeUp: (Char) -> Unit = {},
    onLongPressActionVoice: (() -> Unit)? = null,
    onPressReleaseVoice: (() -> Unit)? = null,
) {
    // 五笔使用小写字母（五笔编码不含大写）
    val keys = listOf(
        "qwertyuiop".toList(),  // row 0: 10 keys
        "asdfghjkl".toList(),   // row 1: 9 keys
        "zxcvbnm".toList(),     // row 2: 7 keys
    )

    val keyHeight = 48.dp
    val spacerHeight = 5.dp

    val scale = WubiKeyboardHelpers.oneHandScale(oneHandMode)
    val handModifier = WubiKeyboardHelpers.oneHandPaddingModifier(oneHandMode)

    val swipeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    // 语音录制状态
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
                            style = Stroke(
                                width = 8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                    }
                },
    ) {
        val halfKeyWidth = maxWidth / 10 / 2

        Column(
            verticalArrangement = Arrangement.spacedBy(spacerHeight * scale),
        ) {
            // Row 0: Q W E R T Y U I O P
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
            ) {
                keys[0].forEach { char ->
                    val isHighlighted = highlightedKeys.contains(char)
                    val swipeSym = SWIPE_UP_SYMBOLS[char]
                    WubiKeyButton(
                        label = char.toString(),
                        rootHint = WubiEngine.getRootHint(char),
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

            // Row 1: A S D F G H J K L
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = halfKeyWidth),
                horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
            ) {
                keys[1].forEach { char ->
                    val isHighlighted = highlightedKeys.contains(char)
                    val swipeSym = SWIPE_UP_SYMBOLS[char]
                    WubiKeyButton(
                        label = char.toString(),
                        rootHint = WubiEngine.getRootHint(char),
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

            // Row 2: Z X C V B N M + 删除键
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = halfKeyWidth),
                horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
            ) {
                // 删除键
                KeyButton(
                    label = "⌫",
                    width = 30.dp * scale,
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
                keys[2].forEach { char ->
                    val isHighlighted = highlightedKeys.contains(char)
                    val swipeSym = SWIPE_UP_SYMBOLS[char]
                    WubiKeyButton(
                        label = char.toString(),
                        rootHint = WubiEngine.getRootHint(char),
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
                // 删除键(右侧)
                KeyButton(
                    label = "⌫",
                    width = 30.dp * scale,
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

            // 底部功能行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp * scale),
                horizontalArrangement = Arrangement.spacedBy(5.dp * scale),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 符
                KeyButton(
                    label = "符",
                    width = 44.dp * scale,
                    height = 40.dp * scale,
                    action = { onSetMode(KeyboardMode.SYMBOL) },
                    keyChar = null,
                    keyPositionMap = keyPositionMap,
                    highlighted = false,
                    onLongPressAlt = null,
                    onDeleteRepeat = null,
                    longPressAlts = null,
                    sizeScale = scale,
                    capsuleShape = true,
                )

                // 中/En
                KeyButton(
                    label = "中",
                    width = 44.dp * scale,
                    height = 40.dp * scale,
                    action = { onToggleLanguageMode() },
                    keyChar = null,
                    keyPositionMap = keyPositionMap,
                    highlighted = false,
                    onLongPressAlt = null,
                    onDeleteRepeat = null,
                    longPressAlts = null,
                    sizeScale = scale,
                    capsuleShape = true,
                )

                // ! ,
                KeyButton(
                    label = "!,",
                    width = 40.dp * scale,
                    height = 40.dp * scale,
                    action = { onKey(',') },
                    keyChar = ',',
                    keyPositionMap = keyPositionMap,
                    highlighted = false,
                    onLongPressAlt = { alt -> onLongPressAlt(',', alt) },
                    onDeleteRepeat = null,
                    longPressAlts = listOf('!'),
                    sizeScale = scale,
                    capsuleShape = true,
                )

                // 空格键
                KeyButton(
                    label = "───",
                    modifier = Modifier.weight(1f),
                    height = 40.dp * scale,
                    action = { onSpace() },
                    keyChar = null,
                    keyPositionMap = keyPositionMap,
                    highlighted = false,
                    onLongPressAlt = null,
                    onDeleteRepeat = null,
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
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                )

                // ? 。
                KeyButton(
                    label = "?。",
                    width = 40.dp * scale,
                    height = 40.dp * scale,
                    action = { onKey('.') },
                    keyChar = '.',
                    keyPositionMap = keyPositionMap,
                    highlighted = false,
                    onLongPressAlt = { alt -> onLongPressAlt('.', alt) },
                    onDeleteRepeat = null,
                    longPressAlts = listOf('?'),
                    sizeScale = scale,
                    capsuleShape = true,
                )

                // 搜索
                KeyButton(
                    label = "搜索",
                    width = 56.dp * scale,
                    height = 40.dp * scale,
                    action = { onSearch() },
                    keyChar = null,
                    keyPositionMap = keyPositionMap,
                    highlighted = false,
                    onLongPressAlt = null,
                    onDeleteRepeat = null,
                    longPressAlts = null,
                    sizeScale = scale,
                    capsuleShape = true,
                    customBgColor = Color(0xFF34D399),
                    customContentColor = Color.White,
                )
            }
        }
    }
}

/**
 * 五笔专用按键 — 在标准按键上方显示字根提示
 */
@Composable
private fun WubiKeyButton(
    label: String,
    rootHint: String,
    modifier: Modifier = Modifier,
    height: Dp,
    action: () -> Unit,
    keyChar: Char?,
    keyPositionMap: MutableMap<Char, androidx.compose.ui.geometry.Rect>,
    highlighted: Boolean,
    onLongPressAlt: ((Char) -> Unit)?,
    onDeleteRepeat: (() -> Unit)?,
    longPressAlts: List<Char>?,
    sizeScale: Float,
    swipeUpSymbol: Char? = null,
    onSwipeUp: ((Char) -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme

    // 复用标准 KeyButton 但叠加字根提示
    Box(modifier = modifier) {
        KeyButton(
            label = label,
            modifier = Modifier.fillMaxWidth(),
            height = height,
            action = action,
            keyChar = keyChar,
            keyPositionMap = keyPositionMap,
            highlighted = highlighted,
            onLongPressAlt = onLongPressAlt,
            onDeleteRepeat = onDeleteRepeat,
            longPressAlts = longPressAlts,
            sizeScale = sizeScale,
            swipeUpSymbol = swipeUpSymbol,
            onSwipeUp = onSwipeUp,
            isAlphaKey = true,
        )
        // 字根提示 — 显示在按键左上角
        if (rootHint.isNotEmpty()) {
            Text(
                text = rootHint,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 3.dp, top = 1.dp),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary.copy(alpha = 0.6f),
                textAlign = TextAlign.Start,
                maxLines = 1,
            )
        }
    }
}

/**
 * 五笔键盘辅助函数 — 单手模式适配
 */
internal object WubiKeyboardHelpers {
    fun oneHandScale(mode: OneHandMode): Float =
        when (mode) {
            OneHandMode.LEFT, OneHandMode.RIGHT -> 0.85f
            else -> 1f
        }

    fun oneHandPaddingModifier(mode: OneHandMode): Modifier =
        when (mode) {
            OneHandMode.LEFT -> Modifier.padding(end = 80.dp)
            OneHandMode.RIGHT -> Modifier.padding(start = 80.dp)
            else -> Modifier
        }
}
