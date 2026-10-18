/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.component

import android.util.Log
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.vmusic.takeserver.canvas.CanvasResult
import com.vmusic.takeserver.canvas.SpotifyCanvasProvider
// NOTE: sesuaikan paket key ini kalau di project lu beda (samain sama import di Thumbnail.kt)
import com.vmusic.takeserver.constants.SpotifyCanvasEnabledKey
import com.vmusic.takeserver.constants.SpotifySpDcTokenKey
import com.vmusic.takeserver.utils.rememberPreference

/**
 * Ambil URL canvas (mp4) buat lagu yang lagi diputar.
 * Return null kalau Spotify Canvas mati di Experimental, token kosong, atau canvas gak ada.
 */
@Composable
fun rememberCanvasUrl(
    mediaId: String?,
    title: String?,
    artist: String?,
): String? {
    val (enabled) = rememberPreference(SpotifyCanvasEnabledKey, false)
    val (token) = rememberPreference(SpotifySpDcTokenKey, "")

    var canvasUrl by remember(mediaId) { mutableStateOf<String?>(null) }

    LaunchedEffect(enabled, mediaId, token) {
        canvasUrl =
            if (enabled && !mediaId.isNullOrBlank() && !title.isNullOrBlank()) {
                val provider =
                    SpotifyCanvasProvider().apply {
                        isEnabled = true
                        spDcToken = token
                    }
                val result = provider.fetchCanvas(mediaId, title, artist)
                Log.d("CanvasBg", "title=$title artist=$artist result=$result")
                (result as? CanvasResult.Success)?.canvasUrl
            } else {
                null
            }
    }
    return canvasUrl
}

/**
 * Video canvas full-screen buat background player (loop, mute, crop).
 * Tanpa cover: transparan sampai frame pertama kerender, jadi background di bawahnya
 * (blur) tetap keliatan selama loading, terus video fade-in.
 */
@OptIn(UnstableApi::class)
@Composable
fun CanvasBackgroundVideo(
    canvasUrl: String,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var isReady by remember(canvasUrl) { mutableStateOf(false) }
    var videoWidth by remember(canvasUrl) { mutableStateOf(0) }
    var videoHeight by remember(canvasUrl) { mutableStateOf(0) }
    val currentOnError by rememberUpdatedState(onError)

    val exoPlayer =
        remember(context, canvasUrl) {
            ExoPlayer.Builder(context).build().apply {
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ALL
                setMediaItem(MediaItem.fromUri(canvasUrl))
                prepare()
                playWhenReady = true
            }
        }

    DisposableEffect(exoPlayer) {
        val listener =
            object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    isReady = true
                }

                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    videoWidth = videoSize.width
                    videoHeight = videoSize.height
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.d("CanvasBg", "player error: ${error.message}")
                    isReady = false
                    currentOnError(canvasUrl)
                }
            }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Fade pakai alpha, BUKAN AnimatedVisibility: TextureView harus selalu ada di composition
    // supaya ExoPlayer punya surface dan frame pertama bisa kerender.
    val videoAlpha by animateFloatAsState(
        targetValue = if (isReady) 1f else 0f,
        animationSpec = tween(600),
        label = "canvasBgAlpha",
    )

    Box(modifier = modifier.fillMaxSize().alpha(videoAlpha)) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    layoutParams =
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    exoPlayer.setVideoTextureView(this)
                }
            },
            update = { view ->
                view.setTransform(
                    calculateTextureViewTransform(
                        videoWidth = videoWidth,
                        videoHeight = videoHeight,
                        viewWidth = view.width,
                        viewHeight = view.height,
                        crop = true,
                    ),
                )
            },
            modifier = Modifier.fillMaxSize(),
        )
        // scrim biar teks/tombol tetap kebaca di atas video
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
    }
}