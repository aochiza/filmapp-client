package com.filmapp.presentation.random

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filmapp.presentation.components.FilmAppErrorState
import com.filmapp.presentation.theme.Spacing

@Composable
fun RandomScreen(
    onFilmClick: (Int) -> Unit = {},
    viewModel: RandomViewModel = hiltViewModel()
) {
    val randomState by viewModel.randomState.collectAsStateWithLifecycle()
    val genres by viewModel.genres.collectAsStateWithLifecycle()
    val selectedGenreId by viewModel.selectedGenreId.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AnimatedContent(
            targetState = randomState,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                when {
                    initialState is RandomState.Loading && targetState is RandomState.Success ->
                        fadeIn(tween(450)) togetherWith fadeOut(tween(200))

                    targetState is RandomState.Loading ->
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))

                    else -> {
                        val target = targetState
                        val initial = initialState

                        val direction =
                            if (target is RandomState.Success &&
                                initial is RandomState.Success
                            ) {
                                if (target.film.id > initial.film.id) 1 else -1
                            } else {
                                1
                            }
                        (slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            initialOffsetX = { fullWidth -> direction * fullWidth / 3 }
                        ) + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                targetOffsetX = { fullWidth -> -direction * fullWidth / 3 }
                            ) + fadeOut(tween(280)))
                    }
                }
            },
            label = "randomStateContent"
        ) { state ->
            when (state) {
                is RandomState.Loading -> {
                    RandomCinematicSkeleton(modifier = Modifier.fillMaxSize())
                }

                is RandomState.Success -> {
                    CinematicRandomCard(
                        film = state.film,
                        isFavorite = state.isFavorite,
                        onSwipeComplete = {
                            viewModel.loadRandomFilm(
                                genreId = selectedGenreId,
                                showLoading = false
                            )
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(state.film.id) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is RandomState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        FilmAppErrorState(
                            title = "Не удалось загрузить",
                            message = state.message,
                            onRetry = { viewModel.loadRandomFilm(selectedGenreId) }
                        )
                    }
                }
            }
        }

        // Category chips overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = Spacing.xs)
        ) {
            AnimatedVisibility(
                visible = genres.isNotEmpty(),
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(200))
            ) {
                RandomCategoryChips(
                    genres = genres,
                    selectedGenreId = selectedGenreId,
                    onGenreSelected = viewModel::onGenreSelected
                )
            }
        }
    }
}
