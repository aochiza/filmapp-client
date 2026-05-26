package com.filmapp.presentation.random

import com.filmapp.domain.model.Film

sealed class RandomState {
    data object Loading : RandomState()
    data class Success(val film: Film, val isFavorite: Boolean) : RandomState()
    data class Error(val message: String) : RandomState()
}
