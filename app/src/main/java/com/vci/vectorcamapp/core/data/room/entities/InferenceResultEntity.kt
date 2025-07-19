package com.vci.vectorcamapp.core.data.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "inference_result", foreignKeys = [
        ForeignKey(
            entity = SpecimenEntity::class,
            parentColumns = ["id"],
            childColumns = ["specimenId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class InferenceResultEntity(
    @PrimaryKey val specimenId: String = "",
    val bboxTopLeftX: Float = 0F,
    val bboxTopLeftY: Float = 0F,
    val bboxWidth: Float = 0F,
    val bboxHeight: Float = 0F,
    val bboxConfidence: Float = 0F,
    val bboxClassId: Int = 0,
    val speciesLogits: List<Float>? = null,
    val sexLogits: List<Float>? = null,
    val abdomenStatusLogits: List<Float>? = null,
)
