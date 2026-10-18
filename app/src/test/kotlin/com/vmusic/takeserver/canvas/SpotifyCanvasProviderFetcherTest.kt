package com.vmusic.takeserver.canvas

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpotifyCanvasProviderFetcherTest {
    private val url = "https://canvaz.scdn.co/xxx.mp4"

    private fun provider(fetcher: CanvasFetcher, enabled: Boolean = true, token: String? = "tok") =
        SpotifyCanvasProvider(fetcher).also { it.isEnabled = enabled; it.spDcToken = token }

    @Test
    fun urlBecomesSuccessAndArgsArePassedThrough() = runBlocking {
        var seen: Triple<String?, String?, String?>? = null
        val result = provider(CanvasFetcher { t, a, s -> seen = Triple(t, a, s); url })
            .fetchCanvas("yt123", "Song", "Artist")
        assertEquals(CanvasResult.Success(canvasUrl = url, artistName = "Artist"), result)
        assertEquals(Triple<String?, String?, String?>("Song", "Artist", "tok"), seen)
    }

    @Test
    fun nullBecomesUnavailable() = runBlocking {
        val r = provider(CanvasFetcher { _, _, _ -> null }).fetchCanvas("x", "Song", "Artist")
        assertTrue(r is CanvasResult.Unavailable)
    }

    @Test
    fun disabledNeverCallsFetcher() = runBlocking {
        var called = false
        val r = provider(CanvasFetcher { _, _, _ -> called = true; url }, enabled = false).fetchCanvas("x", "S", "A")
        assertTrue(r is CanvasResult.Unavailable)
        assertFalse(called)
    }

    @Test
    fun blankTokenIsAuthRequiredAndNeverCallsFetcher() = runBlocking {
        var called = false
        val r = provider(CanvasFetcher { _, _, _ -> called = true; url }, token = " ").fetchCanvas("x", "S", "A")
        assertTrue(r is CanvasResult.AuthRequired)
        assertFalse(called)
    }
}