package com.nuc.omeletteinputmethod.data.remote

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token 管理器 — 使用 SharedPreferences 持久化 accessToken / refreshToken
 *
 * 文件: omelette_auth_prefs
 */
@Singleton
class TokenManager
    @Inject
    constructor(
        @ApplicationContext context: Context
    ) {
        private val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        fun saveTokens(
            accessToken: String,
            refreshToken: String
        ) {
            prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putLong(KEY_SAVED_AT, System.currentTimeMillis())
                .apply()
        }

        fun getToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

        fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

        fun getSavedAt(): Long = prefs.getLong(KEY_SAVED_AT, 0L)

        fun hasToken(): Boolean = !getToken().isNullOrBlank()

        fun clearTokens() {
            prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_SAVED_AT)
                .apply()
        }

        private companion object {
            const val PREFS_NAME = "omelette_auth_prefs"
            const val KEY_ACCESS_TOKEN = "access_token"
            const val KEY_REFRESH_TOKEN = "refresh_token"
            const val KEY_SAVED_AT = "saved_at"
        }
    }
