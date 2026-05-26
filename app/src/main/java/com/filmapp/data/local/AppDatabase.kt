package com.filmapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.filmapp.data.local.dao.FilmDao
import com.filmapp.data.local.dao.GenreDao
import com.filmapp.data.local.entity.FilmEntity
import com.filmapp.data.local.entity.GenreEntity

@Database(
    entities = [FilmEntity::class, GenreEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun filmDao(): FilmDao
    abstract fun genreDao(): GenreDao
}