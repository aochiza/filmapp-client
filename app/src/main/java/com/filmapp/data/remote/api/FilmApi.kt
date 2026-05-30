package com.filmapp.data.remote.api

import com.filmapp.data.remote.dto.FilmRequest
import com.filmapp.data.remote.dto.FilmResponse
import com.filmapp.data.remote.dto.FilmsListResponse
import okhttp3.ResponseBody
import retrofit2.http.*

interface FilmApi {

    @GET("films")
    suspend fun getFilms(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("search") search: String? = null,
        @Query("genreId") genreId: Int? = null
    ): FilmsListResponse

    @GET("films/{id}")
    suspend fun getFilmById(@Path("id") id: Int): FilmResponse

    @POST("films")
    suspend fun createFilm(@Body request: FilmRequest): FilmResponse

    @PUT("films/{id}")
    suspend fun updateFilm(@Path("id") id: Int, @Body request: FilmRequest): FilmResponse

    @DELETE("films/{id}")
    suspend fun deleteFilm(@Path("id") id: Int)

    @GET("films/favorites")
    suspend fun getFavorites(): List<FilmResponse>

    @POST("films/{id}/favorite")
    suspend fun addToFavorites(@Path("id") id: Int)

    @DELETE("films/{id}/favorite")
    suspend fun removeFromFavorites(@Path("id") id: Int)

    @GET("watch-later")
    suspend fun getWatchLater(): ResponseBody

    @POST("watch-later/{filmId}")
    suspend fun addToWatchLater(@Path("filmId") filmId: Int)

    @DELETE("watch-later/{filmId}")
    suspend fun removeFromWatchLater(@Path("filmId") filmId: Int)

    @GET("films/random")
    suspend fun getRandomFilm(
        @Query("genreId") genreId: Int? = null
    ): FilmResponse
}