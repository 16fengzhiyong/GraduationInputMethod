package com.nuc.omeletteinputmethod.ui.themestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.data.remote.dto.SkinPackDto
import com.nuc.omeletteinputmethod.data.remote.dto.StoreThemeDto
import com.nuc.omeletteinputmethod.data.repository.ThemeStoreRepository
import com.nuc.omeletteinputmethod.ui.theme.SkinPack
import com.nuc.omeletteinputmethod.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主题商店分类
 */
val STORE_CATEGORIES = listOf("全部", "科技", "自然", "卡通", "简约", "炫酷", "其他")

/**
 * 主题商店 ViewModel
 */
@HiltViewModel
class ThemeStoreViewModel
    @Inject
    constructor(
        private val themeStoreRepository: ThemeStoreRepository,
        private val themeManager: ThemeManager
    ) : ViewModel() {
        private val _themes = MutableStateFlow<List<StoreThemeDto>>(emptyList())
        val themes: StateFlow<List<StoreThemeDto>> = _themes.asStateFlow()

        private val _selectedCategory = MutableStateFlow("全部")
        val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val _downloadingThemeId = MutableStateFlow<String?>(null)
        val downloadingThemeId: StateFlow<String?> = _downloadingThemeId.asStateFlow()

        private val _downloadedThemeIds = MutableStateFlow<Set<String>>(emptySet())
        val downloadedThemeIds: StateFlow<Set<String>> = _downloadedThemeIds.asStateFlow()

        /** 已下载主题的 DTO 缓存（用于 applyTheme） */
        private val downloadedThemeDtos = mutableMapOf<String, SkinPackDto>()

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

        private val _snackbarMessage = MutableStateFlow<String?>(null)
        val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

        init {
            fetchThemes()
        }

        /**
         * 获取主题列表
         */
        fun fetchThemes(category: String = _selectedCategory.value) {
            viewModelScope.launch {
                _isLoading.value = true
                _selectedCategory.value = category
                themeStoreRepository.fetchStoreThemes(category)
                    .onSuccess { _themes.value = it }
                    .onFailure { _errorMessage.value = it.message }
                _isLoading.value = false
            }
        }

        /**
         * 下载主题（不自动应用）
         */
        fun downloadTheme(themeId: String) {
            viewModelScope.launch {
                _downloadingThemeId.value = themeId
                themeStoreRepository.downloadTheme(themeId)
                    .onSuccess { dto ->
                        downloadedThemeDtos[dto.skinId] = dto
                        _downloadedThemeIds.value = _downloadedThemeIds.value + themeId
                        _snackbarMessage.value = "「${dto.skinName}」下载成功！点击可应用"
                    }
                    .onFailure { _errorMessage.value = it.message }
                _downloadingThemeId.value = null
            }
        }

        /**
         * 应用已下载的主题
         */
        fun applyTheme(themeId: String) {
            val dto = downloadedThemeDtos[themeId] ?: return
            val skinPack = dto.toSkinPack()
            themeManager.setSkin(skinPack)
            _snackbarMessage.value = "已切换到「${dto.skinName}」主题"
        }

        /** 清除错误 */
        fun clearError() {
            _errorMessage.value = null
        }

        /** 清除 Snackbar 消息 */
        fun clearSnackbar() {
            _snackbarMessage.value = null
        }
    }

/** 将 SkinPackDto 转换为 SkinPack（UI 层模型） */
fun SkinPackDto.toSkinPack(): SkinPack {
    val colorMap = colors.mapValues { (_, hex) ->
        runCatching { parseHexColor(hex) }.getOrDefault(androidx.compose.ui.graphics.Color.Gray)
    }
    return SkinPack(
        skinId = skinId,
        skinName = skinName,
        author = author,
        colors = colorMap
    )
}

private fun parseHexColor(hexString: String): androidx.compose.ui.graphics.Color {
    val hex = hexString.removePrefix("#").removePrefix("0x").removePrefix("0X")
    return androidx.compose.ui.graphics.Color(hex.toLong(16) or 0xFF000000)
}
