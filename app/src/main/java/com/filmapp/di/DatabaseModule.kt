package com.filmapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.filmapp.data.local.AppDatabase
import com.filmapp.data.local.dao.FilmDao
import com.filmapp.data.local.dao.GenreDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "filmapp.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideFilmDao(db: AppDatabase): FilmDao = db.filmDao()

    @Provides
    fun provideGenreDao(db: AppDatabase): GenreDao = db.genreDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE films ADD COLUMN isWatchLater INTEGER NOT NULL DEFAULT 0"
            )
        }
    }
}