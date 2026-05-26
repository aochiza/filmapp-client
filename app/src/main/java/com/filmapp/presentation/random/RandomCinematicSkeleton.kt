package com.filmapp.presentation.random

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RandomCinematicSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "cinematicShimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(animation = tween(1400)),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1A1A1F),
            Color(0xFF2A2A35),
            Color(0xFF1A1A1F)
        ),
        start = Offset(translate - 400f, 0f),
        end = Offset(translate, 800f)
    )

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.88f)
                .background(brush)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.12f)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(24.dp)
                    .background(Color(0xFF252530), androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(16.dp)
                    .background(Color(0xFF252530), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            )
        }
    }
}
