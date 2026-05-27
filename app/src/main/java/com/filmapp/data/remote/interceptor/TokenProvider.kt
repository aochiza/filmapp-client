package com.filmapp.data.remote.interceptor

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val NAME_KEY = stringPreferencesKey("name")
        private val ROLE_KEY = stringPreferencesKey("role")
    }

    fun getToken(): Flow<String?> =
        context.dataStore.data.map { it[TOKEN_KEY] }

    fun getUserId(): Flow<Int?> =
        context.dataStore.data.map {
            it[USER_ID_KEY]?.toIntOrNull()
        }

    fun getName(): Flow<String?> =
        context.dataStore.data.map {
            it[NAME_KEY]
        }

    fun getRole(): Flow<String?> =
        context.dataStore.data.map {
            it[ROLE_KEY]
        }

    fun isAdmin(): Flow<Boolean> =
        getRole().map { it == "ADMIN" }

    suspend fun saveAuthData(
        token: String,
        userId: Int,
        name: String,
        role: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId.toString()
            prefs[NAME_KEY] = name
            prefs[ROLE_KEY] = role
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit {
            it.clear()
        }
    }

    fun isLoggedIn(): Flow<Boolean> =
        getToken().map { !it.isNullOrEmpty() }

    fun getTokenSync(): String? =
        runBlocking { getToken().first() }
}