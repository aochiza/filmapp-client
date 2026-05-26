package com.filmapp.presentation.films

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmapp.domain.model.Film
import com.filmapp.domain.model.Genre
import com.filmapp.domain.usecase.film.AddToFavoritesUseCase
import com.filmapp.domain.usecase.film.AddToWatchLaterUseCase
import com.filmapp.domain.usecase.film.GetFilmsUseCase
import com.filmapp.domain.usecase.film.RemoveFromWatchLaterUseCase
import com.filmapp.domain.usecase.genre.GetGenresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FilmsState {
    data object Loading : FilmsState()
    data class Success(val films: List<Film>) : FilmsState()
    data class Error(val message: String) : FilmsState()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class FilmsViewModel @Inject constructor(
    private val getFilmsUseCase: GetFilmsUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val addToWatchLaterUseCase: AddToWatchLaterUseCase,
    private val removeFromWatchLaterUseCase: RemoveFromWatchLaterUseCase,
    private val getGenresUseCase: GetGenresUseCase
) : ViewModel() {

    private val _filmsState = MutableStateFlow<FilmsState>(FilmsState.Loading)
    val filmsState: StateFlow<FilmsState> = _filmsState

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId

    private val _advancedFilters = MutableStateFlow(FilmsAdvancedFilters())
    val advancedFilters: StateFlow<FilmsAdvancedFilters> = _advancedFilters

    private var currentPage = 1
    private var isLastPage = false
    private val allFilms = mutableListOf<Film>()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .distinctUntilChanged()
                .collect {
                    resetPagination()
                    loadFilms()
                }
        }
        loadGenres()
        loadFilms()
    }

    fun loadFilms() {
        viewModelScope.launch {
            if (currentPage == 1) _filmsState.value = FilmsState.Loading

            val search = _searchQuery.value.ifBlank { null }
            val genreId = _advancedFilters.value.apiGenreId ?: _selectedGenreId.value

            getFilmsUseCase(currentPage, 20, search, genreId)
                .onSuccess { films ->
                    if (films.isEmpty()) {
                        isLastPage = true
                    } else {
                        allFilms.addAll(films)
                        currentPage++
                    }
                    publishFilteredList()
                }
                .onFailure {
                    _filmsState.value = FilmsState.Error(it.message ?: "Ошибка загрузки")
                }
        }
    }

    fun loadNextPage() {
        if (isLastPage) return
        loadFilms()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onGenreSelected(genreId: Int?) {
        _selectedGenreId.value = genreId
        _advancedFilters.value = _advancedFilters.value.copy(
            selectedGenreIds = if (genreId != null) setOf(genreId) else emptySet()
        )
        resetPagination()
        loadFilms()
    }

    fun applyAdvancedFilters(filters: FilmsAdvancedFilters) {
        _advancedFilters.value = filters
        _selectedGenreId.value = filters.apiGenreId
        resetPagination()
        loadFilms()
    }

    fun resetAdvancedFilters() {
        _advancedFilters.value = FilmsAdvancedFilters()
        _selectedGenreId.value = null
        resetPagination()
        loadFilms()
    }

    fun toggleFavorite(film: Film) {
        viewModelScope.launch {
            if (!film.isFavorite) {
                addToFavoritesUseCase(film.id)
            }
            val updated = allFilms.map {
                if (it.id == film.id) it.copy(isFavorite = !it.isFavorite) else it
            }
            allFilms.clear()
            allFilms.addAll(updated)
            publishFilteredList()
        }
    }

    fun toggleWatchLater(film: Film) {
        viewModelScope.launch {
            if (film.isWatchLater) {
                removeFromWatchLaterUseCase(film.id)
            } else {
                addToWatchLaterUseCase(film.id)
            }
            val updated = allFilms.map {
                if (it.id == film.id) it.copy(isWatchLater = !it.isWatchLater) else it
            }
            allFilms.clear()
            allFilms.addAll(updated)
            publishFilteredList()
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            getGenresUseCase().onSuccess { _genres.value = it }
        }
    }

    private fun resetPagination() {
        currentPage = 1
        isLastPage = false
        allFilms.clear()
    }

    private fun publishFilteredList() {
        val filtered = allFilms.applyAdvancedFilters(_advancedFilters.value)
        _filmsState.value = FilmsState.Success(filtered)
    }
}
