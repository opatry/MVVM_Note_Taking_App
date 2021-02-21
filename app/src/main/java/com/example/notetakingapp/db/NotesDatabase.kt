package com.example.notetakingapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notetakingapp.models.Note
import com.example.notetakingapp.models.PriorityTypeConverters

@Database(
    entities = [Note::class],
    version = 1
)
@TypeConverters(PriorityTypeConverters::class)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun getNotesDao(): NotesDao

}