package com.filmapp.domain.usecase.auth

import com.filmapp.domain.model.User
import com.filmapp.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String
    ): Result<User> {
        if (email.isBlank()) return Result.failure(Exception("Email не может быть пустым"))
        if (password.isBlank()) return Result.failure(Exception("Пароль не может быть пустым"))
        if (name.isBlank()) return Result.failure(Exception("Имя пользователя не может быть пустым"))
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Некорректный email"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("Пароль должен быть не менее 6 символов"))
        }
        return authRepository.register(email, password, name)
    }
}