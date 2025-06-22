package com.vci.vectorcamapp.core.data.mappers

import com.vci.vectorcamapp.core.data.room.entities.ProgramEntity
import com.vci.vectorcamapp.core.domain.model.Program

fun ProgramEntity.toDomain(): Program =
    Program(
        id = this.id,
        name = this.name,
        country = this.country
    )