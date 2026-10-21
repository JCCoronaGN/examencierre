package com.example.examencierre.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Proveedor de ubicación. Primero intenta el proveedor fusionado de Google Play Services
 * y, si no está disponible o falla, recurre al LocationManager del propio sistema.
 */
class LocationProvider(private val context: Context) {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context.applicationContext)
    }

    suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null
        val fromFused = runCatching { requestFusedLocation() }.getOrNull()
        fromFused ?: runCatching { requestFrameworkLocation() }.getOrNull()
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private suspend fun requestFusedLocation(): Location? {
        val lastKnown = withTimeoutOrNull(LAST_LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                fusedLocationClient.lastLocation
                    .addOnCompleteListener { task ->
                        val location = if (task.isSuccessful) task.result else null
                        if (cont.isActive) cont.resume(location)
                    }
            }
        }
        if (lastKnown != null && isRecent(lastKnown)) return lastKnown

        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                val cancellationTokenSource = CancellationTokenSource()
                cont.invokeOnCancellation { cancellationTokenSource.cancel() }
                fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
                    .addOnSuccessListener { location ->
                        if (cont.isActive) cont.resume(location)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            }
        } ?: lastKnown
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFrameworkLocation(): Location? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        val providers = manager.getProviders(true).orEmpty()
        val lastKnown = providers
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
        if (lastKnown != null && isRecent(lastKnown)) return lastKnown

        val provider = providers.firstOrNull { it == LocationManager.GPS_PROVIDER }
            ?: providers.firstOrNull()
            ?: return lastKnown

        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        manager.removeUpdates(this)
                        if (cont.isActive) cont.resume(location)
                    }

                    override fun onProviderEnabled(provider: String) = Unit

                    override fun onProviderDisabled(provider: String) = Unit

                    @Deprecated("Reemplazado por onProviderEnabled/onProviderDisabled")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
                }
                cont.invokeOnCancellation { manager.removeUpdates(listener) }
                manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            }
        } ?: lastKnown
    }

    private fun isRecent(location: Location): Boolean =
        System.currentTimeMillis() - location.time <= MAX_LOCATION_AGE_MS

    private companion object {
        const val LOCATION_TIMEOUT_MS = 8_000L
        const val LAST_LOCATION_TIMEOUT_MS = 2_000L
        const val MAX_LOCATION_AGE_MS = 60_000L
    }
}
