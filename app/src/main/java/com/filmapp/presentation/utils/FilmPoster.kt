package com.filmapp.presentation.utils

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.filmapp.R
import com.filmapp.domain.model.Film

@DrawableRes
fun getFilmPosterRes(
    context: Context,
    filmId: Int
): Int {
    val resId = context.resources.getIdentifier(
        "poster_$filmId",
        "drawable",
        context.packageName
    )

    return if (resId != 0) {
        resId
    } else {
        R.drawable.placeholder_poster
    }
}

@Composable
fun FilmPosterImage(
    film: Film,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    Image(
        painter = painterResource(getFilmPosterRes(context, film.id)),
        contentDescription = film.title,
        contentScale = contentScale,
        modifier = modifier
    )
}
