package com.vci.vectorcamapp.core.data.cache.serializers

import androidx.datastore.core.Serializer
import com.vci.vectorcamapp.core.data.dto.cache.CurrentSessionCacheDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object CurrentSessionCacheDtoSerializer : Serializer<CurrentSessionCacheDto> {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    override val defaultValue: CurrentSessionCacheDto = CurrentSessionCacheDto()

    override suspend fun readFrom(input: InputStream): CurrentSessionCacheDto {
        return try {
            json.decodeFromString(
                deserializer = CurrentSessionCacheDto.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: CurrentSessionCacheDto, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                json.encodeToString(
                    serializer = CurrentSessionCacheDto.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}
