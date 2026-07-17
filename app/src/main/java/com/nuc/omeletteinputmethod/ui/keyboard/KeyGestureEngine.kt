package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.ui.geometry.Offset

// ═══════════════════════════════════════════════════════════════
// KeyGestureEngine — 统一手势状态机
// 纯数据/逻辑，不含 Composable 或 pointerInput
// ═══════════════════════════════════════════════════════════════

/**
 * 手势事件类型 — 从 KeyboardSurface 向外传递
 */
sealed class GestureEvent {
    /** 普通点击 */
    data class Tap(val keyDef: KeyDef) : GestureEvent()
    /** 长按（无替代字符或 longPressAction=true）触发 action */
    data class LongPressAction(val keyDef: KeyDef) : GestureEvent()
    /** 长按替代字符 Popup 选中某个字符 */
    data class LongPressAlt(val baseChar: Char, val selectedAlt: Char) : GestureEvent()
    /** 长按 Popup 显示请求 */
    data class ShowLongPressPopup(val keyIndex: Int, val keyDef: KeyDef) : GestureEvent()
    /** 长按 Popup 消失 */
    object DismissLongPressPopup : GestureEvent()
    /** 退格连删 */
    object DeleteRepeat : GestureEvent()
    /** 上滑符号 */
    data class SwipeUp(val keyDef: KeyDef) : GestureEvent()
    /** 滑行输入完成 */
    data class SwipeInput(val chars: List<Char>) : GestureEvent()
    /** 按压状态变化 (用于触发 Canvas 重绘) */
    data class PressChanged(val pressedIndex: Int) : GestureEvent()
    /** 滑行高亮变化 */
    data class SwipeHighlightChanged(val highlightedIndices: Set<Int>) : GestureEvent()
    /** 滑行轨迹点更新 */
    data class SwipePathPoint(val point: Offset) : GestureEvent()
}

/**
 * 手势状态机内部状态
 */
data class GestureState(
    /** 当前按下的键 index (-1 = 无) */
    val pressedIndex: Int = -1,
    /** 是否处于滑行模式 */
    val isSwipeMode: Boolean = false,
    /** 滑行轨迹点 */
    val swipePath: MutableList<Offset> = mutableListOf(),
    /** 滑行经过的字符 */
    val traversedKeys: MutableList<Char> = mutableListOf(),
    /** 当前高亮键索引 */
    val highlightedIndices: MutableSet<Int> = mutableSetOf(),
    /** 长按是否已触发 */
    val longPressTriggered: Boolean = false,
    /** 上滑是否已触发 */
    val swipeUpTriggered: Boolean = false,
    /** 长按 Popup 是否可见 */
    val popupVisible: Boolean = false,
    /** 长按 Popup 当前 hover 的替代字符索引 (-1 = 无) */
    val popupHoveredIndex: Int = -1,
    /** 手指按下时的初始位置 */
    val downPos: Offset = Offset.Zero,
    /** 手指按下时间 (System.currentTimeMillis) */
    val downTimeMs: Long = 0L,
)

/**
 * 重置手势状态
 */
fun GestureState.reset(): GestureState = GestureState()

/**
 * 根据手指位置命中检测 → 找到对应的 PlacedKey index
 * 遍历 layout.keys，rect.contains(point)
 * 返回 key index，无命中返回 -1
 */
fun hitTest(point: Offset, layout: ComputedLayout): Int {
    // 倒序遍历（后画的在上层，但这里所有键不重叠，顺序无所谓）
    for (pk in layout.keys) {
        if (pk.rect.contains(point)) {
            return pk.index
        }
    }
    return -1
}

/**
 * 滑行高亮: 找到当前手指位置命中键和所有已遍历键，合并为高亮集合
 */
fun updateSwipeHighlight(
    currentIndex: Int,
    highlightedIndices: MutableSet<Int>,
) {
    if (currentIndex >= 0) {
        highlightedIndices.add(currentIndex)
    }
}

/**
 * 长按 Popup hover 检测: 根据手指 X 相对于 Popup 左侧的位置计算 hover 的替代字符索引
 *
 * @param fingerX 当前手指 X 坐标 (px)
 * @param downX 手指按下时的 X 坐标 (px，作为 Popup 中心)
 * @param altCount 替代字符个数
 * @param itemWidthPx 每个替代字符 item 宽度 (px)
 * @param spacingPx item 间距 (px)
 * @param paddingPx Popup 内边距 (px)
 * @return hoveredIndex, -1 表示未命中任何 item
 */
fun computePopupHoverIndex(
    fingerX: Float,
    downX: Float,
    altCount: Int,
    itemWidthPx: Float,
    spacingPx: Float,
    paddingPx: Float,
): Int {
    val totalWidth = altCount * itemWidthPx + (altCount - 1) * spacingPx
    val popupLeft = downX - totalWidth / 2f - paddingPx
    val relX = fingerX - popupLeft
    val idx = ((relX - paddingPx) / (itemWidthPx + spacingPx)).toInt()
    return if (idx in 0 until altCount) idx else -1
}

/**
 * 检测上滑: dy = downPos.y - currentPos.y > thresholdPx
 */
fun detectSwipeUp(downY: Float, currentY: Float, thresholdPx: Float): Boolean {
    return (downY - currentY) > thresholdPx
}

/**
 * 检测手指是否移出原键 (用于判断是否进入滑行模式)
 */
fun detectExitKey(point: Offset, keyRect: androidx.compose.ui.geometry.Rect): Boolean {
    return !keyRect.contains(point)
}

/**
 * 长按超时检测
 * @param isDeleteKey 退格键使用更短超时 (150ms vs 350ms)
 */
fun isLongPressTimeout(downTimeMs: Long, currentTimeMs: Long, isDeleteKey: Boolean): Boolean {
    val timeout = if (isDeleteKey) 150L else 350L
    return (currentTimeMs - downTimeMs) >= timeout
}