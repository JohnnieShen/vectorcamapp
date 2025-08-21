package com.vci.vectorcamapp.core.domain.network.api

import com.vci.vectorcamapp.core.data.dto.device.DeviceDto
import com.vci.vectorcamapp.core.data.dto.device.RegisterDeviceResponseDto
import com.vci.vectorcamapp.core.domain.model.Device
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError

interface DeviceDataSource {
    suspend fun registerDevice(
        device: Device, programId: Int
    ): Result<RegisterDeviceResponseDto, NetworkError>
    suspend fun getDeviceById(deviceId: Int): Result<DeviceDto, NetworkError>
}
