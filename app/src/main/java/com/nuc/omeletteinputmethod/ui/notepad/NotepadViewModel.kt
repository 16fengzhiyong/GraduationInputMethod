package com.nuc.omeletteinputmethod.ui.notepad

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuc.omeletteinputmethod.data.model.Note
import com.nuc.omeletteinputmethod.data.repository.NotepadRepository
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
class NotepadViewModel
    @Inject
    constructor(
        private val repository: NotepadRepository,
    ) : ViewModel() {
        private val searchQuery = MutableStateFlow("")

        @OptIn(ExperimentalCoroutinesApi::class)
        val notes: StateFlow<List<Note>> =
            searchQuery
                .flatMapLatest { query ->
                    repository.searchNotes(query)
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

        fun addNote(
            title: String,
            content: String,
        ) {
            viewModelScope.launch {
                repository.addNote(title.trim(), content.trim())
            }
        }

        fun updateNote(note: Note) {
            viewModelScope.launch {
                repository.updateNote(note)
            }
        }

        fun deleteNote(note: Note) {
            viewModelScope.launch {
                repository.deleteNote(note)
            }
        }
    }
