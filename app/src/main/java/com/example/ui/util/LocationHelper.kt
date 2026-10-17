package com.example.ui.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class LocationFetchResult(
  val latitude: Double,
  val longitude: Double,
  val mapsUrl: String,
  val accuracyMeters: Float
)

object LocationHelper {

  fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
  }

  fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      ?: return false
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
      locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
  }

  fun buildGoogleMapsUrl(latitude: Double, longitude: Double): String {
    return String.format(Locale.US, "https://maps.google.com/?q=%.6f,%.6f", latitude, longitude)
  }

  @SuppressLint("MissingPermission")
  suspend fun getCurrentLocation(context: Context): LocationFetchResult? {
    if (!hasLocationPermission(context)) {
      return null
    }

    val fusedClient: FusedLocationProviderClient =
      LocationServices.getFusedLocationProviderClient(context)

    val cts = CancellationTokenSource()

    // 1. Try high-accuracy immediate location request
    val currentLocation: Location? = suspendCancellableCoroutine { continuation ->
      fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
        .addOnSuccessListener { loc: Location? ->
          continuation.resume(loc)
        }
        .addOnFailureListener {
          continuation.resume(null)
        }

      continuation.invokeOnCancellation {
        cts.cancel()
      }
    }

    if (currentLocation != null) {
      return LocationFetchResult(
        latitude = currentLocation.latitude,
        longitude = currentLocation.longitude,
        mapsUrl = buildGoogleMapsUrl(currentLocation.latitude, currentLocation.longitude),
        accuracyMeters = currentLocation.accuracy
      )
    }

    // 2. Fallback to last known location if getCurrentLocation was null
    val lastLocation: Location? = suspendCancellableCoroutine { continuation ->
      fusedClient.lastLocation
        .addOnSuccessListener { loc: Location? ->
          continuation.resume(loc)
        }
        .addOnFailureListener {
          continuation.resume(null)
        }
    }

    return lastLocation?.let { loc ->
      LocationFetchResult(
        latitude = loc.latitude,
        longitude = loc.longitude,
        mapsUrl = buildGoogleMapsUrl(loc.latitude, loc.longitude),
        accuracyMeters = loc.accuracy
      )
    }
  }
}
