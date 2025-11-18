package com.carloscode.gestorcam.activities; // (Tu paquete está bien)

// Importaciones necesarias
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.carloscode.gestorcam.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Cambiamos el layout que se muestra:
        //    Ahora carga el 'activity_splash.xml' en lugar de 'activity_main.xml'
        setContentView(R.layout.activity_splash);

        // 2. Ocultamos la barra de acción (se ve mejor para un splash)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 3. Añadimos el código del Handler (el temporizador de 2 segundos)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Este código se ejecuta después de 2 segundos

                // 4. Iniciar la LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);

                // 5. Cerrar esta actividad (MainActivity)
                //    para que el usuario no pueda "volver" al splash
                finish();
            }
        }, 2000); // 2000 milisegundos = 2 segundos
    }
}