package com.filmapp.presentation.films

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmapp.data.remote.dto.FilmRequest
import com.filmapp.domain.model.Genre
import com.filmapp.domain.usecase.film.CreateFilmUseCase
import com.filmapp.domain.usecase.genre.GetGenresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddFilmState {
    data object Idle : AddFilmState()
    data object Loading : AddFilmState()
    data object Success : AddFilmState()
    data class Error(val message: String) : AddFilmState()
}

@HiltViewModel
class AddFilmViewModel @Inject constructor(
    private val createFilmUseCase: CreateFilmUseCase,
    private val getGenresUseCase: GetGenresUseCase
) : ViewModel() {

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres

    private val _state = MutableStateFlow<AddFilmState>(AddFilmState.Idle)
    val state: StateFlow<AddFilmState> = _state

    init {
        loadGenres()
    }

    private fun loadGenres() {
        viewModelScope.launch {
            getGenresUseCase()
                .onSuccess { genres ->
                    _genres.value = genres
                }
                .onFailure { error ->
                    _genres.value = emptyList()
                }
        }
    }

    fun createFilm(
        title: String,
        originalTitle: String,
        description: String,
        releaseYear: String,
        rating: String,
        posterUrl: String,
        genreId: Int?,
        director: String,
        duration: String
    ) {
        viewModelScope.launch {

            if (title.isBlank()) {
                _state.value = AddFilmState.Error(getEmptyTitleError())
                return@launch
            }

            val year = releaseYear.toIntOrNull()
            if (year == null) {
                _state.value = AddFilmState.Error(getInvalidYearError())
                return@launch
            }

            if (genreId == null) {
                _state.value = AddFilmState.Error(getNoGenreError())
                return@launch
            }

            _state.value = AddFilmState.Loading

            val request = FilmRequest(
                title = title,
                originalTitle = originalTitle.ifBlank { null }, //преобр пустую строку в null
                description = description.ifBlank { null },
                releaseYear = year,
                rating = rating.toDoubleOrNull(),
                posterUrl = posterUrl.ifBlank { null },
                genreId = genreId,
                director = director.ifBlank { null },
                duration = duration.toIntOrNull()
            )

            createFilmUseCase(request)
                .onSuccess {
                    _state.value = AddFilmState.Success
                }
                .onFailure { error ->
                    _state.value = AddFilmState.Error(
                        error.message ?: getDefaultErrorMessage()
                    )
                }
        }
    }

    fun resetState() {
        _state.value = AddFilmState.Idle
    }

    private fun getEmptyTitleError(): String = "Введите название фильма"
    private fun getInvalidYearError(): String = "Введите корректный год"
    private fun getNoGenreError(): String = "Выберите жанр"
    private fun getDefaultErrorMessage(): String = "Ошибка создания фильма"
    // endregion
}