package com.vci.vectorcamapp.core.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vci.vectorcamapp.core.data.room.entities.SurveillanceFormEntity
import java.util.UUID

@Dao
interface SurveillanceFormDao {

    @Upsert
    suspend fun upsertSurveillanceForm(surveillanceForm: SurveillanceFormEntity) : Long

    @Query("SELECT * FROM surveillance_form WHERE sessionId = :sessionId")
    suspend fun getSurveillanceFormBySessionId(sessionId: UUID): SurveillanceFormEntity?
}
