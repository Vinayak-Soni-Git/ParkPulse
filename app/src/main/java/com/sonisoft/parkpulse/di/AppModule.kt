package com.sonisoft.parkpulse.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sonisoft.parkpulse.feature.parks.data.location.LocationServiceImpl
import com.sonisoft.parkpulse.feature.parks.data.network.NetworkMonitorImpl
import com.sonisoft.parkpulse.feature.parks.data.repository.SyncRepositoryImpl
import com.sonisoft.parkpulse.feature.parks.domain.location.LocationService
import com.sonisoft.parkpulse.feature.parks.domain.network.MockSyncApi
import com.sonisoft.parkpulse.feature.parks.domain.network.NetworkMonitor
import com.sonisoft.parkpulse.feature.parks.domain.repository.ParkRepository
import com.sonisoft.parkpulse.feature.parks.domain.repository.SyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationService(
        @ApplicationContext context: Context,
        fusedLocationProviderClient: FusedLocationProviderClient
    ): LocationService {
        return LocationServiceImpl(context, fusedLocationProviderClient)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor {
        return NetworkMonitorImpl(context)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        parkRepository: ParkRepository,
        mockSyncApi: MockSyncApi
    ): SyncRepository {
        return SyncRepositoryImpl(parkRepository, mockSyncApi)
    }
}
