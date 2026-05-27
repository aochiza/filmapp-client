package com.filmapp.domain.repository

import com.filmapp.data.remote.dto.FilmRequest
import com.filmapp.domain.model.Film

interface FilmRepository {
    suspend fun getFilms(
        page: Int = 1,
        pageSize: Int = 20,
        search: String? = null,
        genreId: Int? = null
    ): Result<List<Film>>

    suspend fun getFilmById(id: Int): Result<Film>
    suspend fun getFavorites(): Result<List<Film>>
    suspend fun addToFavorites(filmId: Int): Result<Unit>
    suspend fun removeFromFavorites(filmId: Int): Result<Unit>

    suspend fun getWatchLater(): Result<List<Film>>
    suspend fun addToWatchLater(filmId: Int): Result<Unit>
    suspend fun removeFromWatchLater(filmId: Int): Result<Unit>

    suspend fun getCachedFilms(): List<Film>
    suspend fun cacheFilms(films: List<Film>)

    suspend fun getRandomFilm(genreId: Int? = null): Film?

    suspend fun createFilm(filmRequest: FilmRequest): Result<Film>
    suspend fun deleteFilm(id: Int): Result<Unit>
}
