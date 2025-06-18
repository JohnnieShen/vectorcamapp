package com.vci.vectorcamapp.core.data.upload.metadata

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
import com.vci.vectorcamapp.core.data.network.api.RemoteSessionDataSource
import com.vci.vectorcamapp.core.domain.repository.SessionRepository
import com.vci.vectorcamapp.core.domain.util.onError
import com.vci.vectorcamapp.core.domain.util.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import java.util.UUID

@HiltWorker
class MetadataUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sessionRepository: SessionRepository,
    private val remoteSessionDataSource: RemoteSessionDataSource
) : CoroutineWorker(context, workerParams) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        createNotificationChannel()

        setForeground(createForegroundInfo())

        val session = inputData.getString("sessionId")?.let { UUID.fromString(it) }
            ?.let { sessionRepository.getSessionById(it) } ?: return Result.retry()

        try {
            remoteSessionDataSource.postSession(session).onSuccess { it
                Log.d("UploadWorker", "Session uploaded successfully")
            }.onError {
                Log.d("UploadWorker", "Error uploading session: $it")
                return Result.retry()
            }

            for (i in 1..TOTAL_DATAPOINTS) {
                // Simulate metadata upload
                delay(1000)
                Log.d("UploadWorker", "Uploading datapoint ${session.id}")
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
            .setContentTitle("Session Metadata Upload in Progress")
            .setContentText("Uploading datapoint 0 of $TOTAL_DATAPOINTS")
            .setProgress(TOTAL_DATAPOINTS, 0, false).setSmallIcon(R.drawable.ic_upload)
            .setOngoing(true).build()

        return ForegroundInfo(
            NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun updateNotification(counter: Int) {
        val updatedNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Session Metadata Upload in Progress")
            .setContentText("Uploading datapoint $counter of $TOTAL_DATAPOINTS")
            .setProgress(TOTAL_DATAPOINTS, counter, false).setSmallIcon(R.drawable.ic_upload)
            .setOngoing(true).build()

        notificationManager.notify(NOTIFICATION_ID, updatedNotification)
    }

    companion object {
        const val CHANNEL_ID = "metadata_upload_channel"
        const val CHANNEL_NAME = "Metadata Upload Channel"
        const val NOTIFICATION_ID = 1002
        const val TOTAL_DATAPOINTS = 20
    }
}