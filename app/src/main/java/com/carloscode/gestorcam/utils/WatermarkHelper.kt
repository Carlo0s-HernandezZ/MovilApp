package com.carloscode.gestorcam.utils

import android.content.Context
import android.graphics.*
import android.net.Uri

/**
 * ------------------------------------------------------------
 *  Clase: WatermarkHelper
 * ------------------------------------------------------------
 *  Descripción general:
 *  --------------------
 *  Esta clase utilitaria (object en Kotlin) proporciona una función para
 *  agregar una marca de agua de texto a una imagen almacenada en el
 *  almacenamiento del dispositivo.
 *
 *  Se utiliza en conjunto con la clase `CameraUse` para añadir una marca
 *  de agua personalizada (por ejemplo, el nombre de la aplicación y la
 *  fecha/hora actual) inmediatamente después de capturar una foto.
 *
 *  Características principales:
 *  - Abre una imagen mediante su URI.
 *  - Dibuja una marca de agua de texto en la esquina inferior derecha.
 *  - Guarda nuevamente la imagen modificada en el mismo URI.
 *  - Aplica sombra, negritas y anti-aliasing para mejorar la legibilidad.
 *
 *  Autor: Team SWGJ
 *  Proyecto: GestorCam
 *  Fecha: 2025
 * ------------------------------------------------------------
 */

object WatermarkHelper {

    /**
     * Agrega una marca de agua a la imagen especificada.
     *
     * @param context El contexto de la aplicación, necesario para acceder al ContentResolver.
     * @param imageUri El URI de la imagen a la que se le agregará la marca de agua.
     * @param text El texto que se mostrará como marca de agua.
     */
    fun addWatermark(context: Context, imageUri: Uri, text: String) {
        try {
            // Abrir la imagen original desde su URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Crear una copia mutable del bitmap original para poder editarlo
            val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)

            // Configuración de la pintura (color, tamaño, sombra, tipo de letra)
            val paint = Paint().apply {
                color = Color.WHITE                // Color del texto
                textSize = 150f                    // Tamaño de la marca de agua (grande)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(10f, 5f, 5f, Color.BLACK)  // Sombras para contraste
                isAntiAlias = true                 // Suaviza bordes del texto
            }

            // Calcular posición del texto (esquina inferior derecha)
            val textBounds = Rect()
            paint.getTextBounds(text, 0, text.length, textBounds)
            val x = mutableBitmap.width - textBounds.width() - 40f  // Margen derecho
            val y = mutableBitmap.height - 60f                      // Margen inferior

            // Dibujar el texto sobre el bitmap
            canvas.drawText(text, x, y, paint)

            // Guardar la imagen resultante en el mismo URI
            context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

        } catch (e: Exception) {
            // En caso de error (por ejemplo, fallo al acceder al archivo)
            e.printStackTrace()
        }
    }
}
