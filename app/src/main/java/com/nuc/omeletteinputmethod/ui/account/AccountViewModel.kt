package com.nuc.omeletteinputmethod.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.data.model.UserEntity
import com.nuc.omeletteinputmethod.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 账号 ViewModel — 管理登录/注册/个人中心状态
 */
@HiltViewModel
class AccountViewModel
    @Inject
        constructor(
        private val authRepository: AuthRepository
    ) : ViewModel() {
        private val _isLoggedIn = MutableStateFlow(authRepository.isLoggedIn)
        val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

        private val _currentUser = MutableStateFlow<UserEntity?>(null)
        val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

        private val _loginSuccess = MutableStateFlow(false)
        val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

        init {
            loadCurrentUser()
        }

        private fun loadCurrentUser() {
            viewModelScope.launch {
                authRepository.currentUser.collect { user ->
                    _currentUser.value = user
                    _isLoggedIn.value = user != null
                }
            }
        }

        /**
         * 登录
         */
        fun login(
            phone: String,
            password: String
        ) {
            viewModelScope.launch {
                _isLoading.value = true
                _errorMessage.value = null
                authRepository.login(phone, password)
                    .onSuccess {
                        _loginSuccess.value = true
                    }
                    .onFailure { e ->
                        _errorMessage.value = e.message ?: "登录失败"
                    }
                _isLoading.value = false
            }
        }

        /**
         * 注册
         */
        fun register(
            phone: String,
            password: String,
            verificationCode: String
        ) {
            viewModelScope.launch {
                _isLoading.value = true
                _errorMessage.value = null
                authRepository.register(phone, password, verificationCode)
                    .onSuccess {
                        _loginSuccess.value = true
                    }
                    .onFailure { e ->
                        _errorMessage.value = e.message ?: "注册失败"
                    }
                _isLoading.value = false
            }
        }

        /**
         * 退出登录
         */
        fun logout() {
            viewModelScope.launch {
                _isLoading.value = true
                authRepository.logout()
                _currentUser.value = null
                _isLoggedIn.value = false
                _isLoading.value = false
            }
        }

        /**
         * 更新昵称
         */
        fun updateNickname(nickname: String) {
            viewModelScope.launch {
                _isLoading.value = true
                authRepository.updateNickname(nickname)
                    .onSuccess { _errorMessage.value = null }
                    .onFailure { e -> _errorMessage.value = e.message }
                _isLoading.value = false
            }
        }

        /** 清除错误消息 */
        fun clearError() {
            _errorMessage.value = null
        }

        /** 重置登录成功标志 */
        fun resetLoginSuccess() {
            _loginSuccess.value = false
        }
    }
