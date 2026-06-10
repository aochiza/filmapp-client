package com.filmapp.presentation.films

import com.filmapp.domain.model.Film
import java.util.Calendar


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

        val POPULAR_COUNTRIES: List<String>
            get() = listOf(
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

        val POPULAR_COUNTRIES_RES_KEYS: List<String>
            get() = listOf(
                "country_usa",
                "country_russia",
                "country_uk",
                "country_france",
                "country_germany",
                "country_japan",
                "country_korea",
                "country_india",
                "country_italy",
                "country_spain"
            )
    }
}

enum class FilmSortOption(val labelResKey: String) {
    DEFAULT("sort_default"),
    YEAR_DESC("sort_year_desc"),
    YEAR_ASC("sort_year_asc"),
    RATING_DESC("sort_rating_desc");
    fun getLabel(): String = labelResKey
}

fun List<Film>.applyAdvancedFilters(filters: FilmsAdvancedFilters): List<Film> {
    // применяем фильтры
    var result = filter { film ->
        film.releaseYear in filters.yearMin..filters.yearMax &&
                (filters.minRating == null || (film.rating ?: 0.0) >= filters.minRating) &&
                (filters.selectedGenreIds.isEmpty() || film.genreId in filters.selectedGenreIds) &&
                film.matchesCountry(filters.country)
    }

    // сортировка
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

    val haystack = listOfNotNull(
        title,
        originalTitle,
        description,
        director
    ).joinToString(" ").lowercase()

    return haystack.contains(country.lowercase())
}