package com.filmapp.domain.model

data class Film(
    val id: Int,
    val title: String,
    val originalTitle: String?,
    val description: String?,
    val releaseYear: Int,
    val rating: Double?,
    val posterUrl: String?,
    val genreId: Int?,
    val genreName: String?,
    val director: String?,
    val duration: Int?,
    val isFavorite: Boolean = false,
    val isWatchLater: Boolean = false
)