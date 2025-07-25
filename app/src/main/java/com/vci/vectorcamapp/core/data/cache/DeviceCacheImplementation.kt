package com.vci.vectorcamapp.core.data.cache

import androidx.datastore.core.DataStore
import com.vci.vectorcamapp.core.data.dto.cache.DeviceCacheDto
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.Device
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceCacheImplementation @Inject constructor(
    private val dataStore: DataStore<DeviceCacheDto>
) : DeviceCache {
    override suspend fun saveDevice(device: Device, programId: Int) {
        dataStore.updateData {
            DeviceCacheDto(
                id = device.id,
                programId = programId,
                model = device.model,
                registeredAt = device.registeredAt,
                submittedAt = device.submittedAt
            )
        }
    }

    override suspend fun getProgramId(): Int? {
        val deviceCacheDto = dataStore.data.firstOrNull()
        return deviceCacheDto?.programId
    }

    override suspend fun getDevice(): Device? {
        val deviceCacheDto = dataStore.data.firstOrNull()
        return if (deviceCacheDto == null) {
            null
        } else {
            Device(
                id = deviceCacheDto.id,
                model = deviceCacheDto.model,
                registeredAt = deviceCacheDto.registeredAt,
                submittedAt = deviceCacheDto.submittedAt
            )
        }
    }

    override fun observeProgramId(): Flow<Int?> {
        return dataStore.data
            .catch { emit(DeviceCacheDto()) }
            .map { deviceCacheDto -> deviceCacheDto.programId }
    }
}

