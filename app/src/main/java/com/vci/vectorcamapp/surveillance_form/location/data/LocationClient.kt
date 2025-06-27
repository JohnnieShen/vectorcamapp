package com.vci.vectorcamapp.surveillance_form.location.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class LocationClient @Inject constructor(
    private val fused: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MAX_LAST_LOCATION_AGE_MS = 5 * 60 * 1_000L
    }

    suspend fun getCurrentLocation(): Location = suspendCancellableCoroutine { cont ->
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cont.resumeWithException(
                SecurityException("ACCESS_FINE_LOCATION permission not granted")
            )
            return@suspendCancellableCoroutine
        }

            val cancellationTokenSource = CancellationTokenSource()
            val now = System.currentTimeMillis()

            fused.lastLocation
                .addOnSuccessListener { last ->
                    if (last != null && now - last.time <= MAX_LAST_LOCATION_AGE_MS) {
                        if (cont.isActive) cont.resume(last)
                    } else {
                        requestFreshLocation(cancellationTokenSource, cont)
                    }
                }
                .addOnFailureListener {
                    requestFreshLocation(cancellationTokenSource, cont)
                }

            cont.invokeOnCancellation { cancellationTokenSource.cancel() }
        }

    private fun requestFreshLocation(
        cancellationTokenSource: CancellationTokenSource,
        cancellableContinuation: CancellableContinuation<Location>
    ) {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    fused.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { loc ->
            if (loc != null && cancellableContinuation.isActive) {
                cancellableContinuation.resume(loc)
            } else if (cancellableContinuation.isActive) {
                cancellableContinuation.resumeWithException(RuntimeException("No location available"))
            }
        }.addOnFailureListener { e ->
            if (cancellableContinuation.isActive) cancellableContinuation.resumeWithException(e)
        }
    }
}