package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.nuc.omeletteinputmethod.ui.theme.KeyMaterial
import com.nuc.omeletteinputmethod.ui.theme.SkinPack

// ═══════════════════════════════════════════════════════════════
// KeyRenderEngine — 从 SkinPack / KeyMaterial 派生所有绘制参数
// 纯计算，不含任何 Composable
// ═══════════════════════════════════════════════════════════════

/**
 * 按键绘制参数全集 — 供 Canvas.drawBehind 使用
 *
 * 所有值均为像素单位 (px)，由调用方传入 density 完成转换。
 */
data class KeyRenderParams(
    // ── 背景 ──
    val normalBg: Brush,
    val pressedBg: Brush,
    val functionKeyBg: Brush,      // 底部功能键 (空格等)

    // ── 文字 ──
    val textColor: Color,
    val textStyleAlpha: TextStyle,  // 字母键 20sp SemiBold
    val textStyleFunc: TextStyle,   // 功能键 labelLarge
    val textStyleArrow: TextStyle,  // 箭头键 titleMedium Bold
    val swipeUpTextColor: Color,
    val swipeUpTextSize: TextUnit,
    val rootHintColor: Color,
    val rootHintSize: TextUnit,

    // ── 阴影 (仅 ELEVATED / METAL) ──
    val shadowColor: Color,
    val shadowOffsetYPx: Float,

    // ── 高光 (仅 ELEVATED / METAL / GLASS) ──
    val highlightAlpha: Float,
    val highlightHeightPx: Float,

    // ── 边框 ──
    val borderColor: Color,
    val borderWidthPx: Float,

    // ── 圆角 ──
    val cornerRadiusPx: Float,

    // ── 滑行轨迹 ──
    val swipeTrailColor: Color,
    val swipeTrailWidthPx: Float,

    // ── 长按 Popup ──
    val popupBgColor: Color,
    val popupTextColor: Color,
    val popupHoverBgColor: Color,
    val popupItemSizePx: Float,
    val popupOffsetYPx: Float,

    // ── 按压缩放 ──
    val pressScaleMin: Float,
    val pressAnimDurationMs: Int,

    // ── 脏标记 ──
    /** 用于 remember 的 key，皮肤切换时变化 */
    val generation: Long,
)

// ═══════════════════════════════════════════════════════════════
// 默认硬编码配色 (KeyboardColors 等价物，作为皮肤缺失时的 fallback)
// ═══════════════════════════════════════════════════════════════

/** 深空赛博默认配色 */
object DefaultKeyColors {
    val background = Color(0xFF0A0E1A)
    val surface = Color(0xFF1A2332)
    val surfaceVariant = Color(0xFF2A3545)
    val primary = Color(0xFF00D4FF)        // CyberBlue
    val secondary = Color(0xFF7C3AED)      // NeonPurple
    val tertiary = Color(0xFF06FFC8)       // ElectricCyan
    val onSurface = Color(0xFFF8FAFC)      // white text
    val onSurfaceVariant = Color(0xFF94A3B8)
    val darkGray700 = Color(0xFF2A3545)
    val darkGray800 = Color(0xFF1A2332)
    val darkGray900 = Color(0xFF0F1923)
    val pressedKey = Color(0xFF0F1923)
    val error = Color(0xFFEF4444)
}

// ═══════════════════════════════════════════════════════════════
// 核心派生函数
// ═══════════════════════════════════════════════════════════════

/**
 * 从 SkinPack 派生 KeyRenderParams。
 *
 * 优先从 SkinPack.colors map 读取键盘专用键，缺失时用 DefaultKeyColors fallback。
 * 按 KeyMaterial 枚举调整阴影/高光/边框参数。
 *
 * @param skin 当前皮肤
 * @param density 屏幕密度，用于 dp→px 转换
 * @param keyHeightDp 字母键高度 (dp)
 * @param bottomKeyHeightDp 底部功能键高度 (dp)
 */
fun deriveRenderParams(
    skin: SkinPack?,
    density: Float,
    keyHeightDp: Float = 48f,
    bottomKeyHeightDp: Float = 40f,
): KeyRenderParams {
    // 用 skinId 作为 generation，皮肤切换时自动变化
    val generation = skin?.skinId?.hashCode()?.toLong() ?: 0L

    val c = skin?.colors
    val mat = skin?.material?.keyMaterial ?: KeyMaterial.ELEVATED

    // ── 从 SkinPack.colors 取值，缺失回退到 DefaultKeyColors ──
    val primary = c?.get("primary") ?: DefaultKeyColors.primary
    val surface = c?.get("surface") ?: DefaultKeyColors.surface
    val surfaceVari = c?.get("surfaceVariant") ?: DefaultKeyColors.surfaceVariant
    val onSurface = c?.get("onSurface") ?: DefaultKeyColors.onSurface
    val onSurfaceVari = c?.get("onSurfaceVariant") ?: DefaultKeyColors.onSurfaceVariant

    val keyNormalBgColor = c?.get("keyNormalBg") ?: surfaceVari
    val keyPressedBgColor = c?.get("keyPressedBg") ?: DefaultKeyColors.pressedKey
    val keyTextColor = c?.get("keyText") ?: onSurface
    val keyBorderColor = c?.get("keyBorder") ?: primary.copy(alpha = 0.3f)
    val keyShadowColor = c?.get("keyShadow") ?: Color.Black.copy(alpha = 0.30f)
    val keyHighlightAlphaVal = c?.get("keyHighlightAlpha")?.let {
        // keyHighlightAlpha 存储为 Color，取 alpha 分量
        it.alpha
    } ?: 0.15f

    val swipeTrailColor = c?.get("swipeTrail") ?: primary.copy(alpha = 0.6f)
    val popupBgColor = c?.get("popupBg") ?: primary.copy(alpha = 0.25f)
    val popupTextColor = c?.get("popupText") ?: onSurface
    val popupHoverBgColor = c?.get("popupHoverBg") ?: primary.copy(alpha = 0.4f)

    // ── 按 KeyMaterial 计算阴影/高光/边框 ──
    val shadowColor: Color
    val shadowOffsetY: Float
    val highlightAlpha: Float
    val highlightH: Float
    val borderColor: Color
    val borderW: Float
    when (mat) {
        KeyMaterial.FLAT -> {
            shadowColor = Color.Transparent; shadowOffsetY = 0f; highlightAlpha = 0f; highlightH = 0f
            borderColor = Color.Transparent; borderW = 0f
        }
        KeyMaterial.ELEVATED -> {
            shadowColor = keyShadowColor; shadowOffsetY = 4f * density; highlightAlpha = keyHighlightAlphaVal
            highlightH = 8f * density; borderColor = keyBorderColor; borderW = 1f * density
        }
        KeyMaterial.GLASS -> {
            shadowColor = Color.Transparent; shadowOffsetY = 0f; highlightAlpha = 0.25f
            highlightH = 8f * density; borderColor = keyBorderColor; borderW = 1f * density
        }
        KeyMaterial.METAL -> {
            shadowColor = keyShadowColor; shadowOffsetY = 6f * density; highlightAlpha = 0.20f
            highlightH = 10f * density; borderColor = Color.Transparent; borderW = 0f
        }
        KeyMaterial.NEON -> {
            shadowColor = keyShadowColor; shadowOffsetY = 2f * density; highlightAlpha = 0.08f
            highlightH = 6f * density; borderColor = primary.copy(alpha = 0.8f); borderW = 2f * density
        }
    }

    // ── 渐变背景 ──
    // KeyNormalGradient: surfaceVariant → surface (旧 KeyboardColors.KeyNormalGradient 等价)
    val normalBg = Brush.verticalGradient(
        colors = listOf(
            c?.get("keyNormalGradientTop") ?: surfaceVari,
            c?.get("keyNormalGradientBottom") ?: surface,
        )
    )
    // KeyPressedGradient: surface → surfaceVariant (反转)
    val pressedBg = Brush.verticalGradient(
        colors = listOf(
            c?.get("keyPressedGradientTop") ?: surface,
            c?.get("keyPressedGradientBottom") ?: surfaceVari,
        )
    )
    // 功能键背景 (空格等)
    val functionKeyBg = Brush.verticalGradient(
        colors = listOf(keyPressedBgColor, keyPressedBgColor)
    )

    // ── 文字样式 ──
    val alphaTextStyle = TextStyle(
        fontSize = skin?.typography?.keyLabelSize ?: 20.sp,
        fontWeight = skin?.typography?.fontWeight ?: FontWeight.SemiBold,
        letterSpacing = 0.sp,
    )
    val funcTextStyle = TextStyle(
        fontSize = (skin?.typography?.toolbarSize ?: 12.sp) * 1.33f, // ~labelLarge
    )
    val arrowTextStyle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )

    val swUpColor = c?.get("swipeUpText") ?: keyTextColor.copy(alpha = 0.40f)
    val swUpSize = (skin?.typography?.toolbarSize ?: 7.sp) * 0.58f // ~7sp

    val rootHintColor = c?.get("rootHintText") ?: primary.copy(alpha = 0.6f)
    val rootHintSize = (skin?.typography?.toolbarSize ?: 8.sp) * 0.67f // ~8sp

    // ── 圆角 ──
    val cornerRadiusPx = (skin?.layout?.cornerRadius?.value ?: 16f) * density

    // ── Popup 尺寸 ──
    val popupItemSizePx = 32f * density
    val popupOffsetYPx = (keyHeightDp + 8f) * density  // 键上方 8dp + 键高

    // ── 滑行轨迹 ──
    val swipeTrailWidthPx = 8f * density

    // ── 按压动画 ──
    val pressScaleMin = 0.95f
    val pressAnimDurationMs = skin?.animation?.pressDuration ?: 80

    return KeyRenderParams(
        normalBg = normalBg,
        pressedBg = pressedBg,
        functionKeyBg = functionKeyBg,
        textColor = keyTextColor,
        textStyleAlpha = alphaTextStyle,
        textStyleFunc = funcTextStyle,
        textStyleArrow = arrowTextStyle,
        swipeUpTextColor = swUpColor,
        swipeUpTextSize = swUpSize,
        rootHintColor = rootHintColor,
        rootHintSize = rootHintSize,
        shadowColor = shadowColor,
        shadowOffsetYPx = shadowOffsetY,
        highlightAlpha = highlightAlpha,
        highlightHeightPx = highlightH,
        borderColor = borderColor,
        borderWidthPx = borderW,
        cornerRadiusPx = cornerRadiusPx,
        swipeTrailColor = swipeTrailColor,
        swipeTrailWidthPx = swipeTrailWidthPx,
        popupBgColor = popupBgColor,
        popupTextColor = popupTextColor,
        popupHoverBgColor = popupHoverBgColor,
        popupItemSizePx = popupItemSizePx,
        popupOffsetYPx = popupOffsetYPx,
        pressScaleMin = pressScaleMin,
        pressAnimDurationMs = pressAnimDurationMs,
        generation = generation,
    )
}

/**
 * 当无皮肤时 (SkinPack = null) 提供纯默认参数
 */
fun deriveDefaultRenderParams(density: Float): KeyRenderParams =
    deriveRenderParams(skin = null, density = density)