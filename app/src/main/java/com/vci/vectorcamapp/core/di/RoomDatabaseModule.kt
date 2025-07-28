package com.vci.vectorcamapp.core.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vci.vectorcamapp.core.data.dto.program.ProgramDto
import com.vci.vectorcamapp.core.data.dto.site.SiteDto
import com.vci.vectorcamapp.core.data.mappers.toEntity
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Provider
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
        programDaoProvider: Provider<ProgramDao>,
        siteDaoProvider: Provider<SiteDao>,
    ): VectorCamDatabase {
        return Room.databaseBuilder(
            context,
            VectorCamDatabase::class.java,
            DB_NAME,
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val programsJson = context.assets.open(PROGRAM_DATA_FILENAME)
                            .bufferedReader()
                            .use { it.readText() }
                        val sitesJson = context.assets.open(SITE_DATA_FILENAME)
                            .bufferedReader()
                            .use { it.readText() }

                        val programEntities = Json.decodeFromString<List<ProgramDto>>(programsJson).map { it.toEntity() }
                        val siteEntities = Json.decodeFromString<List<SiteDto>>(sitesJson).map { it.toEntity() }

                        programDaoProvider.get().insertAll(programEntities)
                        siteDaoProvider.get().insertAll(siteEntities)
                        Log.i("RoomCallback", "Seeded ${programEntities.size} programs, ${siteEntities.size} sites")
                    } catch (e: Exception) {
                        Log.e("RoomCallback", "Error seeding DB", e)
                    }
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
    fun provideSurveillanceFormDao(db: VectorCamDatabase): SurveillanceFormDao = db.surveillanceFormDao

    @Provides
    fun provideProgramDao(db: VectorCamDatabase): ProgramDao = db.programDao

    @Provides
    fun provideSiteDao(db: VectorCamDatabase): SiteDao = db.siteDao
}
