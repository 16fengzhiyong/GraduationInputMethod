package com.nuc.omeletteinputmethod.data.remote.dto

// ── 主题商店 DTO ──

data class StoreThemeDto(
    val themeId: String,
    val themeName: String,
    val author: String,
    val authorId: String? = null,
    val category: String = "其他",
    val description: String? = null,
    val previewColors: List<String> = emptyList(),   // 主色预览（十六进制字符串列表）
    val previewUrl: String? = null,                   // 预览图 URL
    val downloadUrl: String? = null,                  // 主题包下载 URL
    val rating: Float = 0f,                           // 评分 0-5
    val downloadCount: Int = 0,                       // 下载量
    val fileSize: Long = 0L,                          // 文件大小（字节）
    val version: String = "1.0",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

// 皮肤包 DTO（下载后解析为 SkinPack）

data class SkinPackDto(
    val skinId: String,
    val skinName: String,
    val author: String = "Unknown",
    val version: String = "1.0",
    val description: String? = null,
    val colors: Map<String, String> = emptyMap()      // key → 十六进制颜色字符串
)

// 键盘布局 DTO

data class KeyboardLayoutDto(
    val layoutId: String,
    val layoutName: String,
    val author: String = "Unknown",
    val layoutType: String = "qwerty",               // qwerty / t9 / one_hand
    val config: String = "{}",                        // JSON 配置字符串
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
