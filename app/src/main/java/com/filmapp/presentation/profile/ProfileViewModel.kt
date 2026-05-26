package com.filmapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmapp.data.remote.interceptor.TokenProvider
import com.filmapp.domain.model.Film
import com.filmapp.domain.repository.AuthRepository
import com.filmapp.domain.usecase.film.GetWatchLaterUseCase
import com.filmapp.domain.usecase.film.RemoveFromWatchLaterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WatchLaterState {
    object Loading : WatchLaterState()
    object Empty : WatchLaterState()
    data class Success(val films: List<Film>) : WatchLaterState()
    data class Error(val message: String) : WatchLaterState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenProvider: TokenProvider,
    private val getWatchLaterUseCase: GetWatchLaterUseCase,
    private val removeFromWatchLaterUseCase: RemoveFromWatchLaterUseCase
) : ViewModel() {

    private val _watchLaterState = MutableStateFlow<WatchLaterState>(WatchLaterState.Loading)
    val watchLaterState: StateFlow<WatchLaterState> = _watchLaterState

    val name: StateFlow<String?> = tokenProvider.getName()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    init {
        loadWatchLater()
    }

    fun loadWatchLater() {
        viewModelScope.launch {
            _watchLaterState.value = WatchLaterState.Loading
            getWatchLaterUseCase()
                .onSuccess { films ->
                    _watchLaterState.value = if (films.isEmpty()) WatchLaterState.Empty
                    else WatchLaterState.Success(films)
                }
                .onFailure {
                    _watchLaterState.value = WatchLaterState.Error(
                        it.message ?: "Ошибка загрузки"
                    )
                }
        }
    }

    fun removeFromWatchLater(filmId: Int) {
        viewModelScope.launch {
            removeFromWatchLaterUseCase(filmId)
            val current = (_watchLaterState.value as? WatchLaterState.Success)
                ?.films ?: return@launch
            val updated = current.filter { it.id != filmId }
            _watchLaterState.value = if (updated.isEmpty()) WatchLaterState.Empty
            else WatchLaterState.Success(updated)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}