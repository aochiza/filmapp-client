package com.filmapp.data.remote.api

import com.filmapp.data.remote.dto.GenreResponse
import retrofit2.http.GET

interface GenreApi {
    @GET("genres")
    suspend fun getGenres(): List<GenreResponse>
}