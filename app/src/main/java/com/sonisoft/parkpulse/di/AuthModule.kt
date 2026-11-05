package com.sonisoft.parkpulse.di

import com.sonisoft.parkpulse.feature.auth.data.api.MockAuthApi
import com.sonisoft.parkpulse.feature.auth.data.datastore.AuthSessionManager
import com.sonisoft.parkpulse.feature.auth.data.repository.AuthRepositoryImpl
import com.sonisoft.parkpulse.feature.auth.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        mockAuthApi: MockAuthApi,
        authSessionManager: AuthSessionManager
    ): AuthRepository {
        return AuthRepositoryImpl(mockAuthApi, authSessionManager)
    }
}
