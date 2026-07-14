package com.nuc.omeletteinputmethod.ui.clipboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.data.model.ClipboardItem
import com.nuc.omeletteinputmethod.data.repository.ClipboardRepository
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClipboardViewModel
    @Inject
    constructor(
        private val clipboardRepository: ClipboardRepository,
    ) : ViewModel() {
        private val _items = MutableStateFlow<List<ClipboardItem>>(emptyList())
        val items: StateFlow<List<ClipboardItem>> = _items.asStateFlow()

        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

        private val _effects = MutableSharedFlow<KeyboardEffect>()
        val effects: SharedFlow<KeyboardEffect> = _effects.asSharedFlow()

        private val _maxItems = MutableStateFlow(clipboardRepository.getMaxItems())
        val maxItems: StateFlow<Int> = _maxItems.asStateFlow()

        init {
            viewModelScope.launch {
                clipboardRepository.getAllPinnedFirst().collect { list ->
                    _items.value = list
                }
            }
        }

        fun search(query: String) {
            _searchQuery.value = query
            viewModelScope.launch {
                clipboardRepository.searchContent(query).collect { list ->
                    _items.value = list
                }
            }
        }

        fun clearSearch() {
            _searchQuery.value = ""
            viewModelScope.launch {
                clipboardRepository.getAllPinnedFirst().collect { list ->
                    _items.value = list
                }
            }
        }

        fun pasteItem(item: ClipboardItem) {
            viewModelScope.launch { _effects.emit(KeyboardEffect.Paste(item.content)) }
        }

        fun pinItem(item: ClipboardItem) {
            viewModelScope.launch {
                if (item.pinned) {
                    clipboardRepository.unpinItem(item.id)
                } else {
                    clipboardRepository.pinItem(item.id)
                }
            }
        }

        fun deleteItem(item: ClipboardItem) {
            viewModelScope.launch { clipboardRepository.deleteItem(item) }
        }

        fun deleteAll() {
            viewModelScope.launch { clipboardRepository.deleteAll() }
        }

        fun setMaxItems(limit: Int) {
            _maxItems.value = limit
            clipboardRepository.setMaxItems(limit)
        }
    }
