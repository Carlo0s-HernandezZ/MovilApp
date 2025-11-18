package com.carloscode.gestorcam.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.carloscode.gestorcam.R;
import com.carloscode.gestorcam.utils.DescriptionManager;
import com.example.ambu.utils.LocationManager;
import com.example.ambu.utils.PermissionManager;
import com.example.ambu.utils.WatermarkUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class ReportIncidentActivity extends AppCompatActivity implements
        PermissionManager.PermissionCallback,
        LocationManager.LocationCallback {

    // Vistas
    private ImageButton btnTomarFoto;
    private Button btnEnviarIncidencia;
    private ImageView ivFotoPreview;
    private TextView tvGpsStatus;
    private EditText etDescripcion;

    // Manejadores
    private PermissionManager permissionManager;
    private LocationManager locationManager;

    // Variables
    private Location currentLocation;
    private Uri currentPhotoUri;

    // --- NUEVA VARIABLE: Para recordar el nombre del archivo y actualizar el texto al final ---
    private String currentFileName = null;

    // Lanzador para la Cámara Personalizada
    private final ActivityResultLauncher<Intent> customCameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    this.currentPhotoUri = result.getData().getData();
                    processAndSaveImage();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_incident);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_report);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Vistas
        btnTomarFoto = findViewById(R.id.btn_tomar_foto);
        btnEnviarIncidencia = findViewById(R.id.btn_enviar_incidencia);
        ivFotoPreview = findViewById(R.id.iv_foto_preview);
        tvGpsStatus = findViewById(R.id.tv_gps_status);
        etDescripcion = findViewById(R.id.et_descripcion_incidencia);

        btnTomarFoto.setEnabled(false);

        // Managers
        permissionManager = new PermissionManager(this, this);
        locationManager = new LocationManager(this, this);

        // Listeners
        btnTomarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(ReportIncidentActivity.this, CustomCameraActivity.class);
            customCameraLauncher.launch(intent);
        });

        // --- CORRECCIÓN AQUÍ ---
        btnEnviarIncidencia.setOnClickListener(v -> {
            // Al dar clic en enviar, guardamos el texto FINAL que haya escrito el usuario
            if (currentFileName != null) {
                String textoFinal = etDescripcion.getText().toString();
                if (textoFinal.isEmpty()) {
                    textoFinal = "Sin descripción proporcionada.";
                }
                // Actualizamos la descripción vinculada a la foto
                DescriptionManager.saveDescription(this, currentFileName, textoFinal);
            }

            Toast.makeText(this, "Incidencia guardada y registrada", Toast.LENGTH_SHORT).show();
            finish();
        });

        permissionManager.requestRequiredPermissions();
    }

    // --- LÓGICA PRINCIPAL ---

    private void processAndSaveImage() {
        if (currentPhotoUri == null) return;

        try (InputStream inputStream = getContentResolver().openInputStream(currentPhotoUri)) {
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            long timestamp = System.currentTimeMillis();

            // 1. Marca de Agua
            Bitmap watermarkedBitmap = WatermarkUtils.addWatermark(
                    this,
                    originalBitmap,
                    currentLocation,
                    timestamp
            );

            // 2. Mostrar Preview
            ivFotoPreview.setImageBitmap(watermarkedBitmap);
            ivFotoPreview.setVisibility(View.VISIBLE);

            // 3. Guardar inicialmente (aunque el texto cambie después)
            String textoTemporal = etDescripcion.getText().toString();
            saveToGallery(watermarkedBitmap, timestamp, textoTemporal);

            btnEnviarIncidencia.setEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToGallery(Bitmap bitmap, long timestamp, String description) {
        String fileName = "AMBU_" + timestamp + ".jpg";

        // Guardamos el nombre en la variable global para usarla en el botón 'Enviar'
        this.currentFileName = fileName;

        // Guardamos la descripción inicial
        DescriptionManager.saveDescription(this, fileName, description);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AMBU");

        ContentResolver resolver = getContentResolver();
        Uri uri = null;

        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = resolver.openOutputStream(uri)) {
                    if (out != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    }
                }
                // Solo mostramos mensaje, no cerramos todavía
                Toast.makeText(this, "Evidencia capturada", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Callbacks obligatorios
    @Override
    public void onPermissionsGranted() {
        locationManager.fetchLastLocation();
    }

    @Override
    public void onPermissionsDenied() {
        Toast.makeText(this, "Se requieren permisos", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationFetched(Location location) {
        this.currentLocation = location;
        tvGpsStatus.setText(String.format(Locale.US, "GPS: %.4f, %.4f", location.getLatitude(), location.getLongitude()));
        btnTomarFoto.setEnabled(true);
    }

    @Override
    public void onLocationError() {
        tvGpsStatus.setText("GPS: Error. Reintentando...");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}