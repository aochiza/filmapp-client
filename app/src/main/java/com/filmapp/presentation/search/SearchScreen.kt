package com.filmapp.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import com.filmapp.R
import com.filmapp.presentation.components.FilmAppEmptyState
import com.filmapp.presentation.components.FilmAppTopBar
import com.filmapp.presentation.components.FilmCardSkeleton
import com.filmapp.presentation.components.SearchHistorySection
import com.filmapp.presentation.components.pressableScale
import com.filmapp.presentation.films.AllGenresChip
import com.filmapp.presentation.films.FilmCard
import com.filmapp.presentation.films.GenreChip
import com.filmapp.presentation.theme.Spacing
import com.filmapp.presentation.theme.TextFieldShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onFilmClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGenreId by viewModel.selectedGenreId.collectAsState()
    val minRating by viewModel.minRating.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val focusManager = LocalFocusManager.current

    var showFilters by remember { mutableStateOf(false) }
    val hasActiveFilters = selectedGenreId != null || minRating != null || selectedYear != null

    Scaffold(
        topBar = {
            FilmAppTopBar(
                title = stringResource(R.string.search_title),
                centered = true,
                actions = {
                    IconButton(
                        onClick = { showFilters = !showFilters },
                        modifier = Modifier.pressableScale()
                    ) {
                        BadgedBox(
                            badge = {
                                if (hasActiveFilters) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = stringResource(R.string.filter_button),
                                tint = if (hasActiveFilters) {
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = Spacing.md)
        ) {
            //поиск
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            stringResource(R.string.search_placeholder),
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
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.clear_search)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = TextFieldShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    )
                )
            }

            //история поиска
            if (searchQuery.isBlank()) {
                item {
                    SearchHistorySection(
                        queries = searchHistory,
                        onQueryClick = viewModel::onHistoryQuerySelected
                    )
                }
            }

            // фильтры
            item {
                AnimatedVisibility(
                    visible = showFilters,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text(
                            text = stringResource(R.string.filter_genre_label),
                            style = MaterialTheme.typography.titleMedium
                        )
                        LazyRow {
                            item {
                                AllGenresChip(
                                    isSelected = selectedGenreId == null,
                                    onClick = { viewModel.onGenreSelected(null) }
                                )
                            }
                            items(genres, key = { it.id }) { genre ->
                                GenreChip(
                                    genre = genre,
                                    isSelected = selectedGenreId == genre.id,
                                    onClick = { viewModel.onGenreSelected(genre.id) }
                                )
                            }
                        }

                        //рейтинг
                        Text(
                            text = stringResource(
                                R.string.filter_rating_label,
                                minRating?.let { String.format("%.1f", it) } ?: stringResource(R.string.filter_rating_any)
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Slider(
                            value = minRating ?: 0f,
                            onValueChange = {
                                viewModel.onMinRatingChanged(if (it == 0f) null else it)
                            },
                            valueRange = 0f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // сброс
                        if (hasActiveFilters) {
                            TextButton(
                                onClick = { viewModel.clearFilters() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = stringResource(R.string.filter_clear_button),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                    }
                }
            }

            when (val s = state) {
                is SearchState.Idle -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Spacing.xxl),
                            contentAlignment = Alignment.Center
                        ) {
                            FilmAppEmptyState(
                                emoji = stringResource(R.string.search_idle_emoji),
                                title = stringResource(R.string.search_idle_title),
                                subtitle = stringResource(R.string.search_idle_subtitle)
                            )
                        }
                    }
                }

                is SearchState.Loading -> {
                    items(4) {
                        FilmCardSkeleton(
                            modifier = Modifier.padding(
                                horizontal = Spacing.screenHorizontal,
                                vertical = Spacing.cardVertical
                            )
                        )
                    }
                }

                is SearchState.Empty -> {
                    item {
                        FilmAppEmptyState(
                            emoji = stringResource(R.string.search_empty_emoji),
                            title = stringResource(R.string.search_empty_title),
                            subtitle = stringResource(R.string.search_empty_subtitle)
                        )
                    }
                }

                is SearchState.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.xl),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = s.message ?: stringResource(R.string.search_error_default),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                is SearchState.Success -> {
                    items(s.films, key = { it.id }) { film ->
                        FilmCard(
                            film = film,
                            onClick = { onFilmClick(film.id) },
                            onFavoriteClick = { viewModel.toggleFavorite(film) },
                            modifier = Modifier.padding(
                                horizontal = Spacing.screenHorizontal,
                                vertical = Spacing.cardVertical
                            )
                        )
                    }
                }
            }
        }
    }
}