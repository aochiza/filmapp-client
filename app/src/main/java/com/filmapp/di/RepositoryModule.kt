package com.filmapp.di

import com.filmapp.data.repository.AuthRepositoryImpl
import com.filmapp.data.repository.FilmRepositoryImpl
import com.filmapp.data.repository.GenreRepositoryImpl
import com.filmapp.domain.repository.AuthRepository
import com.filmapp.domain.repository.FilmRepository
import com.filmapp.domain.repository.GenreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFilmRepository(impl: FilmRepositoryImpl): FilmRepository

    @Binds
    @Singleton
    abstract fun bindGenreRepository(impl: GenreRepositoryImpl): GenreRepository
}