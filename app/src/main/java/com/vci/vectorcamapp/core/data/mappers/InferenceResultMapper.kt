package com.vci.vectorcamapp.core.data.mappers

import com.vci.vectorcamapp.core.data.room.entities.InferenceResultEntity
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import java.util.UUID

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
        abdomenStatusLogits = this.abdomenStatusLogits,
        speciesInferenceDuration = this.speciesInferenceDuration,
        sexInferenceDuration = this.sexInferenceDuration,
        abdomenStatusInferenceDuration = this.abdomenStatusInferenceDuration
    )
}

fun InferenceResult.toEntity(specimenImageId: String) : InferenceResultEntity {
    return InferenceResultEntity(
        specimenImageId = specimenImageId,
        bboxTopLeftX = this.bboxTopLeftX,
        bboxTopLeftY = this.bboxTopLeftY,
        bboxWidth = this.bboxWidth,
        bboxHeight = this.bboxHeight,
        bboxConfidence = this.bboxConfidence,
        bboxClassId = this.bboxClassId,
        speciesLogits = this.speciesLogits,
        sexLogits = this.sexLogits,
        abdomenStatusLogits = this.abdomenStatusLogits,
        speciesInferenceDuration = this.speciesInferenceDuration,
        sexInferenceDuration = this.sexInferenceDuration,
        abdomenStatusInferenceDuration = this.abdomenStatusInferenceDuration
    )
}
