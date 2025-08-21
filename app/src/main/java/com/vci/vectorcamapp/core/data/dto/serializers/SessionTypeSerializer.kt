package com.vci.vectorcamapp.core.data.dto.serializers

import com.vci.vectorcamapp.core.domain.model.enums.SessionType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SessionTypeSerializer : KSerializer<SessionType> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("SessionType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SessionType) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): SessionType {
        return SessionType.valueOf(decoder.decodeString())
    }
}
