package com.nuc.omeletteinputmethod.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 赛博朋克深空主题键盘配色方案
 *
 * 参照 doc/新设计文档/02_色彩系统.md
 * 核心：深空黑底 + 霓虹强调色 + 3D 按键渐变
 */
object KeyboardColors {
    // ═══════════════════════════════════════════════
    // 主色调 (Primary Colors)
    // ═══════════════════════════════════════════════
    /** 深空黑 — 主背景色 */
    val DeepSpaceBlack = Color(0xFF0A0E1A)
    /** 赛博蓝 — 主强调色、选中态、链接 */
    val CyberBlue = Color(0xFF00D4FF)
    /** 霓虹紫 — 次强调色、渐变终点 */
    val NeonPurple = Color(0xFF7C3AED)
    /** 电光青 — 成功状态、确认操作 */
    val ElectricCyan = Color(0xFF06FFC8)
    /** 能量橙 — 警告状态、重要提示 */
    val EnergyOrange = Color(0xFFFF6B35)

    // ═══════════════════════════════════════════════
    // 中性色 (Neutral Colors)
    // ═══════════════════════════════════════════════
    /** 深灰-900 — 按键背景、卡片背景 */
    val DarkGray900 = Color(0xFF0F1923)
    /** 深灰-800 — 面板背景、输入框 */
    val DarkGray800 = Color(0xFF1A2332)
    /** 深灰-700 — 分隔线、边框 */
    val DarkGray700 = Color(0xFF2A3545)
    /** 深灰-600 — 禁用状态 */
    val DarkGray600 = Color(0xFF3A4555)
    /** 浅灰-100 — 亮色文字 */
    val LightGray100 = Color(0xFFF8FAFC)
    /** 浅灰-200 — 次要文字 */
    val LightGray200 = Color(0xFFE2E8F0)
    /** 浅灰-300 — 占位符文字 */
    val LightGray300 = Color(0xFF94A3B8)

    // ═══════════════════════════════════════════════
    // 语义色 (Semantic Colors)
    // ═══════════════════════════════════════════════
    /** 成功 */
    val Success = Color(0xFF10B981)
    /** 警告 */
    val Warning = Color(0xFFF59E0B)
    /** 错误 */
    val Error = Color(0xFFEF4444)
    /** 信息 */
    val Info = Color(0xFF3B82F6)

    // ═══════════════════════════════════════════════
    // 文字色 (Text Colors — 语义化别名)
    // ═══════════════════════════════════════════════
    val TextPrimary = LightGray100
    val TextSecondary = LightGray300
    /** 占位符文字 */
    val TextPlaceholder = Color(0xFF64748B)
    /** 禁用文字 */
    val TextDisabled = Color(0xFF475569)
    /** 强调文字 — 链接、选中 */
    val TextAccent = CyberBlue
    /** 错误文字 */
    val TextError = Error

    // ═══════════════════════════════════════════════
    // 背景色 (Background Colors — 语义化别名)
    // ═══════════════════════════════════════════════
    /** 键盘主背景 */
    val Background = DeepSpaceBlack
    /** 面板背景 */
    val PanelBackground = DarkGray800
    /** 卡片背景 */
    val CardBackground = DarkGray900
    /** 按键背景 */
    val KeyBackground = DarkGray800
    /** 输入框背景 */
    val InputBackground = DarkGray900
    /** 候选栏背景（深色半透明，避免闪白） */
    val CandidateBackground = DarkGray800.copy(alpha = 0.6f)
    /** 工具栏背景 */
    val ToolbarBackground = DarkGray900

    // ═══════════════════════════════════════════════
    // 交互色 (Interaction Colors)
    // ═══════════════════════════════════════════════
    /** 按键默认态 */
    val KeyDefault = DarkGray800
    /** 按键悬停态 */
    val KeyHover = DarkGray700
    /** 按键按压态 */
    val KeyPressed = DarkGray900
    /** 按键选中态 */
    val KeySelected = CyberBlue.copy(alpha = 0.2f)
    /** 涟漪效果 */
    val Ripple = CyberBlue.copy(alpha = 0.15f)

    // ═══════════════════════════════════════════════
    // 旧版兼容别名 (保持 KeyboardScreen 现有引用不报错)
    // ═══════════════════════════════════════════════
    @get:JvmName("deprecatedKeyBackground")
    @Deprecated("使用 KeyBackground", ReplaceWith("KeyBackground"))
    val keyBackground = KeyBackground
    @Deprecated("使用 KeyPressed", ReplaceWith("KeyPressed"))
    val KeyBackgroundPressed = KeyPressed
    @get:JvmName("deprecatedTextPrimary")
    @Deprecated("使用 TextPrimary", ReplaceWith("TextPrimary"))
    val textPrimary = TextPrimary
    @get:JvmName("deprecatedTextSecondary")
    @Deprecated("使用 TextSecondary", ReplaceWith("TextSecondary"))
    val textSecondary = TextSecondary
    @Deprecated("使用 CyberBlue", ReplaceWith("CyberBlue"))
    val PrimaryAction = CyberBlue
    @Deprecated("使用 LightGray100", ReplaceWith("LightGray100"))
    val PrimaryActionText = LightGray100
    @Deprecated("使用 TextSecondary", ReplaceWith("TextSecondary"))
    val ToolbarIcon = TextSecondary
    @Deprecated("使用 DarkGray700", ReplaceWith("DarkGray700"))
    val ToolbarIconBackground = DarkGray700
    @Deprecated("使用 DarkGray800", ReplaceWith("DarkGray800"))
    val ToolbarIconBackgroundPressed = DarkGray800
    @Deprecated("使用 Ripple", ReplaceWith("Ripple"))
    val keyShadow = Ripple
    @get:JvmName("deprecatedCandidateBackground")
    @Deprecated("使用 CandidateBackground", ReplaceWith("CandidateBackground"))
    val candidateBackground = CandidateBackground
    @Deprecated("使用 TextPrimary", ReplaceWith("TextPrimary"))
    val CandidateText = TextPrimary
    @Deprecated("使用 KeySelected", ReplaceWith("KeySelected"))
    val CandidateSelected = KeySelected
    @Deprecated("使用 DarkGray700", ReplaceWith("DarkGray700"))
    val Divider = DarkGray700

    // ═══════════════════════════════════════════════
    // 渐变系统 (Gradient System)
    // 参照 02_色彩系统.md §3
    // ═══════════════════════════════════════════════

    /** 霓虹蓝紫渐变 — 用于强调按钮、选中态 */
    val NeonGradient = Brush.horizontalGradient(
        colors = listOf(CyberBlue, NeonPurple)
    )

    /** 电光青蓝渐变 — 用于搜索、确认按钮 */
    val ElectricGradient = Brush.horizontalGradient(
        colors = listOf(ElectricCyan, CyberBlue)
    )

    /** 能量橙红渐变 — 用于警告、重要操作 */
    val EnergyGradient = Brush.horizontalGradient(
        colors = listOf(EnergyOrange, Error)
    )

    /** 深空渐变 — 键盘主背景（垂直） */
    val DeepSpaceGradient = Brush.verticalGradient(
        colors = listOf(DeepSpaceBlack, DarkGray900)
    )

    /** 星云渐变 — 面板背景（径向） */
    val NebulaGradient = Brush.radialGradient(
        colors = listOf(DarkGray800, DeepSpaceBlack)
    )

    /** 普通按键渐变 — 凸起效果：顶部亮 → 底部暗 */
    val KeyNormalGradient = Brush.verticalGradient(
        colors = listOf(DarkGray700, DarkGray800)
    )

    /** 按压按键渐变 — 凹陷效果：顶部暗 → 底部亮 */
    val KeyPressedGradient = Brush.verticalGradient(
        colors = listOf(DarkGray800, DarkGray700)
    )

    /** 功能按键渐变 — 特殊按键（Shift、Delete 等） */
    val KeyFunctionGradient = Brush.verticalGradient(
        colors = listOf(DarkGray600, DarkGray700)
    )

    // ═══════════════════════════════════════════════
    // 透明度系统 (Alpha Constants)
    // 参照 02_色彩系统.md §4
    // ═══════════════════════════════════════════════
    const val ALPHA_OPAQUE = 1.0f
    const val ALPHA_HIGH = 0.87f
    const val ALPHA_MEDIUM = 0.6f
    const val ALPHA_LOW = 0.3f
    const val ALPHA_RIPPLE = 0.15f
    const val ALPHA_GLASS = 0.08f
    const val ALPHA_SUBTLE = 0.04f

    /** 毛玻璃效果色 */
    val GlassEffect = Color.White.copy(alpha = ALPHA_GLASS)

    /** 涟漪效果色 */
    val RippleEffect = CyberBlue.copy(alpha = ALPHA_RIPPLE)

    /** 阴影颜色 */
    val ShadowColor = Color.Black.copy(alpha = ALPHA_LOW)

    // ═══════════════════════════════════════════════
    // 工具栏专用色 (Toolbar Colors)
    // ═══════════════════════════════════════════════
    /** 工具栏活跃图标颜色 */
    val ToolbarActiveIcon = CyberBlue.copy(alpha = 0.7f)
    /** 工具栏分隔线颜色 */
    val ToolbarDivider = DarkGray700.copy(alpha = 0.3f)
}