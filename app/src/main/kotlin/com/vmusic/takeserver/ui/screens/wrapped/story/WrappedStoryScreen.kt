/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.wrapped.story

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vmusic.takeserver.LocalNavController
import com.vmusic.takeserver.LocalPlayerConnection
import com.vmusic.takeserver.R
import com.vmusic.takeserver.ui.screens.wrapped.WrappedAudioService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val NORMAL_DURATION = 6000L
private const val SONG_DURATION = 9000L

@Composable
fun WrappedStoryScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val manager = remember { provideWrappedStoryManager(context) }
    val data by manager.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    WrappedStoryContent(
        manager = manager,
        data = data,
        onClose = {
            navController.previousBackStackEntry?.savedStateHandle?.set("wrapped_seen", true)
            navController.popBackStack()
        },
        onShare = { colors ->
            scope.launch {
                WrappedStoryShare.share(context.applicationContext, data, colors)
            }
        },
    )
}

@Composable
private fun WrappedStoryContent(
    manager: WrappedStoryManager,
    data: WrappedStoryData,
    onClose: () -> Unit,
    onShare: (Pair<Int, Int>) -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current
    val audioService = remember { WrappedAudioService(context) }
    val isMuted by audioService.isMuted.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasSecondSong = data.topSongs.size >= 2
    val pageCount = if (hasSecondSong) 8 else 7
    var index by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var wasPlayingMain by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }

    val scheme = MaterialTheme.colorScheme
    val (background, onColor) =
        when (index) {
            0 -> scheme.primaryContainer to scheme.onPrimaryContainer
            1 -> scheme.tertiaryContainer to scheme.onTertiaryContainer
            2 -> scheme.secondaryContainer to scheme.onSecondaryContainer
            3 -> scheme.primary to scheme.onPrimary
            4 -> scheme.tertiary to scheme.onTertiary
            5 -> scheme.surfaceContainerHigh to scheme.onSurface
            6 -> scheme.tertiaryContainer to scheme.onTertiaryContainer
            else -> scheme.surfaceContainerLowest to scheme.onSurface
        }

    BackHandler(onBack = onClose)

    DisposableEffect(Unit) {
        val window = (view.context as android.app.Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        wasPlayingMain = playerConnection?.isPlaying?.value ?: false
        if (wasPlayingMain) playerConnection?.pause()

        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> audioService.pause()
                    Lifecycle.Event.ON_RESUME -> if (!paused) audioService.resume()
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            lifecycleOwner.lifecycle.removeObserver(observer)
            audioService.release()
            if (wasPlayingMain) playerConnection?.play()
        }
    }

    LaunchedEffect(Unit) {
        manager.prepare()
    }

    val isSongSlide = index == 3 || (hasSecondSong && index == 4)
    LaunchedEffect(index, data.topSongs) {
        val song = data.topSongs.getOrNull(if (index == 3) 0 else 1)
        if (isSongSlide && song != null) {
            val start = (song.durationSeconds * 1000L * 0.25f).toLong().coerceAtLeast(1000L)
            audioService.playTrack(song.id, startMs = start)
        } else {
            audioService.pause()
        }
    }

    LaunchedEffect(index, paused, data.isDataReady) {
        if (!data.isDataReady || paused || index >= pageCount - 1) return@LaunchedEffect
        progress.snapTo(0f)
        val duration = if (index == 3 || index == 4) SONG_DURATION else NORMAL_DURATION
        progress.animateTo(1f, tween(duration.toInt(), easing = FastOutSlowInEasing))
        index += 1
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(background)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { paused = !paused },
                        onTap = { offset ->
                            if (offset.x < size.width * 0.35f) {
                                if (index > 0) index -= 1
                            } else if (index < pageCount - 1) {
                                index += 1
                            }
                        },
                    )
                }.pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        if (dragAmount > 12f && change.position.y - change.previousPosition.y > 0f) {
                            if (change.position.y > size.height * 0.22f) {
                                onClose()
                            }
                        }
                    }
                },
    ) {
        AnimatedContent(
            targetState = index,
            transitionSpec = {
                fadeIn(tween(350)) togetherWith fadeOut(tween(350))
            },
            label = "wrapped_story_slide",
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (page) {
                    0 ->
                        WrappedIntroSlide(
                            year = data.year,
                            onColor = onColor,
                        )

                    1 ->
                        WrappedMinutesSlide(
                            totalMinutes = data.totalMinutes,
                            onColor = onColor,
                        )

                    2 ->
                        WrappedTopArtistSlide(
                            artist = data.topArtists.firstOrNull(),
                            onColor = onColor,
                        )

                    3 ->
                        WrappedTopSongSlide(
                            song = data.topSongs.getOrNull(0),
                            rank = 1,
                            onColor = onColor,
                            audioActive = !paused,
                            isMuted = isMuted,
                        )

                    4 ->
                        if (hasSecondSong) {
                            WrappedTopSongSlide(
                                song = data.topSongs.getOrNull(1),
                                rank = 2,
                                onColor = onColor,
                                audioActive = !paused,
                                isMuted = isMuted,
                            )
                        } else {
                            WrappedTop5SongsSlide(
                                songs = data.topSongs.take(5),
                                year = data.year,
                                onColor = onColor,
                            )
                        }

                    5 ->
                        if (hasSecondSong) {
                            WrappedTop5SongsSlide(
                                songs = data.topSongs.take(5),
                                year = data.year,
                                onColor = onColor,
                            )
                        } else {
                            WrappedHourSlide(
                                data = data,
                                onColor = onColor,
                            )
                        }

                    6 ->
                        WrappedHourSlide(
                            data = data,
                            onColor = onColor,
                        )

                    else ->
                        WrappedSummarySlide(
                            data = data,
                            onColor = onColor,
                            onShare = {
                                onShare(background.toArgb() to onColor.toArgb())
                            },
                            onReplay = { index = 0 },
                        )
                }
            }
        }

        if (data.isDataReady && !data.hasEnoughData) {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.wrapped_story_empty),
                    style = MaterialTheme.typography.titleLarge,
                    color = onColor,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Progress segments
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            repeat(pageCount) { segment ->
                val fill =
                    when {
                        segment < index -> 1f
                        segment == index -> progress.value.coerceIn(0f, 1f)
                        else -> 0f
                    }
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(5.dp)
                            .padding(end = 4.dp)
                            .clip(CircleShape)
                            .background(onColor.copy(alpha = 0.25f)),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(fill)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(onColor),
                    )
                }
            }
        }

        // Floating mute button
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                    .padding(top = 26.dp, end = 18.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = { audioService.toggleMute() },
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = onColor,
                    ),
            ) {
                Icon(
                    painter = painterResource(if (isMuted) R.drawable.volume_off else R.drawable.volume_up),
                    contentDescription = stringResource(R.string.mute),
                )
            }
        }

        if (paused) {
            Text(
                text = stringResource(R.string.wrapped_story_paused),
                style = MaterialTheme.typography.labelMedium,
                color = onColor,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
            )
        }
    }
}
