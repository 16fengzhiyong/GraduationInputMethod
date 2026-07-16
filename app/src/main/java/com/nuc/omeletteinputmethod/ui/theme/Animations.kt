package com.nuc.omeletteinputmethod.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 赛博朋克主题复用动效规范
 *
 * 参照 doc/新设计文档/05_动效设计.md
 */
object Animations {
    // ═══════════════════════════════════════════════
    // 按键按压缩放 (Spring)
    // ═══════════════════════════════════════════════
    /** 按键弹性回弹：100ms 按压 + 200ms 回弹 */
    val pressScale = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    // ═══════════════════════════════════════════════
    // 透明度动画 (Tween)
    // ═══════════════════════════════════════════════
    /** 快速淡入：200ms */
    val fadeIn = tween<Float>(durationMillis = 200)
    /** 快速淡出：200ms */
    val fadeOut = tween<Float>(durationMillis = 200)
    /** 标准淡入淡出：300ms */
    val fadeStandard = tween<Float>(durationMillis = 300)

    // ═══════════════════════════════════════════════
    // 位移动画 (Spring + Tween)
    // ═══════════════════════════════════════════════
    /** 弹性滑入 (从下方) */
    val slideIn = spring<Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )
    /** 平滑滑出 */
    val slideOut = tween<Dp>(durationMillis = 300)

    // ═══════════════════════════════════════════════
    // 颜色动画
    // ═══════════════════════════════════════════════
    /** 颜色过渡：300ms */
    val colorTransition = tween<androidx.compose.ui.graphics.Color>(
        durationMillis = 300,
        easing = FastOutSlowInEasing,
    )

    // ═══════════════════════════════════════════════
    // 组合动效时长常量
    // ═══════════════════════════════════════════════
    const val MICRO_DURATION = 100   // 微动效（按压）
    const val QUICK_DURATION = 200   // 快速动效（状态切换）
    const val STANDARD_DURATION = 300 // 标准动效（面板展开）
    const val SLOW_DURATION = 500     // 慢速动效（转场）
    const val PARTICLE_DURATION = 1000 // 粒子效果

    /** 候选词依次出现 stagger delay */
    const val CANDIDATE_STAGGER_MS = 50L

    /** 涟漪效果时长 */
    const val RIPPLE_DURATION = 300
}