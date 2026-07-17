package com.nuc.omeletteinputmethod.data.remote.api

import com.nuc.omeletteinputmethod.data.remote.dto.ApiResponse
import com.nuc.omeletteinputmethod.data.remote.dto.ClipboardSyncDto
import com.nuc.omeletteinputmethod.data.remote.dto.FullSyncRequest
import com.nuc.omeletteinputmethod.data.remote.dto.FullSyncResponse
import com.nuc.omeletteinputmethod.data.remote.dto.InputStatSyncDto
import com.nuc.omeletteinputmethod.data.remote.dto.KeyboardLayoutDto
import com.nuc.omeletteinputmethod.data.remote.dto.LoginRequest
import com.nuc.omeletteinputmethod.data.remote.dto.LoginResponse
import com.nuc.omeletteinputmethod.data.remote.dto.NoteSyncDto
import com.nuc.omeletteinputmethod.data.remote.dto.RefreshTokenRequest
import com.nuc.omeletteinputmethod.data.remote.dto.RegisterRequest
import com.nuc.omeletteinputmethod.data.remote.dto.ShortcutSyncDto
import com.nuc.omeletteinputmethod.data.remote.dto.SkinPackDto
import com.nuc.omeletteinputmethod.data.remote.dto.StoreThemeDto
import com.nuc.omeletteinputmethod.data.remote.dto.SyncResponse
import com.nuc.omeletteinputmethod.data.remote.dto.TokenPair
import com.nuc.omeletteinputmethod.data.remote.dto.UserDictionarySyncDto
import com.nuc.omeletteinputmethod.data.remote.dto.UserProfileDto

/**
 * 煎蛋输入法云端 API 接口
 *
 * 当前为纯 Kotlin 接口（Mock 模式），由 [MockApiService] 提供实现。
 * 后续切换真实后端时，添加 Retrofit 注解（@GET/@POST 等）并通过 Retrofit 创建代理实例。
 */
interface ApiService {
    // ── 鉴权 ──

    suspend fun register(request: RegisterRequest): ApiResponse<LoginResponse>

    suspend fun login(request: LoginRequest): ApiResponse<LoginResponse>

    suspend fun logout(): ApiResponse<Unit>

    suspend fun refreshToken(request: RefreshTokenRequest): ApiResponse<TokenPair>

    // ── 用户 ──

    suspend fun getUserProfile(): ApiResponse<UserProfileDto>

    suspend fun updateUserProfile(profile: UserProfileDto): ApiResponse<UserProfileDto>

    // ── 用户词典同步 ──

    suspend fun uploadUserDictionary(items: List<UserDictionarySyncDto>): ApiResponse<SyncResponse>

    suspend fun downloadUserDictionary(): ApiResponse<List<UserDictionarySyncDto>>

    // ── 记事本同步 ──

    suspend fun uploadNotes(items: List<NoteSyncDto>): ApiResponse<SyncResponse>

    suspend fun downloadNotes(): ApiResponse<List<NoteSyncDto>>

    // ── 快捷短语同步 ──

    suspend fun uploadShortcuts(items: List<ShortcutSyncDto>): ApiResponse<SyncResponse>

    suspend fun downloadShortcuts(): ApiResponse<List<ShortcutSyncDto>>

    // ── 剪贴板同步 ──

    suspend fun uploadClipboard(items: List<ClipboardSyncDto>): ApiResponse<SyncResponse>

    suspend fun downloadClipboard(): ApiResponse<List<ClipboardSyncDto>>

    // ── 输入统计同步 ──

    suspend fun uploadStats(items: List<InputStatSyncDto>): ApiResponse<SyncResponse>

    suspend fun downloadStats(): ApiResponse<List<InputStatSyncDto>>

    // ── 皮肤包同步 ──

    suspend fun uploadSkinPack(skin: SkinPackDto): ApiResponse<SyncResponse>

    suspend fun downloadSkinPack(skinId: String): ApiResponse<SkinPackDto>

    // ── 键盘布局同步 ──

    suspend fun uploadLayout(layout: KeyboardLayoutDto): ApiResponse<SyncResponse>

    suspend fun downloadLayout(layoutId: String): ApiResponse<KeyboardLayoutDto>

    // ── 主题商店 ──

    suspend fun fetchStoreThemes(category: String = "全部"): ApiResponse<List<StoreThemeDto>>

    suspend fun downloadTheme(themeId: String): ApiResponse<SkinPackDto>

    // ── 全量同步 ──

    suspend fun syncAll(data: FullSyncRequest): ApiResponse<FullSyncResponse>
}
