package com.sonisoft.parkpulse.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sonisoft.parkpulse.feature.parks.data.local.dao.ParkDao
import com.sonisoft.parkpulse.feature.parks.data.local.dao.SurveyDao
import com.sonisoft.parkpulse.feature.parks.data.local.dao.SurveyPhotoDao
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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `surveys` (
                    `id` TEXT NOT NULL,
                    `parkId` TEXT NOT NULL,
                    `checkInTimestamp` INTEGER NOT NULL,
                    `latitude` REAL NOT NULL,
                    `longitude` REAL NOT NULL,
                    `accuracyMeters` REAL NOT NULL,
                    `treeCount` INTEGER NOT NULL,
                    `hasLighting` INTEGER NOT NULL,
                    `cleanliness` TEXT NOT NULL,
                    `notes` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `isPendingSync` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`parkId`) REFERENCES `parks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_surveys_parkId` ON `surveys` (`parkId`)")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `survey_photos` (
                    `id` TEXT NOT NULL,
                    `surveyId` TEXT NOT NULL,
                    `parkId` TEXT NOT NULL,
                    `localFilePath` TEXT NOT NULL,
                    `capturedAt` INTEGER NOT NULL,
                    `latitude` REAL NOT NULL,
                    `longitude` REAL NOT NULL,
                    `accuracyMeters` REAL NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `isPendingSync` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`surveyId`) REFERENCES `surveys`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_survey_photos_surveyId` ON `survey_photos` (`surveyId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_survey_photos_parkId` ON `survey_photos` (`parkId`)")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add syncStatus and remoteId to parks
            db.execSQL("ALTER TABLE `parks` ADD COLUMN `syncStatus` TEXT NOT NULL DEFAULT 'SYNCED'")
            db.execSQL("ALTER TABLE `parks` ADD COLUMN `remoteId` TEXT DEFAULT NULL")
            db.execSQL("UPDATE `parks` SET `syncStatus` = 'PENDING' WHERE `isPendingSync` = 1")

            // Add syncStatus and remoteId to surveys
            db.execSQL("ALTER TABLE `surveys` ADD COLUMN `syncStatus` TEXT NOT NULL DEFAULT 'PENDING'")
            db.execSQL("ALTER TABLE `surveys` ADD COLUMN `remoteId` TEXT DEFAULT NULL")
            db.execSQL("UPDATE `surveys` SET `syncStatus` = 'SYNCED' WHERE `isPendingSync` = 0")

            // Add syncStatus and remoteId to survey_photos
            db.execSQL("ALTER TABLE `survey_photos` ADD COLUMN `syncStatus` TEXT NOT NULL DEFAULT 'PENDING'")
            db.execSQL("ALTER TABLE `survey_photos` ADD COLUMN `remoteId` TEXT DEFAULT NULL")
            db.execSQL("UPDATE `survey_photos` SET `syncStatus` = 'SYNCED' WHERE `isPendingSync` = 0")
        }
    }

    @Provides
    @Singleton
    fun provideParkDatabase(@ApplicationContext context: Context): ParkDatabase {
        return Room.databaseBuilder(
            context,
            ParkDatabase::class.java,
            "park_pulse_db"
        )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideParkDao(database: ParkDatabase): ParkDao {
        return database.parkDao()
    }

    @Provides
    fun provideSurveyDao(database: ParkDatabase): SurveyDao {
        return database.surveyDao()
    }

    @Provides
    fun provideSurveyPhotoDao(database: ParkDatabase): SurveyPhotoDao {
        return database.surveyPhotoDao()
    }
}
