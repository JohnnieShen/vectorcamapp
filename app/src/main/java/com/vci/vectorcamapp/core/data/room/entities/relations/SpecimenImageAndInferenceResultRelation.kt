package com.vci.vectorcamapp.core.data.room.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.vci.vectorcamapp.core.data.room.entities.InferenceResultEntity
import com.vci.vectorcamapp.core.data.room.entities.SpecimenImageEntity

data class SpecimenImageAndInferenceResultRelation(
    @Embedded val specimenImageEntity: SpecimenImageEntity,
    @Relation(
        parentColumn = "localId",
        entityColumn = "specimenImageId"
    )
    val inferenceResultEntity: InferenceResultEntity
)
