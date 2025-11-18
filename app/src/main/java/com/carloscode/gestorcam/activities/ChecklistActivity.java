package com.carloscode.gestorcam.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.carloscode.gestorcam.R;
public class ChecklistActivity extends AppCompatActivity {

    private Spinner spinnerHerramientas;
    private Button btnEnviarChecklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        // --- Configurar la Barra de Herramientas (Toolbar) ---
        Toolbar toolbar = findViewById(R.id.toolbar_checklist);
        setSupportActionBar(toolbar);

        // Añadir la flecha de "Atrás" en la barra de herramientas
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // --- Configurar el Spinner (Dropdown) ---
        spinnerHerramientas = findViewById(R.id.spinner_herramientas);

        // Opciones de ejemplo para el Spinner (Dropdown)
        String[] opciones = {"Seleccionar...", "OK", "Falla", "N/A"};

        // Creamos el adaptador para el spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asignamos el adaptador al Spinner
        spinnerHerramientas.setAdapter(adapter);

        // --- Configurar el Botón de Enviar ---
        btnEnviarChecklist = findViewById(R.id.btn_enviar_checklist);
        btnEnviarChecklist.setOnClickListener(v -> {
            // Aquí iría la lógica para guardar los datos
            Toast.makeText(this, "Check List enviado", Toast.LENGTH_SHORT).show();

            // Cierra la actividad y vuelve a la pantalla Home
            finish();
        });
    }

    // Este método se llama cuando el usuario presiona la flecha "Atrás" de la toolbar
    @Override
    public boolean onSupportNavigateUp() {
        // Simula el comportamiento del botón "Atrás" del dispositivo
        onBackPressed();
        return true;
    }
}