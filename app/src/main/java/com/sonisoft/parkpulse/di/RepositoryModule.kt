package com.sonisoft.parkpulse.di

import com.sonisoft.parkpulse.feature.parks.data.repository.ParkRepositoryImpl
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindParkRepository(
        parkRepositoryImpl: ParkRepositoryImpl
    ): ParkRepository
}
