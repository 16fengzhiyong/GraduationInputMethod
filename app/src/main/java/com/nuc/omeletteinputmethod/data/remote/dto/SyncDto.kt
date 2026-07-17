package com.nuc.omeletteinputmethod.data.remote.dto

// ── 云同步请求/响应 ──

data class SyncResponse(
    val syncedCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

// ── 同步数据类型 DTO — 字段名与本地 Entity ──

/** 用户词典同步 DTO（对应 UserDictionary 实体） */
data class UserDictionarySyncDto(
    val id: Long = 0,
    val word: String,
    val frequency: Int = 0,
    val lastUsedTimestamp: Long = 0L,
    val pinned: Boolean = false,
    val createdTime: Long = 0L
)

/** 记事本同步 DTO（对应 Note 实体） */
data class NoteSyncDto(
    val id: Long = 0,
    val title: String = "",
    val content: String,
    val timestamp: Long = 0L,
    val updatedAt: Long = 0L
)

/** 快捷短语同步 DTO（对应 ShortcutItem 实体） */
data class ShortcutSyncDto(
    val id: Long = 0,
    val packageName: String = "global",
    val content: String,
    val label: String,
    val category: String = "",
    val sortOrder: Int = 0,
    val updatedAt: Long = 0L
)

/** 剪贴板同步 DTO（对应 ClipboardItem 实体） */
data class ClipboardSyncDto(
    val id: Long = 0,
    val content: String,
    val timestamp: Long = 0L,
    val sourcePkg: String? = null,
    val pinned: Boolean = false,
    val category: String = ""
)

/** 输入统计同步 DTO（对应 InputStat 实体，date 为主键） */
data class InputStatSyncDto(
    val date: String,
    val totalChars: Int = 0,
    val totalWords: Int = 0,
    val uniqueChars: Int = 0,
    val sessionCount: Int = 0,
    val avgSpeed: Float = 0f
)

// ── 全量同步 ──

data class FullSyncRequest(
    val userDictionary: List<UserDictionarySyncDto> = emptyList(),
    val notes: List<NoteSyncDto> = emptyList(),
    val shortcuts: List<ShortcutSyncDto> = emptyList(),
    val clipboard: List<ClipboardSyncDto> = emptyList(),
    val inputStats: List<InputStatSyncDto> = emptyList(),
    val lastSyncAt: Long? = null
)

data class FullSyncResponse(
    val userDictionary: List<UserDictionarySyncDto> = emptyList(),
    val notes: List<NoteSyncDto> = emptyList(),
    val shortcuts: List<ShortcutSyncDto> = emptyList(),
    val clipboard: List<ClipboardSyncDto> = emptyList(),
    val inputStats: List<InputStatSyncDto> = emptyList(),
    val serverTime: Long = System.currentTimeMillis()
)
