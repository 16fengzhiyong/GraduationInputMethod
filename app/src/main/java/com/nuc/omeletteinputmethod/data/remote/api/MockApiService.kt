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
import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock API 实现 — 内存中模拟云端服务端
 *
 * - 手机号任意 11 位数字 + 密码 ≥6 位即可注册登录
 * - 验证码固定为 123456
 * - 模拟 300-800ms 网络延迟
 * - 内存维护用户数据和同步数据
 * - 主题商店返回 12 个模拟主题
 */
@Singleton
class MockApiService @Inject constructor() : ApiService {

    // ── 内存数据库 ──

    private val users = ConcurrentHashMap<String, MockUser>() // phone → user
    private val passwords = ConcurrentHashMap<String, String>() // phone → password
    private val syncData = ConcurrentHashMap<String, MockSyncDataStore>() // userId → data
    private val storeThemes = mutableListOf<StoreThemeDto>()

    init {
        initStoreThemes()
    }

    // ── 鉴权 ──

    override suspend fun register(request: RegisterRequest): ApiResponse<LoginResponse> {
        delay((300L..800L).random())
        val phone = request.phone
        if (phone.length != 11 || !phone.all { it.isDigit() }) {
            return ApiResponse(code = 400, message = "手机号格式不正确", data = null)
        }
        if (request.password.length < 6) {
            return ApiResponse(code = 400, message = "密码长度不能少于6位", data = null)
        }
        if (request.verificationCode != MOCK_CODE) {
            return ApiResponse(code = 400, message = "验证码错误", data = null)
        }
        if (users.containsKey(phone)) {
            return ApiResponse(code = 400, message = "该手机号已注册", data = null)
        }

        val userId = UUID.randomUUID().toString()
        val user = MockUser(
            id = userId,
            phone = phone,
            nickname = "用户${phone.takeLast(4)}",
            createdAt = System.currentTimeMillis()
        )
        users[phone] = user
        passwords[phone] = request.password
        syncData[userId] = MockSyncDataStore()

        return ApiResponse(
            code = 200,
            message = "success",
            data = LoginResponse(
                accessToken = generateToken(),
                refreshToken = generateToken(),
                user = user.toDto()
            )
        )
    }

    override suspend fun login(request: LoginRequest): ApiResponse<LoginResponse> {
        delay((300L..800L).random())
        val mockUser = users[request.phone]
            ?: return ApiResponse(code = 400, message = "账号或密码错误", data = null)
        val storedPassword = passwords[request.phone]
        if (storedPassword != null && storedPassword != request.password) {
            return ApiResponse(code = 400, message = "账号或密码错误", data = null)
        } else if (request.password.length < 6) {
            return ApiResponse(code = 400, message = "账号或密码错误", data = null)
        }

        return ApiResponse(
            code = 200,
            message = "success",
            data = LoginResponse(
                accessToken = generateToken(),
                refreshToken = generateToken(),
                user = mockUser.toDto()
            )
        )
    }

    override suspend fun logout(): ApiResponse<Unit> {
        delay(200)
        return ApiResponse(code = 200, message = "success", data = Unit)
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): ApiResponse<TokenPair> {
        delay(200)
        return ApiResponse(
            code = 200,
            message = "success",
            data = TokenPair(
                accessToken = generateToken(),
                refreshToken = generateToken()
            )
        )
    }

    // ── 用户 ──

    override suspend fun getUserProfile(): ApiResponse<UserProfileDto> {
        delay(200)
        val user = users.values.firstOrNull()
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        return ApiResponse(code = 200, message = "success", data = user.toDto())
    }

    override suspend fun updateUserProfile(profile: UserProfileDto): ApiResponse<UserProfileDto> {
        delay(200)
        val existing = users.values.firstOrNull { it.id == profile.id }
            ?: return ApiResponse(code = 404, message = "用户不存在", data = null)
        existing.nickname = profile.nickname
        existing.avatarUrl = profile.avatarUrl
        return ApiResponse(code = 200, message = "success", data = existing.toDto())
    }

    // ── 用户词典 ──

    override suspend fun uploadUserDictionary(items: List<UserDictionarySyncDto>): ApiResponse<SyncResponse> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        items.forEach { item -> store.userDictionary[item.word] = item }
        return ApiResponse(code = 200, message = "success", data = SyncResponse(syncedCount = items.size))
    }

    override suspend fun downloadUserDictionary(): ApiResponse<List<UserDictionarySyncDto>> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        return ApiResponse(code = 200, message = "success", data = store.userDictionary.values.toList())
    }

    // ── 记事本 ──

    override suspend fun uploadNotes(items: List<NoteSyncDto>): ApiResponse<SyncResponse> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        items.forEach { item -> store.notes[item.id] = item }
        return ApiResponse(code = 200, message = "success", data = SyncResponse(syncedCount = items.size))
    }

    override suspend fun downloadNotes(): ApiResponse<List<NoteSyncDto>> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        return ApiResponse(code = 200, message = "success", data = store.notes.values.toList())
    }

    // ── 快捷短语 ──

    override suspend fun uploadShortcuts(items: List<ShortcutSyncDto>): ApiResponse<SyncResponse> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        items.forEach { item -> store.shortcuts[item.id] = item }
        return ApiResponse(code = 200, message = "success", data = SyncResponse(syncedCount = items.size))
    }

    override suspend fun downloadShortcuts(): ApiResponse<List<ShortcutSyncDto>> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        return ApiResponse(code = 200, message = "success", data = store.shortcuts.values.toList())
    }

    // ── 剪贴板 ──

    override suspend fun uploadClipboard(items: List<ClipboardSyncDto>): ApiResponse<SyncResponse> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        items.forEach { item -> store.clipboard[item.id] = item }
        return ApiResponse(code = 200, message = "success", data = SyncResponse(syncedCount = items.size))
    }

    override suspend fun downloadClipboard(): ApiResponse<List<ClipboardSyncDto>> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        return ApiResponse(code = 200, message = "success", data = store.clipboard.values.toList())
    }

    // ── 输入统计 ──

    override suspend fun uploadStats(items: List<InputStatSyncDto>): ApiResponse<SyncResponse> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        items.forEach { item -> store.inputStats[item.date] = item }
        return ApiResponse(code = 200, message = "success", data = SyncResponse(syncedCount = items.size))
    }

    override suspend fun downloadStats(): ApiResponse<List<InputStatSyncDto>> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        return ApiResponse(code = 200, message = "success", data = store.inputStats.values.toList())
    }

    // ── 皮肤包 ──

    override suspend fun uploadSkinPack(skin: SkinPackDto): ApiResponse<SyncResponse> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        store.skinPacks[skin.skinId] = skin
        return ApiResponse(code = 200, message = "success", data = SyncResponse(syncedCount = 1))
    }

    override suspend fun downloadSkinPack(skinId: String): ApiResponse<SkinPackDto> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        return store.skinPacks[skinId]?.let {
            ApiResponse(code = 200, message = "success", data = it)
        } ?: ApiResponse(code = 404, message = "皮肤不存在", data = null)
    }

    // ── 键盘布局 ──

    override suspend fun uploadLayout(layout: KeyboardLayoutDto): ApiResponse<SyncResponse> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        store.layouts[layout.layoutId] = layout
        return ApiResponse(code = 200, message = "success", data = SyncResponse(syncedCount = 1))
    }

    override suspend fun downloadLayout(layoutId: String): ApiResponse<KeyboardLayoutDto> {
        delay(200)
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }
        return store.layouts[layoutId]?.let {
            ApiResponse(code = 200, message = "success", data = it)
        } ?: ApiResponse(code = 404, message = "布局不存在", data = null)
    }

    // ── 主题商店 ──

    override suspend fun fetchStoreThemes(category: String): ApiResponse<List<StoreThemeDto>> {
        delay((300L..600L).random())
        val themes = if (category == "全部") {
            storeThemes
        } else {
            storeThemes.filter { it.category == category }
        }
        return ApiResponse(code = 200, message = "success", data = themes)
    }

    override suspend fun downloadTheme(themeId: String): ApiResponse<SkinPackDto> {
        delay(300)
        val theme = storeThemes.find { it.themeId == themeId }
            ?: return ApiResponse(code = 404, message = "主题不存在", data = null)

        // 根据 previewColors 生成 SkinPackDto
        val colorMap = mutableMapOf<String, String>()
        val colors = theme.previewColors
        if (colors.isNotEmpty()) {
            colorMap["primary"] = colors[0]
            colorMap["onPrimary"] = "#FFFFFF"
            colorMap["background"] = if (colors.size > 1) colors[1] else "#FF0A0E1A"
            colorMap["onBackground"] = "#FFFFFFFF"
            colorMap["surface"] = if (colors.size > 2) colors[2] else "#FF1A1A2E"
            colorMap["onSurface"] = "#FFFFFFFF"
            colorMap["surfaceVariant"] = if (colors.size > 3) colors[3] else "#FF2A2A4E"
            colorMap["keyboardBackground"] = colorMap["background"]!!
            colorMap["keyBackground"] = colorMap["surface"]!!
            colorMap["keyText"] = colorMap["onSurface"]!!
            colorMap["pressedKey"] = colorMap["surfaceVariant"]!!
            colorMap["primaryContainer"] = colors[0] + "33"
            colorMap["onPrimaryContainer"] = colors[0]
        }

        return ApiResponse(
            code = 200,
            message = "success",
            data = SkinPackDto(
                skinId = theme.themeId,
                skinName = theme.themeName,
                author = theme.author,
                description = theme.description,
                colors = colorMap
            )
        )
    }

    // ── 全量同步 ──

    override suspend fun syncAll(data: FullSyncRequest): ApiResponse<FullSyncResponse> {
        delay((400L..800L).random())
        val userId = users.values.firstOrNull()?.id
            ?: return ApiResponse(code = 401, message = "未登录", data = null)
        val store = syncData.getOrPut(userId) { MockSyncDataStore() }

        // 上传：覆盖服务端数据
        data.userDictionary.forEach { store.userDictionary[it.word] = it }
        data.notes.forEach { store.notes[it.id] = it }
        data.shortcuts.forEach { store.shortcuts[it.id] = it }
        data.clipboard.forEach { store.clipboard[it.id] = it }
        data.inputStats.forEach { store.inputStats[it.date] = it }

        // 下载：返回服务端所有数据
        return ApiResponse(
            code = 200,
            message = "success",
            data = FullSyncResponse(
                userDictionary = store.userDictionary.values.toList(),
                notes = store.notes.values.toList(),
                shortcuts = store.shortcuts.values.toList(),
                clipboard = store.clipboard.values.toList(),
                inputStats = store.inputStats.values.toList(),
                serverTime = System.currentTimeMillis()
            )
        )
    }

    // ── 私有辅助 ──

    private fun generateToken(): String = UUID.randomUUID().toString().replace("-", "")

    private data class MockUser(
        val id: String,
        val phone: String,
        var nickname: String? = null,
        var avatarUrl: String? = null,
        val createdAt: Long = System.currentTimeMillis(),
        var lastSyncAt: Long? = null
    ) {
        fun toDto() = UserProfileDto(
            id = id,
            phone = phone,
            nickname = nickname,
            avatarUrl = avatarUrl,
            createdAt = createdAt,
            lastSyncAt = lastSyncAt
        )
    }

    private class MockSyncDataStore {
        val userDictionary = ConcurrentHashMap<String, UserDictionarySyncDto>()
        val notes = ConcurrentHashMap<Long, NoteSyncDto>()
        val shortcuts = ConcurrentHashMap<Long, ShortcutSyncDto>()
        val clipboard = ConcurrentHashMap<Long, ClipboardSyncDto>()
        val inputStats = ConcurrentHashMap<String, InputStatSyncDto>()
        val skinPacks = ConcurrentHashMap<String, SkinPackDto>()
        val layouts = ConcurrentHashMap<String, KeyboardLayoutDto>()
    }

    // ── 主题商店 Mock 数据 ──

    private fun initStoreThemes() {
        storeThemes.addAll(
            listOf(
                StoreThemeDto(
                    themeId = "store_neon_city",
                    themeName = "霓虹都市",
                    author = "CyberPunk",
                    category = "科技",
                    description = "赛博朋克风格，霓虹灯效果",
                    previewColors = listOf("#FF00D4FF", "#FF0A0E1A", "#FF1A1A2E", "#FF7C3AED"),
                    rating = 4.8f,
                    downloadCount = 12580,
                    version = "1.2"
                ),
                StoreThemeDto(
                    themeId = "store_matrix",
                    themeName = "黑客帝国",
                    author = "Neo",
                    category = "科技",
                    description = "绿色字符雨，经典矩阵风格",
                    previewColors = listOf("#FF06FFC8", "#FF0A0E1A", "#FF0A1A0A", "#FF00FF00"),
                    rating = 4.6f,
                    downloadCount = 8920,
                    version = "1.0"
                ),
                StoreThemeDto(
                    themeId = "store_deep_space",
                    themeName = "深空探索",
                    author = "AstroDesigner",
                    category = "科技",
                    description = "星云渐变，宇宙主题",
                    previewColors = listOf("#FF7C3AED", "#FF0A0E1A", "#FF1A1A3E", "#FF3B82F6"),
                    rating = 4.9f,
                    downloadCount = 15230,
                    version = "2.0"
                ),
                StoreThemeDto(
                    themeId = "store_aurora",
                    themeName = "极光之夜",
                    author = "Aurora",
                    category = "自然",
                    description = "北极光渐变，梦幻色彩",
                    previewColors = listOf("#FF10B981", "#FF0A2E1A", "#FF1A3E2E", "#FF06FFC8"),
                    rating = 4.7f,
                    downloadCount = 9870,
                    version = "1.5"
                ),
                StoreThemeDto(
                    themeId = "store_ocean",
                    themeName = "深海奇遇",
                    author = "DeepSea",
                    category = "自然",
                    description = "深海蓝渐变，宁静致远",
                    previewColors = listOf("#FF3B82F6", "#FF0A1A2E", "#FF1A2E4E", "#FF00D4FF"),
                    rating = 4.5f,
                    downloadCount = 7650,
                    version = "1.1"
                ),
                StoreThemeDto(
                    themeId = "store_sakura",
                    themeName = "樱花漫舞",
                    author = "Sakura",
                    category = "自然",
                    description = "粉色樱花主题，温柔治愈",
                    previewColors = listOf("#FFE91E63", "#FFFCE4EC", "#FFFFF0F5", "#FFF8BBD0"),
                    rating = 4.9f,
                    downloadCount = 21340,
                    version = "3.0"
                ),
                StoreThemeDto(
                    themeId = "store_forest",
                    themeName = "森林秘境",
                    author = "ForestElf",
                    category = "自然",
                    description = "绿色森林主题，清新自然",
                    previewColors = listOf("#FF4CAF50", "#FFE8F5E9", "#FFF1F8E9", "#FFA5D6A7"),
                    rating = 4.4f,
                    downloadCount = 6540,
                    version = "1.0"
                ),
                StoreThemeDto(
                    themeId = "store_minimal_white",
                    themeName = "极简白",
                    author = "Minimalist",
                    category = "简约",
                    description = "纯白极简，回归本真",
                    previewColors = listOf("#FF60A5FA", "#FFFFFFFF", "#FFFAFAFA", "#FFE5E7EB"),
                    rating = 4.3f,
                    downloadCount = 18920,
                    version = "2.1"
                ),
                StoreThemeDto(
                    themeId = "store_minimal_dark",
                    themeName = "极简暗黑",
                    author = "DarkMode",
                    category = "简约",
                    description = "纯黑极简，省电护眼",
                    previewColors = listOf("#FF8BB8D6", "#FF1C1B1F", "#FF252528", "#FF48484C"),
                    rating = 4.6f,
                    downloadCount = 14560,
                    version = "1.8"
                ),
                StoreThemeDto(
                    themeId = "store_cute_cat",
                    themeName = "萌宠猫咪",
                    author = "CatLover",
                    category = "卡通",
                    description = "可爱猫咪主题，萌趣十足",
                    previewColors = listOf("#FFFF9800", "#FFFFF3E0", "#FFFFF8E1", "#FFFFE0B2"),
                    rating = 4.7f,
                    downloadCount = 11230,
                    version = "1.3"
                ),
                StoreThemeDto(
                    themeId = "store_rainbow",
                    themeName = "彩虹炫彩",
                    author = "Rainbow",
                    category = "炫酷",
                    description = "七彩渐变，绚丽多彩",
                    previewColors = listOf("#FFFF5722", "#FF6A1B9A", "#FF2196F3", "#FF4CAF50"),
                    rating = 4.2f,
                    downloadCount = 5430,
                    version = "1.0"
                ),
                StoreThemeDto(
                    themeId = "store_galaxy",
                    themeName = "银河漫游",
                    author = "GalaxyWalker",
                    category = "炫酷",
                    description = "银河系主题，星际穿越",
                    previewColors = listOf("#FF3B82F6", "#FF0A0A2E", "#FF1A1A4E", "#FF7C3AED"),
                    rating = 4.8f,
                    downloadCount = 16780,
                    version = "2.2"
                )
            )
        )
    }

    private companion object {
        const val MOCK_CODE = "123456"
    }
}
