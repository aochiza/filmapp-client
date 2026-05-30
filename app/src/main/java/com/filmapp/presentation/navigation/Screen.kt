package com.filmapp.presentation.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Register : Screen("register")

    object Films : Screen("films")
    object Random : Screen("random")
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object Profile : Screen("profile")

    object AddFilm : Screen("add_film")

    object FilmDetail : Screen("film_detail/{filmId}") {
        fun createRoute(filmId: Int): String {
            return "film_detail/$filmId"
        }
    }
}