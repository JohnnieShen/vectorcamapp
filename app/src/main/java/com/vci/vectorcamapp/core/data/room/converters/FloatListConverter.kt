package com.vci.vectorcamapp.core.data.room.converters

import androidx.room.TypeConverter

class FloatListConverter {

    @TypeConverter
    fun fromFloatList(value: List<Float>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toFloatList(value: String?): List<Float>? {
        return value?.takeIf { it.isNotBlank() }?.split(",")?.mapNotNull {
            it.trim().toFloatOrNull()
        }
    }
}