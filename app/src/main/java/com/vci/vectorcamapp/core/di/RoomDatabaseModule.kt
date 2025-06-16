package com.vci.vectorcamapp.core.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.vci.vectorcamapp.BuildConfig
import com.vci.vectorcamapp.core.data.room.TransactionHelper
import com.vci.vectorcamapp.core.data.room.VectorCamDatabase
import com.vci.vectorcamapp.core.data.room.dao.BoundingBoxDao
import com.vci.vectorcamapp.core.data.room.dao.ProgramDao
import com.vci.vectorcamapp.core.data.room.dao.SessionDao
import com.vci.vectorcamapp.core.data.room.dao.SiteDao
import com.vci.vectorcamapp.core.data.room.dao.SpecimenDao
import com.vci.vectorcamapp.core.data.room.dao.SurveillanceFormDao
import com.vci.vectorcamapp.core.data.room.entities.ProgramEntity
import com.vci.vectorcamapp.core.data.room.entities.SiteEntity
import com.vci.vectorcamapp.core.data.room.migrations.ALL_MIGRATIONS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

private const val DB_NAME = "vectorcam.db"

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
        ).addMigrations(*ALL_MIGRATIONS).build().apply {
            // TODO: ⚠️ For development only: wipe database on every launch
            if (BuildConfig.DEBUG) {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.w("VectorCamDatabase", "Clearing all tables (DEBUG only)")
//                    clearAllTables()

                    val seededPrograms = listOf(
                        ProgramEntity(id = 1, name = "Uganda Surveillance Program", country = "Uganda"),
                        ProgramEntity(id = 2, name = "Kenya Research Study", country = "Kenya")
                    )

                    programDao.insertAll(seededPrograms)

                    val seededSites = listOf(
                        SiteEntity(
                            id = 1,
                            programId = 1,
                            district = "Mayuge",
                            subCounty = "county1",
                            parish = "Bukatube",
                            sentinelSite = "Bukasero",
                            healthCenter = "Health Center 1"
                        ),
                        SiteEntity(
                            id = 2,
                            programId = 1,
                            district = "Mayuge",
                            subCounty = "county2",
                            parish = "Malongo",
                            sentinelSite = "Namadhi",
                            healthCenter = "Health Center 2"
                        ),
                        SiteEntity(
                            id = 3,
                            programId = 1,
                            district = "Adjumani",
                            subCounty = "county3",
                            parish = "Ofua",
                            sentinelSite = "Ofua Site",
                            healthCenter = "Health Center 3"
                        ),
                        SiteEntity(
                            id = 4,
                            programId = 2,
                            district = "Kisumu",
                            subCounty = "Central",
                            parish = "Market",
                            sentinelSite = "Site B",
                            healthCenter = "Health Center 2"
                        )
                    )

                    siteDao.insertAll(seededSites)
                }
            }
        }
    }


    @Provides
    fun provideTransactionHelper(db: VectorCamDatabase): TransactionHelper = TransactionHelper(db)

    @Provides
    fun provideSessionDao(db: VectorCamDatabase): SessionDao = db.sessionDao

    @Provides
    fun provideSpecimenDao(db: VectorCamDatabase): SpecimenDao = db.specimenDao

    @Provides
    fun provideBoundingBoxDao(db: VectorCamDatabase): BoundingBoxDao = db.boundingBoxDao

    @Provides
    fun provideSurveillanceFormDao(db: VectorCamDatabase): SurveillanceFormDao = db.surveillanceFormDao

    @Provides
    fun provideProgramDao(db: VectorCamDatabase): ProgramDao = db.programDao

    @Provides
    fun provideSiteDao(db: VectorCamDatabase): SiteDao = db.siteDao
}
