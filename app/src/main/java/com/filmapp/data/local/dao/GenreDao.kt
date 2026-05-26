package com.filmapp.data.local.dao

import androidx.room.*
import com.filmapp.data.local.entity.GenreEntity

@Dao
interface GenreDao {

    @Query("SELECT * FROM genres")
    suspend fun getAllGenres(): List<GenreEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<GenreEntity>)

    @Query("DELETE FROM genres")
    suspend fun clearAll()
}