package com.example.notetakingapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.notetakingapp.db.NotesDatabase
import com.example.notetakingapp.models.Note
import com.example.notetakingapp.repository.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val notesDao = NotesDatabase.invoke(application).getNotesDao()
    private val repository: NotesRepository

    private val _allNotes: LiveData<List<Note>>
    val allNotes: LiveData<List<Note>>
        get() = _allNotes

    init {
        repository = NotesRepository(notesDao)
        _allNotes = repository.getAllNotes
    }

    fun insertData(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

    fun noteIsValid(titleText: String, contentText: String): Boolean {
        return titleText.isNotEmpty() && contentText.isNotEmpty()
    }

}