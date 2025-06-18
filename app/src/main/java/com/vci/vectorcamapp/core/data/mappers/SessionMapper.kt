package com.vci.vectorcamapp.core.data.mappers

import com.vci.vectorcamapp.core.data.dto.SessionDto
import com.vci.vectorcamapp.core.data.room.entities.SessionEntity
import com.vci.vectorcamapp.core.domain.model.Session

fun SessionEntity.toDomain(): Session {
    return Session(
        id = this.id,
        createdAt = this.createdAt,
        submittedAt = this.submittedAt
    )
}

fun Session.toEntity(): SessionEntity {
    return SessionEntity(
        id = this.id,
        createdAt = this.createdAt,
        submittedAt = this.submittedAt
    )
}

fun Session.toDto() : SessionDto {
    return SessionDto(
        id = this.id,
        createdAt = this.createdAt,
        submittedAt = this.submittedAt,
    )
}

fun SessionDto.toDomain() : Session {
    return Session(
        id = this.id,
        createdAt = this.createdAt,
        submittedAt = this.submittedAt
    )
}
