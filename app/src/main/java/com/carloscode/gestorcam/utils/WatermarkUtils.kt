package com.example.ambu.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import com.carloscode.gestorcam.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WatermarkUtils {

    @JvmStatic
    fun addWatermark(
        context: Context,
        originalBitmap: Bitmap,
        location: Location?,
        timestamp: Long
    ): Bitmap {
        // Creamos una copia mutable de la imagen para poder dibujar sobre ella
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val width = canvas.width
        val height = canvas.height

        // --- 1. CONFIGURACIÓN DE TAMAÑOS INTELIGENTES ---
        // El tamaño del texto se adapta al 4% del ancho de la foto
        val textSize = width * 0.04f
        val margin = width * 0.03f

        // --- 2. PINCELES (PAINT) ---
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            style = Paint.Style.FILL
            // Sombra fuerte para legibilidad en cualquier fondo
            setShadowLayer(12f, 6f, 6f, Color.BLACK)
        }

        // --- 3. PREPARAR DATOS ---
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val orgName = "AMBU" // El nombre que pediste
        val dateStr = "Fecha: " + dateFormat.format(Date(timestamp))
        val timeStr = "Hora: " + timeFormat.format(Date(timestamp))

        val gpsStr = location?.let {
            String.format(Locale.US, "Lat: %.5f  Lon: %.5f", it.latitude, it.longitude)
        } ?: "GPS: Buscando señal..."

        // --- 4. DIBUJAR TEXTO (Esquina INFERIOR Izquierda) ---
        // Empezamos desde abajo del todo y vamos subiendo línea por línea

        var textY = height - margin - 20f // Posición inicial (Línea más baja)
        val textX = margin

        // Línea 1: Nombre AMBU (Abajo del todo)
        // Puedes hacerlo más destacado si quieres (ej. negrita), pero aquí usa el mismo estilo
        canvas.drawText(orgName, textX, textY, textPaint)

        // Subimos para la siguiente línea
        textY -= (textSize * 1.4f)
        // Línea 2: GPS
        canvas.drawText(gpsStr, textX, textY, textPaint)

        // Subimos otra vez
        textY -= (textSize * 1.4f)
        // Línea 3: Hora
        canvas.drawText(timeStr, textX, textY, textPaint)

        // Subimos otra vez
        textY -= (textSize * 1.4f)
        // Línea 4: Fecha
        canvas.drawText(dateStr, textX, textY, textPaint)

        return mutableBitmap
    }
}