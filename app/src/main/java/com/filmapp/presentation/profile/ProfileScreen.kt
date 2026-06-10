package com.filmapp.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.filmapp.R
import com.filmapp.presentation.components.FilmAppEmptyState
import com.filmapp.presentation.components.FilmAppErrorState
import com.filmapp.presentation.components.FilmAppLoading
import com.filmapp.presentation.components.FilmAppTopBar
import com.filmapp.presentation.components.pressableScale
import com.filmapp.presentation.films.FilmCard
import com.filmapp.presentation.theme.CardShape
import com.filmapp.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val watchLaterState by viewModel.watchLaterState.collectAsState()
    val name by viewModel.name.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LifecycleResumeEffect {
        viewModel.loadWatchLater()
        onPauseOrDispose { }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout_dialog_title)) },
            text = { Text(stringResource(R.string.logout_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.logout_button),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.logout_cancel_button))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = CardShape
        )
    }

    Scaffold(
        topBar = {
            FilmAppTopBar(
                title = stringResource(R.string.profile_title),
                centered = true,
                actions = {
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.pressableScale()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = stringResource(R.string.logout_icon_description),
                            tint = MaterialTheme.colorScheme.error
                        )
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
           //профиль
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        //ава
                        Surface(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        //имя
                        Text(
                            text = name ?: stringResource(R.string.profile_default_name),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            //посмотреть позже
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(R.string.profile_watch_later_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = Spacing.screenHorizontal),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            }

            when (val s = watchLaterState) {
                is WatchLaterState.Loading -> {
                    item {
                        FilmAppLoading(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.xl)
                        )
                    }
                }

                is WatchLaterState.Empty -> {
                    item {
                        FilmAppEmptyState(
                            emoji = stringResource(R.string.watch_later_empty_emoji),
                            title = stringResource(R.string.watch_later_empty_title),
                            subtitle = stringResource(R.string.watch_later_empty_subtitle)
                        )
                    }
                }

                is WatchLaterState.Error -> {
                    item {
                        FilmAppErrorState(
                            title = stringResource(R.string.watch_later_error_title),
                            message = s.message,
                            onRetry = { viewModel.loadWatchLater() }
                        )
                    }
                }

                is WatchLaterState.Success -> {
                    items(s.films, key = { it.id }) { film ->
                        FilmCard(
                            film = film,
                            onClick = {},
                            onFavoriteClick = {},
                            onWatchLaterClick = {
                                viewModel.removeFromWatchLater(film.id)
                            },
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