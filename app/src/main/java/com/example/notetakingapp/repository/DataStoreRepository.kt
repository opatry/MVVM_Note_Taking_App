package com.example.notetakingapp.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject


const val PREFERENCE_NAME = "PREFERENCE_NAME"
const val SORT_BY_PREFERENCE_KEY_NAME = "SORT_BY_KEY"
const val DEFAULT_SORT_BY_VALUE = "DATE_NEWEST"

/**
 * This class persists the users "Sort By" preference
 */
@ActivityRetainedScoped
class DataStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKey {
        val sortBy = stringPreferencesKey(SORT_BY_PREFERENCE_KEY_NAME)
    }

    private val datastore: DataStore<Preferences> = context.createDataStore(name = PREFERENCE_NAME)

    suspend fun saveToDataStore(sortBy: String) {
        datastore.edit { preference ->
            preference[PreferencesKey.sortBy] = sortBy
        }
    }

    val readFromDataStore: Flow<String> = datastore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.d("DataStore", exception.message.toString())
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preference ->
            val sortBy = preference[PreferencesKey.sortBy] ?: DEFAULT_SORT_BY_VALUE
            sortBy
        }

}