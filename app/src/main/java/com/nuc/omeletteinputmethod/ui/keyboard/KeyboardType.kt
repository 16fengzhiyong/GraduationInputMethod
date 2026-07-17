package com.nuc.omeletteinputmethod.ui.keyboard

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import com.nuc.omeletteinputmethod.R

/**
 * 键盘类型数据模型 — 定义切换面板中每个键盘选项的完整信息
 *
 * @param typeId       唯一标识符，用于持久化存储和逻辑判断
 * @param displayName  用户可见的显示名称（如"拼音26键"）
 * @param icon         图标（使用 Material Icons Vector，优先于 drawable 资源）
 * @param mode         对应 KeyboardMode 中的目标模式
 * @param enabled      该键盘是否已实现（false 则显示为灰色禁用状态）
 */
data class KeyboardType(
    val typeId: String,
    val displayName: String,
    val icon: ImageVector,
    val mode: KeyboardMode,
    val enabled: Boolean = true,
)

/**
 * 键盘类型注册表 — 面板中展示的所有键盘选项
 *
 * 布局顺序（3x4 网格）：
 *   拼音9键   |  拼音26键  |  手写  |  英文
 *   语音      |  五笔      |  笔画  |  仓颉
 *   注音      |  更多语言  |  (留空)|  (留空)
 */
object KeyboardRegistry {

    // ── typeId 常量（用于 SharedPreferences 存储）──────────────────
    const val ID_PINYIN_26 = "pinyin_26"
    const val ID_PINYIN_9 = "pinyin_9"
    const val ID_HANDWRITE = "handwrite"
    const val ID_ENGLISH = "english"
    const val ID_VOICE = "voice"
    const val ID_WUBI = "wubi"
    const val ID_STROKE = "stroke"
    const val ID_CANGJIE = "cangjie"
    const val ID_BOPOMOFO = "bopomofo"
    const val ID_MORE_LANG = "more_language"

    // ── 所有键盘选项（按面板展示顺序排列）─────────────────────────
    val all: List<KeyboardType> = listOf(
        KeyboardType(
            typeId = ID_PINYIN_9,
            displayName = "拼音9键",
            icon = Icons.Filled.Keyboard,
            mode = KeyboardMode.T9,
            enabled = true,
        ),
        KeyboardType(
            typeId = ID_PINYIN_26,
            displayName = "拼音26键",
            icon = Icons.Filled.Keyboard,
            mode = KeyboardMode.ALPHA,
            enabled = true,
        ),
        KeyboardType(
            typeId = ID_HANDWRITE,
            displayName = "手写",
            icon = Icons.Filled.Draw,
            mode = KeyboardMode.HANDWRITING,
            enabled = true,
        ),
        KeyboardType(
            typeId = ID_ENGLISH,
            displayName = "英文",
            icon = Icons.Filled.Language,
            mode = KeyboardMode.ALPHA,  // 英文模式通过 isEnglishMode 标志区分
            enabled = true,
        ),
        KeyboardType(
            typeId = ID_VOICE,
            displayName = "语音",
            icon = Icons.Filled.KeyboardVoice,
            mode = KeyboardMode.VOICE,
            enabled = true,
        ),
        KeyboardType(
            typeId = ID_WUBI,
            displayName = "五笔",
            icon = Icons.Filled.FontDownload,
            mode = KeyboardMode.WUBI,   // 独立五笔模式
            enabled = true,             // 已实现
        ),
        KeyboardType(
            typeId = ID_STROKE,
            displayName = "笔画",
            icon = Icons.Filled.Edit,
            mode = KeyboardMode.STROKE,
            enabled = true,
        ),
        KeyboardType(
            typeId = ID_CANGJIE,
            displayName = "仓颉",
            icon = Icons.Filled.Translate,
            mode = KeyboardMode.ALPHA,  // 仓颉复用拼音26键布局作为骨架（TODO: 独立实现）
            enabled = false,            // 尚未实现，显示为禁用
        ),
        KeyboardType(
            typeId = ID_BOPOMOFO,
            displayName = "注音",
            icon = Icons.Filled.FontDownload,
            mode = KeyboardMode.ALPHA,  // 注音复用拼音26键布局作为骨架（TODO: 独立实现）
            enabled = false,            // 尚未实现，显示为禁用
        ),
        KeyboardType(
            typeId = ID_MORE_LANG,
            displayName = "更多语言",
            icon = Icons.Filled.MoreHoriz,
            mode = KeyboardMode.ALPHA,  // 点击后跳转语言设置页面
            enabled = true,
        ),
    )

    /** 根据 typeId 查找对应的 KeyboardType */
    fun findById(typeId: String): KeyboardType? = all.find { it.typeId == typeId }

    /** 根据 KeyboardMode 反查对应的 KeyboardType（取第一个匹配的） */
    fun findByMode(mode: KeyboardMode): KeyboardType? = all.find { it.mode == mode }
}
