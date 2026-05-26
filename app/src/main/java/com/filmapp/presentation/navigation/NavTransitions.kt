package com.filmapp.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val TRANSITION_DURATION = 300

fun AnimatedContentTransitionScope<*>.filmEnterTransition(): EnterTransition =
    fadeIn(animationSpec = tween(TRANSITION_DURATION)) +
        slideInHorizontally(
            animationSpec = tween(TRANSITION_DURATION),
            initialOffsetX = { it / 4 }
        )

fun AnimatedContentTransitionScope<*>.filmExitTransition(): ExitTransition =
    fadeOut(animationSpec = tween(TRANSITION_DURATION)) +
        slideOutHorizontally(
            animationSpec = tween(TRANSITION_DURATION),
            targetOffsetX = { -it / 4 }
        )

fun AnimatedContentTransitionScope<*>.filmPopEnterTransition(): EnterTransition =
    fadeIn(animationSpec = tween(TRANSITION_DURATION)) +
        slideInHorizontally(
            animationSpec = tween(TRANSITION_DURATION),
            initialOffsetX = { -it / 4 }
        )

fun AnimatedContentTransitionScope<*>.filmPopExitTransition(): ExitTransition =
    fadeOut(animationSpec = tween(TRANSITION_DURATION)) +
        slideOutHorizontally(
            animationSpec = tween(TRANSITION_DURATION),
            targetOffsetX = { it / 4 }
        )
