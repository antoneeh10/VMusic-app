/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.wrapped.story

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.graphics.ColorUtils
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.vmusic.takeserver.R
import com.vmusic.takeserver.utils.ComposeToImage
import java.util.Locale

/** Renders the summary card to a bitmap and opens the Android share sheet. */
object WrappedStoryShare {
    private const val WIDTH = 1080
    private const val HEIGHT = 1920

    suspend fun share(
        context: Context,
        data: WrappedStoryData,
        colors: Pair<Int, Int>,
    ) {
        val bitmap = render(context, data, colors)
        val uri = ComposeToImage.saveBitmapAsFile(context, bitmap, "vmusic_wrapped_${data.year}_${System.currentTimeMillis()}")
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        val chooser =
            Intent.createChooser(intent, context.getString(R.string.wrapped_story_share_label))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private suspend fun render(
        context: Context,
        data: WrappedStoryData,
        colors: Pair<Int, Int>,
    ): Bitmap {
        val (background, onBackground) = colors
        val result = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val bgPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader =
                    LinearGradient(
                        0f,
                        0f,
                        WIDTH.toFloat(),
                        HEIGHT.toFloat(),
                        background,
                        ColorUtils.blendARGB(background, onBackground, 0.22f),
                        Shader.TileMode.CLAMP,
                    )
            }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), bgPaint)

        val titlePaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = onBackground
                textSize = 96f
                typeface = Typeface.DEFAULT_BOLD
            }
        val labelPaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = onBackground
                textSize = 40f
                typeface = Typeface.DEFAULT_BOLD
            }
        val bodyPaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = onBackground
                textSize = 36f
            }

        canvas.drawText("VMusic Wrapped", 80f, 170f, titlePaint)
        canvas.drawText("${data.year}", 80f, 280f, titlePaint)

        val cardPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(40, 255, 255, 255)
            }
        canvas.drawRoundRect(RectF(60f, 360f, WIDTH - 60f, 1500f), 56f, 56f, cardPaint)

        canvas.drawText(
            context.getString(R.string.wrapped_story_minutes_label).uppercase(Locale("id", "ID")),
            110f,
            460f,
            labelPaint,
        )
        canvas.drawText(String.format(Locale("id", "ID"), "%,d", data.totalMinutes), 110f, 600f, titlePaint)
        canvas.drawText(context.getString(R.string.wrapped_story_minutes_unit), 110f, 670f, bodyPaint)

        var y = 790f
        val top = data.topSongs.take(5)
        if (top.isEmpty()) {
            canvas.drawText(context.getString(R.string.wrapped_story_empty), 110f, y, bodyPaint)
        } else {
            top.forEachIndexed { index, song ->
                canvas.drawText("${index + 1}.", 110f, y, labelPaint)
                val text =
                    TextUtils.ellipsize(
                        "${song.title} — ${song.artist}",
                        bodyPaint,
                        (WIDTH - 340).toFloat(),
                        TextUtils.TruncateAt.END,
                    ).toString()
                canvas.drawText(text, 200f, y, bodyPaint)
                y += 100f
            }
        }

        canvas.drawText(context.getString(R.string.wrapped_story_handle), 80f, HEIGHT - 120f, bodyPaint)
        return result
    }
}
