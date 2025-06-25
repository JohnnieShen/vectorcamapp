package com.vci.vectorcamapp.core.data.mappers

import com.vci.vectorcamapp.core.data.dto.DeviceDto
import com.vci.vectorcamapp.core.domain.model.Device

fun DeviceDto.toDomain() : Device {
    return Device(
        id = this.id,
        model = this.model,
        registeredAt = this.registeredAt,
        submittedAt = this.submittedAt
    )
}

fun Device.toDto(programId: Int) : DeviceDto {
    return DeviceDto(
        id = this.id,
        programId = programId,
        model = this.model,
        registeredAt = this.registeredAt,
        submittedAt = this.submittedAt
    )
}
