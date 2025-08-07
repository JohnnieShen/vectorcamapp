package com.vci.vectorcamapp.core.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vci.vectorcamapp.core.data.dto.program.ProgramDto
import com.vci.vectorcamapp.core.data.dto.site.SiteDto
import com.vci.vectorcamapp.core.data.room.TransactionHelper
import com.vci.vectorcamapp.core.data.room.VectorCamDatabase
import com.vci.vectorcamapp.core.data.room.dao.InferenceResultDao
import com.vci.vectorcamapp.core.data.room.dao.ProgramDao
import com.vci.vectorcamapp.core.data.room.dao.SessionDao
import com.vci.vectorcamapp.core.data.room.dao.SiteDao
import com.vci.vectorcamapp.core.data.room.dao.SpecimenDao
import com.vci.vectorcamapp.core.data.room.dao.SpecimenImageDao
import com.vci.vectorcamapp.core.data.room.dao.SurveillanceFormDao
import com.vci.vectorcamapp.core.data.room.migrations.ALL_MIGRATIONS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

private const val DB_NAME = "vectorcam.db"
private const val PROGRAM_DATA_FILENAME = "programs.json"
private const val SITE_DATA_FILENAME = "sites.json"

@Module
@InstallIn(SingletonComponent::class)
object RoomDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): VectorCamDatabase {
        return Room.databaseBuilder(
            context,
            VectorCamDatabase::class.java,
            DB_NAME,
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)

                try {
                    val programsJson = context.assets.open(PROGRAM_DATA_FILENAME).bufferedReader()
                        .use { it.readText() }
                    val sitesJson = context.assets.open(SITE_DATA_FILENAME).bufferedReader()
                        .use { it.readText() }

                    val programs = Json.decodeFromString<List<ProgramDto>>(programsJson)
                    val sites = Json.decodeFromString<List<SiteDto>>(sitesJson)

                    db.beginTransaction()
                    try {
                        // Use direct SQL to avoid circular dependency
                        programs.forEach { program ->
                            db.execSQL(
                                """
                                    INSERT INTO program (id, name, country) 
                                    VALUES (?, ?, ?)
                                    ON CONFLICT (id) DO UPDATE SET
                                        name = excluded.name,
                                        country = excluded.country
                                    WHERE
                                        name != excluded.name OR
                                        country != excluded.country
                                """.trimIndent(), arrayOf(program.id, program.name, program.country)
                            )
                        }

                        sites.forEach { site ->
                            db.execSQL(
                                """
                                    INSERT INTO site (id, programId, district, subCounty, parish, sentinelSite, healthCenter)
                                    VALUES (?, ?, ?, ?, ?, ?, ?)
                                    ON CONFLICT (id) DO UPDATE SET
                                        programId = excluded.programId,
                                        district = excluded.district,
                                        subCounty = excluded.subCounty,
                                        parish = excluded.parish,
                                        sentinelSite = excluded.sentinelSite,
                                        healthCenter = excluded.healthCenter
                                    WHERE
                                        programId != excluded.programId OR
                                        district != excluded.district OR
                                        subCounty != excluded.subCounty OR
                                        parish != excluded.parish OR
                                        sentinelSite != excluded.sentinelSite OR
                                        healthCenter != excluded.healthCenter
                                """.trimIndent(),
                                arrayOf(
                                    site.id,
                                    site.programId,
                                    site.district,
                                    site.subCounty,
                                    site.parish,
                                    site.sentinelSite,
                                    site.healthCenter
                                )
                            )
                        }

                        db.setTransactionSuccessful()
                        Log.i(
                            "RoomCallback", "Seeded ${programs.size} programs, ${sites.size} sites"
                        )
                    } finally {
                        db.endTransaction()
                    }
                } catch (e: Exception) {
                    Log.e("RoomCallback", "Error seeding database", e)
                }
            }
        }).addMigrations(*ALL_MIGRATIONS).build()
    }

    @Provides
    fun provideTransactionHelper(db: VectorCamDatabase): TransactionHelper = TransactionHelper(db)

    @Provides
    fun provideSessionDao(db: VectorCamDatabase): SessionDao = db.sessionDao

    @Provides
    fun provideSpecimenDao(db: VectorCamDatabase): SpecimenDao = db.specimenDao

    @Provides
    fun provideSpecimenImageDao(db: VectorCamDatabase): SpecimenImageDao = db.specimenImageDao

    @Provides
    fun provideInferenceResultDao(db: VectorCamDatabase): InferenceResultDao = db.inferenceResultDao

    @Provides
    fun provideSurveillanceFormDao(db: VectorCamDatabase): SurveillanceFormDao =
        db.surveillanceFormDao

    @Provides
    fun provideProgramDao(db: VectorCamDatabase): ProgramDao = db.programDao

    @Provides
    fun provideSiteDao(db: VectorCamDatabase): SiteDao = db.siteDao
}
