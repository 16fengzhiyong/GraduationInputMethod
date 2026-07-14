package com.nuc.omeletteinputmethod.ui.shortcut

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.data.model.ShortcutItem
import com.nuc.omeletteinputmethod.data.repository.ShortcutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShortcutViewModel @Inject constructor(
    private val repository: ShortcutRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val currentPackage = MutableStateFlow("global")

    @OptIn(ExperimentalCoroutinesApi::class)
    val shortcuts: StateFlow<List<ShortcutItem>> = searchQuery.flatMapLatest { query ->
        repository.searchShortcuts(query, currentPackage.value)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSearching: StateFlow<Boolean> get() = _isSearching
    private val _isSearching = MutableStateFlow(false)
    val searchText: StateFlow<String> = searchQuery.asStateFlow()

    fun setSearch(query: String) {
        searchQuery.value = query
    }

    fun toggleSearch(active: Boolean) {
        _isSearching.value = active
        if (!active) searchQuery.value = ""
    }

    fun addShortcut(label: String, content: String, category: String) {
        viewModelScope.launch {
            repository.addShortcut(
                ShortcutItem(
                    packageName = currentPackage.value,
                    label = label.trim(),
                    content = content.trim(),
                    category = category.trim()
                )
            )
        }
    }

    fun updateShortcut(item: ShortcutItem) {
        viewModelScope.launch {
            repository.updateShortcut(item)
        }
    }

    fun deleteShortcut(item: ShortcutItem) {
        viewModelScope.launch {
            repository.deleteShortcut(item)
        }
    }

    private val clipboardCallback = MutableStateFlow<(() -> Unit)?>(null)

    fun registerClipboardCallback(callback: () -> Unit) {
        clipboardCallback.value = callback
    }
}