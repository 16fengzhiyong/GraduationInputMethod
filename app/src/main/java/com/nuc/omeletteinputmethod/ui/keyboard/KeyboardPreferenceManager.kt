package com.nuc.omeletteinputmethod.ui.keyboard

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 键盘偏好管理单例 — 负责持久化用户选择的键盘类型
 *
 * 使用 SharedPreferences 存储当前键盘 typeId，面板打开时读取以高亮当前项。
 * 与项目中 ThemeManager / KeyboardViewModel 的 SharedPreferences 模式保持一致。
 */
@Singleton
class KeyboardPreferenceManager
    @Inject
    constructor(
        @ApplicationContext private val appContext: Context,
    ) {
        private val prefs: SharedPreferences =
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 当前选中的键盘 typeId（StateFlow 供 Compose 观察）
        private val _currentTypeId = MutableStateFlow(readSavedTypeId())
        val currentTypeId: StateFlow<String> = _currentTypeId.asStateFlow()

        /**
         * 读取本地存储的当前键盘 typeId
         * 默认值：拼音26键（ALPHA 模式）
         */
        private fun readSavedTypeId(): String {
            return prefs.getString(KEY_CURRENT_TYPE_ID, KeyboardRegistry.ID_PINYIN_26)
                ?: KeyboardRegistry.ID_PINYIN_26
        }

        /**
         * 获取当前选中的键盘类型对象
         */
        fun getCurrentKeyboardType(): KeyboardType {
            val id = _currentTypeId.value
            return KeyboardRegistry.findById(id) ?: KeyboardRegistry.all.first()
        }

        /**
         * 切换键盘类型 — 持久化到本地并更新 StateFlow
         *
         * @param typeId 目标键盘的 typeId
         * @return 是否成功切换（false 表示该键盘未启用或不存在）
         */
        fun switchTo(typeId: String): Boolean {
            val target = KeyboardRegistry.findById(typeId) ?: return false
            if (!target.enabled) return false

            prefs.edit().putString(KEY_CURRENT_TYPE_ID, typeId).apply()
            _currentTypeId.value = typeId
            return true
        }

        /**
         * 根据当前 KeyboardMode 反查并同步 typeId
         * 用于当用户通过其他方式（如 cycleInputMode）切换模式后，同步面板高亮状态
         */
        fun syncFromMode(mode: KeyboardMode) {
            val matched = KeyboardRegistry.findByMode(mode) ?: return
            if (matched.typeId != _currentTypeId.value) {
                prefs.edit().putString(KEY_CURRENT_TYPE_ID, matched.typeId).apply()
                _currentTypeId.value = matched.typeId
            }
        }

        companion object {
            private const val PREFS_NAME = "omelette_keyboard_prefs"
            private const val KEY_CURRENT_TYPE_ID = "current_keyboard_type_id"
        }
    }
