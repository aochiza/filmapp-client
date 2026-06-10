package com.filmapp.presentation.films

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.filmapp.R
import com.filmapp.domain.model.Genre

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFilmScreen(
    onBack: () -> Unit,
    viewModel: AddFilmViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsState()
    val genres by viewModel.genres.collectAsState()

    var title by remember { mutableStateOf("") }
    var originalTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var releaseYear by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var posterUrl by remember { mutableStateOf("") }
    var director by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var selectedGenre by remember {
        mutableStateOf<Genre?>(null)
    }

    //отслеж успеш создание и закрывает
    LaunchedEffect(state) {
        if (state is AddFilmState.Success) {
            viewModel.resetState()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.add_film_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.field_title)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = originalTitle,
                onValueChange = { originalTitle = it },
                label = {
                    Text(stringResource(R.string.field_original_title))
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.field_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            OutlinedTextField(
                value = releaseYear,
                onValueChange = { releaseYear = it },
                label = { Text(stringResource(R.string.field_release_year)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = rating,
                onValueChange = { rating = it },
                label = { Text(stringResource(R.string.field_rating)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = posterUrl,
                onValueChange = { posterUrl = it },
                label = { Text(stringResource(R.string.field_poster_url)) },
                modifier = Modifier.fillMaxWidth()
            )

            //выпадающий
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                }
            ) {
                OutlinedTextField(
                    value = selectedGenre?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.field_genre)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    genres.forEach { genre ->
                        DropdownMenuItem(
                            text = {
                                Text(genre.name)
                            },
                            onClick = {
                                selectedGenre = genre
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = director,
                onValueChange = { director = it },
                label = { Text(stringResource(R.string.field_director)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = {
                    Text(stringResource(R.string.field_duration))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (state is AddFilmState.Error) {
                Text(
                    text = (state as AddFilmState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    viewModel.createFilm(
                        title = title,
                        originalTitle = originalTitle,
                        description = description,
                        releaseYear = releaseYear,
                        rating = rating,
                        posterUrl = posterUrl,
                        genreId = selectedGenre?.id,
                        director = director,
                        duration = duration
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is AddFilmState.Loading
            ) {
                if (state is AddFilmState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.add_film_button))
                }
            }
        }
    }
}