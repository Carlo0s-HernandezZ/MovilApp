package com.carloscode.gestorcam

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture // Import para capturar imagen
import androidx.camera.core.ImageCaptureException // Import para errores
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.carloscode.gestorcam.databinding.ActivityCameraUseBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraUse : ComponentActivity() {

    private lateinit var viewBinding: ActivityCameraUseBinding
    private lateinit var cameraExecutor: ExecutorService

    // variable para el caso de uso de Captura de Imagen
    private var imageCapture: ImageCapture? = null

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permiso denegado.",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraUseBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Configuramos el "click listener" para el botón de captura
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    // función que se llama al pulsar el botón
    private fun takePhoto() {
        // Obtenemos una referencia estable del caso de uso de captura
        // Si imageCapture no está listo (null), salimos de la función.
        val imageCapture = imageCapture ?: return

        // nombre único para el archivo basado en la hora actual
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        // Configuramos los metadatos de la imagen para MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GestorCam-Images")
            }
        }

        // objeto de opciones de salida (donde guardar la foto)
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        //  takePicture()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this), // En qué hilo correr el resultado
            object : ImageCapture.OnImageSavedCallback { // Qué hacer después

                // En caso de error al guardar
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Fallo al guardar la foto: ${exc.message}", exc)
                    Toast.makeText(baseContext, "Error al guardar foto", Toast.LENGTH_SHORT).show()
                }

                // En caso de éxito al guardar
                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Foto guardada con éxito: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            // Inicializamos nuestro imageCapture
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                // Añadimos imageCapture al "bind"
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture) // <--- AÑADIDO AQUÍ

            } catch(exc: Exception) {
                Log.e(TAG, "Fallo al iniciar la cámara", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        //  Formato para el nombre del archivo
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                //  permiso de escritura si es Android <= P (API 28)

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}