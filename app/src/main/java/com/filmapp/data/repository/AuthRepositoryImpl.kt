package com.filmapp.data.repository

import com.filmapp.data.remote.api.AuthApi
import com.filmapp.data.remote.dto.LoginRequest
import com.filmapp.data.remote.dto.RegisterRequest
import com.filmapp.data.remote.firebase.FirebaseAuthSource
import com.filmapp.data.remote.interceptor.TokenProvider
import com.filmapp.domain.model.User
import com.filmapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenProvider: TokenProvider,
    private val firebaseAuthSource: FirebaseAuthSource
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        name: String
    ): Result<User> = runCatching {

        firebaseAuthSource.register(email, password)

        val response =
            authApi.register(RegisterRequest(email, password, name))

        tokenProvider.saveAuthData(
            token = response.token,
            userId = response.userId,
            name = response.name,
            role = response.role
        )

        User(
            userId = response.userId,
            name = response.name,
            email = response.email,
            token = response.token,
            role = response.role
        )
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<User> = runCatching {

        firebaseAuthSource.login(email, password)

        val response =
            authApi.login(LoginRequest(email, password))

        tokenProvider.saveAuthData(
            token = response.token,
            userId = response.userId,
            name = response.name,
            role = response.role
        )

        User(
            userId = response.userId,
            name = response.name,
            email = response.email,
            token = response.token,
            role = response.role
        )
    }

    override suspend fun logout() {
        firebaseAuthSource.logout()
        tokenProvider.clearAuthData()
    }

    override fun isLoggedIn(): Flow<Boolean> =
        tokenProvider.isLoggedIn()

    override fun getCurrentName(): Flow<String?> =
        tokenProvider.getName()

    override fun getCurrentRole(): Flow<String?> =
        tokenProvider.getRole()
}