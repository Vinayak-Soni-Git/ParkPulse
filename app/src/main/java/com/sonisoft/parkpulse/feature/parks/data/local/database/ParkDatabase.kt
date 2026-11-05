package com.sonisoft.parkpulse.feature.parks.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sonisoft.parkpulse.feature.parks.data.local.dao.ParkDao
import com.sonisoft.parkpulse.feature.parks.data.local.dao.SurveyDao
import com.sonisoft.parkpulse.feature.parks.data.local.dao.SurveyPhotoDao
import com.sonisoft.parkpulse.feature.parks.data.local.entity.ParkBoundaryPointEntity
import com.sonisoft.parkpulse.feature.parks.data.local.entity.ParkEntity
import com.sonisoft.parkpulse.feature.parks.data.local.entity.SurveyEntity
import com.sonisoft.parkpulse.feature.parks.data.local.entity.SurveyPhotoEntity

@Database(
    entities = [
        ParkEntity::class,
        ParkBoundaryPointEntity::class,
        SurveyEntity::class,
        SurveyPhotoEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ParkDatabase : RoomDatabase() {
    abstract fun parkDao(): ParkDao
    abstract fun surveyDao(): SurveyDao
    abstract fun surveyPhotoDao(): SurveyPhotoDao
}
