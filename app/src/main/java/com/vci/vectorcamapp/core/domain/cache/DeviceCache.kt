package com.vci.vectorcamapp.core.domain.cache

import com.vci.vectorcamapp.core.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceCache {
    suspend fun saveDevice(device: Device, programId: Int)
    suspend fun getDevice(): Device?
    suspend fun getProgramId(): Int?

    fun observeProgramId(): Flow<Int?>
}
