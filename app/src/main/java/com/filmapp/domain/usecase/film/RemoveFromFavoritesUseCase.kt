package com.filmapp.domain.usecase.film

import com.filmapp.domain.repository.FilmRepository
import javax.inject.Inject

class RemoveFromFavoritesUseCase @Inject constructor(
    private val repository: FilmRepository
) {
    suspend operator fun invoke(filmId: Int) {
        repository.removeFromFavorites(filmId)
    }
}