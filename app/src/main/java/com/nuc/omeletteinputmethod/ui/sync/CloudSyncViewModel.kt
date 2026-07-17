package com.nuc.omeletteinputmethod.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.data.repository.CloudSyncRepository
import com.nuc.omeletteinputmethod.data.repository.SyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 同步状态枚举
 */
enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

/**
 * 云同步 ViewModel — 管理同步状态和进度
 */
@HiltViewModel
class CloudSyncViewModel
    @Inject
    constructor(
        private val cloudSyncRepository: CloudSyncRepository
    ) : ViewModel() {
        private val _syncState = MutableStateFlow(SyncState.IDLE)
        val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

        private val _lastSyncTime = MutableStateFlow<Long?>(null)
        val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

        private val _syncResult = MutableStateFlow<SyncResult?>(null)
        val syncResult: StateFlow<SyncResult?> = _syncResult.asStateFlow()

        private val _syncMessage = MutableStateFlow<String?>(null)
        val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

        /**
         * 全量同步
         */
        fun syncAll() {
            viewModelScope.launch {
                _syncState.value = SyncState.SYNCING
                _syncMessage.value = "正在同步..."
                val result = cloudSyncRepository.syncAll()
                _syncResult.value = result
                if (result.success) {
                    _syncState.value = SyncState.SUCCESS
                    _lastSyncTime.value = System.currentTimeMillis()
                    _syncMessage.value = "同步成功：上传 ${result.uploaded} 条，下载 ${result.downloaded} 条"
                } else {
                    _syncState.value = SyncState.ERROR
                    _syncMessage.value = result.errorMessage ?: "同步失败"
                }
            }
        }

        /**
         * 仅上传
         */
        fun uploadOnly() {
            viewModelScope.launch {
                _syncState.value = SyncState.SYNCING
                _syncMessage.value = "正在上传..."
                val result = cloudSyncRepository.uploadOnly()
                _syncResult.value = result
                if (result.success) {
                    _syncState.value = SyncState.SUCCESS
                    _syncMessage.value = "上传成功：${result.uploaded} 条数据"
                } else {
                    _syncState.value = SyncState.ERROR
                    _syncMessage.value = result.errorMessage ?: "上传失败"
                }
            }
        }

        /**
         * 仅下载
         */
        fun downloadOnly() {
            viewModelScope.launch {
                _syncState.value = SyncState.SYNCING
                _syncMessage.value = "正在下载..."
                val result = cloudSyncRepository.downloadOnly()
                _syncResult.value = result
                if (result.success) {
                    _syncState.value = SyncState.SUCCESS
                    _lastSyncTime.value = System.currentTimeMillis()
                    _syncMessage.value = "下载成功：${result.downloaded} 条数据"
                } else {
                    _syncState.value = SyncState.ERROR
                    _syncMessage.value = result.errorMessage ?: "下载失败"
                }
            }
        }

        /** 重置状态 */
        fun resetState() {
            _syncState.value = SyncState.IDLE
            _syncMessage.value = null
        }
    }
