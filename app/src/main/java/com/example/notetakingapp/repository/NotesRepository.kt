package com.example.notetakingapp.repository

import androidx.lifecycle.LiveData
import com.example.notetakingapp.db.NotesDao
import com.example.notetakingapp.models.Note

class NotesRepository(private val notesDao: NotesDao) {

    val getAllNotes: LiveData<List<Note>> = notesDao.getAllNotes()

    suspend fun insertNote(note: Note) = notesDao.insertNote(note)


}