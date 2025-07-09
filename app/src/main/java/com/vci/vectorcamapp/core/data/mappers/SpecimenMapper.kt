package com.vci.vectorcamapp.core.data.mappers

import com.vci.vectorcamapp.core.data.room.entities.SpecimenEntity
import com.vci.vectorcamapp.core.domain.model.Specimen
import java.util.UUID

fun SpecimenEntity.toDomain(): Specimen {
    return Specimen(
        id = this.id,
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

fun Specimen.toEntity(sessionId: UUID): SpecimenEntity {
    return SpecimenEntity(
        id = this.id,
        sessionId = sessionId,
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
