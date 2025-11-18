package com.carloscode.gestorcam.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.carloscode.gestorcam.R;
import com.carloscode.gestorcam.fragment.CargarVideo;
import com.carloscode.gestorcam.fragment.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // --- Declaración de Vistas ---
    // Es buena práctica declararlas aquí para tener una visión general de los componentes de la clase.
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;
    // No es necesario declarar aquí vistas que solo se usan en un método (como Toolbar).

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicializar las vistas principales
        initViews();

        // 2. Configurar la barra de herramientas y el menú lateral
        setupToolbarAndDrawer();

        // 3. Configurar los listeners para los eventos de click
        setupListeners();

        // 4. Cargar el fragmento inicial solo la primera vez que se crea la actividad
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.home); // Marcar el ítem de inicio
        }
    }

    /**
     * Inicializa las vistas principales de la actividad.
     */
    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);
        drawerLayout = findViewById(R.id.drawer_layout);
    }

    /**
     * Configura la Toolbar como ActionBar y el DrawerLayout (menú lateral).
     */
    private void setupToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this); // Asignar el listener para el menú lateral
    }

    /**
     * Centraliza la configuración de todos los listeners de la actividad.
     */
    private void setupListeners() {
        // Listener para la barra de navegación inferior
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.cargarvideo) { // Asegúrate que este ID exista en `menu/bottom_menu.xml`
                replaceFragment(new CargarVideo());
                return true;
            }
            return false;
        });

        // Listener para el Floating Action Button (FAB)
        fab.setOnClickListener(view -> showBottomDialog());
    }

    /**
     * Reemplaza el fragmento actual en el contenedor principal.
     * Es más eficiente verificar si el fragmento ya está visible para no reemplazarlo innecesariamente.
     * @param fragment El nuevo fragmento a mostrar.
     */
    private void replaceFragment(Fragment fragment) {
        // Usamos el ID correcto que corregimos en el paso anterior.
        // Si no lo has hecho, asegúrate de que el FrameLayout en activity_main.xml tenga este ID.
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Muestra un diálogo personalizado en la parte inferior de la pantalla.
     */
    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        // Configuración de los clics dentro del diálogo
        LinearLayout videoLayout = dialog.findViewById(R.id.layoutVideo);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        videoLayout.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Upload a Video is clicked", Toast.LENGTH_SHORT).show();
            // Aquí puedes añadir la lógica para abrir la galería o lo que necesites
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(view -> dialog.dismiss());

        // Estilo y animación del diálogo
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            window.setGravity(Gravity.BOTTOM);
        }

        dialog.show();
    }

    /**
     * Maneja los clics en los ítems del menú de navegación lateral (Drawer).
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
        int itemId = item.getItemId();

        // Ejemplo de cómo manejar los clics del menú lateral
        // if (itemId == R.id.nav_profile) {
        //     Toast.makeText(this, "Perfil seleccionado", Toast.LENGTH_SHORT).show();
        // } else if (itemId == R.id.nav_settings) {
        //     Toast.makeText(this, "Ajustes seleccionado", Toast.LENGTH_SHORT).show();
        // }

        // Cierra el menú lateral después de pulsar un ítem
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Cierra el menú lateral si está abierto al presionar el botón de "atrás".
     * Si no, ejecuta el comportamiento por defecto.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
