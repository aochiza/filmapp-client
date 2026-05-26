package com.filmapp.data.repository

import com.filmapp.data.local.dao.GenreDao
import com.filmapp.data.local.entity.GenreEntity
import com.filmapp.data.remote.api.GenreApi
import com.filmapp.domain.model.Genre
import com.filmapp.domain.repository.GenreRepository
import javax.inject.Inject

class GenreRepositoryImpl @Inject constructor(
    private val genreApi: GenreApi,
    private val genreDao: GenreDao
) : GenreRepository {

    override suspend fun getGenres(): Result<List<Genre>> = runCatching {
        val genres = genreApi.getGenres().map { it.toDomain() }
        cacheGenres(genres)
        genres
    }

    override suspend fun getCachedGenres(): List<Genre> =
        genreDao.getAllGenres().map { it.toDomain() }

    override suspend fun cacheGenres(genres: List<Genre>) {
        genreDao.clearAll()
        genreDao.insertGenres(genres.map { it.toEntity() })
    }

    private fun com.filmapp.data.remote.dto.GenreResponse.toDomain() = Genre(
        id = id,
        name = name,
        description = description
    )

    private fun Genre.toEntity() = GenreEntity(
        id = id,
        name = name,
        description = description
    )

    private fun GenreEntity.toDomain() = Genre(
        id = id,
        name = name,
        description = description
    )
}