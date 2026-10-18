/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 * Adapted from open source components with GPL-3.0 compatibility.
 */

package com.vmusic.takeserver.ui.component

import android.graphics.Matrix
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest

enum class CanvasDisplayMode {
    VIDEO,
    ARTWORK,
    BLANK
}

fun shouldPlayCanvas(enabled: Boolean, url: String?, errorUrl: String?): Boolean {
    val hasError = !url.isNullOrBlank() && url == errorUrl
    return enabled && !url.isNullOrBlank() && !hasError
}

fun resolveCanvasDisplayMode(
    enabled: Boolean,
    fallbackEnabled: Boolean,
    url: String?,
    errorUrl: String?,
    hideThumbnail: Boolean
): CanvasDisplayMode {
    if (hideThumbnail) return CanvasDisplayMode.BLANK
    if (!enabled) return CanvasDisplayMode.ARTWORK // Disabling Canvas ALWAYS shows album art

    val canPlay = shouldPlayCanvas(enabled, url, errorUrl)
    if (canPlay) return CanvasDisplayMode.VIDEO

    if (fallbackEnabled || url.isNullOrBlank()) return CanvasDisplayMode.ARTWORK
    return CanvasDisplayMode.ARTWORK // Never produce blank screen
}

data class ScaleFactors(val scaleX: Float, val scaleY: Float)

fun calculateScaleFactors(
    videoWidth: Int,
    videoHeight: Int,
    viewWidth: Int,
    viewHeight: Int,
    crop: Boolean
): ScaleFactors {
    if (videoWidth <= 0 || videoHeight <= 0 || viewWidth <= 0 || viewHeight <= 0) {
        return ScaleFactors(1f, 1f)
    }
    val viewRatio = viewWidth.toFloat() / viewHeight.toFloat()
    val videoRatio = videoWidth.toFloat() / videoHeight.toFloat()

    var scaleX = 1f
    var scaleY = 1f

    if (crop) {
        if (videoRatio > viewRatio) {
            scaleX = videoRatio / viewRatio
        } else {
            scaleY = viewRatio / videoRatio
        }
    } else {
        if (videoRatio > viewRatio) {
            scaleY = viewRatio / videoRatio
        } else {
            scaleX = viewRatio / videoRatio
        }
    }
    return ScaleFactors(scaleX, scaleY)
}

fun calculateTextureViewTransform(
    videoWidth: Int,
    videoHeight: Int,
    viewWidth: Int,
    viewHeight: Int,
    crop: Boolean
): Matrix {
    val matrix = Matrix()
    val factors = calculateScaleFactors(videoWidth, videoHeight, viewWidth, viewHeight, crop)
    matrix.setScale(factors.scaleX, factors.scaleY, viewWidth / 2f, viewHeight / 2f)
    return matrix
}

/**
 * Dedicated Compose Canvas player component with URL-keyed error handling and crop matrix transform.
 */
@OptIn(UnstableApi::class)
@Composable
fun CanvasPlayerView(
    canvasUrl: String?,
    artworkUri: String?,
    cropArtwork: Boolean,
    isEnabled: Boolean = true,
    fallbackEnabled: Boolean = true,
    hideThumbnail: Boolean = false,
    modifier: Modifier = Modifier,
    onError: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var isVideoReady by remember { mutableStateOf(false) }
    var errorUrl by remember { mutableStateOf<String?>(null) }
    var lastNotifiedErrorUrl by remember { mutableStateOf<String?>(null) }
    var videoWidth by remember { mutableStateOf(0) }
    var videoHeight by remember { mutableStateOf(0) }

    val currentOnError by rememberUpdatedState(onError)

    // Reset readiness when canvasUrl changes
    LaunchedEffect(canvasUrl) {
        isVideoReady = false
    }

    val displayMode = resolveCanvasDisplayMode(
        enabled = isEnabled,
        fallbackEnabled = fallbackEnabled,
        url = canvasUrl,
        errorUrl = errorUrl,
        hideThumbnail = hideThumbnail
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Fallback / Base Album Artwork
        if (displayMode == CanvasDisplayMode.ARTWORK) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artworkUri)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = null,
                contentScale = if (cropArtwork) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Looping Canvas Video Layer
        if (displayMode == CanvasDisplayMode.VIDEO && !canvasUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artworkUri)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = null,
                contentScale = if (cropArtwork) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            val exoPlayer = remember(context, canvasUrl) {
                ExoPlayer.Builder(context).build().apply {
                    volume = 0f
                    repeatMode = Player.REPEAT_MODE_ALL
                    val mediaItem = MediaItem.fromUri(canvasUrl)
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true
                }
            }

            DisposableEffect(exoPlayer, canvasUrl) {
                val listener = object : Player.Listener {
                    override fun onRenderedFirstFrame() {
                        isVideoReady = true
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        if (errorUrl != canvasUrl) {
                            errorUrl = canvasUrl
                            isVideoReady = false
                            if (lastNotifiedErrorUrl != canvasUrl) {
                                lastNotifiedErrorUrl = canvasUrl
                                currentOnError?.invoke(canvasUrl)
                            }
                        }
                    }

                    override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                        videoWidth = videoSize.width
                        videoHeight = videoSize.height
                    }
                }
                exoPlayer.addListener(listener)

                onDispose {
                    exoPlayer.removeListener(listener)
                    exoPlayer.stop()
                    exoPlayer.release()
                }
            }

            AnimatedVisibility(
                visible = isVideoReady,
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(200)),
                modifier = Modifier.fillMaxSize()
            ) {
                AndroidView(
                    factory = { ctx ->
                        TextureView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
                                val transform = calculateTextureViewTransform(
                                    videoWidth = videoWidth,
                                    videoHeight = videoHeight,
                                    viewWidth = right - left,
                                    viewHeight = bottom - top,
                                    crop = cropArtwork
                                )
                                setTransform(transform)
                            }
                            exoPlayer.setVideoTextureView(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
