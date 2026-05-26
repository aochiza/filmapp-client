package com.filmapp.domain.usecase.film

import com.filmapp.domain.repository.FilmRepository
import javax.inject.Inject

class RemoveFromWatchLaterUseCase @Inject constructor(
    private val filmRepository: FilmRepository
) {
    suspend operator fun invoke(filmId: Int): Result<Unit> =
        filmRepository.removeFromWatchLater(filmId)
}