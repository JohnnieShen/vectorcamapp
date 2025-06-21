package com.vci.vectorcamapp.core.data.upload.image

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.vci.vectorcamapp.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class ImageUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        createNotificationChannel()

        setForeground(createForegroundInfo())

        val sessionId = inputData.getString("session_id") ?: return Result.retry()

        try {
            for (i in 1..TOTAL_IMAGES) {
                // Simulate image upload
                delay(1000)
                Log.d("UploadWorker", "Uploading image $sessionId")
                updateNotification(i)
            }
        } catch (e: Exception) {
            Log.d("UploadWorker", "Error during upload: ${e.message}")
            return Result.retry()
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Image Upload in Progress")
            .setContentText("Uploading image 0 of $TOTAL_IMAGES")
            .setProgress(TOTAL_IMAGES, 0, false)
            .setSmallIcon(R.drawable.ic_upload)
            .setOngoing(true).build()

        return ForegroundInfo(
            NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun updateNotification(counter: Int) {
        val updatedNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Image Upload in Progress")
            .setContentText("Uploading image $counter of $TOTAL_IMAGES")
            .setProgress(TOTAL_IMAGES, counter, false)
            .setSmallIcon(R.drawable.ic_upload)
            .setOngoing(true).build()

        notificationManager.notify(NOTIFICATION_ID, updatedNotification)
    }

    companion object {
        const val CHANNEL_ID = "image_upload_channel"
        const val CHANNEL_NAME = "Image Upload Channel"
        const val NOTIFICATION_ID = 1001
        const val TOTAL_IMAGES = 20
    }
}
