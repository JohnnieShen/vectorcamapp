package com.vci.vectorcamapp.core.data.mappers

import com.vci.vectorcamapp.core.data.room.entities.InferenceResultEntity
import com.vci.vectorcamapp.core.domain.model.InferenceResult

fun InferenceResultEntity.toDomain() : InferenceResult {
    return InferenceResult(
        bboxTopLeftX = this.bboxTopLeftX,
        bboxTopLeftY = this.bboxTopLeftY,
        bboxWidth = this.bboxWidth,
        bboxHeight = this.bboxHeight,
        bboxConfidence = this.bboxConfidence,
        bboxClassId = this.bboxClassId,
        speciesLogits = this.speciesLogits,
        sexLogits = this.sexLogits,
        abdomenStatusLogits = this.abdomenStatusLogits
    )
}

fun InferenceResult.toEntity(specimenId: String) : InferenceResultEntity {
    return InferenceResultEntity(
        specimenId = specimenId,
        bboxTopLeftX = this.bboxTopLeftX,
        bboxTopLeftY = this.bboxTopLeftY,
        bboxWidth = this.bboxWidth,
        bboxHeight = this.bboxHeight,
        bboxConfidence = this.bboxConfidence,
        bboxClassId = this.bboxClassId,
        speciesLogits = this.speciesLogits,
        sexLogits = this.sexLogits,
        abdomenStatusLogits = this.abdomenStatusLogits
    )
}
