package com.filmapp.presentation.films

import com.filmapp.domain.model.Film
import java.util.Calendar

/**
 * UI-layer filter state. Year, rating, multi-genre and country are applied locally;
 * a single genre id is still sent to the API when exactly one genre is selected.
 */
data class FilmsAdvancedFilters(
    val yearMin: Int = DEFAULT_YEAR_MIN,
    val yearMax: Int = DEFAULT_YEAR_MAX,
    val selectedGenreIds: Set<Int> = emptySet(),
    val minRating: Float? = null,
    val country: String? = null,
    val sortBy: FilmSortOption = FilmSortOption.DEFAULT
) {
    val hasActiveFilters: Boolean
        get() = yearMin != DEFAULT_YEAR_MIN ||
            yearMax != DEFAULT_YEAR_MAX ||
            selectedGenreIds.isNotEmpty() ||
            minRating != null ||
            country != null ||
            sortBy != FilmSortOption.DEFAULT


    val apiGenreId: Int?
        get() = selectedGenreIds.singleOrNull()

    companion object {
        val DEFAULT_YEAR_MIN = 1950
        val DEFAULT_YEAR_MAX: Int = Calendar.getInstance().get(Calendar.YEAR)

        val POPULAR_COUNTRIES = listOf(
            "США",
            "Россия",
            "Великобритания",
            "Франция",
            "Германия",
            "Япония",
            "Корея",
            "Индия",
            "Италия",
            "Испания"
        )
    }
}

enum class FilmSortOption(val label: String) {
    DEFAULT("По умолчанию"),
    YEAR_DESC("Сначала новые"),
    YEAR_ASC("Сначала старые"),
    RATING_DESC("По рейтингу")
}

fun List<Film>.applyAdvancedFilters(filters: FilmsAdvancedFilters): List<Film> {
    var result = filter { film ->
        film.releaseYear in filters.yearMin..filters.yearMax &&
            (filters.minRating == null || (film.rating ?: 0.0) >= filters.minRating) &&
            (filters.selectedGenreIds.isEmpty() || film.genreId in filters.selectedGenreIds) &&
            film.matchesCountry(filters.country)
    }

    result = when (filters.sortBy) {
        FilmSortOption.DEFAULT -> result
        FilmSortOption.YEAR_DESC -> result.sortedByDescending { it.releaseYear }
        FilmSortOption.YEAR_ASC -> result.sortedBy { it.releaseYear }
        FilmSortOption.RATING_DESC -> result.sortedByDescending { it.rating ?: 0.0 }
    }

    return result
}

private fun Film.matchesCountry(country: String?): Boolean {
    if (country.isNullOrBlank()) return true
    val haystack = listOfNotNull(title, originalTitle, description, director)
        .joinToString(" ")
        .lowercase()
    return haystack.contains(country.lowercase())
}
