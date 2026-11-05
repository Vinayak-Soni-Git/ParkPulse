package com.sonisoft.parkpulse.feature.parks.data.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatermarkProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class WatermarkMetadata(
        val parkName: String,
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float,
        val capturedAt: Long
    )

    suspend fun processAndSaveWatermarkedPhoto(
        tempImageFile: File,
        metadata: WatermarkMetadata
    ): File = withContext(Dispatchers.IO) {
        val originalBitmap = BitmapFactory.decodeFile(tempImageFile.absolutePath)
            ?: throw IllegalArgumentException("Failed to decode image from ${tempImageFile.absolutePath}")

        // 1. Correct EXIF Orientation
        val rotatedBitmap = rotateBitmapIfRequired(originalBitmap, tempImageFile)

        // 2. Create mutable copy for drawing
        val mutableBitmap = rotatedBitmap.copy(Bitmap.Config.ARGB_8888, true)
        if (rotatedBitmap != originalBitmap && !rotatedBitmap.isRecycled) {
            rotatedBitmap.recycle()
        }
        if (!originalBitmap.isRecycled) {
            originalBitmap.recycle()
        }

        // 3. Burn watermark into bitmap pixels using Canvas
        val canvas = Canvas(mutableBitmap)
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        // Adaptive scaling based on image width
        val baseTextSize = width * 0.025f
        val padding = width * 0.03f
        val lineSpacing = baseTextSize * 1.3f

        val headerPaint = Paint().apply {
            color = Color.parseColor("#4CAF50") // Forest Green accent
            textSize = baseTextSize * 1.2f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = baseTextSize
            isAntiAlias = true
            typeface = Typeface.DEFAULT
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(metadata.capturedAt))

        val textLines = listOf(
            "PARKPULSE | FIELD SURVEY",
            "Park: ${metadata.parkName}",
            String.format(Locale.getDefault(), "Latitude: %.5f  Longitude: %.5f", metadata.latitude, metadata.longitude),
            String.format(Locale.getDefault(), "GPS Accuracy: %.1f m", metadata.accuracyMeters),
            "Captured: $formattedDate"
        )

        val bannerHeight = (textLines.size * lineSpacing) + (padding * 2)
        val bannerTop = height - bannerHeight - padding

        // Draw semi-transparent background banner
        val bgPaint = Paint().apply {
            color = Color.argb(190, 20, 20, 20) // Semi-transparent dark
            isAntiAlias = true
        }

        val bannerRect = RectF(padding, bannerTop, width - padding, height - padding)
        val cornerRadius = width * 0.02f
        canvas.drawRoundRect(bannerRect, cornerRadius, cornerRadius, bgPaint)

        // Draw text lines
        var currentY = bannerTop + padding + baseTextSize
        textLines.forEachIndexed { index, line ->
            val p = if (index == 0) headerPaint else textPaint
            canvas.drawText(line, padding + (width * 0.02f), currentY, p)
            currentY += lineSpacing
        }

        // 4. Save processed image to persistent app storage directory
        val photosDir = File(context.filesDir, "survey_photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }

        val outputFile = File(photosDir, "survey_${UUID.randomUUID()}.jpg")
        FileOutputStream(outputFile).use { out ->
            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        mutableBitmap.recycle()

        // Clean up temporary capture file
        if (tempImageFile.exists()) {
            tempImageFile.delete()
        }

        outputFile
    }

    private fun rotateBitmapIfRequired(bitmap: Bitmap, imageFile: File): Bitmap {
        return try {
            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val degrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (degrees != 0f) {
                val matrix = Matrix().apply { postRotate(degrees) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }
}
