package com.filmapp.domain.usecase.genre

import com.filmapp.domain.model.Genre
import com.filmapp.domain.repository.GenreRepository
import javax.inject.Inject

class GetGenresUseCase @Inject constructor(
    private val genreRepository: GenreRepository
) {
    suspend operator fun invoke(): Result<List<Genre>> =
        genreRepository.getGenres()
}