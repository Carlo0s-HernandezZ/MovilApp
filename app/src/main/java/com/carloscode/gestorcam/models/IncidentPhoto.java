package com.carloscode.gestorcam.models;

import android.net.Uri;

public class IncidentPhoto {
    private final Uri uri;
    private final String date;
    private final String description; // Nuevo campo

    public IncidentPhoto(Uri uri, String date, String description) {
        this.uri = uri;
        this.date = date;
        this.description = description;
    }

    public Uri getUri() { return uri; }
    public String getDate() { return date; }
    public String getDescription() { return description; } // Nuevo getter
}