package com.filmapp.domain.usecase.film

import com.filmapp.domain.model.Film
import com.filmapp.domain.repository.FilmRepository
import javax.inject.Inject

class GetFilmsUseCase @Inject constructor(
    private val filmRepository: FilmRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = 20,
        search: String? = null,
        genreId: Int? = null
    ): Result<List<Film>> = filmRepository.getFilms(page, pageSize, search, genreId)
}