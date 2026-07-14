package com.nuc.omeletteinputmethod.ui.shortcut

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.data.model.ShortcutItem
import com.nuc.omeletteinputmethod.data.repository.ShortcutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShortcutViewModel @Inject constructor(
    private val repository: ShortcutRepository
) : ViewModel() {

    // Ideally, this would be observed from an AccessibilityService detecting the current foreground app
    // For now, we default to 'global' or allow user to switch context manually in UI
    private val currentPackage = MutableStateFlow("global")

    val shortcuts: StateFlow<List<ShortcutItem>> = currentPackage.flatMapLatest { pkg ->
        repository.getShortcuts(pkg)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addShortcut(content: String, label: String) {
        viewModelScope.launch {
            repository.addShortcut(
                ShortcutItem(
                    packageName = currentPackage.value,
                    content = content,
                    label = label
                )
            )
        }
    }

    fun deleteShortcut(item: ShortcutItem) {
        viewModelScope.launch {
            repository.deleteShortcut(item)
        }
    }
}
