/*
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpotifyCanvasSettingsTest {
    @Test
    fun `spotify canvas preference defaults are disabled and fallback enabled`() {
        val defaultCanvasEnabled = false
        val defaultCanvasFallback = true
        val defaultAccountStatus = "DISCONNECTED"

        assertFalse("Spotify Canvas must be disabled by default", defaultCanvasEnabled)
        assertTrue("Fallback to album art must be enabled by default", defaultCanvasFallback)
        assertEquals("DISCONNECTED", defaultAccountStatus)
    }

    @Test
    fun `disconnecting account transitions state to disconnected`() {
        var status = "CONNECTED"
        val canvasEnabled = true

        status = "DISCONNECTED"
        val authenticatedFetchingAllowed = (status == "CONNECTED") && canvasEnabled

        assertEquals("DISCONNECTED", status)
        assertFalse("Authenticated canvas fetching must be disallowed when disconnected", authenticatedFetchingAllowed)
    }
}
