package com.vci.vectorcamapp.ui.extensions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.vci.vectorcamapp.core.domain.model.UploadStatus

@Composable
fun UploadStatus.color(): Color = when (this) {
    UploadStatus.NOT_STARTED -> MaterialTheme.colors.error
    UploadStatus.PAUSED -> MaterialTheme.colors.error
    UploadStatus.IN_PROGRESS -> MaterialTheme.colors.warning
    UploadStatus.COMPLETED -> MaterialTheme.colors.successConfirm
}

fun UploadStatus.displayName(): String =
    name.replace("_", " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }