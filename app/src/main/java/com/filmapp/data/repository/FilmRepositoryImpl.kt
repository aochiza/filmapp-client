package com.filmapp.data.repository

import com.filmapp.data.local.dao.FilmDao
import com.filmapp.data.local.entity.FilmEntity
import com.filmapp.data.remote.api.FilmApi
import com.filmapp.data.remote.dto.FilmRequest
import com.filmapp.data.remote.dto.FilmResponse
import com.filmapp.data.remote.dto.WatchLaterEntryResponse
import com.filmapp.data.remote.dto.WatchLaterListResponse
import com.filmapp.domain.model.Film
import com.filmapp.domain.repository.FilmRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class FilmRepositoryImpl @Inject constructor(
    private val filmApi: FilmApi,
    private val filmDao: FilmDao,
    private val gson: Gson
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
            mergeFilmsIntoCache(films)
        }
        films
    }

    override suspend fun createFilm(filmRequest: FilmRequest): Result<Film> = runCatching {
        filmApi.createFilm(filmRequest).toDomain()
    }

    override suspend fun deleteFilm(id: Int): Result<Unit> =
        runCatching {
            filmApi.deleteFilm(id)
        }

    override suspend fun getFilmById(id: Int): Result<Film> = runCatching {
        val remote = filmApi.getFilmById(id).toDomain()
        val local = filmDao.getFilmById(id)
        val merged = remote.copy(
            isFavorite = remote.isFavorite || (local?.isFavorite == true),
            isWatchLater = remote.isWatchLater || (local?.isWatchLater == true)
        )
        saveFilmLocally(merged)
        merged
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
        mergeFilmsIntoCache(films)
    }

    override suspend fun getWatchLater(): Result<List<Film>> = runCatching {
        try {
            val body = filmApi.getWatchLater().string()
            parseWatchLaterResponse(body)
                .map { it.toDomain(markWatchLater = true) }
                .also { films -> films.forEach { saveFilmLocally(it) } }
        } catch (_: Exception) {
            // Server watch-later may fail (e.g. missing DB column added_at) — use local cache.
            filmDao.getWatchLaterFilms().map { it.toDomain() }
        }
    }

    override suspend fun addToWatchLater(filmId: Int): Result<Unit> = runCatching {
        try {
            filmApi.addToWatchLater(filmId)
        } catch (_: Exception) {
            // Persist locally when server endpoint is unavailable.
        }
        markWatchLaterLocally(filmId, isWatchLater = true)
    }

    override suspend fun removeFromWatchLater(filmId: Int): Result<Unit> = runCatching {
        try {
            filmApi.removeFromWatchLater(filmId)
        } catch (_: Exception) {
            // Persist locally when server endpoint is unavailable.
        }
        markWatchLaterLocally(filmId, isWatchLater = false)
    }

    override suspend fun getRandomFilm(genreId: Int?): Film? {
        return try {
            val response = filmApi.getRandomFilm(genreId)
            response.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun markWatchLaterLocally(filmId: Int, isWatchLater: Boolean) {
        val cached = filmDao.getFilmById(filmId)
        if (cached != null) {
            filmDao.updateWatchLaterStatus(filmId, isWatchLater)
        } else if (isWatchLater) {
            val film = filmApi.getFilmById(filmId).toDomain(markWatchLater = true)
            filmDao.insertFilm(film.toEntity())
        }
    }

    private suspend fun saveFilmLocally(film: Film) {
        val existing = filmDao.getFilmById(film.id)
        if (existing != null) {
            filmDao.insertFilm(
                film.toEntity().copy(
                    isFavorite = existing.isFavorite || film.isFavorite,
                    isWatchLater = existing.isWatchLater || film.isWatchLater
                )
            )
        } else {
            filmDao.insertFilm(film.toEntity())
        }
    }

    private suspend fun mergeFilmsIntoCache(films: List<Film>) {
        films.forEach { saveFilmLocally(it) }
    }

    private fun parseWatchLaterResponse(body: String): List<FilmResponse> {
        return try {
            val json = gson.fromJson(body, com.google.gson.JsonElement::class.java)

            when {
                json.isJsonArray -> {
                    val array = json.asJsonArray
                    if (array.size() == 0) return emptyList()

                    val first = array[0].asJsonObject
                    if (first.has("film")) {
                        val type = object : TypeToken<List<WatchLaterEntryResponse>>() {}.type
                        gson.fromJson<List<WatchLaterEntryResponse>>(array, type)
                            .mapNotNull { it.film }
                    } else {
                        val type = object : TypeToken<List<FilmResponse>>() {}.type
                        gson.fromJson(array, type)
                    }
                }

                json.isJsonObject -> {
                    val wrapped = gson.fromJson(json, WatchLaterListResponse::class.java)
                    wrapped.films ?: wrapped.items ?: emptyList()
                }

                else -> emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun com.filmapp.data.remote.dto.FilmResponse.toDomain(
        markWatchLater: Boolean? = null
    ) = Film(
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
        isWatchLater = markWatchLater ?: isWatchLater
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
