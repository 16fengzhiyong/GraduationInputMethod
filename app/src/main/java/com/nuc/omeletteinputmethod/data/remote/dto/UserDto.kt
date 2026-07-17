package com.nuc.omeletteinputmethod.data.remote.dto

// ── 用户信息 ──

data class UserProfileDto(
    val id: String,
    val phone: String,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Long = 0L,
    val lastSyncAt: Long? = null
)
