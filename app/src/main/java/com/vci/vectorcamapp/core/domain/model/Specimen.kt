package com.vci.vectorcamapp.core.domain.model

import android.net.Uri

data class Specimen(
    val id: String,
    val species: String?,
    val sex: String?,
    val abdomenStatus: String?,
    val imageUri: Uri,
    val textUploadStatus: UploadStatus,
    val imageUploadStatus: UploadStatus,
    val capturedAt: Long,
    val submittedAt: Long?
)
