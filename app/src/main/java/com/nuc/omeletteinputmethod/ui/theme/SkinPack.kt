package com.nuc.omeletteinputmethod.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import kotlin.math.pow

// ═══════════════════════════════════════════════════════════════
// 主题子结构 (对齐 doc/新设计文档/07_主题系统.md)
// ═══════════════════════════════════════════════════════════════

/** 字体配置 */
data class ThemeTypography(
    val primaryFont: FontFamily = FontFamily.Default,
    val secondaryFont: FontFamily = FontFamily.Monospace,
    val keyLabelSize: TextUnit = 20.sp,
    val candidateSize: TextUnit = 16.sp,
    val toolbarSize: TextUnit = 12.sp,
    val fontWeight: FontWeight = FontWeight.SemiBold
)

/** 布局配置 */
data class ThemeLayout(
    val keyHeight: Dp = 48.dp,
    val keySpacing: Dp = 5.dp,
    val rowSpacing: Dp = 5.dp,
    val cornerRadius: Dp = 16.dp,
    val padding: PaddingValues = PaddingValues(horizontal = 5.dp)
)

/** 动效配置 */
data class ThemeAnimation(
    val pressDuration: Int = 100,
    val transitionDuration: Int = 300,
    val enableParticles: Boolean = true,
    val enableGlow: Boolean = true,
    val enableHaptic: Boolean = true
)

/** 按键材质 */
enum class KeyMaterial {
    FLAT,       // 平面
    ELEVATED,   // 凸起
    GLASS,      // 玻璃
    METAL,      // 金属
    NEON        // 霓虹
}

/** 背景材质 */
enum class BackgroundMaterial {
    SOLID,      // 纯色
    GRADIENT,   // 渐变
    BLUR,       // 毛玻璃
    PATTERN     // 图案
}

/** 材质配置 */
data class ThemeMaterial(
    val keyMaterial: KeyMaterial = KeyMaterial.ELEVATED,
    val backgroundMaterial: BackgroundMaterial = BackgroundMaterial.GRADIENT,
    val enableBlur: Boolean = false,
    val enableShadow: Boolean = true
)

// ═══════════════════════════════════════════════════════════════
// SkinPack (对齐设计文档 KeyboardTheme 语义)
// ═══════════════════════════════════════════════════════════════

data class SkinPack(
    val skinId: String,
    val skinName: String,
    val author: String = "OmeletteIME",
    /** 色彩（Map 形式 — 向后兼容） */
    val colors: Map<String, Color>,
    val wallpaperResId: Int = 0,
    /** 字体配置 */
    val typography: ThemeTypography = ThemeTypography(),
    /** 布局配置 */
    val layout: ThemeLayout = ThemeLayout(),
    /** 动效配置 */
    val animation: ThemeAnimation = ThemeAnimation(),
    /** 材质配置 */
    val material: ThemeMaterial = ThemeMaterial()
) {
    companion object {
        // ═══════════════════════════════════════════════
        // 赛博朋克 6 内置皮肤
        // 对齐 doc/新设计文档/07_主题系统.md §3
        // ═══════════════════════════════════════════════

        /** 深空主题（默认） — 深邃宇宙，科技感十足 */
        fun builtinDefault(id: String = "default"): SkinPack = SkinPack(
            skinId = id,
            skinName = "深空",
            author = "Omelette",
            colors = mapOf(
                "primary" to Color(0xFF00D4FF),           // 赛博蓝
                "onPrimary" to Color(0xFFF8FAFC),
                "primaryContainer" to Color(0xFF00D4FF).copy(alpha = 0.2f),
                "onPrimaryContainer" to Color(0xFFF8FAFC),
                "secondary" to Color(0xFF7C3AED),         // 霓虹紫
                "onSecondary" to Color(0xFFF8FAFC),
                "secondaryContainer" to Color(0xFF7C3AED).copy(alpha = 0.2f),
                "onSecondaryContainer" to Color(0xFFF8FAFC),
                "tertiary" to Color(0xFF06FFC8),          // 电光青
                "onTertiary" to Color(0xFF0A0E1A),
                "tertiaryContainer" to Color(0xFF06FFC8).copy(alpha = 0.2f),
                "onTertiaryContainer" to Color(0xFFF8FAFC),
                "background" to Color(0xFF0A0E1A),        // 深空黑
                "onBackground" to Color(0xFFF8FAFC),
                "surface" to Color(0xFF1A2332),
                "onSurface" to Color(0xFFF8FAFC),
                "surfaceVariant" to Color(0xFF2A3545),
                "onSurfaceVariant" to Color(0xFF94A3B8),
                "outline" to Color(0xFF2A3545),
                "outlineVariant" to Color(0xFF1A2332),
                "error" to Color(0xFFEF4444),
                "onError" to Color.White,
                "keyboardBackground" to Color(0xFF0A0E1A),
                "keyBackground" to Color(0xFF1A2332),
                "keyText" to Color(0xFFF8FAFC),
                "pressedKey" to Color(0xFF0F1923)
            ),
            typography = ThemeTypography(
                primaryFont = FontFamily.Default,
                secondaryFont = FontFamily.Monospace,
                keyLabelSize = 20.sp,
                candidateSize = 16.sp,
                toolbarSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            ),
            layout = ThemeLayout(
                keyHeight = 48.dp,
                keySpacing = 5.dp,
                rowSpacing = 5.dp,
                cornerRadius = 16.dp,
                padding = PaddingValues(horizontal = 5.dp)
            ),
            animation = ThemeAnimation(
                pressDuration = 100,
                transitionDuration = 300,
                enableParticles = true,
                enableGlow = true,
                enableHaptic = true
            ),
            material = ThemeMaterial(
                keyMaterial = KeyMaterial.ELEVATED,
                backgroundMaterial = BackgroundMaterial.GRADIENT,
                enableBlur = false,
                enableShadow = true
            )
        )

        /** 霓虹主题 — 霓虹灯风格，绚丽多彩 */
        fun builtinNight(id: String = "night"): SkinPack = SkinPack(
            skinId = id,
            skinName = "霓虹",
            author = "Omelette",
            colors = mapOf(
                "primary" to Color(0xFFFF00FF),           // 品红
                "onPrimary" to Color(0xFF0A0E1A),
                "primaryContainer" to Color(0xFFFF00FF).copy(alpha = 0.2f),
                "onPrimaryContainer" to Color(0xFFF8FAFC),
                "secondary" to Color(0xFF00FF88),         // 荧光绿
                "onSecondary" to Color(0xFF0A0E1A),
                "secondaryContainer" to Color(0xFF00FF88).copy(alpha = 0.2f),
                "onSecondaryContainer" to Color(0xFFF8FAFC),
                "tertiary" to Color(0xFF00FFFF),          // 青色
                "onTertiary" to Color(0xFF0A0E1A),
                "tertiaryContainer" to Color(0xFF00FFFF).copy(alpha = 0.2f),
                "onTertiaryContainer" to Color(0xFFF8FAFC),
                "background" to Color(0xFF1A0A2E),        // 深紫黑
                "onBackground" to Color(0xFFFF00FF),
                "surface" to Color(0xFF2D1B4E),
                "onSurface" to Color(0xFFF8FAFC),
                "surfaceVariant" to Color(0xFF2D1B4E),
                "onSurfaceVariant" to Color(0xFF94A3B8),
                "outline" to Color(0xFF2D1B4E),
                "outlineVariant" to Color(0xFF1A0A2E),
                "error" to Color(0xFFFF0000),
                "onError" to Color.White,
                "keyboardBackground" to Color(0xFF1A0A2E),
                "keyBackground" to Color(0xFF2D1B4E),
                "keyText" to Color(0xFFFF00FF),
                "pressedKey" to Color(0xFF1A0A2E)
            ),
            typography = ThemeTypography(
                primaryFont = FontFamily.Default,
                keyLabelSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            layout = ThemeLayout(
                keyHeight = 48.dp,
                keySpacing = 5.dp,
                rowSpacing = 5.dp,
                cornerRadius = 16.dp
            ),
            animation = ThemeAnimation(
                pressDuration = 100,
                transitionDuration = 300,
                enableParticles = true,
                enableGlow = true,
                enableHaptic = true
            ),
            material = ThemeMaterial(
                keyMaterial = KeyMaterial.NEON,
                backgroundMaterial = BackgroundMaterial.GRADIENT,
                enableShadow = true
            )
        )

        /** 赛博主题 — 赛博朋克风格，未来感十足 */
        fun builtinForest(id: String = "forest"): SkinPack = SkinPack(
            skinId = id,
            skinName = "赛博",
            author = "Omelette",
            colors = mapOf(
                "primary" to Color(0xFF00FFFF),           // 青色
                "onPrimary" to Color(0xFF0A0E1A),
                "primaryContainer" to Color(0xFF00FFFF).copy(alpha = 0.2f),
                "onPrimaryContainer" to Color(0xFFF8FAFC),
                "secondary" to Color(0xFFFF6B35),         // 能量橙
                "onSecondary" to Color(0xFF0A0E1A),
                "secondaryContainer" to Color(0xFFFF6B35).copy(alpha = 0.2f),
                "onSecondaryContainer" to Color(0xFFF8FAFC),
                "tertiary" to Color(0xFF00FF88),
                "onTertiary" to Color(0xFF0A0E1A),
                "tertiaryContainer" to Color(0xFF00FF88).copy(alpha = 0.2f),
                "onTertiaryContainer" to Color(0xFFF8FAFC),
                "background" to Color(0xFF0A1628),
                "onBackground" to Color(0xFF00FFFF),
                "surface" to Color(0xFF162038),
                "onSurface" to Color(0xFFF8FAFC),
                "surfaceVariant" to Color(0xFF162038),
                "onSurfaceVariant" to Color(0xFF94A3B8),
                "outline" to Color(0xFF162038),
                "outlineVariant" to Color(0xFF0A1628),
                "error" to Color(0xFFFF0000),
                "onError" to Color.White,
                "keyboardBackground" to Color(0xFF0A1628),
                "keyBackground" to Color(0xFF162038),
                "keyText" to Color(0xFF00FFFF),
                "pressedKey" to Color(0xFF0A1628)
            ),
            typography = ThemeTypography(
                keyLabelSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            layout = ThemeLayout(keyHeight = 48.dp, keySpacing = 5.dp, rowSpacing = 5.dp, cornerRadius = 16.dp),
            animation = ThemeAnimation(enableParticles = true, enableGlow = true),
            material = ThemeMaterial(keyMaterial = KeyMaterial.ELEVATED, backgroundMaterial = BackgroundMaterial.GRADIENT)
        )

        /** 极光主题 — 极光风格，梦幻绚丽 */
        fun builtinOcean(id: String = "ocean"): SkinPack = SkinPack(
            skinId = id,
            skinName = "极光",
            author = "Omelette",
            colors = mapOf(
                "primary" to Color(0xFF06FFC8),           // 电光青
                "onPrimary" to Color(0xFF0A0E1A),
                "primaryContainer" to Color(0xFF06FFC8).copy(alpha = 0.2f),
                "onPrimaryContainer" to Color(0xFFF8FAFC),
                "secondary" to Color(0xFF7C3AED),         // 霓虹紫
                "onSecondary" to Color(0xFFF8FAFC),
                "secondaryContainer" to Color(0xFF7C3AED).copy(alpha = 0.2f),
                "onSecondaryContainer" to Color(0xFFF8FAFC),
                "tertiary" to Color(0xFF00D4FF),
                "onTertiary" to Color(0xFF0A0E1A),
                "tertiaryContainer" to Color(0xFF00D4FF).copy(alpha = 0.2f),
                "onTertiaryContainer" to Color(0xFFF8FAFC),
                "background" to Color(0xFF0A1A2E),
                "onBackground" to Color(0xFF06FFC8),
                "surface" to Color(0xFF1A2A3E),
                "onSurface" to Color(0xFFF8FAFC),
                "surfaceVariant" to Color(0xFF1A2A3E),
                "onSurfaceVariant" to Color(0xFF94A3B8),
                "outline" to Color(0xFF1A2A3E),
                "outlineVariant" to Color(0xFF0A1A2E),
                "error" to Color(0xFFEF4444),
                "onError" to Color.White,
                "keyboardBackground" to Color(0xFF0A1A2E),
                "keyBackground" to Color(0xFF1A2A3E),
                "keyText" to Color(0xFF06FFC8),
                "pressedKey" to Color(0xFF0A1A2E)
            ),
            typography = ThemeTypography(keyLabelSize = 20.sp, fontWeight = FontWeight.SemiBold),
            layout = ThemeLayout(keyHeight = 48.dp, keySpacing = 5.dp, rowSpacing = 5.dp, cornerRadius = 16.dp),
            animation = ThemeAnimation(enableParticles = true, enableGlow = true),
            material = ThemeMaterial(keyMaterial = KeyMaterial.ELEVATED, backgroundMaterial = BackgroundMaterial.GRADIENT)
        )

        /** 暖橙主题（保留） — 重新调色为赛博补色调 */
        fun builtinWarmOrange(id: String = "warm_orange"): SkinPack = SkinPack(
            skinId = id,
            skinName = "暖橙",
            author = "Omelette",
            colors = mapOf(
                "primary" to Color(0xFFFF6B35),           // 能量橙
                "onPrimary" to Color.White,
                "primaryContainer" to Color(0xFFFF6B35).copy(alpha = 0.2f),
                "onPrimaryContainer" to Color(0xFFF8FAFC),
                "secondary" to Color(0xFFFFB74D),
                "onSecondary" to Color(0xFF0A0E1A),
                "secondaryContainer" to Color(0xFFFFB74D).copy(alpha = 0.2f),
                "onSecondaryContainer" to Color(0xFFF8FAFC),
                "tertiary" to Color(0xFFEF4444),
                "onTertiary" to Color.White,
                "tertiaryContainer" to Color(0xFFEF4444).copy(alpha = 0.2f),
                "onTertiaryContainer" to Color(0xFFF8FAFC),
                "background" to Color(0xFF0F1923),
                "onBackground" to Color(0xFFF8FAFC),
                "surface" to Color(0xFF1A2332),
                "onSurface" to Color(0xFFF8FAFC),
                "surfaceVariant" to Color(0xFF2A3545),
                "onSurfaceVariant" to Color(0xFF94A3B8),
                "outline" to Color(0xFF2A3545),
                "outlineVariant" to Color(0xFF1A2332),
                "error" to Color(0xFFEF4444),
                "onError" to Color.White,
                "keyboardBackground" to Color(0xFF0F1923),
                "keyBackground" to Color(0xFF1A2332),
                "keyText" to Color(0xFFF8FAFC),
                "pressedKey" to Color(0xFF0F1923)
            ),
            typography = ThemeTypography(keyLabelSize = 20.sp),
            layout = ThemeLayout(keyHeight = 48.dp, cornerRadius = 16.dp),
            animation = ThemeAnimation(),
            material = ThemeMaterial(keyMaterial = KeyMaterial.ELEVATED)
        )

        /** 樱花主题（保留） */
        fun builtinSakura(id: String = "sakura"): SkinPack = SkinPack(
            skinId = id,
            skinName = "樱花",
            author = "Omelette",
            colors = mapOf(
                "primary" to Color(0xFFE91E63),
                "onPrimary" to Color.White,
                "primaryContainer" to Color(0xFFF8BBD0),
                "onPrimaryContainer" to Color(0xFF880E4F),
                "secondary" to Color(0xFFF06292),
                "onSecondary" to Color.White,
                "secondaryContainer" to Color(0xFFF48FB1),
                "onSecondaryContainer" to Color(0xFFAD1457),
                "tertiary" to Color(0xFFCE93D8),
                "onTertiary" to Color.White,
                "tertiaryContainer" to Color(0xFFE1BEE7),
                "onTertiaryContainer" to Color(0xFF6A1B9A),
                "background" to Color(0xFFFCE4EC),
                "onBackground" to Color(0xFF1C1B1F),
                "surface" to Color(0xFFFFF0F5),
                "onSurface" to Color(0xFF1C1B1F),
                "surfaceVariant" to Color(0xFFFCE4EC),
                "onSurfaceVariant" to Color(0xFF7D5260),
                "outline" to Color(0xFFF48FB1),
                "outlineVariant" to Color(0xFFF8BBD0),
                "error" to Color(0xFFDC2626),
                "onError" to Color.White,
                "keyboardBackground" to Color(0xFFFCE4EC),
                "keyBackground" to Color.White,
                "keyText" to Color(0xFF880E4F),
                "pressedKey" to Color(0xFFF8BBD0)
            ),
            typography = ThemeTypography(keyLabelSize = 20.sp),
            layout = ThemeLayout(keyHeight = 48.dp, cornerRadius = 16.dp),
            animation = ThemeAnimation(),
            material = ThemeMaterial(keyMaterial = KeyMaterial.ELEVATED, backgroundMaterial = BackgroundMaterial.SOLID)
        )

        val BUILTIN_SKINS = listOf(
            builtinDefault(),
            builtinNight(),
            builtinForest(),
            builtinOcean(),
            builtinWarmOrange(),
            builtinSakura()
        )

        // ═══════════════════════════════════════════════
        // HSL 色彩工具 (deriveFromPrimary / fromJson / toJson)
        // ═══════════════════════════════════════════════

        fun deriveFromPrimary(primary: Color, surface: Color = Color(0xFF1A2332)): Map<String, Color> {
            val primaryHsl = hslFromColor(primary)
            val primaryContainer = hslToColor(primaryHsl[0], primaryHsl[1] * 0.4f, 0.25f)
            val onPrimaryContainer = hslToColor(primaryHsl[0], primaryHsl[1] * 0.2f, 0.9f)
            val secondary = hslToColor(primaryHsl[0], 0.15f, 0.55f)
            val tertiary = hslToColor((primaryHsl[0] + 60f) % 360f, 0.25f, 0.60f)
            val surfaceVariant = hslToColor(primaryHsl[0], primaryHsl[1] * 0.15f, 0.15f)
            return mapOf(
                "primary" to primary,
                "onPrimary" to Color(0xFF0A0E1A),
                "primaryContainer" to primaryContainer,
                "onPrimaryContainer" to onPrimaryContainer,
                "secondary" to secondary,
                "onSecondary" to Color(0xFF0A0E1A),
                "secondaryContainer" to secondary.copy(alpha = 0.2f),
                "onSecondaryContainer" to onPrimaryContainer,
                "tertiary" to tertiary,
                "onTertiary" to Color(0xFF0A0E1A),
                "tertiaryContainer" to tertiary.copy(alpha = 0.2f),
                "onTertiaryContainer" to onPrimaryContainer,
                "background" to Color(0xFF0A0E1A),
                "onBackground" to Color(0xFFF8FAFC),
                "surface" to surface,
                "onSurface" to Color(0xFFF8FAFC),
                "surfaceVariant" to surfaceVariant,
                "onSurfaceVariant" to Color(0xFF94A3B8),
                "outline" to Color(0xFF2A3545),
                "outlineVariant" to Color(0xFF1A2332),
                "error" to Color(0xFFEF4444),
                "onError" to Color.White,
                "keyboardBackground" to Color(0xFF0A0E1A),
                "keyBackground" to Color(0xFF1A2332),
                "keyText" to Color(0xFFF8FAFC),
                "pressedKey" to Color(0xFF0F1923)
            )
        }

        private fun hslFromColor(color: Color): FloatArray {
            val r = color.red
            val g = color.green
            val b = color.blue
            val max = maxOf(r, g, b)
            val min = minOf(r, g, b)
            val l = (max + min) / 2f
            val s: Float
            val h: Float
            if (max == min) { s = 0f; h = 0f }
            else {
                val d = max - min
                s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
                h = when (max) {
                    r -> ((g - b) / d + (if (g < b) 6f else 0f)) * 60f
                    g -> ((b - r) / d + 2f) * 60f
                    else -> ((r - g) / d + 4f) * 60f
                }
            }
            return floatArrayOf(h, s, l)
        }

        private fun hslToColor(h: Float, s: Float, l: Float): Color {
            val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
            val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
            val m = l - c / 2f
            val (r1, g1, b1) = when {
                h < 60f -> Triple(c, x, 0f)
                h < 120f -> Triple(x, c, 0f)
                h < 180f -> Triple(0f, c, x)
                h < 240f -> Triple(0f, x, c)
                h < 300f -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
            return Color((r1 + m).coerceIn(0f, 1f), (g1 + m).coerceIn(0f, 1f), (b1 + m).coerceIn(0f, 1f), 1f)
        }

        fun fromJson(json: String): SkinPack? {
            return try {
                val gson = com.google.gson.Gson()
                val map = gson.fromJson(json, Map::class.java) as? Map<*, *> ?: return null
                val skinId = map["skinId"] as? String ?: "custom"
                val skinName = map["skinName"] as? String ?: "导入皮肤"
                val author = map["author"] as? String ?: "Unknown"
                val colorsRaw = map["colors"] as? Map<*, *> ?: return null
                val colors = mutableMapOf<String, Color>()
                for ((k, v) in colorsRaw) {
                    val hex = v as? String ?: continue
                    colors[k as String] = Color(hex.toLong(16) or 0xFF000000)
                }
                SkinPack(skinId = skinId, skinName = skinName, author = author, colors = colors)
            } catch (_: Exception) {
                null
            }
        }
    }

    // ═══════════════════════════════════════════════
    // 实例方法
    // ═══════════════════════════════════════════════

    fun toColorScheme(): androidx.compose.material3.ColorScheme {
        val isDark = true  // 赛博主题全部为深色
        return androidx.compose.material3.darkColorScheme(
            primary = colors["primary"] ?: Color(0xFF00D4FF),
            onPrimary = colors["onPrimary"] ?: Color.White,
            primaryContainer = colors["primaryContainer"] ?: Color(0xFF00D4FF).copy(alpha = 0.2f),
            onPrimaryContainer = colors["onPrimaryContainer"] ?: Color.White,
            secondary = colors["secondary"] ?: Color(0xFF7C3AED),
            onSecondary = colors["onSecondary"] ?: Color.White,
            secondaryContainer = colors["secondaryContainer"] ?: Color(0xFF7C3AED).copy(alpha = 0.2f),
            onSecondaryContainer = colors["onSecondaryContainer"] ?: Color.White,
            tertiary = colors["tertiary"] ?: Color(0xFF06FFC8),
            onTertiary = colors["onTertiary"] ?: Color.Black,
            tertiaryContainer = colors["tertiaryContainer"] ?: Color(0xFF06FFC8).copy(alpha = 0.2f),
            onTertiaryContainer = colors["onTertiaryContainer"] ?: Color.White,
            background = colors["background"] ?: Color(0xFF0A0E1A),
            onBackground = colors["onBackground"] ?: Color.White,
            surface = colors["surface"] ?: Color(0xFF1A2332),
            onSurface = colors["onSurface"] ?: Color.White,
            surfaceVariant = colors["surfaceVariant"] ?: Color(0xFF2A3545),
            onSurfaceVariant = colors["onSurfaceVariant"] ?: Color(0xFF94A3B8),
            outline = colors["outline"] ?: Color(0xFF2A3545),
            outlineVariant = colors["outlineVariant"] ?: Color(0xFF1A2332),
            error = colors["error"] ?: Color(0xFFEF4444),
            onError = colors["onError"] ?: Color.White
        )
    }

    fun toJson(): String {
        val sb = StringBuilder()
        sb.append("{\"skinId\":\"$skinId\",\"skinName\":\"$skinName\",\"author\":\"$author\",\"colors\":{")
        colors.entries.forEachIndexed { i, (k, v) ->
            sb.append("\"$k\":\"${v.value.toLong().toString(16)}\"")
            if (i < colors.size - 1) sb.append(",")
        }
        sb.append("}}")
        return sb.toString()
    }
}

// ═══════════════════════════════════════════════
// 亮度计算工具
// ═══════════════════════════════════════════════

fun estimateLuminance(color: Color): Float {
    val r = linearize(color.red)
    val g = linearize(color.green)
    val b = linearize(color.blue)
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

private fun linearize(c: Float): Float {
    val v = c.coerceIn(0f, 1f)
    return if (v <= 0.04045f) v / 12.92f else ((v + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
}