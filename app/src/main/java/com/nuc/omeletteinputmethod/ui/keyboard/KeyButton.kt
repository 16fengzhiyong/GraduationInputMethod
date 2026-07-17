package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuc.omeletteinputmethod.ui.theme.KeyboardColors

// ═══════════════════════════════════════════════════════════════
// KeyRenderer — 纯渲染按键 (无事件处理)
// 用于 CandidateBar、T9Keyboard 等仍需要独立 Box 的场景
// ═══════════════════════════════════════════════════════════════

/**
 * 纯渲染按键 — 不含任何 pointerInput。
 * 视觉效果保持与原 KeyButton 一致 (赛博朋克 3D 风格)。
 *
 * @param label 按键文字
 * @param modifier 外层 modifier (weight, size 等)
 * @param width 固定宽度
 * @param height 固定高度
 * @param pressed 是否按压态 (由外层控制)
 * @param isAlphaKey 字母键 → 20sp SemiBold
 * @param capsuleShape 胶囊形 (percent=50)
 * @param customBgColor 自定义背景色覆盖
 * @param customContentColor 自定义文字色覆盖
 * @param swipeUpSymbol 上滑符号 (非按压态在顶部显示)
 */
@Composable
fun KeyRenderer(
    label: String,
    modifier: Modifier = Modifier,
    width: Dp = 30.dp,
    height: Dp = 44.dp,
    pressed: Boolean = false,
    isAlphaKey: Boolean = false,
    capsuleShape: Boolean = false,
    customBgColor: Color? = null,
    customContentColor: Color? = null,
    swipeUpSymbol: Char? = null,
    rootHint: String? = null,
) {
    val density = LocalDensity.current

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 80),
        label = "pressScale",
    )

    val keyBg =
        if (pressed)
            KeyboardColors.KeyPressedGradient
        else
            (customBgColor?.let { Brush.verticalGradient(listOf(it, it)) } ?: KeyboardColors.KeyNormalGradient)

    val contentColor = customContentColor ?: KeyboardColors.TextPrimary
    val borderAlpha = if (pressed) 0.8f else 0.3f
    val highlightAlpha = if (pressed) 0.05f else 0.15f

    val shape = if (capsuleShape)
        RoundedCornerShape(percent = 50)
    else
        RoundedCornerShape(16.dp)

    Box(
        modifier =
            modifier
                .width(width)
                .height(height)
                .keyShadowLegacy(
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
                .keyHighlightLegacy(alpha = highlightAlpha, shapeRadius = density.run { 16.dp.toPx() })
                .neonBorderLegacy(
                    color = KeyboardColors.CyberBlue,
                    width = 1.dp,
                    alpha = borderAlpha,
                    shape = shape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        val textStyle = if (isAlphaKey) {
            MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
            )
        } else {
            MaterialTheme.typography.labelLarge
        }

        Text(
            text = label,
            style = textStyle,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )

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

        if (rootHint != null) {
            Text(
                text = rootHint,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 2.dp, y = 1.dp),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = KeyboardColors.CyberBlue.copy(alpha = 0.6f),
                textAlign = TextAlign.Start,
                maxLines = 1,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 从旧 KeyboardScreen 复用的视觉 modifier (保留不变)
// ═══════════════════════════════════════════════════════════════

private fun Modifier.keyShadowLegacy(
    elevation: Dp = 4.dp,
    shapeRadius: Float = 16f,
    isPressed: Boolean = false,
): Modifier = this.drawBehind {
    if (!isPressed) {
        val elevPx = elevation.toPx()
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.30f),
            topLeft = Offset(0f, elevPx),
            size = size.copy(width = size.width, height = size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(shapeRadius, shapeRadius),
        )
    }
}

private fun Modifier.keyHighlightLegacy(
    alpha: Float = 0.15f,
    shapeRadius: Float = 16f,
): Modifier = this.drawWithContent {
    drawContent()
    if (alpha <= 0f) return@drawWithContent
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
}

private fun Modifier.neonBorderLegacy(
    color: Color = KeyboardColors.CyberBlue,
    width: Dp = 1.dp,
    alpha: Float = 0.3f,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
): Modifier = this.border(width = width, color = color.copy(alpha = alpha), shape = shape)

// ═══════════════════════════════════════════════════════════════
// LongPressPopup — 独立长按替代字符浮层组件
// 从旧 KeyButton 剥离，供需要独立手势的场景使用
// ═══════════════════════════════════════════════════════════════

/**
 * 长按替代字符浮层 — 独立 Composable
 *
 * @param alts 替代字符列表
 * @param hoveredIndex 当前手指 hover 的 item 索引 (-1 = 未命中)
 * @param keyCenterX 按键中心 X 坐标 (px，用于定位 Popup 中心)
 * @param keyTopY 按键顶部 Y 坐标 (px)
 * @param itemSize item 尺寸 (dp)
 */
@Composable
fun LongPressPopup(
    alts: List<Char>,
    hoveredIndex: Int,
    keyCenterX: Float,  // pixel
    keyTopY: Float,     // pixel
    itemSize: Dp = 32.dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val itemWidthPx = with(density) { itemSize.toPx() }
    val spacingPx = with(density) { 3.dp.toPx() }
    val paddingPx = with(density) { 6.dp.toPx() }
    val totalWidthPx = alts.size * itemWidthPx + (alts.size - 1) * spacingPx + 2f * paddingPx
    val popupHeightPx = itemWidthPx + 8f * spacingPx

    // offset 定位: 居中于 keyCenterX，在 keyTopY 上方 8dp
    val offsetX = keyCenterX - totalWidthPx / 2f
    val offsetY = keyTopY - popupHeightPx - with(density) { 8.dp.toPx() }

    Box(
        modifier =
            modifier
                .offset(
                    x = with(density) { (offsetX / density.density).toDp() },
                    y = with(density) { (offsetY / density.density).toDp() },
                )
                .clip(RoundedCornerShape(12.dp))
                .background(KeyboardColors.CyberBlue.copy(alpha = 0.25f))
                .size(
                    width = with(density) { (totalWidthPx / density.density).toDp() },
                    height = with(density) { (popupHeightPx / density.density).toDp() },
                ),
    ) {
        Row(
            modifier = Modifier.padding(
                start = with(density) { (paddingPx / density.density).toDp() },
                top = 4.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(with(density) { (spacingPx / density.density).toDp() }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            alts.forEachIndexed { index, alt ->
                val isHovered = index == hoveredIndex
                Box(
                    modifier =
                        Modifier
                            .size(width = itemSize, height = itemSize)
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