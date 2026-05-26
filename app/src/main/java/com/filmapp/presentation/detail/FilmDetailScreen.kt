package com.filmapp.presentation.detail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.filmapp.presentation.components.FilmAppErrorState
import com.filmapp.presentation.components.FilmAppLoading
import com.filmapp.presentation.components.FilmAppTopBar
import com.filmapp.presentation.components.pressableScale
import com.filmapp.presentation.theme.AccentGold
import com.filmapp.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmDetailScreen(
    filmId: Int,
    onBack: () -> Unit,
    viewModel: FilmDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(filmId) {
        viewModel.loadFilm(filmId)
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    val topBarTitle = when (val s = state) {
        is FilmDetailState.Success -> s.film.title
        else -> ""
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FilmAppTopBar(
                title = topBarTitle,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBack,
                centered = true,
                actions = {
                    if (state is FilmDetailState.Success) {
                        val film = (state as FilmDetailState.Success).film

                        IconButton(
                            onClick = { viewModel.toggleWatchLater() },
                            modifier = Modifier.pressableScale()
                        ) {
                            Icon(
                                imageVector = if (film.isWatchLater) {
                                    Icons.Default.Bookmark
                                } else {
                                    Icons.Default.BookmarkBorder
                                },
                                contentDescription = "Посмотреть позже",
                                tint = if (film.isWatchLater) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleFavorite() },
                            modifier = Modifier.pressableScale()
                        ) {
                            Icon(
                                imageVector = if (film.isFavorite) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                                contentDescription = "Избранное",
                                tint = if (film.isFavorite) {
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
        Crossfade(
            targetState = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            label = "detailCrossfade"
        ) { s ->
            when (s) {
                is FilmDetailState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FilmAppLoading(message = "Загрузка фильма...")
                    }
                }

                is FilmDetailState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        FilmAppErrorState(
                            title = "Ошибка загрузки",
                            message = s.message,
                            onRetry = { viewModel.loadFilm(filmId) }
                        )
                    }
                }

                is FilmDetailState.Success -> {
                    val film = s.film
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            AsyncImage(
                                model = film.posterUrl,
                                contentDescription = film.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                androidx.compose.ui.graphics.Color.Transparent,
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                                                MaterialTheme.colorScheme.background
                                            ),
                                            startY = 120f
                                        )
                                    )
                            )
                        }

                        Column(
                            modifier = Modifier.padding(Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Text(
                                text = film.title,
                                style = MaterialTheme.typography.headlineMedium
                            )

                            film.originalTitle?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                film.rating?.let { rating ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 6.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = AccentGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = String.format("%.1f", rating),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = AccentGold
                                            )
                                        }
                                    }
                                }
                                InfoChip(text = film.releaseYear.toString())
                                film.genreName?.let { InfoChip(text = it) }
                                film.duration?.let { InfoChip(text = "$it мин") }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )

                            film.director?.let {
                                DetailRow(label = "Режиссёр", value = it)
                            }

                            film.description?.let { description ->
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Описание",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Spacing.md))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
