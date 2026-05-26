package com.filmapp.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthSource @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun register(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Ошибка создания пользователя")
    }

    suspend fun login(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Ошибка входа")
    }

    suspend fun getIdToken(): String {
        val user = currentUser ?: throw Exception("Пользователь не авторизован")
        val result = user.getIdToken(false).await()
        return result.token ?: throw Exception("Ошибка получения токена")
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean = currentUser != null
}