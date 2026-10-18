/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.canvas

interface CanvasProvider {
    suspend fun fetchCanvas(
        trackIdOrIsrc: String,
        title: String? = null,
        artist: String? = null
    ): CanvasResult
}
