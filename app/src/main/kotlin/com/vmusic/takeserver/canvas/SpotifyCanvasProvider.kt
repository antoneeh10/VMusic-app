/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.canvas

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpotifyCanvasProvider(
    private val fetcher: CanvasFetcher = SpotifyCanvasClient()
) : CanvasProvider {

    @Volatile
    var spDcToken: String? = null

    @Volatile
    var isEnabled: Boolean = false

    override suspend fun fetchCanvas(
        trackIdOrIsrc: String,
        title: String?,
        artist: String?
    ): CanvasResult = withContext(Dispatchers.IO) {
        if (!isEnabled) return@withContext CanvasResult.Unavailable

        val token = spDcToken
        if (token.isNullOrBlank()) {
            return@withContext CanvasResult.AuthRequired("Spotify sp_dc token is required in settings.")
        }

        val canvasUrl = fetcher.fetchCanvas(title, artist, token)
        if (canvasUrl != null) {
            CanvasResult.Success(canvasUrl = canvasUrl, artistName = artist)
        } else {
            CanvasResult.Unavailable
        }
    }
}