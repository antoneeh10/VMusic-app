/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.musicshare

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/** Draggable preview that renders through [MusicShareRenderer] — the same code used for export. */
class MusicShareCanvasView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : View(context, attrs) {
    var song: ShareSongInfo? = null
    var photo: Bitmap? = null
    var foreground: Bitmap? = null
    var art: Bitmap? = null
    var state: MusicShareState = MusicShareState()
    var accent: Int = 0xFF3A2E55.toInt()
    var placeholder: String = ""
    var onStateChange: (MusicShareState) -> Unit = {}

    private val layout = MusicShareRenderer.Layout()
    private var dragTarget = 0
    private var startX = 0f
    private var startY = 0f
    private var baseX = 0f
    private var baseY = 0f

    init {
        // shadowLayer on the preview must match the software canvas used for export
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        val current = song ?: return
        MusicShareRenderer.draw(canvas, width, height, current, photo, foreground, art, state, accent, placeholder, layout)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (song == null) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragTarget =
                    when {
                        layout.photo.contains(event.x, event.y) -> 1
                        layout.music.contains(event.x, event.y) -> 2
                        else -> 0
                    }
                if (dragTarget != 0) {
                    startX = event.x
                    startY = event.y
                    baseX = if (dragTarget == 1) state.photoOffsetX else state.musicOffsetX
                    baseY = if (dragTarget == 1) state.photoOffsetY else state.musicOffsetY
                    parent?.requestDisallowInterceptTouchEvent(true)
                    performClick()
                    return true
                }
            }

            MotionEvent.ACTION_MOVE ->
                if (dragTarget != 0) {
                    val x = (baseX + (event.x - startX) / width).coerceIn(-0.5f, 0.5f)
                    val y = (baseY + (event.y - startY) / height).coerceIn(-0.5f, 0.5f)
                    state =
                        if (dragTarget == 1) {
                            state.copy(photoOffsetX = x, photoOffsetY = y)
                        } else {
                            state.copy(musicOffsetX = x, musicOffsetY = y)
                        }
                    onStateChange(state)
                    invalidate()
                    return true
                }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                if (dragTarget != 0) {
                    dragTarget = 0
                    parent?.requestDisallowInterceptTouchEvent(false)
                    return true
                }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
