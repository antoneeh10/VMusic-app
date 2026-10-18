/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.canvas

sealed class CanvasResult {
    data class Success(
        val canvasUrl: String,
        val trackUri: String? = null,
        val artistName: String? = null,
        val durationMs: Long? = null
    ) : CanvasResult()

    object Unavailable : CanvasResult()
    data class AuthRequired(val message: String) : CanvasResult()
    data class NetworkError(val message: String) : CanvasResult()
    data class InvalidResponse(val message: String) : CanvasResult()
}
