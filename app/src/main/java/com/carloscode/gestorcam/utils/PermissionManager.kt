package com.example.ambu.utils

import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class PermissionManager(activity: AppCompatActivity, private val callback: PermissionCallback) {

    // Interface (Callback) para notificar a la Activity
    interface PermissionCallback {
        fun onPermissionsGranted()
        fun onPermissionsDenied()
    }

    private val permissionLauncher: ActivityResultLauncher<Array<String>>

    init {
        // Registramos el lanzador de permisos en la Actividad
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Verificamos si los permisos clave fueron otorgados
            val cameraGranted = permissions.getOrDefault(Manifest.permission.CAMERA, false)
            val locationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)

            if (cameraGranted && locationGranted) {
                callback.onPermissionsGranted()
            } else {
                callback.onPermissionsDenied()
            }
        }
    }

    // Método público para que la Activity inicie la solicitud
    fun requestRequiredPermissions() {
        val permissionsToRequest = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        permissionLauncher.launch(permissionsToRequest)
    }
}