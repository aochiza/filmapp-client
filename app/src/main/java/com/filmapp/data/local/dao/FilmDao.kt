package com.filmapp.data.local.dao

import androidx.room.*
import com.filmapp.data.local.entity.FilmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilmDao {

    @Query("SELECT * FROM films ORDER BY rating DESC")
    suspend fun getAllFilms(): List<FilmEntity>

    @Query("SELECT * FROM films WHERE id = :id")
    suspend fun getFilmById(id: Int): FilmEntity?

    @Query("SELECT * FROM films WHERE isFavorite = 1")
    suspend fun getFavoriteFilms(): List<FilmEntity>

    @Query("""
        SELECT * FROM films 
        WHERE title LIKE '%' || :query || '%' 
        OR director LIKE '%' || :query || '%'
    """)
    suspend fun searchFilms(query: String): List<FilmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilms(films: List<FilmEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilm(film: FilmEntity)

    @Update
    suspend fun updateFilm(film: FilmEntity)

    @Query("UPDATE films SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

    @Query("DELETE FROM films")
    suspend fun clearAll()

    @Query("UPDATE films SET isWatchLater = :isWatchLater WHERE id = :id")
    suspend fun updateWatchLaterStatus(id: Int, isWatchLater: Boolean)

    @Query("SELECT * FROM films WHERE isWatchLater = 1")
    suspend fun getWatchLaterFilms(): List<FilmEntity>
}