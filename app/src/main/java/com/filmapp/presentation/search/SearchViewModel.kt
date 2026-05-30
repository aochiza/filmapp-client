package com.filmapp.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmapp.data.local.SearchHistoryStore
import com.filmapp.domain.model.Film
import com.filmapp.domain.model.Genre
import com.filmapp.domain.usecase.film.AddToFavoritesUseCase
import com.filmapp.domain.usecase.film.GetFilmsUseCase
import com.filmapp.domain.usecase.genre.GetGenresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    object Empty : SearchState()
    data class Success(val films: List<Film>) : SearchState()
    data class Error(val message: String) : SearchState()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getFilmsUseCase: GetFilmsUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val searchHistoryStore: SearchHistoryStore,
) : ViewModel() {

    private val _state = MutableStateFlow<SearchState>(SearchState.Idle)
    val state: StateFlow<SearchState> = _state

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId

    private val _minRating = MutableStateFlow<Float?>(null)
    val minRating: StateFlow<Float?> = _minRating

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear

    val searchHistory: StateFlow<List<String>> = searchHistoryStore.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        loadGenres()
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(400).distinctUntilChanged(),
                _selectedGenreId,
                _minRating,
                _selectedYear
            ) { query, genreId, rating, year ->
                SearchParams(query, genreId, rating, year)
            }.collect { params ->
                if (params.query.isBlank() &&
                    params.genreId == null &&
                    params.rating == null &&
                    params.year == null
                ) {
                    _state.value = SearchState.Idle
                } else {
                    search(params)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onHistoryQuerySelected(query: String) {
        _searchQuery.value = query
    }

    fun onGenreSelected(genreId: Int?) {
        _selectedGenreId.value = genreId
    }

    fun onMinRatingChanged(rating: Float?) {
        _minRating.value = rating
    }

    fun onYearSelected(year: Int?) {
        _selectedYear.value = year
    }

    fun clearFilters() {
        _selectedGenreId.value = null
        _minRating.value = null
        _selectedYear.value = null
    }

    fun toggleFavorite(film: Film) {
        viewModelScope.launch {
            addToFavoritesUseCase(film.id)
            val current = (_state.value as? SearchState.Success)?.films ?: return@launch
            _state.value = SearchState.Success(
                current.map {
                    if (it.id == film.id) it.copy(isFavorite = !it.isFavorite) else it
                }
            )
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            getGenresUseCase().onSuccess { _genres.value = it }
        }
    }

    private suspend fun search(params: SearchParams) {
        _state.value = SearchState.Loading
        getFilmsUseCase(
            search = params.query.ifBlank { null },
            genreId = params.genreId
        ).onSuccess { films ->
            val trimmedQuery = params.query.trim()
            if (trimmedQuery.isNotBlank()) {
                searchHistoryStore.addQuery(trimmedQuery)
            }

            val filtered = films
                .filter { params.rating == null || (it.rating ?: 0.0) >= params.rating }
                .filter { params.year == null || it.releaseYear == params.year }
            _state.value = if (filtered.isEmpty()) SearchState.Empty
            else SearchState.Success(filtered)
        }.onFailure {
            _state.value = SearchState.Error(it.message ?: "Ошибка поиска")
        }
    }

    private data class SearchParams(
        val query: String,
        val genreId: Int?,
        val rating: Float?,
        val year: Int?
    )
}
