/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Official Google ML Kit Selfie Segmentation processor for automatic human subject cutout
 * with accurate mask mapping, alpha compositing, and bilinear edge refinement.
 */
object SubjectSegmentationProcessor {
    suspend fun segment(bitmap: Bitmap): Pair<Bitmap, Bitmap?> =
        withContext(Dispatchers.Default) {
            try {
                val options =
                    SelfieSegmenterOptions
                        .Builder()
                        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                        .build()
                val segmenter = Segmentation.getClient(options)
                val inputImage = InputImage.fromBitmap(bitmap, 0)

                val mask = Tasks.await(segmenter.process(inputImage))
                val buffer = mask.buffer
                val maskWidth = mask.width
                val maskHeight = mask.height

                val width = bitmap.width
                val height = bitmap.height
                val fgBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                buffer.rewind()
                val confidenceMap = FloatArray(maskWidth * maskHeight)
                for (i in confidenceMap.indices) {
                    if (buffer.hasRemaining()) {
                        confidenceMap[i] = buffer.float
                    }
                }

                // Bilinear interpolation mapping from mask coordinates to original bitmap resolution
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val fx = (x.toFloat() / width) * (maskWidth - 1)
                        val fy = (y.toFloat() / height) * (maskHeight - 1)
                        val x0 = fx.toInt().coerceIn(0, maskWidth - 1)
                        val y0 = fy.toInt().coerceIn(0, maskHeight - 1)
                        val x1 = (x0 + 1).coerceIn(0, maskWidth - 1)
                        val y1 = (y0 + 1).coerceIn(0, maskHeight - 1)

                        val dx = fx - x0
                        val dy = fy - y0

                        val c00 = confidenceMap[y0 * maskWidth + x0]
                        val c10 = confidenceMap[y0 * maskWidth + x1]
                        val c01 = confidenceMap[y1 * maskWidth + x0]
                        val c11 = confidenceMap[y1 * maskWidth + x1]

                        val confidence =
                            c00 * (1 - dx) * (1 - dy) +
                                c10 * dx * (1 - dy) +
                                c01 * (1 - dx) * dy +
                                c11 * dx * dy

                        val pixel = bitmap.getPixel(x, y)
                        val alpha = (confidence * 255f).toInt().coerceIn(0, 255)
                        if (alpha > 12) {
                            val r = Color.red(pixel)
                            val g = Color.green(pixel)
                            val b = Color.blue(pixel)
                            fgBitmap.setPixel(x, y, Color.argb(alpha, r, g, b))
                        } else {
                            fgBitmap.setPixel(x, y, Color.TRANSPARENT)
                        }
                    }
                }

                Pair(fgBitmap, bitmap)
            } catch (e: Exception) {
                // Fallback to original bitmap if segmentation fails
                Pair(bitmap, null)
            }
        }
}
