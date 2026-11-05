package com.sonisoft.parkpulse.feature.parks.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sonisoft.parkpulse.feature.parks.data.local.dao.ParkDao
import com.sonisoft.parkpulse.feature.parks.data.local.entity.ParkBoundaryPointEntity
import com.sonisoft.parkpulse.feature.parks.data.local.entity.ParkEntity

@Database(
    entities = [ParkEntity::class, ParkBoundaryPointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ParkDatabase : RoomDatabase() {
    abstract fun parkDao(): ParkDao
}
