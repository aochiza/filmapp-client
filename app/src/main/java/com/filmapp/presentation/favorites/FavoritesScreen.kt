package com.filmapp.presentation.favorites

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.filmapp.presentation.components.FilmAppEmptyState
import com.filmapp.presentation.components.FilmAppErrorState
import com.filmapp.presentation.components.FilmAppLoading
import com.filmapp.presentation.components.FilmAppTopBar
import com.filmapp.presentation.films.FilmCard
import com.filmapp.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onFilmClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            FilmAppTopBar(title = "Избранное", centered = true)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Crossfade(
            targetState = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            label = "favoritesCrossfade"
        ) { s ->
            when (s) {
                is FavoritesState.Loading -> {
                    FilmAppLoading(
                        modifier = Modifier.fillMaxSize(),
                        message = "Загрузка избранного..."
                    )
                }

                is FavoritesState.Empty -> {
                    FilmAppEmptyState(
                        emoji = "❤️",
                        title = "Избранное пусто",
                        subtitle = "Добавляйте фильмы, нажав на сердечко",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is FavoritesState.Error -> {
                    FilmAppErrorState(
                        title = "Ошибка",
                        message = s.message,
                        onRetry = { viewModel.loadFavorites() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is FavoritesState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = Spacing.xs)
                    ) {
                        items(s.films, key = { it.id }) { film ->
                            FilmCard(
                                film = film,
                                onClick = { onFilmClick(film.id) },
                                onFavoriteClick = {},
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
}
