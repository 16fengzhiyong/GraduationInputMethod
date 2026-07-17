package com.nuc.omeletteinputmethod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户实体 — 本地缓存的当前登录用户信息
 *
 * 与云端 UserProfileDto 对应，仅存储当前登录用户（单设备单账号）。
 */
@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val phone: String,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Long = 0L,
    val lastSyncAt: Long? = null
)
