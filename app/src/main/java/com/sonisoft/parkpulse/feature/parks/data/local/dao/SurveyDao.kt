package com.sonisoft.parkpulse.feature.parks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sonisoft.parkpulse.feature.parks.data.local.entity.SurveyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: SurveyEntity)

    @Query("SELECT * FROM surveys WHERE parkId = :parkId ORDER BY createdAt DESC")
    fun getSurveysForPark(parkId: String): Flow<List<SurveyEntity>>

    @Query("SELECT * FROM surveys WHERE id = :surveyId")
    fun getSurveyById(surveyId: String): Flow<SurveyEntity?>
}
