package com.example.notetakingapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.notetakingapp.models.Note
import com.example.notetakingapp.repository.DataStoreRepository
import com.example.notetakingapp.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel
@Inject constructor(
    private val repository: NotesRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _allNotes: LiveData<List<Note>>
    val allNotes: LiveData<List<Note>>
        get() = _allNotes

    private val _readFromDataStore: LiveData<String>
    val readFromDataStore: LiveData<String>
        get() = _readFromDataStore

    init {
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