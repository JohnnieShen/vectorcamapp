package com.vci.vectorcamapp.core.domain.model

import android.net.Uri
import com.vci.vectorcamapp.core.domain.model.enums.UploadStatus
import java.util.UUID

data class SpecimenImage(
    val localId: String,
    val remoteId: Int?,
    val species: String?,
    val sex: String?,
    val abdomenStatus: String?,
    val imageUri: Uri,
    val metadataUploadStatus: UploadStatus,
    val imageUploadStatus: UploadStatus,
    val capturedAt: Long,
    val submittedAt: Long?
)
