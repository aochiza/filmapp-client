package com.filmapp.domain.usecase.film

import com.filmapp.domain.model.Film
import com.filmapp.domain.repository.FilmRepository
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val filmRepository: FilmRepository
) {
    suspend operator fun invoke(): Result<List<Film>> =
        filmRepository.getFavorites()
}