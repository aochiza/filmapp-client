package com.filmapp.data.repository

import com.filmapp.data.local.dao.FilmDao
import com.filmapp.data.local.entity.FilmEntity
import com.filmapp.data.remote.api.FilmApi
import com.filmapp.domain.model.Film
import com.filmapp.domain.repository.FilmRepository
import javax.inject.Inject

class FilmRepositoryImpl @Inject constructor(
    private val filmApi: FilmApi,
    private val filmDao: FilmDao
) : FilmRepository {

    override suspend fun getFilms(
        page: Int,
        pageSize: Int,
        search: String?,
        genreId: Int?
    ): Result<List<Film>> = runCatching {
        val response = filmApi.getFilms(page, pageSize, search, genreId)
        val films = response.films.map { it.toDomain() }
        if (page == 1 && search == null && genreId == null) {
            cacheFilms(films)
        }
        films
    }

    override suspend fun getFilmById(id: Int): Result<Film> = runCatching {
        filmApi.getFilmById(id).toDomain()
    }

    override suspend fun getFavorites(): Result<List<Film>> = runCatching {
        filmApi.getFavorites().map { it.toDomain() }
    }

    override suspend fun addToFavorites(filmId: Int): Result<Unit> = runCatching {
        filmApi.addToFavorites(filmId)
        filmDao.updateFavoriteStatus(filmId, true)
    }

    override suspend fun removeFromFavorites(filmId: Int): Result<Unit> = runCatching {
        filmApi.removeFromFavorites(filmId)
        filmDao.updateFavoriteStatus(filmId, false)
    }

    override suspend fun getCachedFilms(): List<Film> =
        filmDao.getAllFilms().map { it.toDomain() }

    override suspend fun cacheFilms(films: List<Film>) {
        filmDao.clearAll()
        filmDao.insertFilms(films.map { it.toEntity() })
    }

    override suspend fun getWatchLater(): Result<List<Film>> = runCatching {
        filmApi.getWatchLater().map { it.toDomain() }
    }

    override suspend fun addToWatchLater(filmId: Int): Result<Unit> = runCatching {
        filmApi.addToWatchLater(filmId)
        filmDao.updateWatchLaterStatus(filmId, true)
    }

    override suspend fun removeFromWatchLater(filmId: Int): Result<Unit> = runCatching {
        filmApi.removeFromWatchLater(filmId)
        filmDao.updateWatchLaterStatus(filmId, false)
    }

    override suspend fun getRandomFilm(genreId: Int?): Film? {
        return try {
            val response = filmApi.getRandomFilm(genreId)
            response.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    private fun com.filmapp.data.remote.dto.FilmResponse.toDomain() = Film(
        id = id,
        title = title,
        originalTitle = originalTitle,
        description = description,
        releaseYear = releaseYear,
        rating = rating,
        posterUrl = posterUrl,
        genreId = genreId,
        genreName = genreName,
        director = director,
        duration = duration,
        isFavorite = isFavorite,
        isWatchLater = isWatchLater
    )

    private fun Film.toEntity() = FilmEntity(
        id = id,
        title = title,
        originalTitle = originalTitle,
        description = description,
        releaseYear = releaseYear,
        rating = rating,
        posterUrl = posterUrl,
        genreId = genreId,
        genreName = genreName,
        director = director,
        duration = duration,
        isFavorite = isFavorite,
        isWatchLater = isWatchLater
    )

    private fun FilmEntity.toDomain() = Film(
        id = id,
        title = title,
        originalTitle = originalTitle,
        description = description,
        releaseYear = releaseYear,
        rating = rating,
        posterUrl = posterUrl,
        genreId = genreId,
        genreName = genreName,
        director = director,
        duration = duration,
        isFavorite = isFavorite,
        isWatchLater = isWatchLater
    )
}