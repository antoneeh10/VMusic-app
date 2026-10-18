/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.component

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vmusic.takeserver.R

private const val SPOTIFY_LOGIN_URL =
    "https://accounts.spotify.com/login?continue=https%3A%2F%2Fopen.spotify.com%2F"

/**
 * Browser dalam app (WebView) buat login Spotify, polanya sama kayak LoginScreen YouTube:
 * buka halaman login, lalu baca cookie lewat CookieManager begitu halaman selesai dimuat.
 * Begitu cookie `sp_dc` ada, token dikirim ke [onSpDcCaptured] dan cookie WebView dibersihin.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SpotifyLoginDialog(
    onDismiss: () -> Unit,
    onSpDcCaptured: (String) -> Unit,
) {
    val currentOnCaptured by rememberUpdatedState(onSpDcCaptured)
    val captured = remember { booleanArrayOf(false) }

    fun checkForSpDc() {
        if (captured[0]) return
        val manager = CookieManager.getInstance()
        val token =
            listOf("https://open.spotify.com", "https://accounts.spotify.com")
                .mapNotNull { manager.getCookie(it) }
                .flatMap { it.split(";") }
                .map { it.trim() }
                .firstOrNull { it.startsWith("sp_dc=") }
                ?.removePrefix("sp_dc=")
        if (!token.isNullOrBlank()) {
            captured[0] = true
            // token udah diambil, jangan simpan sesi login di WebView
            manager.removeAllCookies(null)
            manager.flush()
            currentOnCaptured(token)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = null,
                        )
                    }
                    Text(
                        text = "Login Spotify",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                // buang penanda WebView; tombol "Lanjut dengan Google" milik Spotify
                                // bisa tetap ditolak Google, kalau begitu pakai login email/kode
                                userAgentString = userAgentString.replace("; wv", "")
                            }

                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            webViewClient =
                                object : WebViewClient() {
                                    override fun onPageFinished(view: WebView, url: String?) {
                                        checkForSpDc()
                                    }

                                    override fun doUpdateVisitedHistory(
                                        view: WebView,
                                        url: String?,
                                        isReload: Boolean,
                                    ) {
                                        checkForSpDc()
                                    }
                                }

                            loadUrl(SPOTIFY_LOGIN_URL)
                        }
                    },
                    onRelease = {
                        it.stopLoading()
                        it.destroy()
                    },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
        }
    }
}