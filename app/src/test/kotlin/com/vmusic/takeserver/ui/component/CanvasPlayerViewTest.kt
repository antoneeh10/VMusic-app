/*
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanvasPlayerViewTest {

    @Test
    fun `shouldPlayCanvas returns correct evaluation`() {
        val url = "https://canvaz.scdn.co/canvas.mp4"
        assertTrue(shouldPlayCanvas(enabled = true, url = url, errorUrl = null))
        assertFalse(shouldPlayCanvas(enabled = false, url = url, errorUrl = null))
        assertFalse(shouldPlayCanvas(enabled = true, url = null, errorUrl = null))
        assertFalse(shouldPlayCanvas(enabled = true, url = "", errorUrl = null))
        assertFalse(shouldPlayCanvas(enabled = true, url = url, errorUrl = url))
    }

    @Test
    fun `resolveCanvasDisplayMode handles all preference and error combinations`() {
        val url = "https://canvaz.scdn.co/canvas.mp4"

        // 1. Enabled, no error -> VIDEO
        assertEquals(
            CanvasDisplayMode.VIDEO,
            resolveCanvasDisplayMode(enabled = true, fallbackEnabled = true, url = url, errorUrl = null, hideThumbnail = false)
        )

        // 2. Disabled -> ARTWORK (Disabling Canvas ALWAYS shows album art, never blank)
        assertEquals(
            CanvasDisplayMode.ARTWORK,
            resolveCanvasDisplayMode(enabled = false, fallbackEnabled = true, url = url, errorUrl = null, hideThumbnail = false)
        )

        // 3. Disabled, fallback disabled -> ARTWORK (Never produce blank screen)
        assertEquals(
            CanvasDisplayMode.ARTWORK,
            resolveCanvasDisplayMode(enabled = false, fallbackEnabled = false, url = url, errorUrl = null, hideThumbnail = false)
        )

        // 4. Has error (errorUrl == url) -> ARTWORK (fallback enabled)
        assertEquals(
            CanvasDisplayMode.ARTWORK,
            resolveCanvasDisplayMode(enabled = true, fallbackEnabled = true, url = url, errorUrl = url, hideThumbnail = false)
        )

        // 5. Hide thumbnail requested -> BLANK
        assertEquals(
            CanvasDisplayMode.BLANK,
            resolveCanvasDisplayMode(enabled = true, fallbackEnabled = true, url = url, errorUrl = null, hideThumbnail = true)
        )
    }

    @Test
    fun `calculateScaleFactors handles zero or negative dimensions safely`() {
        val factors1 = calculateScaleFactors(1920, 1080, 1000, 1000, true)
        assertTrue(factors1.scaleX > 0f && factors1.scaleY > 0f)

        val factors2 = calculateScaleFactors(0, 1080, 1000, 1000, true)
        assertEquals(1f, factors2.scaleX)
        assertEquals(1f, factors2.scaleY)

        val factors3 = calculateScaleFactors(1920, -100, 1000, 1000, false)
        assertEquals(1f, factors3.scaleX)
        assertEquals(1f, factors3.scaleY)
    }
}
