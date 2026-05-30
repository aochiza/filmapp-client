package com.filmapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.searchHistoryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "search_history"
)

@Singleton
class SearchHistoryStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val QUERIES_KEY = stringPreferencesKey("queries")
        private const val SEPARATOR = "\u001F"
        private const val MAX_SIZE = 5
    }

    val history: Flow<List<String>> = context.searchHistoryDataStore.data.map { prefs ->
        prefs[QUERIES_KEY]
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    suspend fun addQuery(rawQuery: String) {
        val query = rawQuery.trim()
        if (query.isBlank()) return

        context.searchHistoryDataStore.edit { prefs ->
            val current = prefs[QUERIES_KEY]
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?: emptyList()
            val updated = listOf(query) + current.filter { !it.equals(query, ignoreCase = true) }
            prefs[QUERIES_KEY] = updated.take(MAX_SIZE).joinToString(SEPARATOR)
        }
    }
}
