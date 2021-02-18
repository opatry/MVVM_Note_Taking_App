package com.example.notetakingapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.notetakingapp.models.Note

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes_table ORDER by date DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes_table")
    suspend fun deleteAllNotes()

}