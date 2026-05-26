package com.filmapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmapp.domain.model.User
import com.filmapp.domain.usecase.auth.LoginUseCase
import com.filmapp.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            loginUseCase(email, password)
                .onSuccess { _authState.value = AuthState.Success(it) }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Ошибка входа") }
        }
    }

    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            registerUseCase(email, password, name)
                .onSuccess { _authState.value = AuthState.Success(it) }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Ошибка регистрации") }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}