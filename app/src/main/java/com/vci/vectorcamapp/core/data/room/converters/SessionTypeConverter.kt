package com.vci.vectorcamapp.core.data.room.converters

import androidx.room.TypeConverter
import com.vci.vectorcamapp.core.domain.model.enums.SessionType

class SessionTypeConverter {
    @TypeConverter
    fun fromSessionType(value: SessionType?): String? = value?.name

    @TypeConverter
    fun toSessionType(value: String?): SessionType? = value?.let { SessionType.valueOf(it) }
}
