package com.carloscode.gestorcam.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.carloscode.gestorcam.R;
import com.carloscode.gestorcam.activities.HomeActivity;


public class LoginActivity extends AppCompatActivity{
    private EditText etUsuario;
    private EditText etContrasena;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ocultar la barra de acción
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializar las vistas
        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        btnEntrar = findViewById(R.id.btnEntrar);

        // (Aquí pondremos la lógica del botón "Entrar" después)
        btnEntrar.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString();
            String contrasena = etContrasena.getText().toString();

            // Usuario y contraseña por defecto
            if (usuario.equals("admin") && contrasena.equals("ambu123")) {
                // Credenciales correctas -> Abrir HomeActivity
                Intent intent =  new Intent(LoginActivity.this, HomeActivity.class);
//                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // Cerramos LoginActivity para que no pueda volver
            } else {
                // Error
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
