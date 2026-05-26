package com.filmapp.data.remote.api

import com.filmapp.data.remote.dto.AuthResponse
import com.filmapp.data.remote.dto.LoginRequest
import com.filmapp.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}