package com.filmapp.domain.repository

import com.filmapp.domain.model.Genre

interface GenreRepository {
    suspend fun getGenres(): Result<List<Genre>>
    suspend fun getCachedGenres(): List<Genre>
    suspend fun cacheGenres(genres: List<Genre>)
}