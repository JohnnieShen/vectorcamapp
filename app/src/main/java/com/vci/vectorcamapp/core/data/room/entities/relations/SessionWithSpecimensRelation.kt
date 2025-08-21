package com.vci.vectorcamapp.core.data.room.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.vci.vectorcamapp.core.data.room.entities.SessionEntity
import com.vci.vectorcamapp.core.data.room.entities.SpecimenEntity

data class SessionWithSpecimensRelation(
    @Embedded val sessionEntity: SessionEntity,
    @Relation(
        parentColumn = "localId",
        entityColumn = "sessionId"
    )
    val specimenEntities: List<SpecimenEntity>
)
