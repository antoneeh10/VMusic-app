package com.vmusic.takeserver.canvas

import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayOutputStream

class SpotifyCanvasClientTest {

    private val rfcKey = "12345678901234567890".toByteArray(Charsets.US_ASCII)
    private val trackId = "4uLU6hMCjMI75M1A2tKUQC"
    private val trackUri = "spotify:track:$trackId"
    private val goodUrl = "https://canvaz.scdn.co/xxx.mp4"

    @Test
    fun totpMatchesRfc6238Vectors() {
        assertEquals("287082", spotifyTotp(rfcKey, 59))
        assertEquals("081804", spotifyTotp(rfcKey, 1111111109))
        assertEquals("050471", spotifyTotp(rfcKey, 1111111111))
        assertEquals("005924", spotifyTotp(rfcKey, 1234567890))
        assertEquals("279037", spotifyTotp(rfcKey, 2000000000))
    }

    @Test
    fun normalizeSpDcAcceptsRawQuotedAndFullCookieString() {
        val v = "AQD1234567890abcdefXYZ_-="
        assertEquals(v, normalizeSpDc(v))
        assertEquals(v, normalizeSpDc("sp_dc=$v"))
        assertEquals(v, normalizeSpDc("\"$v\""))
        assertEquals(v, normalizeSpDc("foo=1; sp_dc=$v; bar=2"))
    }

    @Test
    fun normalizeSpDcRejectsInvalid() {
        assertNull(normalizeSpDc(null))
        assertNull(normalizeSpDc(""))
        assertNull(normalizeSpDc("short"))
        assertNull(normalizeSpDc("abcdefghijklmnop qrstuvwxyz"))
        assertNull(normalizeSpDc("foo=1; bar=2; sp_dc="))
    }

    @Test
    fun decodeCanvasUrlReturnsUrlWhenUriMatches() {
        assertEquals(goodUrl, decodeCanvasUrl(fakeResponse(goodUrl, trackUri), trackUri))
    }

    @Test
    fun decodeCanvasUrlRejectsWrongTrackUri() {
        assertNull(decodeCanvasUrl(fakeResponse(goodUrl, "spotify:track:OTHER"), trackUri))
    }

    @Test
    fun decodeCanvasUrlRejectsWrongHostSchemeAndExtension() {
        assertNull(decodeCanvasUrl(fakeResponse("https://example.com/xxx.mp4", trackUri), trackUri))
        assertNull(decodeCanvasUrl(fakeResponse("http://canvaz.scdn.co/xxx.mp4", trackUri), trackUri))
        assertNull(decodeCanvasUrl(fakeResponse("https://canvaz.scdn.co/xxx.jpg", trackUri), trackUri))
    }

    @Test
    fun decodeCanvasUrlHandlesEmptyBytes() {
        assertNull(decodeCanvasUrl(ByteArray(0), trackUri))
    }

    @Test
    fun canvasRequestEncodesTrackUriAndRejectsBadIds() {
        val outer = CodedInputStream.newInstance(canvasRequest(trackId))
        assertEquals(10, outer.readTag())
        val inner = CodedInputStream.newInstance(outer.readByteArray())
        assertEquals(10, inner.readTag())
        assertEquals(trackUri, inner.readStringRequireUtf8())
        assertThrows(IllegalArgumentException::class.java) { canvasRequest("tooshort") }
    }

    @Test
    fun cleanCanvasTitleStripsNoiseOnly() {
        assertEquals("Song", cleanCanvasTitle("Song (Official Video)"))
        assertEquals("Song", cleanCanvasTitle("Song [feat. Someone]"))
        assertEquals("Song (Live)", cleanCanvasTitle("Song (Live)"))
    }

    private fun fakeResponse(url: String, uri: String): ByteArray {
        val inner = ByteArrayOutputStream().also { b ->
            CodedOutputStream.newInstance(b).apply { writeString(2, url); writeString(5, uri); flush() }
        }.toByteArray()
        return ByteArrayOutputStream().also { b ->
            CodedOutputStream.newInstance(b).apply { writeByteArray(1, inner); flush() }
        }.toByteArray()
    }
}