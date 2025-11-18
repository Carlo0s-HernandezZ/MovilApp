package com.carloscode.gestorcam.controller

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * ------------------------------------------------------------
 *  Clase: CameraManager
 * ------------------------------------------------------------
 *  Descripción general:
 *  --------------------
 *  Esta clase se encarga de controlar la cámara del dispositivo usando
 *  la librería **CameraX**, separando la lógica de captura de imagen
 *  del Activity principal.
 *
 *  Su objetivo es ofrecer una interfaz limpia para:
 *   - Iniciar la cámara.
 *   - Configurar la vista previa (Preview).
 *   - Tomar fotografías.
 *   - Guardar las imágenes capturadas en el almacenamiento del dispositivo.
 *
 *  Esta clase actúa como un "controlador" (Controller) dentro de la arquitectura
 *  del proyecto, recibiendo las órdenes desde la Activity (vista) y gestionando
 *  las operaciones de cámara y almacenamiento.
 *
 *  Autor: CarlosCode
 *  Proyecto: GestorCam
 *  Fecha: 2025
 * ------------------------------------------------------------
 */

class CameraManager(private val context: Context) {

    // Objeto responsable de realizar las capturas de imagen
    private var imageCapture: ImageCapture? = null

    // Hilo de ejecución dedicado a tareas de cámara
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Inicia la cámara y configura la vista previa en el componente recibido.
     *
     * @param surfaceProvider El proveedor de superficie donde se mostrará la vista previa.
     */
    fun startCamera(surfaceProvider: Preview.SurfaceProvider) {
        // Se obtiene el proveedor de cámara de CameraX
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configura la vista previa (Preview)
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(surfaceProvider)
            }

            // Configura la captura de imagen
            imageCapture = ImageCapture.Builder().build()

            // Selecciona la cámara trasera por defecto
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Desvincula cualquier cámara activa antes de volver a enlazar
                cameraProvider.unbindAll()

                // Enlaza la cámara con el ciclo de vida del componente que la llama
                cameraProvider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Error al iniciar cámara", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Captura una fotografía y la guarda en el almacenamiento externo del dispositivo.
     *
     * @param onResult Callback que devuelve la URI de la imagen guardada,
     *                 o null si hubo un error durante la captura.
     */
    fun takePhoto(onResult: (Uri?) -> Unit) {
        val imageCapture = imageCapture ?: return

        // Se genera un nombre de archivo con formato de fecha
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        // Definición de los metadatos y ruta donde se guardará la imagen
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            // En versiones recientes de Android, se define un directorio específico
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GestorCam-Images")
            }
        }

        // Se configuran las opciones de salida (OutputFileOptions)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        // Se realiza la captura de la foto
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {

                // En caso de error al guardar
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Fallo al guardar la foto: ${exc.message}", exc)
                    Toast.makeText(context, "Error al guardar la foto", Toast.LENGTH_SHORT).show()
                    onResult(null)
                }

                // Si la foto se guardó correctamente
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Foto guardada: ${output.savedUri}")
                    onResult(output.savedUri)
                }
            }
        )
    }

    /**
     * Libera los recursos asociados al hilo de cámara cuando ya no se usa.
     * Es recomendable llamarlo en el método onDestroy() del Activity.
     */
    fun shutdown() {
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraManager"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}
