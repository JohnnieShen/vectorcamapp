package com.vci.vectorcamapp.core.data.cache.serializers

import androidx.datastore.core.Serializer
import com.vci.vectorcamapp.core.data.dto.DeviceDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object DeviceDtoSerializer : Serializer<DeviceDto> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    override val defaultValue: DeviceDto = DeviceDto()

    override suspend fun readFrom(input: InputStream): DeviceDto {
        return try {
            json.decodeFromString(
                deserializer = DeviceDto.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: DeviceDto, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                json.encodeToString(
                    serializer = DeviceDto.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}
