package com.nuc.omeletteinputmethod.data.repository

import com.nuc.omeletteinputmethod.data.local.NoteDao
import com.nuc.omeletteinputmethod.data.model.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotepadRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun addNote(content: String) {
        if (content.isNotBlank()) {
            noteDao.insertNote(Note(content = content))
        }
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }
}
