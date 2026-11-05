package com.sonisoft.parkpulse.feature.parks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sonisoft.parkpulse.feature.parks.data.local.entity.SurveyPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurveyPhoto(photo: SurveyPhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurveyPhotos(photos: List<SurveyPhotoEntity>)

    @Query("SELECT * FROM survey_photos WHERE surveyId = :surveyId ORDER BY createdAt ASC")
    fun getPhotosForSurvey(surveyId: String): Flow<List<SurveyPhotoEntity>>

    @Query("SELECT * FROM survey_photos WHERE parkId = :parkId ORDER BY createdAt DESC")
    fun getPhotosForPark(parkId: String): Flow<List<SurveyPhotoEntity>>
}
