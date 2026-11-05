package com.sonisoft.parkpulse.feature.parks.data.seed

import android.content.Context
import android.util.Log
import com.sonisoft.parkpulse.feature.parks.data.local.dao.ParkDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkSeedImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parkDao: ParkDao,
    private val parkSeedParser: ParkSeedParser
) {

    suspend fun seedDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        try {
            val count = parkDao.getParksCount()
            if (count == 0) {
                Log.d("ParkSeedImporter", "Database empty. Starting seed process.")
                val jsonString = context.assets.open("parks_seed.json").bufferedReader().use {
                    it.readText()
                }

                val (parks, points) = parkSeedParser.parse(jsonString)

                if (parks.isNotEmpty()) {
                    parkDao.insertParksWithBoundaries(parks, points)
                    Log.d("ParkSeedImporter", "Successfully seeded ${parks.size} parks and ${points.size} boundary points.")
                }
            } else {
                Log.d("ParkSeedImporter", "Database already seeded with $count parks.")
            }
        } catch (e: Exception) {
            Log.e("ParkSeedImporter", "Error seeding database: ${e.message}", e)
        }
    }
}
