package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════════
// 纯布局计算 — 不含任何 Composable
// 输入：键盘宽度 (px)、键高/间距 (dp)、单手模式、行列定义
// 输出：每个键的像素坐标 + 命中检测 Rect
// ═══════════════════════════════════════════════════════════════

/**
 * 单个按键的定义 — 描述其语义属性，不包含坐标
 */
data class KeyDef(
    val label: String,
    /** 按键对应的字符 (null = 功能键如 shift/delete/符) */
    val keyChar: Char? = null,
    /** Row 内部宽度权重 (对应旧方案 Modifier.weight) */
    val widthWeight: Float = 1f,
    /** 底部胶囊形 */
    val isCapsule: Boolean = false,
    /** 长按替代字符列表 */
    val longPressAlts: List<Char>? = null,
    /** 上滑符号 */
    val swipeUpSymbol: Char? = null,
    /** 是否为字母键 (影响字体样式) */
    val isAlpha: Boolean = false,
    /** 是否为退格键 (长按 150ms 连删) */
    val isDelete: Boolean = false,
    /** 自定义背景色覆盖 (功能键用) */
    val customBgColor: androidx.compose.ui.graphics.Color? = null,
    /** 自定义文字色覆盖 */
    val customContentColor: androidx.compose.ui.graphics.Color? = null,
    /** 五笔字根提示 */
    val rootHint: String? = null,
    /** 长按触发 action (不弹 Popup) */
    val longPressAction: Boolean = false,
    /** 上滑字符的目标 char */
    val swipeUpTargetChar: Char? = null,
    /** 箭头键标签 (←↑→↓|<>| 等) — 使用特殊加粗大字渲染 */
    val arrowLabel: String? = null,
)

/**
 * 布局计算后放置好的按键
 */
data class PlacedKey(
    val index: Int,
    val keyDef: KeyDef,
    /** 按键左上角 X (px) */
    val x: Float,
    /** 按键左上角 Y (px) */
    val y: Float,
    /** 按键宽度 (px) */
    val width: Float,
    /** 按键高度 (px) */
    val height: Float,
    /** 命中检测矩形 */
    val rect: Rect,
    /** 圆角半径 (px) — 胶囊形时 = heightPx / 2 */
    val cornerRadiusPx: Float,
)

/**
 * 完整布局计算结果
 */
data class ComputedLayout(
    val keys: List<PlacedKey>,
    val totalWidthPx: Float,
    val totalHeightPx: Float,
    /** 行高度列表 (每行 y + height) */
    val rowBounds: List<Pair<Float, Float>>,
    /** 可供滑行检测的 keyChar → Rect 映射 */
    val charRectMap: Map<Char, Rect>,
)

/**
 * 布局参数
 */
data class LayoutParams(
    val widthPx: Float,
    val keyHeightDp: Dp = 48.dp,
    val spacingDp: Dp = 5.dp,
    val rowSpacingDp: Dp = 5.dp,
    val cornerRadiusDp: Dp = 16.dp,
    val horizontalPaddingPx: Float = 0f,
    val oneHandScale: Float = 1f,
)

// ═══════════════════════════════════════════════════════════════
// 4 个键盘模式的行列定义
// ═══════════════════════════════════════════════════════════════

/** 拼音/英文 QWERTY 布局 */
fun buildPinyinRows(isShifted: Boolean): List<List<KeyDef>> {
    val alpha = if (isShifted) "QWERTYUIOPASDFGHJKLZXCVBNM" else "qwertyuiopasdfghjklzxcvbnm"
    return listOf(
        // Row 0: 10 keys
        alpha.substring(0, 10).map { ch ->
            KeyDef(
                label = ch.toString(),
                keyChar = ch,
                isAlpha = true,
                longPressAlts = LONG_PRESS_ALTERNATES[ch],
                swipeUpSymbol = SWIPE_UP_SYMBOLS[ch],
            )
        },
        // Row 1: 9 keys
        alpha.substring(10, 19).map { ch ->
            KeyDef(
                label = ch.toString(),
                keyChar = ch,
                isAlpha = true,
                longPressAlts = LONG_PRESS_ALTERNATES[ch],
                swipeUpSymbol = SWIPE_UP_SYMBOLS[ch],
            )
        },
        // Row 2: ⇧ + 7 keys + ⌫
        listOf(
            KeyDef(label = "⇧", widthWeight = 1.2f, longPressAction = true),
        ) + alpha.substring(19, 26).map { ch ->
            KeyDef(
                label = ch.toString(),
                keyChar = ch,
                isAlpha = true,
                longPressAlts = LONG_PRESS_ALTERNATES[ch],
                swipeUpSymbol = SWIPE_UP_SYMBOLS[ch],
            )
        } + listOf(
            KeyDef(label = "⌫", widthWeight = 1.2f, isDelete = true),
        ),
        // Row 3: 底部功能行 (胶囊形)
        listOf(
            KeyDef(label = "符", widthWeight = 0f, isCapsule = true, longPressAction = true),
            KeyDef(label = "中", widthWeight = 0f, isCapsule = true),
            KeyDef(label = "!,", keyChar = ',', widthWeight = 0f, isCapsule = true, longPressAlts = listOf('!')),
            KeyDef(label = "───", widthWeight = 1f, customBgColor = androidx.compose.ui.graphics.Color(0xFF0F1923).copy(alpha = 0.6f)),
            KeyDef(label = "?。", keyChar = '.', widthWeight = 0f, isCapsule = true, longPressAlts = listOf('?')),
            KeyDef(label = "搜索", widthWeight = 0f, isCapsule = true,
                customBgColor = androidx.compose.ui.graphics.Color(0xFF00D4FF),
                customContentColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC)),
        ),
    )
}

/** 符号键盘布局 */
fun buildSymbolRows(): List<List<KeyDef>> = listOf(
    // Row 0: 1-0
    "1234567890".map { ch ->
        KeyDef(label = ch.toString(), keyChar = ch, longPressAlts = LONG_PRESS_ALTERNATES[ch])
    },
    // Row 1: @ # $ % & * - + ( )
    "@#\$%&*-+()".map { ch ->
        KeyDef(label = ch.toString(), keyChar = ch, longPressAlts = LONG_PRESS_ALTERNATES[ch])
    },
    // Row 2: ⇧ + 9 symbols + ⌫
    listOf(KeyDef(label = "⇧", widthWeight = 1f, longPressAction = true)) +
    "!\"':;/ ?.,".filter { it != ' ' }.map { ch ->
        KeyDef(label = ch.toString(), keyChar = ch, longPressAlts = LONG_PRESS_ALTERNATES[ch])
    } +
    listOf(KeyDef(label = "⌫", widthWeight = 1f, isDelete = true)),
    // Bottom: ABC — space — ↵
    listOf(
        KeyDef(label = "ABC", widthWeight = 0f, isCapsule = true),
        KeyDef(label = "───", widthWeight = 1f, customBgColor = androidx.compose.ui.graphics.Color(0xFF0F1923).copy(alpha = 0.6f)),
        KeyDef(label = "↵", widthWeight = 0f, isCapsule = true,
            customBgColor = androidx.compose.ui.graphics.Color(0xFF00D4FF),
            customContentColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC)),
    ),
)

/** 数字键盘布局 */
fun buildNumberRows(): List<List<KeyDef>> = listOf(
    listOf(
        KeyDef(label = "1", keyChar = '1', longPressAlts = LONG_PRESS_ALTERNATES['1']),
        KeyDef(label = "2", keyChar = '2', longPressAlts = LONG_PRESS_ALTERNATES['2']),
        KeyDef(label = "3", keyChar = '3', longPressAlts = LONG_PRESS_ALTERNATES['3']),
    ),
    listOf(
        KeyDef(label = "4", keyChar = '4', longPressAlts = LONG_PRESS_ALTERNATES['4']),
        KeyDef(label = "5", keyChar = '5', longPressAlts = LONG_PRESS_ALTERNATES['5']),
        KeyDef(label = "6", keyChar = '6', longPressAlts = LONG_PRESS_ALTERNATES['6']),
    ),
    listOf(
        KeyDef(label = "7", keyChar = '7', longPressAlts = LONG_PRESS_ALTERNATES['7']),
        KeyDef(label = "8", keyChar = '8', longPressAlts = LONG_PRESS_ALTERNATES['8']),
        KeyDef(label = "9", keyChar = '9', longPressAlts = LONG_PRESS_ALTERNATES['9']),
    ),
    // Row 3: 符 — 0 — ⌫
    listOf(
        KeyDef(label = "符", longPressAction = true),
        KeyDef(label = "0", keyChar = '0', longPressAlts = LONG_PRESS_ALTERNATES['0']),
        KeyDef(label = "⌫", isDelete = true),
    ),
    // Bottom: ABC — space
    listOf(
        KeyDef(label = "ABC", widthWeight = 0f, isCapsule = true),
        KeyDef(label = "───", widthWeight = 1f, customBgColor = androidx.compose.ui.graphics.Color(0xFF0F1923).copy(alpha = 0.6f)),
    ),
)

/** 五笔键盘布局 (QWERTY 3行 + 底部功能行，字母键带字根提示) */
fun buildWubiRows(isShifted: Boolean, rootHintProvider: (Char) -> String?): List<List<KeyDef>> {
    val alpha = if (isShifted) "QWERTYUIOPASDFGHJKLZXCVBNM" else "qwertyuiopasdfghjklzxcvbnm"
    return listOf(
        alpha.substring(0, 10).map { ch ->
            KeyDef(
                label = ch.toString(),
                keyChar = ch,
                isAlpha = true,
                longPressAlts = LONG_PRESS_ALTERNATES[ch],
                swipeUpSymbol = SWIPE_UP_SYMBOLS[ch],
                rootHint = rootHintProvider(ch),
            )
        },
        alpha.substring(10, 19).map { ch ->
            KeyDef(
                label = ch.toString(),
                keyChar = ch,
                isAlpha = true,
                longPressAlts = LONG_PRESS_ALTERNATES[ch],
                swipeUpSymbol = SWIPE_UP_SYMBOLS[ch],
                rootHint = rootHintProvider(ch),
            )
        },
        listOf(
            KeyDef(label = "⇧", widthWeight = 1.2f, longPressAction = true),
        ) + alpha.substring(19, 26).map { ch ->
            KeyDef(
                label = ch.toString(),
                keyChar = ch,
                isAlpha = true,
                longPressAlts = LONG_PRESS_ALTERNATES[ch],
                swipeUpSymbol = SWIPE_UP_SYMBOLS[ch],
                rootHint = rootHintProvider(ch),
            )
        } + listOf(
            KeyDef(label = "⌫", widthWeight = 1.2f, isDelete = true),
        ),
        listOf(
            KeyDef(label = "符", widthWeight = 0f, isCapsule = true, longPressAction = true),
            KeyDef(label = "中", widthWeight = 0f, isCapsule = true),
            KeyDef(label = "!,", keyChar = ',', widthWeight = 0f, isCapsule = true, longPressAlts = listOf('!')),
            KeyDef(label = "───", widthWeight = 1f, customBgColor = androidx.compose.ui.graphics.Color(0xFF0F1923).copy(alpha = 0.6f)),
            KeyDef(label = "?。", keyChar = '.', widthWeight = 0f, isCapsule = true, longPressAlts = listOf('?')),
            KeyDef(label = "搜索", widthWeight = 0f, isCapsule = true,
                customBgColor = androidx.compose.ui.graphics.Color(0xFF00D4FF),
                customContentColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC)),
        ),
    )
}

// ═══════════════════════════════════════════════════════════════
// 核心布局计算函数
// ═══════════════════════════════════════════════════════════════

/**
 * 计算键盘布局：给定宽度和行列定义，输出每个按键的像素坐标。
 *
 * 布局规则:
 * - Row 0 (拼音) 10 键均分完整宽度
 * - Row 1 (拼音) 9 键均分，左右缩进半个键宽
 * - Row 2 (拼音) shift 1.2x + 7 键 1x + delete 1.2x
 * - 字母键行高 keyHeightDp
 * - 胶囊形底部功能行高 40dp
 * - Row 3 中 weightWeight=0f 的胶囊键使用固定宽度:
 *   符/中/En: 44dp, !,和?。: 40dp, 搜索: 56dp
 * - 符号键盘: 顶部三行统一用 keyHeightDp，底部行用 40dp
 */
fun computeLayout(
    params: LayoutParams,
    rows: List<List<KeyDef>>,
    density: Float, // real screen density factor, NOT estimated
): ComputedLayout {
    val scale = params.oneHandScale

    val keyHeightPx = if (params.keyHeightDp.value <= 0f) 48f * density else params.keyHeightDp.value * density * scale
    val spacingPx = params.spacingDp.value * density * scale
    val rowSpacingPx = params.rowSpacingDp.value * density * scale
    val cornerRadiusPx = params.cornerRadiusDp.value * density
    val hPad = params.horizontalPaddingPx

    // 胶囊键固定宽度 (dp → px)
    fun capW(dp: Float): Float = dp * density * scale
    val capsuleWidths = mapOf(
        "符" to capW(44f), "中" to capW(44f), "En" to capW(44f),
        "!," to capW(40f), "?。" to capW(40f), "搜索" to capW(56f),
        "ABC" to capW(48f), "↵" to capW(48f),
    )

    val placed = mutableListOf<PlacedKey>()
    val rowBoundList = mutableListOf<Pair<Float, Float>>()
    val charRectMap = mutableMapOf<Char, Rect>()

    var index = 0
    var currentY = 0f
    val usableWidth = params.widthPx - 2f * hPad

    for ((rowIdx, rowDefs) in rows.withIndex()) {
        val isBottomCapsule = rowDefs.any { it.isCapsule }
        val rowKeyHeight = if (isBottomCapsule) 40f * density * scale else keyHeightPx

        // 计算本行总权重和固定宽度键
        var totalWeight = 0f
        var fixedWidthTotal = 0f
        for (kd in rowDefs) {
            if (kd.isCapsule || kd.widthWeight == 0f) {
                fixedWidthTotal += capsuleWidths[kd.label] ?: (44f * density * scale)
            } else {
                totalWeight += kd.widthWeight
            }
        }

        val flexPool = usableWidth - fixedWidthTotal - spacingPx * (rowDefs.size - 1)
        val weightUnit = if (totalWeight > 0f) flexPool / totalWeight else 0f

        // Row 1 of QWERTY has 9 keys, needs half-key-width indentation on both sides
        val isQwertyRow1 = rowDefs.size == 9 && !isBottomCapsule
        val rowIndent = if (isQwertyRow1) {
            // indent = half a key width (computed from a hypothetical 10-key row)
            (usableWidth - spacingPx * 9f) / 10f / 2f
        } else 0f

        var currentX = hPad + rowIndent

        for (kd in rowDefs) {
            val keyW: Float
            if (kd.isCapsule || kd.widthWeight == 0f) {
                keyW = capsuleWidths[kd.label] ?: (44f * density * scale)
            } else {
                keyW = weightUnit * kd.widthWeight
            }

            val keyH = rowKeyHeight
            val cr = if (kd.isCapsule) keyH / 2f else cornerRadiusPx
            val rect = Rect(currentX, currentY, currentX + keyW, currentY + keyH)

            placed.add(
                PlacedKey(
                    index = index,
                    keyDef = kd,
                    x = currentX,
                    y = currentY,
                    width = keyW,
                    height = keyH,
                    rect = rect,
                    cornerRadiusPx = cr,
                )
            )

            if (kd.keyChar != null) {
                charRectMap[kd.keyChar] = rect
            }

            index++
            currentX += keyW + spacingPx
        }

        rowBoundList.add(currentY to currentY + rowKeyHeight)
        currentY += rowKeyHeight + rowSpacingPx
    }

    return ComputedLayout(
        keys = placed,
        totalWidthPx = params.widthPx,
        totalHeightPx = currentY - rowSpacingPx, // subtract last spacing
        rowBounds = rowBoundList,
        charRectMap = charRectMap,
    )
}

// ═══════════════════════════════════════════════════════════════
// T9 九宫格布局 — 4 行数字键 + 1 行底部功能键
// 统一使用 KeyDef 体系，接入 KeyboardSurface 渲染管线
// ═══════════════════════════════════════════════════════════════

/** T9 键盘布局 */
fun buildT9Rows(): List<List<KeyDef>> {
    // T9 长按替代（数字 → 首字母提示，给用户参考字母映射）
    val t9LongPress: Map<Char, List<Char>> = mapOf(
        '2' to listOf('a'), '3' to listOf('d'), '4' to listOf('g'),
        '5' to listOf('j'), '6' to listOf('m'), '7' to listOf('p'),
        '8' to listOf('t'), '9' to listOf('w'), '0' to listOf(','),
    )
    return listOf(
        // Row 0: 1 2 3
        listOf(
            KeyDef(label = "1", keyChar = '1', isAlpha = true),
            KeyDef(label = "2", keyChar = '2', isAlpha = true, longPressAlts = t9LongPress['2']),
            KeyDef(label = "3", keyChar = '3', isAlpha = true, longPressAlts = t9LongPress['3']),
        ),
        // Row 1: 4 5 6
        listOf(
            KeyDef(label = "4", keyChar = '4', isAlpha = true, longPressAlts = t9LongPress['4']),
            KeyDef(label = "5", keyChar = '5', isAlpha = true, longPressAlts = t9LongPress['5']),
            KeyDef(label = "6", keyChar = '6', isAlpha = true, longPressAlts = t9LongPress['6']),
        ),
        // Row 2: 7 8 9
        listOf(
            KeyDef(label = "7", keyChar = '7', isAlpha = true, longPressAlts = t9LongPress['7']),
            KeyDef(label = "8", keyChar = '8', isAlpha = true, longPressAlts = t9LongPress['8']),
            KeyDef(label = "9", keyChar = '9', isAlpha = true, longPressAlts = t9LongPress['9']),
        ),
        // Row 3: * 0 #
        listOf(
            KeyDef(label = "*", keyChar = '*', isAlpha = true),
            KeyDef(label = "0", keyChar = '0', isAlpha = true, longPressAlts = t9LongPress['0']),
            KeyDef(label = "#", keyChar = '#', isAlpha = true),
        ),
        // Bottom row: 全键(capsule) / 空格 / ↵(capsule)
        listOf(
            KeyDef(label = "全键", widthWeight = 0f, isCapsule = true),
            KeyDef(label = "───", widthWeight = 1f,
                customBgColor = androidx.compose.ui.graphics.Color(0xFF0F1923).copy(alpha = 0.6f)),
            KeyDef(label = "↵", widthWeight = 0f, isCapsule = true,
                customBgColor = androidx.compose.ui.graphics.Color(0xFF00D4FF),
                customContentColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC)),
        ),
    )
}

// ═══════════════════════════════════════════════════════════════
// 方向键布局 (Arrow Keys) — 2 行统一 Canvas 按键
// ═══════════════════════════════════════════════════════════════

/** 方向键 / 文本操作布局 */
fun buildArrowsRows(): List<List<KeyDef>> = listOf(
    // Row 0: ← ↑ → + 复制 + 粘贴
    listOf(
        KeyDef(label = "←", arrowLabel = "←", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFF00D4FF)),
        KeyDef(label = "↑", arrowLabel = "↑", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFF00D4FF)),
        KeyDef(label = "→", arrowLabel = "→", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFF00D4FF)),
        KeyDef(label = "复制", widthWeight = 1f, isCapsule = true,
            customBgColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
        KeyDef(label = "粘贴", widthWeight = 1f, isCapsule = true,
            customBgColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
    ),
    // Row 1: ↓ + Home + End + 剪切 + 全选
    listOf(
        KeyDef(label = "↓", arrowLabel = "↓", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFF00D4FF)),
        KeyDef(label = "|<\n行首", arrowLabel = "|<", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC)),
        KeyDef(label = ">|\n行尾", arrowLabel = ">|", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC)),
        KeyDef(label = "剪切", widthWeight = 1f, isCapsule = true,
            customBgColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
        KeyDef(label = "全选", widthWeight = 1f, isCapsule = true,
            customBgColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
    ),
)

// ═══════════════════════════════════════════════════════════════
// 文本编辑工具布局 (Edit Tool) — 3 行操作网格
// ═══════════════════════════════════════════════════════════════

/** 文本编辑工具面板布局 */
fun buildEditToolRows(): List<List<KeyDef>> = listOf(
    // Row 0: 全选 / 退格 / 复制
    listOf(
        KeyDef(label = "全选", widthWeight = 1f, isCapsule = true,
            customBgColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
        KeyDef(label = "⌫", widthWeight = 1f, isDelete = true),
        KeyDef(label = "复制", widthWeight = 1f, isCapsule = true,
            customBgColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
    ),
    // Row 1: 撤销 / 粘贴 / 剪贴板
    listOf(
        KeyDef(label = "撤销", widthWeight = 1f, isCapsule = true,
            customBgColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
        KeyDef(label = "粘贴", widthWeight = 1f, isCapsule = true,
            customBgColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
        KeyDef(label = "剪贴板", widthWeight = 1f, isCapsule = true,
            customBgColor = androidx.compose.ui.graphics.Color(0xFF1A2332)),
    ),
    // Row 2: ← ↑ → ↓ |< >|
    listOf(
        KeyDef(label = "←", arrowLabel = "←", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFF00D4FF)),
        KeyDef(label = "↑", arrowLabel = "↑", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFF00D4FF)),
        KeyDef(label = "→", arrowLabel = "→", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFF00D4FF)),
        KeyDef(label = "↓", arrowLabel = "↓", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFF00D4FF)),
        KeyDef(label = "|<\n行首", arrowLabel = "|<", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC)),
        KeyDef(label = ">|\n行尾", arrowLabel = ">|", widthWeight = 1f,
            customContentColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC)),
    ),
)

// ═══════════════════════════════════════════════════════════════
// 从 KeyboardViewModel 复用 LONG_PRESS_ALTERNATES 和 SWIPE_UP_SYMBOLS
// (这些定义在 KeyboardViewModel.kt 中，这里引用)
// ═══════════════════════════════════════════════════════════════

// 这些常量实际定义在 KeyboardViewModel.kt，这里声明为 internal 供 KeyLayout 引用
// 避免循环依赖：KeyLayout 不依赖 ViewModel，而是由调用方传入