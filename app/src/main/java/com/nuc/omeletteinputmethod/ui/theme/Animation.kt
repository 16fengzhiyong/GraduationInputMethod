package com.nuc.omeletteinputmethod.ui.theme

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring

object AnimationSpecs {
    // 页面过渡
    val pageTransitionDuration = 300
    val pageTransitionEasing = EaseInOutCubic

    // 列表项出现
    val itemAppearDuration = 400
    val itemAppearEasing = EaseOutCubic
    val itemStaggerDelay = 60

    // 卡片按压反馈
    val cardPressDuration = 150
    val cardPressEasing = FastOutSlowInEasing

    // FAB 展开/收起
    val fabExpandDuration = 350
    val fabExpandEasing = EaseOutBack

    // 弹窗弹出
    val dialogEnterDuration = 300
    val dialogExitDuration = 250
    val dialogEasing = EaseOutQuint

    // 搜索栏展开
    val searchExpandDuration = 300
    val searchExpandEasing = EaseOutCubic

    // 删除动画
    val deleteDuration = 300
    val deleteEasing = EaseInOutCubic

    // 弹簧动画
    val springBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    val springSmooth = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}