/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.musicshare

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.vmusic.takeserver.utils.makeTimeString
import kotlin.math.max

/**
 * Single source of truth for the 3D Spotify Edit style layout with ML Kit Selfie Segmentation cutout:
 * the editor preview and the exported bitmap both go through [draw].
 */
object MusicShareRenderer {
    const val EXPORT_WIDTH = 1440
    const val EXPORT_HEIGHT = 1800

    class Layout {
        val photo = RectF()
        val music = RectF()
    }

    fun accentColor(
        art: Bitmap?,
        photo: Bitmap?,
    ): Int {
        val swatch = art?.let { Palette.from(it).generate().dominantSwatch?.rgb }
            ?: photo?.let { Palette.from(it).generate().dominantSwatch?.rgb }
        return swatch ?: 0xFF3A2E55.toInt()
    }

    fun layout(
        w: Int,
        h: Int,
        state: MusicShareState,
    ): Layout =
        Layout().apply {
            photo.set(0f, 0f, w.toFloat(), h.toFloat())

            val mw = w * 0.44f * state.musicScale
            val mh = h * 0.54f * state.musicScale
            val (mx, my) =
                when (state.placement) {
                    MusicPlacement.LEFT -> w * 0.32f to h * 0.50f
                    MusicPlacement.CENTER -> w * 0.50f to h * 0.50f
                    MusicPlacement.RIGHT -> w * 0.68f to h * 0.50f
                }
            val mcx = mx + state.musicOffsetX * w
            val mcy = my + state.musicOffsetY * h
            music.set(mcx - mw / 2f, mcy - mh / 2f, mcx + mw / 2f, mcy + mh / 2f)
        }

    fun draw(
        canvas: Canvas,
        w: Int,
        h: Int,
        song: ShareSongInfo,
        photo: Bitmap?,
        foreground: Bitmap?,
        art: Bitmap?,
        state: MusicShareState,
        accent: Int,
        placeholder: String,
        out: Layout? = null,
    ) {
        val layout = layout(w, h, state)
        out?.let {
            it.photo.set(layout.photo)
            it.music.set(layout.music)
        }

        // Layer 1: Background photo
        drawBackgroundPhoto(canvas, w, h, photo, state, placeholder, accent)

        // Layer 2: 3D Floating Music Card
        drawMusic(canvas, layout.music, song, art, state, accent)

        // Layer 3: Foreground cutout subject (ML Kit Selfie Segmentation) in front of music card for depth/occlusion
        if (foreground != null) {
            drawForegroundSubject(canvas, w, h, foreground, state)
        }

        // VMusic Watermark at bottom-right corner
        val watermarkPaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(160, 255, 255, 255)
                textSize = w * 0.035f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.RIGHT
            }
        canvas.drawText("VMusic", w - w * 0.05f, h - h * 0.04f, watermarkPaint)
    }

    private fun drawBackgroundPhoto(
        canvas: Canvas,
        w: Int,
        h: Int,
        photo: Bitmap?,
        state: MusicShareState,
        placeholder: String,
        accent: Int,
    ) {
        if (photo != null) {
            canvas.save()
            val cx = w / 2f + state.photoOffsetX * w
            val cy = h / 2f + state.photoOffsetY * h
            canvas.translate(cx, cy)
            canvas.rotate(state.photoRotation)
            canvas.scale(state.photoScale, state.photoScale)

            val dest = RectF(-w / 2f, -h / 2f, w / 2f, h / 2f)
            val scale = max(dest.width() / photo.width, dest.height() / photo.height)
            val sw = dest.width() / scale
            val sh = dest.height() / scale
            val sx = (photo.width - sw) / 2f
            val sy = (photo.height - sh) / 2f
            val src = Rect(sx.toInt(), sy.toInt(), (sx + sw).toInt(), (sy + sh).toInt())

            val alpha = (state.photoAlpha * 255f).toInt().coerceIn(0, 255)
            canvas.drawBitmap(
                photo,
                src,
                dest,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { this.alpha = alpha },
            )
            canvas.restore()
        } else {
            val paint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader =
                        LinearGradient(
                            0f,
                            0f,
                            w.toFloat(),
                            h.toFloat(),
                            ColorUtils.blendARGB(accent, Color.BLACK, 0.45f),
                            ColorUtils.blendARGB(accent, Color.BLACK, 0.86f),
                            Shader.TileMode.CLAMP,
                        )
                }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

            val glow =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader =
                        RadialGradient(
                            w * 0.5f,
                            h * 0.45f,
                            w * 0.72f,
                            ColorUtils.blendARGB(accent, Color.WHITE, 0.22f),
                            Color.TRANSPARENT,
                            Shader.TileMode.CLAMP,
                        )
                }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), glow)

            val textPaint =
                TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = w * 0.045f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                }
            canvas.drawText(placeholder, w / 2f, h / 2f, textPaint)
        }
    }

    private fun drawForegroundSubject(
        canvas: Canvas,
        w: Int,
        h: Int,
        foreground: Bitmap,
        state: MusicShareState,
    ) {
        canvas.save()
        val cx = w / 2f + state.photoOffsetX * w
        val cy = h / 2f + state.photoOffsetY * h
        canvas.translate(cx, cy)
        canvas.rotate(state.photoRotation)
        canvas.scale(state.photoScale, state.photoScale)

        val dest = RectF(-w / 2f, -h / 2f, w / 2f, h / 2f)
        val scale = max(dest.width() / foreground.width, dest.height() / foreground.height)
        val sw = dest.width() / scale
        val sh = dest.height() / scale
        val sx = (foreground.width - sw) / 2f
        val sy = (foreground.height - sh) / 2f
        val src = Rect(sx.toInt(), sy.toInt(), (sx + sw).toInt(), (sy + sh).toInt())

        val alpha = (state.photoAlpha * 255f).toInt().coerceIn(0, 255)
        canvas.drawBitmap(
            foreground,
            src,
            dest,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.alpha = alpha },
        )
        canvas.restore()
    }

    private fun drawMusic(
        canvas: Canvas,
        rect: RectF,
        song: ShareSongInfo,
        art: Bitmap?,
        state: MusicShareState,
        accent: Int,
    ) {
        canvas.save()
        canvas.translate(rect.centerX(), rect.centerY())
        canvas.rotate(state.musicRotation)
        canvas.skew(state.musicSkew, 0f)

        val r = RectF(-rect.width() / 2f, -rect.height() / 2f, rect.width() / 2f, rect.height() / 2f)
        val radius = r.width() * 0.08f
        val alpha = (state.musicAlpha * 255f).toInt().coerceIn(0, 255)

        // Glassmorphism Card with 3D drop shadow
        val cardPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(215, 20, 18, 28)
                this.alpha = alpha
                setShadowLayer(50f, 0f, 25f, Color.argb(200, 0, 0, 0))
            }
        canvas.drawRoundRect(r, radius, radius, cardPaint)

        // Glowing border
        val borderPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = ColorUtils.blendARGB(accent, Color.WHITE, 0.45f)
                this.alpha = (alpha * 0.75f).toInt()
            }
        canvas.drawRoundRect(r, radius, radius, borderPaint)

        canvas.clipPath(Path().apply { addRoundRect(r, radius, radius, Path.Direction.CW) })

        // Top highlight
        val highlight = ColorUtils.blendARGB(accent, Color.WHITE, 0.55f)
        canvas.drawRect(
            r.left,
            r.top,
            r.right,
            r.top + r.height() * 0.015f,
            Paint().apply {
                color = highlight
                this.alpha = alpha
            },
        )

        // Album Art (Large square)
        val artMargin = r.width() * 0.10f
        val artSize = r.width() - (artMargin * 2f)
        val artRect = RectF(r.left + artMargin, r.top + r.height() * 0.07f, r.left + artMargin + artSize, r.top + r.height() * 0.07f + artSize)
        val artRadius = artSize * 0.08f

        if (art != null) {
            val scale = max(artSize / art.width, artSize / art.height)
            val sw = artSize / scale
            val sh = artSize / scale
            val src = Rect(((art.width - sw) / 2f).toInt(), ((art.height - sh) / 2f).toInt(), 0, 0)
            src.right = (src.left + sw).toInt()
            src.bottom = (src.top + sh).toInt()
            canvas.save()
            canvas.clipPath(Path().apply { addRoundRect(artRect, artRadius, artRadius, Path.Direction.CW) })
            canvas.drawBitmap(art, src, artRect, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.alpha = alpha })
            canvas.restore()
        } else {
            canvas.drawRoundRect(
                artRect,
                artRadius,
                artRadius,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = ColorUtils.blendARGB(accent, Color.WHITE, 0.3f)
                    this.alpha = alpha
                },
            )
        }

        // Title
        val textLeft = r.left + artMargin
        val maxTextWidth = r.width() - (artMargin * 2f)
        val titlePaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = r.width() * 0.085f
                typeface = Typeface.DEFAULT_BOLD
                this.alpha = alpha
            }
        val titleY = artRect.bottom + r.height() * 0.08f
        val title = TextUtils.ellipsize(song.title, titlePaint, maxTextWidth, TextUtils.TruncateAt.END).toString()
        canvas.drawText(title, textLeft, titleY, titlePaint)

        // Artist
        val artistPaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ColorUtils.blendARGB(Color.WHITE, Color.GRAY, 0.3f)
                textSize = r.width() * 0.065f
                this.alpha = alpha
            }
        val artistY = titleY + r.height() * 0.065f
        val artist = TextUtils.ellipsize(song.artist, artistPaint, maxTextWidth, TextUtils.TruncateAt.END).toString()
        canvas.drawText(artist, textLeft, artistY, artistPaint)

        // Progress Bar
        val barTop = artistY + r.height() * 0.06f
        val barRect = RectF(textLeft, barTop, r.right - artMargin, barTop + r.height() * 0.022f)
        val barRadius = barRect.height() / 2f
        canvas.drawRoundRect(
            barRect,
            barRadius,
            barRadius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(100, 255, 255, 255)
                this.alpha = alpha
            },
        )
        canvas.drawRoundRect(
            RectF(barRect.left, barRect.top, barRect.left + barRect.width() * 0.42f, barRect.bottom),
            barRadius,
            barRadius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = highlight
                this.alpha = alpha
            },
        )

        // Timestamps
        val timePaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(180, 255, 255, 255)
                textSize = r.width() * 0.045f
                this.alpha = alpha
            }
        val timeY = barRect.bottom + r.height() * 0.048f
        canvas.drawText("1:23", barRect.left, timeY, timePaint)
        val durationStr = if (song.duration > 0) makeTimeString(song.duration * 1000L) else "3:21"
        val durWidth = timePaint.measureText(durationStr)
        canvas.drawText(durationStr, barRect.right - durWidth, timeY, timePaint)

        // Playback Controls
        val ctrlY = timeY + r.height() * 0.09f
        val ctrlPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                this.alpha = alpha
            }
        val iconSize = r.width() * 0.065f
        val spacing = r.width() * 0.20f
        val centerX = r.centerX()

        // Play button circle
        val playRadius = r.width() * 0.08f
        canvas.drawCircle(
            centerX,
            ctrlY,
            playRadius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                this.alpha = alpha
            },
        )
        val triPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                this.alpha = alpha
            }
        canvas.drawPath(
            Path().apply {
                moveTo(centerX - playRadius * 0.3f, ctrlY - playRadius * 0.45f)
                lineTo(centerX + playRadius * 0.4f, ctrlY)
                lineTo(centerX - playRadius * 0.3f, ctrlY + playRadius * 0.45f)
                close()
            },
            triPaint,
        )

        // Prev button
        val prevX = centerX - spacing
        canvas.drawRect(prevX - iconSize * 0.15f, ctrlY - iconSize * 0.35f, prevX - iconSize * 0.05f, ctrlY + iconSize * 0.35f, ctrlPaint)
        canvas.drawPath(
            Path().apply {
                moveTo(prevX + iconSize * 0.35f, ctrlY - iconSize * 0.35f)
                lineTo(prevX - iconSize * 0.15f, ctrlY)
                lineTo(prevX + iconSize * 0.35f, ctrlY + iconSize * 0.35f)
                close()
            },
            ctrlPaint,
        )

        // Next button
        val nextX = centerX + spacing
        canvas.drawPath(
            Path().apply {
                moveTo(nextX - iconSize * 0.35f, ctrlY - iconSize * 0.35f)
                lineTo(nextX + iconSize * 0.15f, ctrlY)
                lineTo(nextX - iconSize * 0.35f, ctrlY + iconSize * 0.35f)
                close()
            },
            ctrlPaint,
        )
        canvas.drawRect(nextX + iconSize * 0.05f, ctrlY - iconSize * 0.35f, nextX + iconSize * 0.15f, ctrlY + iconSize * 0.35f, ctrlPaint)

        // VMusic Branding at bottom of card
        val brandPaint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(160, 255, 255, 255)
                textSize = r.width() * 0.045f
                typeface = Typeface.DEFAULT_BOLD
                this.alpha = alpha
            }
        val brandY = r.bottom - r.height() * 0.04f
        canvas.drawText("VMusic", textLeft, brandY, brandPaint)

        canvas.restore()
    }
}
