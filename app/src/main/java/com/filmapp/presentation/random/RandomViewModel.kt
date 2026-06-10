package com.filmapp.presentation.random

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmapp.domain.model.Film
import com.filmapp.domain.model.Genre
import com.filmapp.domain.usecase.film.AddToFavoritesUseCase
import com.filmapp.domain.usecase.film.GetRandomFilmUseCase
import com.filmapp.domain.usecase.film.RemoveFromFavoritesUseCase
import com.filmapp.domain.usecase.genre.GetGenresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class RandomState {
    data object Loading : RandomState()
    data class Success(val film: Film, val isFavorite: Boolean) : RandomState()
    data class Error(val message: String) : RandomState()
}

@HiltViewModel
class RandomViewModel @Inject constructor(
    private val getRandomFilmUseCase: GetRandomFilmUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val getGenresUseCase: GetGenresUseCase
) : ViewModel() {

    private val _randomState = MutableStateFlow<RandomState>(RandomState.Loading)
    val randomState: StateFlow<RandomState> = _randomState.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId.asStateFlow()

    init {
        loadGenres()
    }

    private fun loadGenres() {
        viewModelScope.launch {
            getGenresUseCase().onSuccess { genres ->
                _genres.value = genres
            }
        }
    }

    fun onGenreSelected(genreId: Int?) {
        _selectedGenreId.value = genreId
        loadRandomFilm(genreId)
    }

    fun loadRandomFilm(
        genreId: Int? = _selectedGenreId.value,
        showLoading: Boolean = true
    ) {
        viewModelScope.launch {
            if (showLoading) {
                _randomState.value = RandomState.Loading
            }
            try {
                val film = getRandomFilmUseCase(genreId)
                if (film != null) {
                    _randomState.value = RandomState.Success(film, film.isFavorite)
                } else {
                    _randomState.value = RandomState.Error(getNoFilmsErrorMessage())
                }
            } catch (e: Exception) {
                _randomState.value = RandomState.Error(
                    e.message ?: getDefaultErrorMessage()
                )
            }
        }
    }

    fun toggleFavorite(filmId: Int) {
        viewModelScope.launch {
            val currentState = _randomState.value
            if (currentState is RandomState.Success) {
                try {
                    if (currentState.isFavorite) {
                        removeFromFavoritesUseCase(filmId)
                        _randomState.value = currentState.copy(isFavorite = false)
                    } else {
                        addToFavoritesUseCase(filmId)
                        _randomState.value = currentState.copy(isFavorite = true)
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
    
    private fun getNoFilmsErrorMessage(): String = "Фильмы не найдены"
    private fun getDefaultErrorMessage(): String = "Ошибка загрузки"
    // endregion
}