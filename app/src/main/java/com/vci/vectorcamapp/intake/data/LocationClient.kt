package com.vci.vectorcamapp.intake.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.vci.vectorcamapp.core.domain.util.Result
import com.vci.vectorcamapp.intake.domain.util.IntakeError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationClient @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val LOCATION_CACHE_DURATION_MS = 5 * 60 * 1000L
    }

    suspend fun getCurrentLocation(): Result<Location, IntakeError> =
        suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            val currentTime = System.currentTimeMillis()

            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { lastKnownLocation ->
                    when {
                        lastKnownLocation != null && currentTime - lastKnownLocation.time <= LOCATION_CACHE_DURATION_MS -> {
                            if (continuation.isActive && !continuation.isCompleted) {
                                continuation.resume(Result.Success(lastKnownLocation))
                            }
                        }

                        else -> {
                            requestFreshLocation(cancellationTokenSource, continuation)
                        }
                    }
                }.addOnFailureListener {
                    requestFreshLocation(cancellationTokenSource, continuation)
                }
            } else {
                if (continuation.isActive && !continuation.isCompleted) {
                    continuation.resume(Result.Error(IntakeError.LOCATION_PERMISSION_DENIED))
                }
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }

    private fun requestFreshLocation(
        cancellationTokenSource: CancellationTokenSource,
        continuation: CancellableContinuation<Result<Location, IntakeError>>
    ) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                when {
                    location != null -> {
                        if (continuation.isActive && !continuation.isCompleted) {
                            continuation.resume(Result.Success(location))
                        }
                    }

                    else -> {
                        if (continuation.isActive && !continuation.isCompleted) {
                            continuation.resume(Result.Error(IntakeError.UNKNOWN_ERROR))
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                val error = when (exception) {
                    is SecurityException -> IntakeError.LOCATION_PERMISSION_DENIED
                    else -> IntakeError.UNKNOWN_ERROR
                }
                if (continuation.isActive && !continuation.isCompleted) {
                    continuation.resume(Result.Error(error))
                }
            }
        } else {
            if (continuation.isActive && !continuation.isCompleted) {
                continuation.resume(Result.Error(IntakeError.LOCATION_PERMISSION_DENIED))
            }
        }
    }
}