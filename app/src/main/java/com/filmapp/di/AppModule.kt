package com.filmapp.di

import com.filmapp.data.remote.firebase.FirebaseAuthSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuthSource(): FirebaseAuthSource = FirebaseAuthSource()
}