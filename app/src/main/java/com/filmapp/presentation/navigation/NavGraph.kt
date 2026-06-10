package com.filmapp.presentation.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.filmapp.presentation.auth.AuthViewModel
import com.filmapp.presentation.auth.LoginScreen
import com.filmapp.presentation.auth.RegisterScreen
import com.filmapp.presentation.detail.FilmDetailScreen
import com.filmapp.presentation.favorites.FavoritesScreen
import com.filmapp.presentation.films.FilmsScreen
import com.filmapp.presentation.profile.ProfileScreen
import com.filmapp.presentation.films.AddEditFilmScreen
import com.filmapp.presentation.random.RandomScreen
import com.filmapp.presentation.search.SearchScreen
import com.filmapp.presentation.theme.Elevation

@Composable
fun NavGraph(
    startDestination: String
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarScreens = listOf(
        Screen.Films.route,
        Screen.Random.route,
        Screen.Search.route,
        Screen.Favorites.route,
        Screen.Profile.route
    )
    val showBottomBar = currentDestination?.route in bottomBarScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    tonalElevation = Elevation.bottomBar,
                    shadowElevation = Elevation.bottomBar,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        BottomNavItem.items.forEach { item ->
                            val selected = currentDestination?.hierarchy
                                ?.any { it.route == item.screen.route } == true

                            val iconScale by animateFloatAsState(
                                targetValue = if (selected) 1.1f else 1f,
                                animationSpec = spring(
                                    dampingRatio = 0.6f,
                                    stiffness = 400f
                                ),
                                label = "navIconScale"
                            )

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.scale(iconScale)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        //контейнер маршрутов
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(
                route = Screen.Login.route,
                enterTransition = { filmEnterTransition() },
                exitTransition = { filmExitTransition() }
            ) {
                val viewModel = hiltViewModel<AuthViewModel>()
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Films.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(
                route = Screen.Register.route,
                enterTransition = { filmEnterTransition() },
                exitTransition = { filmExitTransition() },
                popEnterTransition = { filmPopEnterTransition() },
                popExitTransition = { filmPopExitTransition() }
            ) {
                val viewModel = hiltViewModel<AuthViewModel>()
                RegisterScreen(
                    viewModel = viewModel,
                    onRegisterSuccess = {
                        navController.navigate(Screen.Films.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.Films.route) {
                FilmsScreen(
                    onFilmClick = { filmId ->
                        navController.navigate(
                            Screen.FilmDetail.createRoute(filmId)
                        )
                    },
                    onAddFilmClick = {
                        navController.navigate(Screen.AddFilm.route)
                    }
                )
            }

            composable(Screen.Random.route) {
                RandomScreen(
                    onFilmClick = { filmId ->
                        navController.navigate(Screen.FilmDetail.createRoute(filmId))
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onFilmClick = { filmId ->
                        navController.navigate(Screen.FilmDetail.createRoute(filmId))
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onFilmClick = { filmId ->
                        navController.navigate(Screen.FilmDetail.createRoute(filmId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AddFilm.route) {
                AddEditFilmScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.FilmDetail.route,
                arguments = listOf(navArgument("filmId") { type = NavType.IntType }),
                enterTransition = { filmEnterTransition() },
                exitTransition = { filmExitTransition() },
                popEnterTransition = { filmPopEnterTransition() },
                popExitTransition = { filmPopExitTransition() }
            ) { backStackEntry ->
                val filmId = backStackEntry.arguments?.getInt("filmId") ?: return@composable
                FilmDetailScreen(
                    filmId = filmId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
