package com.vci.vectorcamapp.core.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vci.vectorcamapp.core.data.room.converters.FloatListConverter
import com.vci.vectorcamapp.core.data.room.converters.UploadStatusConverter
import com.vci.vectorcamapp.core.data.room.converters.UriConverter
import com.vci.vectorcamapp.core.data.room.converters.UuidConverter
import com.vci.vectorcamapp.core.data.room.dao.InferenceResultDao
import com.vci.vectorcamapp.core.data.room.dao.ProgramDao
import com.vci.vectorcamapp.core.data.room.dao.SessionDao
import com.vci.vectorcamapp.core.data.room.dao.SiteDao
import com.vci.vectorcamapp.core.data.room.dao.SpecimenDao
import com.vci.vectorcamapp.core.data.room.dao.SurveillanceFormDao
import com.vci.vectorcamapp.core.data.room.entities.InferenceResultEntity
import com.vci.vectorcamapp.core.data.room.entities.ProgramEntity
import com.vci.vectorcamapp.core.data.room.entities.SessionEntity
import com.vci.vectorcamapp.core.data.room.entities.SiteEntity
import com.vci.vectorcamapp.core.data.room.entities.SpecimenEntity
import com.vci.vectorcamapp.core.data.room.entities.SurveillanceFormEntity

@Database(
    entities = [ProgramEntity::class, SiteEntity::class, SessionEntity::class, SpecimenEntity::class, InferenceResultEntity::class, SurveillanceFormEntity::class],
    version = 7,
)
@TypeConverters(
    UuidConverter::class,
    UriConverter::class,
    UploadStatusConverter::class,
    FloatListConverter::class
)
abstract class VectorCamDatabase : RoomDatabase() {
    abstract val sessionDao: SessionDao
    abstract val specimenDao: SpecimenDao
    abstract val inferenceResultDao: InferenceResultDao
    abstract val surveillanceFormDao: SurveillanceFormDao
    abstract val programDao: ProgramDao
    abstract val siteDao: SiteDao
}
