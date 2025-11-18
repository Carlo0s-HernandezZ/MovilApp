package com.carloscode.gestorcam.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.carloscode.gestorcam.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomCameraActivity extends AppCompatActivity {

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private PreviewView viewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);

        // Ocultar ActionBar para pantalla completa
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        viewFinder = findViewById(R.id.viewFinder);
        ImageButton captureButton = findViewById(R.id.camera_capture_button);

        // Iniciar la cámara
        startCamera();

        // Listener del botón de disparo
        captureButton.setOnClickListener(v -> takePhoto());

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Configurar Vista Previa
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // Configurar Captura de Imagen
                imageCapture = new ImageCapture.Builder().build();

                // Seleccionar cámara trasera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind y Bind al ciclo de vida
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Error al iniciar cámara", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        // Crear archivo temporal para guardar la foto cruda
        String filename = "TEMP_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg";
        File photoFile = new File(getExternalFilesDir(null), filename);

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // La foto se tomó con éxito.
                        Uri savedUri = Uri.fromFile(photoFile);

                        // Devolvemos el URI a la actividad anterior
                        Intent resultIntent = new Intent();
                        resultIntent.setData(savedUri);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Falló la captura: " + exception.getMessage(), exception);
                        Toast.makeText(getBaseContext(), "Error al capturar", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}