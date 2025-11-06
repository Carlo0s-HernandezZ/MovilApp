package com.carloscode.gestorcam.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.carloscode.gestorcam.controller.CameraManager
import com.carloscode.gestorcam.databinding.ActivityCameraUseBinding
import com.carloscode.gestorcam.utils.WatermarkHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * ------------------------------------------------------------
 * Clase: CameraUse
 * ------------------------------------------------------------
 * Descripción general:
 * --------------------
 * Esta Activity representa la interfaz principal de captura de fotos.
 *
 * Su función es coordinar el flujo de la cámara mediante `CameraManager`
 * y aplicar una marca de agua a cada imagen capturada usando `WatermarkHelper`.
 *
 * Autor: Team SWGJ
 * Proyecto: GestorCam
 * Fecha: 2025
 * ------------------------------------------------------------
 */

class CameraUse : ComponentActivity() {

    // Enlace con el layout mediante ViewBinding
    private lateinit var viewBinding: ActivityCameraUseBinding

    // Controlador de cámara (maneja toda la lógica de CameraX)
    private lateinit var cameraManager: CameraManager

    /**
     * Permisos de cámara solicitados en tiempo de ejecución
     */
    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.containsValue(false)) {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            } else {
                iniciarCamara()
            }
        }

    /**
     * Método principal que inicializa la vista, permisos y eventos.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraUseBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Inicializamos el controlador de cámara
        cameraManager = CameraManager(this)

        // Verificamos permisos
        if (todosLosPermisosConcedidos()) {
            iniciarCamara()
        } else {
            solicitarPermisos()
        }

        // Listener del botón de captura
        viewBinding.imageCaptureButton.setOnClickListener {
            capturarFoto()
        }
    }

    /**
     * Inicia la cámara con la vista previa en el componente de la interfaz.
     */
    private fun iniciarCamara() {
        cameraManager.startCamera(viewBinding.viewFinder.surfaceProvider)
    }

    /**
     * Captura una foto delegando la lógica a `CameraManager`
     * y aplica una marca de agua a la imagen guardada.
     */
    private fun capturarFoto() {
        cameraManager.takePhoto { uri ->
            if (uri != null) {
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                WatermarkHelper.addWatermark(this, uri, "AMBU | $timestamp")

                val msg = "Foto guardada con éxito con marca de agua"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                Log.d(TAG, msg)
            } else {
                Toast.makeText(this, "Error al capturar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Verifica si todos los permisos requeridos están concedidos.
     */
    private fun todosLosPermisosConcedidos(): Boolean =
        PERMISOS_REQUERIDOS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Solicita los permisos necesarios al usuario.
     */
    private fun solicitarPermisos() {
        permissionsLauncher.launch(PERMISOS_REQUERIDOS)
    }

    /**
     * Libera los recursos de cámara cuando se destruye la actividad.
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraManager.shutdown()
    }

    companion object {
        private const val TAG = "CameraUseActivity"

        /**
         * Lista de permisos requeridos para el funcionamiento de la cámara.
         */
        private val PERMISOS_REQUERIDOS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}
