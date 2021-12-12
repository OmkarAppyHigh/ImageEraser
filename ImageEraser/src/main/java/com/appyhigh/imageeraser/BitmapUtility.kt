package com.appyhigh.imageeraser

import android.graphics.*

object BitmapUtility {

    fun getResizedBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap? {
        val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val originalWidth = bitmap.width.toFloat()
        val originalHeight = bitmap.height.toFloat()
        val canvas = Canvas(background)
        val scale = width / originalWidth
        val xTranslation = 0.0f
        val yTranslation = (height - originalHeight * scale) / 2.0f
        val transformation = Matrix()
        transformation.postTranslate(xTranslation, yTranslation)
        transformation.preScale(scale, scale)
        val paint = Paint()
        paint.isFilterBitmap = true
        canvas.drawBitmap(bitmap, transformation, paint)
        return background
    }

    fun getBorderedBitmap(image: Bitmap, borderColor: Int, borderSize: Int): Bitmap? {

        // Creating a canvas with an empty bitmap, this is the bitmap that gonna store the final canvas changes
        val finalImage = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalImage)

        // Make a smaller copy of the image to draw on top of original
        val imageCopy = Bitmap.createScaledBitmap(
            image,
            image.width - borderSize,
            image.height - borderSize,
            true
        )

        // Let's draw the bigger image using a white paint brush
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(borderColor, PorterDuff.Mode.SRC_ATOP)
        canvas.drawBitmap(image, 0f, 0f, paint)
        val width = image.width
        val height = image.height
        val centerX = (width - imageCopy.width) * 0.5f
        val centerY = (height - imageCopy.height) * 0.5f
        // Now let's draw the original image on top of the white image, passing a null paint because we want to keep it original
        canvas.drawBitmap(imageCopy, centerX, centerY, null)

        // Returning the image with the final results
        return finalImage
    }

}