package com.sonisoft.parkpulse.di

import android.content.Context
import androidx.room.Room
import com.sonisoft.parkpulse.feature.parks.data.local.dao.ParkDao
import com.sonisoft.parkpulse.feature.parks.data.local.database.ParkDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideParkDatabase(@ApplicationContext context: Context): ParkDatabase {
        return Room.databaseBuilder(
            context,
            ParkDatabase::class.java,
            "park_pulse_db"
        )
        .fallbackToDestructiveMigration() // Allowed during dev phase for take-home if needed, but schema is v1
        .build()
    }

    @Provides
    fun provideParkDao(database: ParkDatabase): ParkDao {
        return database.parkDao()
    }
}
