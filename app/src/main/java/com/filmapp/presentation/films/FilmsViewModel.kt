package com.filmapp.presentation.films

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmapp.R
import com.filmapp.data.local.SearchHistoryStore
import com.filmapp.domain.model.Film
import com.filmapp.domain.model.Genre
import com.filmapp.domain.repository.AuthRepository
import com.filmapp.domain.usecase.film.AddToFavoritesUseCase
import com.filmapp.domain.usecase.film.AddToWatchLaterUseCase
import com.filmapp.domain.usecase.film.DeleteFilmUseCase
import com.filmapp.domain.usecase.film.GetFilmsUseCase
import com.filmapp.domain.usecase.film.RemoveFromWatchLaterUseCase
import com.filmapp.domain.usecase.genre.GetGenresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
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
    private val getGenresUseCase: GetGenresUseCase,
    private val authRepository: AuthRepository,
    private val deleteFilmUseCase: DeleteFilmUseCase,
    private val searchHistoryStore: SearchHistoryStore,
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

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    val searchHistory: StateFlow<List<String>> = searchHistoryStore.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var currentPage = 1
    private var isLastPage = false
    private var isLoadingPage = false
    private var loadJob: Job? = null
    private val allFilms = mutableListOf<Film>()

    init {
        //получаем роль
        viewModelScope.launch {
            authRepository.getCurrentRole().collect { role ->
                _isAdmin.value = role == "ADMIN"
            }
        }

        viewModelScope.launch {
            _searchQuery
                .debounce(400)
                .distinctUntilChanged()//не ищем дважды
                .drop(1)//пропуск пустое
                .collect {
                    resetPagination()
                    loadFilms()
                }
        }

        loadGenres()
        loadFilms()
    }


    fun loadFilms() {
        if (isLoadingPage || (currentPage > 1 && isLastPage)) return

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            isLoadingPage = true
            val pageToLoad = currentPage

            try {
                if (pageToLoad == 1) {
                    _filmsState.value = FilmsState.Loading
                }

                val search = _searchQuery.value.ifBlank { null }
                val genreId = _advancedFilters.value.apiGenreId ?: _selectedGenreId.value

                getFilmsUseCase(pageToLoad, 20, search, genreId)
                    .onSuccess { films ->
                        if (films.isEmpty()) {
                            isLastPage = true
                        } else {
                            films.forEach { film ->
                                if (allFilms.none { it.id == film.id }) {
                                    allFilms.add(film)
                                }
                            }
                            currentPage++
                        }

                        //сохраняем запрос в историю
                        val trimmedQuery = _searchQuery.value.trim()
                        if (pageToLoad == 1 && trimmedQuery.isNotBlank()) {
                            searchHistoryStore.addQuery(trimmedQuery)
                        }

                        publishFilteredList()
                    }
                    .onFailure { error ->
                        if (pageToLoad == 1) {
                            _filmsState.value = FilmsState.Error(
                                error.message ?: getDefaultErrorMessage()
                            )
                        }
                    }
            } finally {
                isLoadingPage = false
            }
        }
    }


    fun loadNextPage() {
        if (isLastPage || isLoadingPage) return
        loadFilms()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }


    fun onHistoryQuerySelected(query: String) {
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
            updateFilmInList(film.copy(isFavorite = !film.isFavorite))
        }
    }

    fun toggleWatchLater(film: Film) {
        viewModelScope.launch {
            if (film.isWatchLater) {
                removeFromWatchLaterUseCase(film.id)
            } else {
                addToWatchLaterUseCase(film.id)
            }
            updateFilmInList(film.copy(isWatchLater = !film.isWatchLater))
        }
    }


    fun deleteFilm(filmId: Int) {
        viewModelScope.launch {
            deleteFilmUseCase(filmId)
                .onSuccess {
                    val updated = allFilms.filter { it.id != filmId }
                    allFilms.clear()
                    allFilms.addAll(updated)
                    publishFilteredList()
                }
                .onFailure { error ->
                    _filmsState.value = FilmsState.Error(
                        error.message ?: getDeleteErrorMessage()
                    )
                }
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            getGenresUseCase().onSuccess { genres ->
                _genres.value = genres
            }
        }
    }

    //сброс при новом посике
    private fun resetPagination() {
        loadJob?.cancel()
        isLoadingPage = false
        currentPage = 1
        isLastPage = false
        allFilms.clear()
    }

    //применение фильтров
    private fun publishFilteredList() {
        val filtered = allFilms.applyAdvancedFilters(_advancedFilters.value)
        _filmsState.value = FilmsState.Success(filtered)
    }


    private fun updateFilmInList(updatedFilm: Film) {
        val updatedList = allFilms.map { film ->
            if (film.id == updatedFilm.id) updatedFilm else film
        }
        allFilms.clear()
        allFilms.addAll(updatedList)
        publishFilteredList()
    }

    private fun getDefaultErrorMessage(): String = "Ошибка загрузки"
    private fun getDeleteErrorMessage(): String = "Ошибка удаления фильма"
}