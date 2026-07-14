package com.nuc.omeletteinputmethod.ui.translate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.data.repository.TranslateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateViewModel @Inject constructor(
    private val repository: TranslateRepository
) : ViewModel() {

    private val _translationResult = MutableStateFlow("")
    val translationResult: StateFlow<String> = _translationResult.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun translate(content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.translate(content)
            _translationResult.value = result
            _isLoading.value = false
        }
    }
}
