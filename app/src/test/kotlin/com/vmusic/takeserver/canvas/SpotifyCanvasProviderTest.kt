/*
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.canvas

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class SpotifyCanvasProviderTest {

    @Test
    fun `disabled provider returns unavailable`() = runBlocking {
        val provider = SpotifyCanvasProvider().apply {
            isEnabled = false
            spDcToken = "token123"
        }

        val result = provider.fetchCanvas("track123", "Title", "Artist")
        assertTrue("Disabled provider must return Unavailable", result is CanvasResult.Unavailable)
    }

    @Test
    fun `enabled provider without token returns auth required`() = runBlocking {
        val provider = SpotifyCanvasProvider().apply {
            isEnabled = true
            spDcToken = null
        }

        val result = provider.fetchCanvas("track123", "Title", "Artist")
        assertTrue("Missing token must return AuthRequired", result is CanvasResult.AuthRequired)
    }

    @Test
    fun `blank track id and blank title returns unavailable`() = runBlocking {
        val provider = SpotifyCanvasProvider().apply {
            isEnabled = true
            spDcToken = "validToken"
        }

        val result = provider.fetchCanvas("   ", null, null)
        assertTrue("Blank track ID and title must return Unavailable", result is CanvasResult.Unavailable)
    }
}
