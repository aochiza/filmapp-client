package com.filmapp.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmapp.domain.model.Film
import com.filmapp.domain.usecase.film.GetFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FavoritesState {
    object Loading : FavoritesState()
    object Empty : FavoritesState()
    data class Success(val films: List<Film>) : FavoritesState()
    data class Error(val message: String) : FavoritesState()
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<FavoritesState>(FavoritesState.Loading)
    val state: StateFlow<FavoritesState> = _state

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _state.value = FavoritesState.Loading
            getFavoritesUseCase()
                .onSuccess { films ->
                    _state.value = if (films.isEmpty()) FavoritesState.Empty
                    else FavoritesState.Success(films)
                }
                .onFailure {
                    _state.value = FavoritesState.Error(it.message ?: "Ошибка загрузки")
                }
        }
    }
}