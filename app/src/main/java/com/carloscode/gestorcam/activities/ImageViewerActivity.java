package com.carloscode.gestorcam.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.carloscode.gestorcam.R;

public class ImageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        Toolbar toolbar = findViewById(R.id.image_viewer_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(""); // Opcional: quitar título si solo quieres la flecha
        }

        ImageView fullScreenImageView = findViewById(R.id.full_screen_image_view);

        // Recibir la URI de la imagen
        String imageUriString = getIntent().getStringExtra("image_uri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            Glide.with(this)
                    .load(imageUri)
                    .into(fullScreenImageView);
        } else {
            // Manejar caso donde no se pasó URI
            finish();
            return;
        }

        // Para ocultar la barra de estado y navegación para una experiencia inmersiva
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}