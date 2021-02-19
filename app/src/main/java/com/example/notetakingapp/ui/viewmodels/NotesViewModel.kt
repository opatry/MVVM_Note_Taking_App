package com.example.notetakingapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.notetakingapp.db.NotesDatabase
import com.example.notetakingapp.models.Note
import com.example.notetakingapp.repository.DataStoreRepository
import com.example.notetakingapp.repository.NotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val notesDao = NotesDatabase.invoke(application).getNotesDao()
    private val repository: NotesRepository

    private val dataStoreRepository = DataStoreRepository(application)

    private val _allNotes: LiveData<List<Note>>
    val allNotes: LiveData<List<Note>>
        get() = _allNotes

    private val _readFromDataStore: LiveData<String>
    val readFromDataStore: LiveData<String>
        get() = _readFromDataStore

    init {
        repository = NotesRepository(notesDao)
        _readFromDataStore = dataStoreRepository.readFromDataStore.asLiveData()
        _allNotes = repository.getAllNotes
    }

    fun insertNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    fun deleteAllNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllNotes()
        }
    }

    fun saveToDataStore(sortBy: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepository.saveToDataStore(sortBy)
    }

    fun noteIsValid(titleText: String, contentText: String): Boolean {
        return titleText.isNotEmpty() && contentText.isNotEmpty()
    }

}