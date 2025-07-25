package com.vci.vectorcamapp.core.data.mappers

import com.vci.vectorcamapp.core.data.room.entities.SpecimenImageEntity
import com.vci.vectorcamapp.core.domain.model.SpecimenImage
import java.util.UUID

fun SpecimenImageEntity.toDomain() : SpecimenImage {
    return SpecimenImage(
        localId = this.localId,
        remoteId = this.remoteId,
        species = this.species,
        sex = this.sex,
        abdomenStatus = this.abdomenStatus,
        imageUri = this.imageUri,
        metadataUploadStatus = this.metadataUploadStatus,
        imageUploadStatus = this.imageUploadStatus,
        capturedAt = this.capturedAt,
        submittedAt = this.submittedAt
    )
}

fun SpecimenImage.toEntity(specimenId: String, sessionId: UUID) : SpecimenImageEntity {
    return SpecimenImageEntity(
        localId = this.localId,
        specimenId = specimenId,
        sessionId = sessionId,
        remoteId = this.remoteId,
        species = this.species,
        sex = this.sex,
        abdomenStatus = this.abdomenStatus,
        imageUri = this.imageUri,
        metadataUploadStatus = this.metadataUploadStatus,
        imageUploadStatus = this.imageUploadStatus,
        capturedAt = this.capturedAt,
        submittedAt = this.submittedAt
    )
}
