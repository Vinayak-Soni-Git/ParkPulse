package com.sonisoft.parkpulse.feature.parks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sonisoft.parkpulse.feature.parks.data.local.entity.ParkBoundaryPointEntity
import com.sonisoft.parkpulse.feature.parks.data.local.entity.ParkEntity
import com.sonisoft.parkpulse.feature.parks.data.local.entity.ParkWithBoundaryPoints
import com.sonisoft.parkpulse.feature.parks.domain.model.ParkStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkDao {

    @Transaction
    @Query("SELECT * FROM parks")
    fun getAllParks(): Flow<List<ParkWithBoundaryPoints>>

    @Transaction
    @Query("SELECT * FROM parks WHERE name LIKE '%' || :query || '%'")
    fun searchParks(query: String): Flow<List<ParkWithBoundaryPoints>>

    @Transaction
    @Query("SELECT * FROM parks WHERE status = :status")
    fun filterParksByStatus(status: ParkStatus): Flow<List<ParkWithBoundaryPoints>>

    @Transaction
    @Query("SELECT * FROM parks WHERE name LIKE '%' || :query || '%' AND status = :status")
    fun searchAndFilterParks(query: String, status: ParkStatus): Flow<List<ParkWithBoundaryPoints>>

    @Transaction
    @Query("SELECT * FROM parks WHERE id = :parkId")
    fun getParkById(parkId: String): Flow<ParkWithBoundaryPoints?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParks(parks: List<ParkEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoundaryPoints(points: List<ParkBoundaryPointEntity>)

    @Transaction
    suspend fun insertParksWithBoundaries(parks: List<ParkEntity>, points: List<ParkBoundaryPointEntity>) {
        insertParks(parks)
        insertBoundaryPoints(points)
    }

    @Query("SELECT COUNT(*) FROM parks")
    suspend fun getParksCount(): Int

    @Query("SELECT COUNT(*) FROM parks")
    fun getParksCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM parks WHERE status = 'OPEN'")
    fun getOpenParksCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM parks WHERE status = 'NEEDS_REPAIR'")
    fun getNeedsRepairParksCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM parks WHERE status = 'CLOSED'")
    fun getClosedParksCountFlow(): Flow<Int>
}
