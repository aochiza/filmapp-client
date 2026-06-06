package com.filmapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FilmResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("originalTitle") val originalTitle: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("releaseYear") val releaseYear: Int,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("posterUrl") val posterUrl: String?,
    @SerializedName("genreId") val genreId: Int?,
    @SerializedName("genreName") val genreName: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("isFavorite") val isFavorite: Boolean = false,
    @SerializedName("isWatchLater") val isWatchLater: Boolean = false
)

data class FilmRequest(
    @SerializedName("title") val title: String,
    @SerializedName("originalTitle") val originalTitle: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("releaseYear") val releaseYear: Int,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("posterUrl") val posterUrl: String? = null,
    @SerializedName("genreId") val genreId: Int? = null,
    @SerializedName("director") val director: String? = null,
    @SerializedName("duration") val duration: Int? = null
)

data class FilmsListResponse(
    @SerializedName("films") val films: List<FilmResponse>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("pageSize") val pageSize: Int
)

data class GenreResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?
)

data class WatchLaterEntryResponse(
    @SerializedName("film") val film: FilmResponse?,
    @SerializedName("filmId") val filmId: Int? = null
)

data class WatchLaterListResponse(
    @SerializedName("films") val films: List<FilmResponse>? = null,
    @SerializedName("items") val items: List<FilmResponse>? = null
)