package com.filmapp.domain.usecase.film

import com.filmapp.domain.repository.FilmRepository
import javax.inject.Inject

class DeleteFilmUseCase @Inject constructor(
    private val filmRepository: FilmRepository
) {
    suspend operator fun invoke(id: Int): Result<Unit> {
        return filmRepository.deleteFilm(id)
    }
}