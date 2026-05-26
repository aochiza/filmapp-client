package com.filmapp.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmapp.domain.model.Film
import com.filmapp.domain.usecase.film.AddToFavoritesUseCase
import com.filmapp.domain.usecase.film.AddToWatchLaterUseCase
import com.filmapp.domain.usecase.film.GetFilmByIdUseCase
import com.filmapp.domain.usecase.film.RemoveFromWatchLaterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FilmDetailState {
    object Loading : FilmDetailState()
    data class Success(val film: Film) : FilmDetailState()
    data class Error(val message: String) : FilmDetailState()
}

@HiltViewModel
class FilmDetailViewModel @Inject constructor(
    private val getFilmByIdUseCase: GetFilmByIdUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val addToWatchLaterUseCase: AddToWatchLaterUseCase,
    private val removeFromWatchLaterUseCase: RemoveFromWatchLaterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<FilmDetailState>(FilmDetailState.Loading)
    val state: StateFlow<FilmDetailState> = _state

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun loadFilm(id: Int) {
        viewModelScope.launch {
            _state.value = FilmDetailState.Loading
            getFilmByIdUseCase(id)
                .onSuccess { _state.value = FilmDetailState.Success(it) }
                .onFailure { _state.value = FilmDetailState.Error(it.message ?: "Ошибка") }
        }
    }

    fun toggleFavorite() {
        val film = currentFilm() ?: return
        viewModelScope.launch {
            addToFavoritesUseCase(film.id)
                .onSuccess {
                    updateFilm(film.copy(isFavorite = !film.isFavorite))
                    _snackbarMessage.value = if (!film.isFavorite)
                        "Добавлено в избранное" else "Удалено из избранного"
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Ошибка" }
        }
    }

    fun toggleWatchLater() {
        val film = currentFilm() ?: return
        viewModelScope.launch {
            if (film.isWatchLater) {
                removeFromWatchLaterUseCase(film.id)
                    .onSuccess {
                        updateFilm(film.copy(isWatchLater = false))
                        _snackbarMessage.value = "Удалено из списка"
                    }
                    .onFailure { _snackbarMessage.value = it.message ?: "Ошибка" }
            } else {
                addToWatchLaterUseCase(film.id)
                    .onSuccess {
                        updateFilm(film.copy(isWatchLater = true))
                        _snackbarMessage.value = "Добавлено в «Посмотреть позже»"
                    }
                    .onFailure { _snackbarMessage.value = it.message ?: "Ошибка" }
            }
        }
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    private fun currentFilm() =
        (_state.value as? FilmDetailState.Success)?.film

    private fun updateFilm(film: Film) {
        _state.value = FilmDetailState.Success(film)
    }
}