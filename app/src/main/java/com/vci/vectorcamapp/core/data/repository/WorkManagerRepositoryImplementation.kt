package com.vci.vectorcamapp.core.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.vci.vectorcamapp.core.domain.repository.WorkManagerRepository
import com.vci.vectorcamapp.core.data.upload.image.ImageUploadWorker
import com.vci.vectorcamapp.core.data.upload.metadata.MetadataUploadWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerRepositoryImplementation @Inject constructor(
    private val workManager: WorkManager
) : WorkManagerRepository {

    companion object {
        private const val UPLOAD_WORK_CHAIN_PREFIX = "session_upload_chain_"
    }

    private val uploadConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    override fun enqueueSessionUpload(sessionId: UUID, siteId: Int) {
        val chainName = UPLOAD_WORK_CHAIN_PREFIX + sessionId
        workManager.beginUniqueWork(
            chainName,
            ExistingWorkPolicy.REPLACE,
            buildSessionMetadataWork(sessionId, siteId)
        ).then(buildSessionImageWork(sessionId)).enqueue()
    }

    override fun observeAnySessionUploadRunning(sessionIds: List<UUID>): Flow<Boolean> {
        val chainNames = sessionIds.map { "session_upload_chain_$it" }
        val flows: List<Flow<Boolean>> = chainNames.map { chainName ->
            workManager.getWorkInfosForUniqueWorkFlow(chainName)
                .map { workInfos ->
                    workInfos.any { it.state in listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING ) }
                }
        }
        return when {
            flows.isEmpty() -> flowOf(false)
            else -> combine(flows) { runningArray: Array<Boolean> ->
                runningArray.any { it }
            }
        }
    }

    private fun buildSessionMetadataWork(sessionId: UUID, siteId: Int): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<MetadataUploadWorker>()
            .setInputData(workDataOf(
                "session_id" to sessionId.toString(),
                "site_id" to siteId,
            ))
            .setConstraints(uploadConstraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
    }

    private fun buildSessionImageWork(sessionId: UUID): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<ImageUploadWorker>()
            .setInputData(workDataOf(
                ImageUploadWorker.KEY_SESSION_ID to sessionId.toString()
            ))
            .setConstraints(uploadConstraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
    }
}
