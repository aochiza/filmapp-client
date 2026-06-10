package com.filmapp.presentation.random

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.filmapp.R
import com.filmapp.domain.model.Film
import com.filmapp.presentation.components.pressableScale
import com.filmapp.presentation.theme.AccentGold
import com.filmapp.presentation.theme.Spacing
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val SWIPE_THRESHOLD_FRACTION = 0.22f


@Composable
fun CinematicRandomCard(
    film: Film,
    isFavorite: Boolean,
    onSwipeComplete: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember(film.id) { Animatable(0f) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val thresholdPx = remember(configuration, density) {
        with(density) {
            configuration.screenWidthDp.dp.toPx() * SWIPE_THRESHOLD_FRACTION
        }
    }

    val dragProgress = (abs(offsetX.value) / thresholdPx).coerceIn(0f, 1f)
    val cardScale = 1f - dragProgress * 0.04f
    val imageParallax = offsetX.value * 0.35f
    val contentAlpha = 1f - dragProgress * 0.25f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(film.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (abs(offsetX.value) > thresholdPx) {
                                val target = if (offsetX.value > 0) {
                                    size.width.toFloat() * 1.2f
                                } else {
                                    -size.width.toFloat() * 1.2f
                                }
                                offsetX.animateTo(
                                    targetValue = target,
                                    animationSpec = tween(durationMillis = 250)
                                )
                                onSwipeComplete()
                                offsetX.snapTo(0f)
                            } else {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 200)
                                )
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                    alpha = 1f - dragProgress * 0.15f
                }
        ) {
            //постер
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .align(Alignment.TopCenter)
            ) {
                FilmPoster(
                    film = film,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { translationX = imageParallax }
                )

                //градиенты
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.35f to Color.Transparent,
                                    0.55f to Color.Black.copy(alpha = 0.35f),
                                    0.72f to Color.Black.copy(alpha = 0.75f),
                                    0.88f to Color.Black.copy(alpha = 0.95f),
                                    1.0f to Color.Black
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.35f)
                                )
                            )
                        )
                )
            }

            //инфо и фильме
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .graphicsLayer { alpha = contentAlpha }
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md)
            ) {
                Text(
                    text = film.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = film.releaseYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    film.genreName?.let { genre ->
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    film.rating?.let { rating ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = String.format("%.1f", rating),
                                style = MaterialTheme.typography.titleMedium,
                                color = AccentGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                val descriptionText = film.description?.let { desc ->
                    if (desc.length > 160) desc.take(160) + "…" else desc
                } ?: stringResource(R.string.random_no_description)

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                //избранное
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.random_swipe_hint),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.4f)
                    )

                    FilledIconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .size(52.dp)
                            .pressableScale(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.White.copy(alpha = 0.15f)
                            },
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = stringResource(R.string.random_favorite_icon)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilmPoster(
    film: Film,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imageModel = remember(film.id, film.posterUrl) {
        val localPosterName = "poster_${film.id}"
        val drawableResId = context.resources.getIdentifier(
            localPosterName,
            "drawable",
            context.packageName
        )

        when {
            //локльно
            drawableResId != 0 -> {
                ImageRequest.Builder(context)
                    .data(drawableResId)
                    .crossfade(400)
                    .build()
            }
            //из интернета
            !film.posterUrl.isNullOrEmpty() -> {
                ImageRequest.Builder(context)
                    .data(film.posterUrl)
                    .crossfade(400)
                    .error(android.R.drawable.ic_menu_gallery)
                    .build()
            }
            //загоушка
            else -> {
                ImageRequest.Builder(context)
                    .data(android.R.drawable.ic_menu_gallery)
                    .build()
            }
        }
    }

    AsyncImage(
        model = imageModel,
        contentDescription = film.title,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}