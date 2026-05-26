package com.filmapp.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle  // ← добавьте
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
) {
    object Films : BottomNavItem(Screen.Films, "Фильмы", Icons.Default.Home)
    object Random : BottomNavItem(Screen.Random, "Случайный", Icons.Default.Shuffle)
    object Search : BottomNavItem(Screen.Search, "Поиск", Icons.Default.Search)
    object Favorites : BottomNavItem(Screen.Favorites, "Избранное", Icons.Default.Favorite)
    object Profile : BottomNavItem(Screen.Profile, "Профиль", Icons.Default.Person)

    companion object {
        val items = listOf(Films, Random, Search, Favorites, Profile)
    }
}