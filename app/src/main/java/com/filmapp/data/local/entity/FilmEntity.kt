package com.filmapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "films")
data class FilmEntity(
    @PrimaryKey val id: Int,
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