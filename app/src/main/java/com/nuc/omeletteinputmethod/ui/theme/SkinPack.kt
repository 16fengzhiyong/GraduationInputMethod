package com.nuc.omeletteinputmethod.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

data class SkinPack(
    val skinId: String,
    val skinName: String,
    val author: String = "OmeletteIME",
    val colors: Map<String, Color>,
    val wallpaperResId: Int = 0
) {
    companion object {
        // 6 内置皮肤
        fun builtinDefault(id: String = "default"): SkinPack = SkinPack(
            skinId = id,
            skinName = "默认",
            colors = mapOf(
                "primary" to Color(0xFF5B7F95),
                "onPrimary" to Color.White,
                "primaryContainer" to Color(0xFFF0F4F8),
                "onPrimaryContainer" to Color(0xFF3D5A6E),
                "secondary" to Color(0xFF8E9AAF),
                "onSecondary" to Color.White,
                "secondaryContainer" to Color(0xFFF8F4F0),
                "onSecondaryContainer" to Color(0xFF5D4E37),
                "tertiary" to Color(0xFFB8A9C6),
                "onTertiary" to Color.White,
                "tertiaryContainer" to Color(0xFFF0F0F6),
                "onTertiaryContainer" to Color(0xFF4A3D5C),
                "background" to Color(0xFFF5F3F0),
                "onBackground" to Color(0xFF1C1B1F),
                "surface" to Color(0xFFFAFAFA),
                "onSurface" to Color(0xFF1C1B1F),
                "surfaceVariant" to Color(0xFFF0F4F8),
                "onSurfaceVariant" to Color(0xFF6B7280),
                "outline" to Color(0xFFD1D5DB),
                "outlineVariant" to Color(0xFFE5E7EB),
                "error" to Color(0xFFDC2626),
                "onError" to Color.White,
                "keyboardBackground" to Color(0xFFECEFF1),
                "keyBackground" to Color.White,
                "keyText" to Color(0xFF263238),
                "pressedKey" to Color(0xFFE1E6EC)
            )
        )

        fun builtinNight(id: String = "night"): SkinPack = SkinPack(
            skinId = id,
            skinName = "暗夜",
            colors = mapOf(
                "primary" to Color(0xFF8BB8D6),
                "onPrimary" to Color(0xFF1A3347),
                "primaryContainer" to Color(0xFF3D5A6E),
                "onPrimaryContainer" to Color(0xFFDCE8F0),
                "secondary" to Color(0xFFA8B2C4),
                "onSecondary" to Color(0xFF2D3340),
                "secondaryContainer" to Color(0xFF4A4F5C),
                "onSecondaryContainer" to Color(0xFFE0E4EC),
                "tertiary" to Color(0xFFC4B8D6),
                "onTertiary" to Color(0xFF332D42),
                "tertiaryContainer" to Color(0xFF5A4F6E),
                "onTertiaryContainer" to Color(0xFFF0ECF8),
                "background" to Color(0xFF1C1B1F),
                "onBackground" to Color(0xFFE5E1E6),
                "surface" to Color(0xFF252528),
                "onSurface" to Color(0xFFE5E1E6),
                "surfaceVariant" to Color(0xFF2C2C30),
                "onSurfaceVariant" to Color(0xFFC5C5C8),
                "outline" to Color(0xFF48484C),
                "outlineVariant" to Color(0xFF38383C),
                "error" to Color(0xFFFFB4AB),
                "onError" to Color(0xFF690005),
                "keyboardBackground" to Color(0xFF1A1A1E),
                "keyBackground" to Color(0xFF2C2C30),
                "keyText" to Color(0xFFE0E0E0),
                "pressedKey" to Color(0xFF4A4A50)
            )
        )

        fun builtinForest(id: String = "forest"): SkinPack = SkinPack(
            skinId = id,
            skinName = "森系",
            colors = mapOf(
                "primary" to Color(0xFF4CAF50),
                "onPrimary" to Color.White,
                "primaryContainer" to Color(0xFFC8E6C9),
                "onPrimaryContainer" to Color(0xFF1B5E20),
                "secondary" to Color(0xFF81C784),
                "onSecondary" to Color.White,
                "secondaryContainer" to Color(0xFFA5D6A7),
                "onSecondaryContainer" to Color(0xFF2E7D32),
                "tertiary" to Color(0xFF8BC34A),
                "onTertiary" to Color.White,
                "tertiaryContainer" to Color(0xFFDCEDC8),
                "onTertiaryContainer" to Color(0xFF33691E),
                "background" to Color(0xFFF1F8E9),
                "onBackground" to Color(0xFF1C1B1F),
                "surface" to Color(0xFFF9FBE7),
                "onSurface" to Color(0xFF1C1B1F),
                "surfaceVariant" to Color(0xFFE8F5E9),
                "onSurfaceVariant" to Color(0xFF4E6A4F),
                "outline" to Color(0xFFA5D6A7),
                "outlineVariant" to Color(0xFFC8E6C9),
                "error" to Color(0xFFDC2626),
                "onError" to Color.White,
                "keyboardBackground" to Color(0xFFE8F5E9),
                "keyBackground" to Color(0xFFF1F8E9),
                "keyText" to Color(0xFF1B5E20),
                "pressedKey" to Color(0xFFC8E6C9)
            )
        )

        fun builtinOcean(id: String = "ocean"): SkinPack = SkinPack(
            skinId = id,
            skinName = "海蓝",
            colors = mapOf(
                "primary" to Color(0xFF2196F3),
                "onPrimary" to Color.White,
                "primaryContainer" to Color(0xFFBBDEFB),
                "onPrimaryContainer" to Color(0xFF0D47A1),
                "secondary" to Color(0xFF64B5F6),
                "onSecondary" to Color.White,
                "secondaryContainer" to Color(0xFF90CAF9),
                "onSecondaryContainer" to Color(0xFF1565C0),
                "tertiary" to Color(0xFF42A5F5),
                "onTertiary" to Color.White,
                "tertiaryContainer" to Color(0xFFB3E5FC),
                "onTertiaryContainer" to Color(0xFF01579B),
                "background" to Color(0xFFE3F2FD),
                "onBackground" to Color(0xFF1C1B1F),
                "surface" to Color(0xFFBBDEFB),
                "onSurface" to Color(0xFF1C1B1F),
                "surfaceVariant" to Color(0xFFE3F2FD),
                "onSurfaceVariant" to Color(0xFF3F729B),
                "outline" to Color(0xFF90CAF9),
                "outlineVariant" to Color(0xFFBBDEFB),
                "error" to Color(0xFFDC2626),
                "onError" to Color.White,
                "keyboardBackground" to Color(0xFFE3F2FD),
                "keyBackground" to Color.White,
                "keyText" to Color(0xFF0D47A1),
                "pressedKey" to Color(0xFFBBDEFB)
            )
        )

        fun builtinWarmOrange(id: String = "warm_orange"): SkinPack = SkinPack(
            skinId = id,
            skinName = "暖橙",
            colors = mapOf(
                "primary" to Color(0xFFFF9800),
                "onPrimary" to Color.White,
                "primaryContainer" to Color(0xFFFFE0B2),
                "onPrimaryContainer" to Color(0xFFE65100),
                "secondary" to Color(0xFFFFB74D),
                "onSecondary" to Color.White,
                "secondaryContainer" to Color(0xFFFFCC80),
                "onSecondaryContainer" to Color(0xFFEF6C00),
                "tertiary" to Color(0xFFFF5722),
                "onTertiary" to Color.White,
                "tertiaryContainer" to Color(0xFFFFCCBC),
                "onTertiaryContainer" to Color(0xFFBF360C),
                "background" to Color(0xFFFFF3E0),
                "onBackground" to Color(0xFF1C1B1F),
                "surface" to Color(0xFFFFF8E1),
                "onSurface" to Color(0xFF1C1B1F),
                "surfaceVariant" to Color(0xFFFFF3E0),
                "onSurfaceVariant" to Color(0xFF7D5B3A),
                "outline" to Color(0xFFFFCC80),
                "outlineVariant" to Color(0xFFFFE0B2),
                "error" to Color(0xFFDC2626),
                "onError" to Color.White,
                "keyboardBackground" to Color(0xFFFFF3E0),
                "keyBackground" to Color.White,
                "keyText" to Color(0xFFE65100),
                "pressedKey" to Color(0xFFFFE0B2)
            )
        )

        fun builtinSakura(id: String = "sakura"): SkinPack = SkinPack(
            skinId = id,
            skinName = "樱花",
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
            )
        )

        val BUILTIN_SKINS = listOf(
            builtinDefault(),
            builtinNight(),
            builtinForest(),
            builtinOcean(),
            builtinWarmOrange(),
            builtinSakura()
        )

        fun deriveFromPrimary(primary: Color, surface: Color = Color(0xFFFAFAFA)): Map<String, Color> {
            val primaryHsl = hslFromColor(primary)
            val primaryContainer = hslToColor(primaryHsl[0], primaryHsl[1] * 0.4f, 0.95f)
            val onPrimaryContainer = hslToColor(primaryHsl[0], primaryHsl[1] * 0.6f, 0.25f)
            val secondary = hslToColor(primaryHsl[0], 0.15f, 0.65f)
            val tertiary = hslToColor((primaryHsl[0] + 60f) % 360f, 0.25f, 0.72f)
            val surfaceVariant = hslToColor(primaryHsl[0], primaryHsl[1] * 0.3f, 0.93f)
            return mapOf(
                "primary" to primary,
                "onPrimary" to Color.White,
                "primaryContainer" to primaryContainer,
                "onPrimaryContainer" to onPrimaryContainer,
                "secondary" to secondary,
                "onSecondary" to Color.White,
                "secondaryContainer" to secondary.copy(alpha = 0.2f),
                "onSecondaryContainer" to secondary.copy(red = secondary.red * 0.4f, green = secondary.green * 0.4f, blue = secondary.blue * 0.4f),
                "tertiary" to tertiary,
                "onTertiary" to Color.White,
                "tertiaryContainer" to tertiary.copy(alpha = 0.2f),
                "onTertiaryContainer" to tertiary.copy(red = tertiary.red * 0.4f, green = tertiary.green * 0.4f, blue = tertiary.blue * 0.4f),
                "background" to Color(0xFFF5F3F0),
                "onBackground" to Color(0xFF1C1B1F),
                "surface" to surface,
                "onSurface" to Color(0xFF1C1B1F),
                "surfaceVariant" to surfaceVariant,
                "onSurfaceVariant" to Color(0xFF6B7280),
                "outline" to Color(0xFFD1D5DB),
                "outlineVariant" to Color(0xFFE5E7EB),
                "error" to Color(0xFFDC2626),
                "onError" to Color.White,
                "keyboardBackground" to surfaceVariant,
                "keyBackground" to Color.White,
                "keyText" to Color(0xFF263238),
                "pressedKey" to surfaceVariant.copy(alpha = 0.7f)
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

    fun toColorScheme(): androidx.compose.material3.ColorScheme {
        val isDark = (colors["surface"]?.let { estimateLuminance(it) < 0.5f } ?: false)
        return if (isDark) {
            androidx.compose.material3.darkColorScheme(
                primary = colors["primary"]!!,
                onPrimary = colors["onPrimary"]!!,
                primaryContainer = colors["primaryContainer"]!!,
                onPrimaryContainer = colors["onPrimaryContainer"]!!,
                secondary = colors["secondary"]!!,
                onSecondary = colors["onSecondary"]!!,
                secondaryContainer = colors["secondaryContainer"]!!,
                onSecondaryContainer = colors["onSecondaryContainer"]!!,
                tertiary = colors["tertiary"]!!,
                onTertiary = colors["onTertiary"]!!,
                tertiaryContainer = colors["tertiaryContainer"]!!,
                onTertiaryContainer = colors["onTertiaryContainer"]!!,
                background = colors["background"]!!,
                onBackground = colors["onBackground"]!!,
                surface = colors["surface"]!!,
                onSurface = colors["onSurface"]!!,
                surfaceVariant = colors["surfaceVariant"]!!,
                onSurfaceVariant = colors["onSurfaceVariant"]!!,
                outline = colors["outline"]!!,
                outlineVariant = colors["outlineVariant"]!!,
                error = colors["error"] ?: Color(0xFFFFB4AB),
                onError = colors["onError"] ?: Color(0xFF690005)
            )
        } else {
            androidx.compose.material3.lightColorScheme(
                primary = colors["primary"]!!,
                onPrimary = colors["onPrimary"]!!,
                primaryContainer = colors["primaryContainer"]!!,
                onPrimaryContainer = colors["onPrimaryContainer"]!!,
                secondary = colors["secondary"]!!,
                onSecondary = colors["onSecondary"]!!,
                secondaryContainer = colors["secondaryContainer"]!!,
                onSecondaryContainer = colors["onSecondaryContainer"]!!,
                tertiary = colors["tertiary"]!!,
                onTertiary = colors["onTertiary"]!!,
                tertiaryContainer = colors["tertiaryContainer"]!!,
                onTertiaryContainer = colors["onTertiaryContainer"]!!,
                background = colors["background"]!!,
                onBackground = colors["onBackground"]!!,
                surface = colors["surface"]!!,
                onSurface = colors["onSurface"]!!,
                surfaceVariant = colors["surfaceVariant"]!!,
                onSurfaceVariant = colors["onSurfaceVariant"]!!,
                outline = colors["outline"]!!,
                outlineVariant = colors["outlineVariant"]!!,
                error = colors["error"] ?: Color(0xFFDC2626),
                onError = colors["onError"] ?: Color.White
            )
        }
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