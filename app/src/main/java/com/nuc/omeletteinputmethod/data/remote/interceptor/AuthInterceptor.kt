package com.nuc.omeletteinputmethod.data.remote.interceptor

import com.nuc.omeletteinputmethod.data.remote.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 认证拦截器 — 自动为请求添加 Authorization Header
 *
 * 从 TokenManager 读取 accessToken，注入到每个请求的 Header 中。
 */
class AuthInterceptor
    @Inject
    constructor(
        private val tokenManager: TokenManager
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val token = tokenManager.getToken()

            // 无 token 或已是认证请求则直接放行
            if (token.isNullOrBlank() || originalRequest.header("Authorization") != null) {
                return chain.proceed(originalRequest)
            }

            val authenticatedRequest =
                originalRequest
                    .newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()

            return chain.proceed(authenticatedRequest)
        }
    }
