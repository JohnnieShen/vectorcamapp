package com.vci.vectorcamapp.core.data.room.entities

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.vci.vectorcamapp.core.domain.model.enums.UploadStatus
import java.util.UUID

@Entity(
    tableName = "specimen_image", foreignKeys = [
        ForeignKey(
            entity = SpecimenEntity::class,
            parentColumns = ["id", "sessionId"],
            childColumns = ["specimenId", "sessionId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ], indices = [Index(value = ["specimenId", "sessionId"])]
)
data class SpecimenImageEntity(
    @PrimaryKey val localId: String = "",
    val specimenId: String = "",
    val sessionId: UUID = UUID(0, 0),
    val remoteId: Int? = null,
    val species: String? = null,
    val sex: String? = null,
    val abdomenStatus: String? = null,
    val imageUri: Uri = Uri.EMPTY,
    val metadataUploadStatus: UploadStatus = UploadStatus.NOT_STARTED,
    val imageUploadStatus: UploadStatus = UploadStatus.NOT_STARTED,
    val capturedAt: Long = 0L,
    val submittedAt: Long? = null
)
