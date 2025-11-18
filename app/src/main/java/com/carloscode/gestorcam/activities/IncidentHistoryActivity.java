package com.carloscode.gestorcam.activities;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carloscode.gestorcam.R;
import com.carloscode.gestorcam.models.IncidentPhoto;
import com.carloscode.gestorcam.utils.DescriptionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncidentHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private IncidentHistoryAdapter adapter;
    private List<IncidentPhoto> photoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_history);

        Toolbar toolbar = findViewById(R.id.history_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rvHistory = findViewById(R.id.rv_full_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new IncidentHistoryAdapter(this, photoList);
        rvHistory.setAdapter(adapter);

        loadPhotosFromGallery();
    }

    private void loadPhotosFromGallery() {
        photoList.clear();

        // Pedimos ID, FECHA y NOMBRE DEL ARCHIVO (para buscar la descripción)
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DISPLAY_NAME
        };
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null, null, sortOrder)) {

            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
                int nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idCol);
                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    long dateAdded = cursor.getLong(dateCol);
                    String dateStr = sdf.format(new Date(dateAdded * 1000));

                    // Obtenemos el nombre del archivo para buscar la descripción guardada
                    String fileName = cursor.getString(nameCol);
                    String description = DescriptionManager.getDescription(this, fileName);

                    photoList.add(new IncidentPhoto(uri, dateStr, description));
                }
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}