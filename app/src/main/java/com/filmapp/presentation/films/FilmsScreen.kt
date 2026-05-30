package com.filmapp.presentation.films

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.filmapp.presentation.components.FilmAppErrorState
import com.filmapp.presentation.components.FilmAppTopBar
import com.filmapp.presentation.components.FilmAppEmptyState
import com.filmapp.presentation.components.FilmCardSkeleton
import com.filmapp.presentation.components.SearchHistorySection
import com.filmapp.presentation.components.pressableScale
import com.filmapp.presentation.theme.Spacing
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import com.filmapp.presentation.theme.TextFieldShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmsScreen(
    onFilmClick: (Int) -> Unit,
    onAddFilmClick: () -> Unit,
    viewModel: FilmsViewModel = hiltViewModel()
) {
    val filmsState by viewModel.filmsState.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGenreId by viewModel.selectedGenreId.collectAsState()
    val advancedFilters by viewModel.advancedFilters.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val listState = rememberLazyListState()

    var showFilterSheet by remember { mutableStateOf(false) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val total = listState.layoutInfo.totalItemsCount
            lastVisible != null && lastVisible.index >= total - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    if (showFilterSheet) {
        FilmsFilterBottomSheet(
            genres = genres,
            initialFilters = advancedFilters,
            onDismiss = { showFilterSheet = false },
            onApply = viewModel::applyAdvancedFilters,
            onReset = viewModel::resetAdvancedFilters
        )
    }

    Scaffold(
        topBar = {
            FilmAppTopBar(
                title = "FilmApp",
                actions = {
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.pressableScale()
                    ) {
                        BadgedBox(
                            badge = {
                                if (advancedFilters.hasActiveFilters) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Фильтры",
                                tint = if (advancedFilters.hasActiveFilters) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onAddFilmClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить фильм"
                    )
                }
            }
        },

        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = Spacing.md)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            "Поиск фильмов...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    singleLine = true,
                    shape = TextFieldShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    )
                )
            }

            if (searchQuery.isBlank()) {
                item {
                    SearchHistorySection(
                        queries = searchHistory,
                        onQueryClick = viewModel::onHistoryQuerySelected
                    )
                }
            }

            if (genres.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal),
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    ) {
                        item {
                            AllGenresChip(
                                isSelected = selectedGenreId == null &&
                                    advancedFilters.selectedGenreIds.isEmpty(),
                                onClick = { viewModel.onGenreSelected(null) }
                            )
                        }
                        items(genres, key = { it.id }) { genre ->
                            GenreChip(
                                genre = genre,
                                isSelected = selectedGenreId == genre.id ||
                                    genre.id in advancedFilters.selectedGenreIds,
                                onClick = { viewModel.onGenreSelected(genre.id) }
                            )
                        }
                    }
                }
            }

            when (val state = filmsState) {
                is FilmsState.Loading -> {
                    items(6) {
                        FilmCardSkeleton(
                            modifier = Modifier.padding(
                                horizontal = Spacing.screenHorizontal,
                                vertical = Spacing.cardVertical
                            )
                        )
                    }
                }

                is FilmsState.Error -> {
                    item {
                        FilmAppErrorState(
                            title = "Ошибка загрузки",
                            message = state.message,
                            onRetry = { viewModel.loadFilms() }
                        )
                    }
                }

                is FilmsState.Success -> {
                    if (state.films.isEmpty()) {
                        item {
                            FilmAppEmptyState(
                                emoji = "🎬",
                                title = "Фильмы не найдены",
                                subtitle = "Измените фильтры или поисковый запрос"
                            )
                        }
                    } else {
                        items(state.films, key = { it.id }) { film ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                val dismissState =
                                    rememberSwipeToDismissBoxState(
                                        confirmValueChange = { value ->

                                            if (
                                                value ==
                                                SwipeToDismissBoxValue.EndToStart
                                            ) {
                                                viewModel.deleteFilm(film.id)
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                    )

                                if (isAdmin) {

                                    SwipeToDismissBox(
                                        state = dismissState,
                                        enableDismissFromStartToEnd = false,

                                        backgroundContent = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(
                                                        horizontal = Spacing.screenHorizontal,
                                                        vertical = Spacing.cardVertical
                                                    ),
                                                contentAlignment =
                                                    Alignment.CenterEnd
                                            ) {
                                                Icon(
                                                    imageVector =
                                                        Icons.Default.Delete,
                                                    contentDescription =
                                                        "Удалить"
                                                )
                                            }
                                        }
                                    ) {

                                        FilmCard(
                                            film = film,
                                            onClick = {
                                                onFilmClick(film.id)
                                            },
                                            onFavoriteClick = {
                                                viewModel.toggleFavorite(film)
                                            },
                                            onWatchLaterClick = {
                                                viewModel.toggleWatchLater(film)
                                            },
                                            modifier = Modifier.padding(
                                                horizontal =
                                                    Spacing.screenHorizontal,
                                                vertical =
                                                    Spacing.cardVertical
                                            )
                                        )
                                    }

                                } else {

                                    FilmCard(
                                        film = film,
                                        onClick = {
                                            onFilmClick(film.id)
                                        },
                                        onFavoriteClick = {
                                            viewModel.toggleFavorite(film)
                                        },
                                        onWatchLaterClick = {
                                            viewModel.toggleWatchLater(film)
                                        },
                                        modifier = Modifier.padding(
                                            horizontal =
                                                Spacing.screenHorizontal,
                                            vertical =
                                                Spacing.cardVertical
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
