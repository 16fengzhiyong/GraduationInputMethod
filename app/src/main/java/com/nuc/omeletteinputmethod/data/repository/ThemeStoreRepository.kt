package com.nuc.omeletteinputmethod.data.repository

import com.nuc.omeletteinputmethod.data.remote.api.ApiService
import com.nuc.omeletteinputmethod.data.remote.dto.SkinPackDto
import com.nuc.omeletteinputmethod.data.remote.dto.StoreThemeDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 主题商店仓库 — 处理远程主题获取和下载
 */
@Singleton
class ThemeStoreRepository
    @Inject
    constructor(
        private val apiService: ApiService
    ) {
        /**
         * 获取主题商店列表
         * @param category 分类筛选（"全部" 表示不筛选）
         */
        suspend fun fetchStoreThemes(category: String = "全部"): Result<List<StoreThemeDto>> =
            runCatching {
                val response = apiService.fetchStoreThemes(category)
                if (response.isSuccess && response.data != null) {
                    response.data
                } else {
                    throw Exception(response.message.ifBlank { "获取主题列表失败" })
                }
            }

        /**
         * 下载指定主题
         * @param themeId 主题 ID
         * @return 下载的皮肤包 DTO
         */
        suspend fun downloadTheme(themeId: String): Result<SkinPackDto> =
            runCatching {
                val response = apiService.downloadTheme(themeId)
                if (response.isSuccess && response.data != null) {
                    response.data
                } else {
                    throw Exception(response.message.ifBlank { "下载主题失败" })
                }
            }
    }
