package com.vci.vectorcamapp.ui.extensions

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.vci.vectorcamapp.R
import com.vci.vectorcamapp.core.domain.model.UploadStatus

@Composable
fun UploadStatus.color(): Color = when (this) {
    UploadStatus.NOT_STARTED -> MaterialTheme.colors.error
    UploadStatus.IN_PROGRESS -> MaterialTheme.colors.warning
    UploadStatus.COMPLETED -> MaterialTheme.colors.successConfirm
    UploadStatus.FAILED -> MaterialTheme.colors.error
}

fun UploadStatus.displayText(context: Context): String {
    val resId = when (this) {
        UploadStatus.COMPLETED -> R.string.upload_status_completed
        UploadStatus.IN_PROGRESS -> R.string.upload_status_in_progress
        UploadStatus.NOT_STARTED -> R.string.upload_status_not_started
        UploadStatus.FAILED -> R.string.upload_status_retry
    }
    return context.getString(resId)
}