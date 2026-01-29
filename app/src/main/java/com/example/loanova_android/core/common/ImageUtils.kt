package com.example.loanova_android.core.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {

    private const val MAXIMAL_SIZE = 1000000 // 1 MB

    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val myFile = createCustomTempFile(context)

            // 1. Get EXIF Orientation
            var rotation = 0f
            try {
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val exif = android.media.ExifInterface(inputStream)
                    val orientation = exif.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                    )
                    rotation = when (orientation) {
                        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                    inputStream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Proceed without rotation if EXIF fails
            }

            // 2. Decode Bitmap
            val inputStream2 = contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream2)
            inputStream2.close()

            if (originalBitmap == null) return null

            // 3. Rotate Bitmap if needed
            val bitmap = if (rotation != 0f) {
                rotateBitmap(originalBitmap, rotation)
            } else {
                originalBitmap
            }

            var compressQuality = 100
            var streamLength: Int
            
            do {
                val bmpStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
                val bmpPicByteArray = bmpStream.toByteArray()
                streamLength = bmpPicByteArray.size
                
                if (streamLength > MAXIMAL_SIZE) {
                    compressQuality -= 5
                }
                
                if (compressQuality < 5) break
                bmpStream.close()
            } while (streamLength > MAXIMAL_SIZE)

            val fos = FileOutputStream(myFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, fos)
            fos.flush()
            fos.close()
            
            myFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun createCustomTempFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File.createTempFile(timeStamp, ".jpg", context.cacheDir)
    }
}
