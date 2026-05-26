package com.filmapp.domain.usecase.film

import com.filmapp.domain.model.Film
import com.filmapp.domain.repository.FilmRepository
import javax.inject.Inject

class SearchFilmsUseCase @Inject constructor(
    private val filmRepository: FilmRepository
) {
    suspend operator fun invoke(query: String): Result<List<Film>> {
        if (query.isBlank()) return Result.success(emptyList())
        return filmRepository.getFilms(search = query)
    }
}