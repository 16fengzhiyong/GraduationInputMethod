package com.nuc.omeletteinputmethod.data.repository

import com.nuc.omeletteinputmethod.data.local.UserDao
import com.nuc.omeletteinputmethod.data.model.UserEntity
import com.nuc.omeletteinputmethod.data.remote.TokenManager
import com.nuc.omeletteinputmethod.data.remote.api.ApiService
import com.nuc.omeletteinputmethod.data.remote.dto.LoginRequest
import com.nuc.omeletteinputmethod.data.remote.dto.RefreshTokenRequest
import com.nuc.omeletteinputmethod.data.remote.dto.RegisterRequest
import com.nuc.omeletteinputmethod.data.remote.dto.UserProfileDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证仓库 — 处理注册、登录、退出、Token 刷新
 *
 * 协调 ApiService（云端）和 UserDao（本地缓存），
 * 登录成功后自动保存 Token 和用户信息到本地。
 */
@Singleton
class AuthRepository
    @Inject
    constructor(
        private val apiService: ApiService,
        private val tokenManager: TokenManager,
        private val userDao: UserDao
    ) {
        /** 当前登录用户 Flow（null 表示未登录） */
        val currentUser: Flow<UserEntity?> = userDao.getUserFlow()

        /** 是否已登录 */
        val isLoggedIn: Boolean
            get() = tokenManager.hasToken()

        /**
         * 用户注册
         * @return 成功返回用户信息，失败抛出异常
         */
        suspend fun register(
            phone: String,
            password: String,
            verificationCode: String
        ): Result<UserProfileDto> =
            runCatching {
                val response = apiService.register(RegisterRequest(phone, password, verificationCode))
                if (response.isSuccess && response.data != null) {
                    saveLoginData(response.data)
                    response.data.user
                } else {
                    throw Exception(response.message.ifBlank { "注册失败" })
                }
            }

        /**
         * 用户登录
         * @return 成功返回用户信息，失败抛出异常
         */
        suspend fun login(
            phone: String,
            password: String
        ): Result<UserProfileDto> =
            runCatching {
                val response = apiService.login(LoginRequest(phone, password))
                if (response.isSuccess && response.data != null) {
                    saveLoginData(response.data)
                    response.data.user
                } else {
                    throw Exception(response.message.ifBlank { "登录失败" })
                }
            }

        /**
         * 退出登录 — 清除本地 Token 和用户数据
         */
        suspend fun logout() {
            runCatching { apiService.logout() }
            tokenManager.clearTokens()
            userDao.deleteAll()
        }

        /**
         * 刷新 Token
         * @return 是否刷新成功
         */
        suspend fun refreshToken(): Boolean =
            runCatching {
                val refreshToken = tokenManager.getRefreshToken() ?: return false
                val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
                if (response.isSuccess && response.data != null) {
                    tokenManager.saveTokens(response.data.accessToken, response.data.refreshToken)
                    true
                } else {
                    false
                }
            }.getOrDefault(false)

        /**
         * 获取当前用户信息（本地缓存）
         */
        suspend fun getCurrentUser(): UserEntity? = userDao.getUser()

        /**
         * 更新用户昵称
         */
        suspend fun updateNickname(nickname: String): Result<UserProfileDto> =
            runCatching {
                val user = userDao.getUser() ?: throw Exception("未登录")
                val updated = user.copy(nickname = nickname)
                val response = apiService.updateUserProfile(updated.toDto())
                if (response.isSuccess && response.data != null) {
                    userDao.update(updated)
                    response.data
                } else {
                    throw Exception(response.message.ifBlank { "更新失败" })
                }
            }

        // ── 私有方法 ──

        private suspend fun saveLoginData(data: com.nuc.omeletteinputmethod.data.remote.dto.LoginResponse) {
            tokenManager.saveTokens(data.accessToken, data.refreshToken)
            userDao.insert(data.user.toEntity())
        }

        // ── 映射扩展 ──

        private fun UserProfileDto.toEntity() =
            UserEntity(
                id = id,
                phone = phone,
                nickname = nickname,
                avatarUrl = avatarUrl,
                createdAt = createdAt,
                lastSyncAt = lastSyncAt
            )

        private fun UserEntity.toDto() =
            UserProfileDto(
                id = id,
                phone = phone,
                nickname = nickname,
                avatarUrl = avatarUrl,
                createdAt = createdAt,
                lastSyncAt = lastSyncAt
            )
    }
