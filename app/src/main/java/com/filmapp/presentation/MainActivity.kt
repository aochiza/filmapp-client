package com.filmapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.filmapp.data.remote.firebase.FirebaseAuthSource
import com.filmapp.data.remote.interceptor.TokenProvider
import com.filmapp.presentation.navigation.NavGraph
import com.filmapp.presentation.navigation.Screen
import com.filmapp.presentation.theme.FilmAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenProvider: TokenProvider

    @Inject
    lateinit var firebaseAuthSource: FirebaseAuthSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isLoggedIn = runBlocking { tokenProvider.isLoggedIn().first() }
                && firebaseAuthSource.isLoggedIn()

        val startDestination = if (isLoggedIn) Screen.Films.route else Screen.Login.route

        setContent {
            FilmAppTheme {
                NavGraph(startDestination = startDestination)
            }
        }
    }
}