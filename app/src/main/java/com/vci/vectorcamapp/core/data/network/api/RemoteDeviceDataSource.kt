package com.vci.vectorcamapp.core.data.network.api

import com.vci.vectorcamapp.core.data.dto.device.DeviceDto
import com.vci.vectorcamapp.core.data.dto.device.RegisterDeviceRequestDto
import com.vci.vectorcamapp.core.data.dto.device.RegisterDeviceResponseDto
import com.vci.vectorcamapp.core.data.network.constructUrl
import com.vci.vectorcamapp.core.data.network.safeCall
import com.vci.vectorcamapp.core.domain.model.Device
import com.vci.vectorcamapp.core.domain.network.api.DeviceDataSource
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.core.domain.util.network.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class RemoteDeviceDataSource @Inject constructor(
    private val httpClient: HttpClient
) : DeviceDataSource {
    override suspend fun registerDevice(
        device: Device, programId: Int
    ): Result<RegisterDeviceResponseDto, NetworkError> {
        return safeCall<RegisterDeviceResponseDto> {
            httpClient.post(constructUrl("devices/register")) {
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterDeviceRequestDto(
                        model = device.model,
                        registeredAt = device.registeredAt,
                        programId = programId
                    )
                )
            }
        }
    }

    override suspend fun getDeviceById(deviceId: Int): Result<DeviceDto, NetworkError> {
        return safeCall<DeviceDto> {
            httpClient.get(constructUrl("devices/$deviceId")) {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
