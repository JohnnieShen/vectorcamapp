package com.vci.vectorcamapp.core.data.room.converters

import androidx.room.TypeConverter
import com.vci.vectorcamapp.core.domain.model.UploadStatus

class UploadStatusConverter {
    @TypeConverter
    fun fromUploadStatus(status: UploadStatus?): String? = status?.name

    @TypeConverter
    fun toUploadStatus(value: String?): UploadStatus? = value?.let { UploadStatus.valueOf(it) }
}