package com.example.notetakingapp.di

import android.content.Context
import androidx.room.Room
import com.example.notetakingapp.db.NotesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context.applicationContext,
        NotesDatabase::class.java,
        "notes_database.db"
    ).build()

    @Singleton
    @Provides
    fun provideDao(
        database: NotesDatabase
    ) = database.getNotesDao()

}