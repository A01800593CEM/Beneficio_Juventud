package mx.itesm.beneficiojuventud.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Downloads an image from a URL and saves it to internal storage.
 * Returns the file path if successful, null otherwise.
 * Images are compressed to JPEG with quality 75 and scaled down if too large.
 */
suspend fun downloadAndSaveImage(context: Context, imageUrl: String?, promotionId: Int): String? {
    if (imageUrl.isNullOrBlank()) return null

    return withContext(Dispatchers.IO) {
        try {
            // Create images directory in internal storage
            val imagesDir = File(context.filesDir, "promotion_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            // Create a unique filename based on promotion ID
            val filename = "promo_${promotionId}.jpg"
            val imageFile = File(imagesDir, filename)

            // Download and decode the image
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            val inputStream = connection.inputStream
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            connection.disconnect()

            if (originalBitmap == null) {
                Log.e("ImageDownloader", "Failed to decode bitmap from $imageUrl")
                return@withContext null
            }

            // Scale down if image is too large (max dimension 1024px)
            val maxDimension = 1024
            val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
                val scale = maxDimension.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                val newWidth = (originalBitmap.width * scale).toInt()
                val newHeight = (originalBitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true).also {
                    originalBitmap.recycle()
                }
            } else {
                originalBitmap
            }

            // Save to file as JPEG with quality 75
            FileOutputStream(imageFile).use { fos ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos)
            }
            scaledBitmap.recycle()

            Log.d("ImageDownloader", "Saved image for promotion $promotionId to ${imageFile.absolutePath}. Size: ${imageFile.length() / 1024}KB")

            // Return the absolute path
            imageFile.absolutePath
        } catch (e: Exception) {
            Log.e("ImageDownloader", "Failed to download and save image from $imageUrl", e)
            null
        }
    }
}

/**
 * Deletes the image file for a promotion from internal storage
 */
fun deletePromotionImage(context: Context, promotionId: Int) {
    try {
        val imagesDir = File(context.filesDir, "promotion_images")
        val filename = "promo_${promotionId}.jpg"
        val imageFile = File(imagesDir, filename)

        if (imageFile.exists()) {
            imageFile.delete()
            Log.d("ImageDownloader", "Deleted image file for promotion $promotionId")
        }
    } catch (e: Exception) {
        Log.e("ImageDownloader", "Failed to delete image file for promotion $promotionId", e)
    }
}

/**
 * Legacy function - kept for compatibility but deprecated
 * Use downloadAndSaveImage instead
 */
@Deprecated("Use downloadAndSaveImage instead", ReplaceWith("downloadAndSaveImage(context, imageUrl, promotionId)"))
suspend fun downloadImageAsByteArray(imageUrl: String?): ByteArray? {
    if (imageUrl.isNullOrBlank()) return null

    return withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            val inputStream = connection.inputStream
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            connection.disconnect()

            if (originalBitmap == null) {
                Log.e("ImageDownloader", "Failed to decode bitmap from $imageUrl")
                return@withContext null
            }

            val maxDimension = 1024
            val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
                val scale = maxDimension.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                val newWidth = (originalBitmap.width * scale).toInt()
                val newHeight = (originalBitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true).also {
                    originalBitmap.recycle()
                }
            } else {
                originalBitmap
            }

            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            scaledBitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e("ImageDownloader", "Failed to download image from $imageUrl", e)
            null
        }
    }
}
