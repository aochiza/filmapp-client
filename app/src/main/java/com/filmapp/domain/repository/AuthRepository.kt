package com.filmapp.domain.repository

import com.filmapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
    fun getCurrentName(): Flow<String?>
    fun getCurrentRole(): Flow<String?>
}