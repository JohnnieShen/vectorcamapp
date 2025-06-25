package com.vci.vectorcamapp.core.data.cache

import androidx.datastore.core.DataStore
import com.vci.vectorcamapp.core.data.dto.DeviceDto
import com.vci.vectorcamapp.core.data.mappers.toDomain
import com.vci.vectorcamapp.core.data.mappers.toDto
import com.vci.vectorcamapp.core.domain.cache.DeviceCache
import com.vci.vectorcamapp.core.domain.model.Device
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class DeviceCacheImplementation @Inject constructor(
    private val dataStore: DataStore<DeviceDto>
) : DeviceCache {
    override suspend fun saveDevice(device: Device, programId: Int) {
        dataStore.updateData {
            device.toDto(programId)
        }
    }

    override suspend fun getDevice(): Device? {
        val deviceDto = dataStore.data.firstOrNull()
        return if (deviceDto == null || deviceDto == DeviceDto()) null else deviceDto.toDomain()
    }

    override suspend fun getProgramId(): Int? {
        val deviceDto = dataStore.data.firstOrNull()
        return if (deviceDto == null || deviceDto == DeviceDto()) null else deviceDto.programId
    }
}
