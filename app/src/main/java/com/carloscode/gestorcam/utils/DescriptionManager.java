package com.carloscode.gestorcam.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class DescriptionManager {

    private static final String PREF_NAME = "ambu_descriptions";

    // Guardar la descripción vinculada al nombre del archivo
    public static void saveDescription(Context context, String fileName, String description) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(fileName, description);
        editor.apply();
    }

    // Recuperar la descripción usando el nombre del archivo
    public static String getDescription(Context context, String fileName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(fileName, "Sin descripción disponible.");
    }
}