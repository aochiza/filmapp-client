package com.filmapp.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmapp.R
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
                .onSuccess { film ->
                    _state.value = FilmDetailState.Success(film)
                }
                .onFailure { error ->
                    _state.value = FilmDetailState.Error(
                        error.message ?: getDefaultErrorMessage()
                    )
                }
        }
    }

    fun toggleFavorite() {
        val film = currentFilm() ?: return
        viewModelScope.launch {
            addToFavoritesUseCase(film.id)
                .onSuccess {
                    updateFilm(film.copy(isFavorite = !film.isFavorite))
                    _snackbarMessage.value = if (!film.isFavorite) {
                        getFavoriteAddedMessage()
                    } else {
                        getFavoriteRemovedMessage()
                    }
                }
                .onFailure { error ->
                    _snackbarMessage.value = error.message ?: getDefaultErrorMessage()
                }
        }
    }

    fun toggleWatchLater() {
        val film = currentFilm() ?: return
        viewModelScope.launch {
            if (film.isWatchLater) {
                removeFromWatchLaterUseCase(film.id)
                    .onSuccess {
                        updateFilm(film.copy(isWatchLater = false))
                        _snackbarMessage.value = getWatchLaterRemovedMessage()
                    }
                    .onFailure { error ->
                        _snackbarMessage.value = error.message ?: getDefaultErrorMessage()
                    }
            } else {
                addToWatchLaterUseCase(film.id)
                    .onSuccess {
                        updateFilm(film.copy(isWatchLater = true))
                        _snackbarMessage.value = getWatchLaterAddedMessage()
                    }
                    .onFailure { error ->
                        _snackbarMessage.value = error.message ?: getDefaultErrorMessage()
                    }
            }
        }
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    private fun currentFilm(): Film? =
        (_state.value as? FilmDetailState.Success)?.film

    private fun updateFilm(film: Film) {
        _state.value = FilmDetailState.Success(film)
    }

    private fun getDefaultErrorMessage(): String = "Ошибка"
    private fun getFavoriteAddedMessage(): String = "Добавлено в избранное"
    private fun getFavoriteRemovedMessage(): String = "Удалено из избранного"
    private fun getWatchLaterAddedMessage(): String = "Добавлено в «Посмотреть позже»"
    private fun getWatchLaterRemovedMessage(): String = "Удалено из списка"
}