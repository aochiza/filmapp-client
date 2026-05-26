package com.filmapp.domain.usecase.auth

import com.filmapp.domain.model.User
import com.filmapp.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank()) return Result.failure(Exception("Email не может быть пустым"))
        if (password.isBlank()) return Result.failure(Exception("Пароль не может быть пустым"))
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Некорректный email"))
        }
        return authRepository.login(email, password)
    }
}