package com.example.ambu.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationManager(context: Context, private val callback: LocationCallback) {

    // Interface (Callback) para devolver la ubicación
    interface LocationCallback {
        fun onLocationFetched(location: Location)
        fun onLocationError()
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // OJO: Esto solo debe llamarse DESPUÉS de que los permisos fueron otorgados.
    @SuppressLint("MissingPermission")
    fun fetchLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    callback.onLocationFetched(location)
                } else {
                    callback.onLocationError()
                }
            }
            .addOnFailureListener {
                callback.onLocationError()
            }
    }
}