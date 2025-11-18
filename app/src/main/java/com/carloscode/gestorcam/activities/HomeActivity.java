package com.carloscode.gestorcam.activities;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carloscode.gestorcam.R;
import com.carloscode.gestorcam.models.IncidentPhoto;
import com.carloscode.gestorcam.utils.DescriptionManager;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private List<IncidentPhoto> photoList = new ArrayList<>();

    // Código de solicitud para permisos
    private static final int REQUEST_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- 1. Inicializar Vistas ---
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        rvHistory = findViewById(R.id.rv_history);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // --- 2. Configurar Menú Lateral ---
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        TextView profileButton = navigationView.findViewById(R.id.nav_profile);
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                Toast.makeText(this, "Abriendo Perfil...", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        // --- 3. Configurar RecyclerView ---
        rvHistory.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new HistoryAdapter(this, photoList);
        rvHistory.setAdapter(adapter);

        // --- 4. Permisos y Carga ---
        checkReadPermissions();

        // --- 5. Botón Atrás ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasPermissions()) {
            loadPhotosFromGallery();
        }
    }

    private boolean hasPermissions() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkReadPermissions() {
        if (!hasPermissions()) {
            String permission;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission = Manifest.permission.READ_MEDIA_IMAGES;
            } else {
                permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            }
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PERMISSION_CODE);
        } else {
            loadPhotosFromGallery();
        }
    }

    // --- AQUÍ ESTABA EL ERROR, AHORA ESTÁ CORREGIDO ---
    private void loadPhotosFromGallery() {
        photoList.clear();

        // Ahora pedimos 3 cosas: ID, FECHA y NOMBRE (para buscar la descripción)
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DISPLAY_NAME // <-- Necesario para la descripción
        };

        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        )) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME); // <-- Columna nombre

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    // Fecha
                    long dateAdded = cursor.getLong(dateColumn);
                    String dateString = sdf.format(new Date(dateAdded * 1000));

                    // Descripción
                    String fileName = cursor.getString(nameColumn);
                    String description = DescriptionManager.getDescription(this, fileName);

                    // AHORA SÍ PASAMOS LOS 3 ARGUMENTOS: (Uri, Fecha, Descripción)
                    photoList.add(new IncidentPhoto(contentUri, dateString, description));
                }
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar galería", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPhotosFromGallery();
            } else {
                Toast.makeText(this, "Se necesita permiso para mostrar el historial", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_checklist) {
            startActivity(new Intent(this, ChecklistActivity.class));
        } else if (itemId == R.id.nav_report) {
            startActivity(new Intent(this, ReportIncidentActivity.class));
        } else if (itemId == R.id.nav_history) {
            // Cambiado de Toast a Activity para ver el historial completo
            startActivity(new Intent(this, IncidentHistoryActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}