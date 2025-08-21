package com.vci.vectorcamapp.core.data.cache.serializers

import androidx.datastore.core.Serializer
import com.vci.vectorcamapp.core.data.dto.cache.DeviceCacheDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object DeviceCacheDtoSerializer : Serializer<DeviceCacheDto> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    override val defaultValue: DeviceCacheDto = DeviceCacheDto()

    override suspend fun readFrom(input: InputStream): DeviceCacheDto {
        return try {
            json.decodeFromString(
                deserializer = DeviceCacheDto.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: DeviceCacheDto, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                json.encodeToString(
                    serializer = DeviceCacheDto.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}
