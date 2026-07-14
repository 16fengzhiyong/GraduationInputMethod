package com.nuc.omeletteinputmethod.data.repository

import com.nuc.omeletteinputmethod.data.local.NoteDao
import com.nuc.omeletteinputmethod.data.model.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotepadRepository
    @Inject
    constructor(
        private val noteDao: NoteDao,
    ) {
        fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

        fun searchNotes(query: String): Flow<List<Note>> =
            if (query.isBlank()) {
                noteDao.getAllNotes()
            } else {
                noteDao.searchNotes(query.trim())
            }

        suspend fun addNote(
            title: String,
            content: String,
        ): Long {
            val now = System.currentTimeMillis()
            return noteDao.insertNote(
                Note(title = title.trim(), content = content.trim(), timestamp = now, updatedAt = now),
            )
        }

        suspend fun updateNote(note: Note) {
            noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }

        suspend fun deleteNote(note: Note) {
            noteDao.deleteNote(note)
        }
    }
