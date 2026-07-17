package com.nuc.omeletteinputmethod.data.remote.dto

// ── 通用 API 响应包装 ──

data class ApiResponse<T>(
    val code: Int = 0,
    val message: String = "",
    val data: T? = null
) {
    val isSuccess: Boolean get() = code == 200
}

// ── 鉴权请求/响应 ──

data class LoginRequest(
    val phone: String,
    val password: String
)

data class RegisterRequest(
    val phone: String,
    val password: String,
    val verificationCode: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserProfileDto
)

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)
