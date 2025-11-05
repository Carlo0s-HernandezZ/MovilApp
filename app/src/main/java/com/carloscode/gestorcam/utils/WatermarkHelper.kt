package com.carloscode.gestorcam.utils

import android.content.Context
import android.graphics.*
import android.net.Uri


object WatermarkHelper {

    fun addWatermark(context: Context, imageUri: Uri, text: String) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)

            val paint = Paint().apply {
                color = Color.WHITE
                textSize = 150f  // tamaño de la marca de agua
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(10f, 5f, 5f, Color.BLACK)
                isAntiAlias = true
            }

            val textBounds = Rect()
            paint.getTextBounds(text, 0, text.length, textBounds)
            val x = mutableBitmap.width - textBounds.width() - 40f
            val y = mutableBitmap.height - 60f

            canvas.drawText(text, x, y, paint)

            context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}