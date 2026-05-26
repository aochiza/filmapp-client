package com.filmapp.domain.model

data class User(
    val userId: Int,
    val name: String,
    val email: String,
    val token: String
)