package com.filmapp.domain.usecase.film

import com.filmapp.data.remote.dto.FilmRequest
import com.filmapp.domain.model.Film
import com.filmapp.domain.repository.FilmRepository
import javax.inject.Inject

class CreateFilmUseCase @Inject constructor(
    private val repository: FilmRepository
) {
    suspend operator fun invoke(
        request: FilmRequest
    ): Result<Film> {
        return repository.createFilm(request)
    }
}