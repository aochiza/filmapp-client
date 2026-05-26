package com.filmapp.domain.usecase.film

import com.filmapp.domain.model.Film
import com.filmapp.domain.repository.FilmRepository
import javax.inject.Inject

class GetRandomFilmUseCase @Inject constructor(
    private val repository: FilmRepository
) {
    suspend operator fun invoke(genreId: Int? = null): Film? {
        return repository.getRandomFilm(genreId)
    }
}