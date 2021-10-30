package com.example.notetakingapp.di

import android.content.Context
import androidx.room.Room
import com.example.notetakingapp.db.NotesDatabase
import com.myscript.certificate.MyCertificate
import com.myscript.iink.Engine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
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

    // TODO named provide
    @Singleton
    @Provides
    fun provideRootDirectory(
        @ApplicationContext context: Context
    ) = context.filesDir

    // TODO named provide
    @Singleton
    @Provides
    fun provideMyScriptCertificate(
    ) = MyCertificate.getBytes()

    @Singleton
    @Provides
    fun provideMyScriptEngine(
        @ApplicationContext context: Context,
        certificate: ByteArray
    ) = Engine.create(certificate).also {
        it.configuration?.let { conf ->
            conf.setStringArray("configuration-manager.search-path", arrayOf("zip://${context.packageCodePath}!/assets/conf"))
            conf.setString("content-package.temp-folder", File(context.filesDir, "iink").absolutePath)
        }
    }
}