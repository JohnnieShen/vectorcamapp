package com.vci.vectorcamapp.core.data.cache

import androidx.datastore.core.DataStore
import com.vci.vectorcamapp.core.data.dto.cache.DeviceCacheDto
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.Device
import kotlinx.coroutines.flow.firstOrNull
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

    override suspend fun getDevice(): Device? {
        val deviceCacheDto = dataStore.data.firstOrNull()
        return if (deviceCacheDto == null || deviceCacheDto == DeviceCacheDto()) {
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

    override suspend fun getProgramId(): Int? {
        val deviceCacheDto = dataStore.data.firstOrNull()
        return if (deviceCacheDto == null || deviceCacheDto == DeviceCacheDto()) {
            null
        } else {
            deviceCacheDto.programId
        }
    }
}
