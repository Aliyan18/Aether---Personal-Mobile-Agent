package com.aether.agent.triggers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.aether.agent.agent.AgentCore
import com.google.android.gms.location.*
import kotlin.math.*

class LocationTrigger(
    private val context: Context,
    private val agentCore: AgentCore,
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null

    fun startMonitoring() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Get continuous location updates
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            60000L, // 1 minute interval
        ).apply {
            setMinUpdateIntervalMillis(30000L) // 30 seconds minimum
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                location?.let {
                    val triggerText = "Location update: ${it.latitude}, ${it.longitude}"
                    agentCore.processTrigger(triggerText, isProactive = true)

                    // Check for geofences (predefined places)
                    checkGeofences(it.latitude, it.longitude)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            context.mainLooper,
        )
    }

    private fun checkGeofences(lat: Double, lng: Double) {
        // Define important places (in real app, user configures these)
        val places = mapOf(
            "home" to Pair(37.7749, -122.4194), // Replace with actual
            "work" to Pair(37.7694, -122.4862),
            "gym" to Pair(37.7710, -122.5099),
        )

        for ((name, coords) in places) {
            val distance = calculateDistance(lat, lng, coords.first, coords.second)
            if (distance < 100) { // Within 100 meters
                agentCore.processTrigger("Arrived at $name", isProactive = true)
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun stopMonitoring() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
}
