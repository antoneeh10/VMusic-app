/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.wrapped.story

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.vmusic.takeserver.R
import kotlinx.coroutines.delay
import java.util.Locale

private val EaseOutCubic = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)
private val LocaleId = Locale("id", "ID")

@Composable
private fun SectionLabel(
    text: String,
    onColor: Color,
) {
    Text(
        text = text.uppercase(LocaleId),
        style =
            MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            ),
        color = onColor.copy(alpha = 0.78f),
    )
}

@Composable
private fun WrappedWordmark(
    text: String,
    onColor: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.small_icon),
            contentDescription = null,
            tint = onColor,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        SectionLabel(text = text, onColor = onColor)
    }
}

@Composable
private fun CoverArt(
    url: String?,
    modifier: Modifier,
    corner: androidx.compose.ui.graphics.Shape = RoundedCornerShape(28.dp),
) {
    if (url != null) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(url)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(corner),
        )
    } else {
        Box(
            modifier = modifier.clip(corner).background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.album),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
private fun VinylAccent(
    color: Color,
    onColor: Color,
) {
    Box(
        modifier =
            Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(onColor.copy(alpha = 0.12f))
                .padding(22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.7f)),
        )
    }
}

@Composable
private fun Equalizer(
    active: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "wrapped_equalizer")
    Row(
        modifier = modifier.height(44.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(10) { index ->
            val animated by
                infinite.animateFloat(
                    initialValue = 0.16f,
                    targetValue = 1f,
                    animationSpec =
                        infiniteRepeatable(
                            animation = tween(durationMillis = 700 + index * 35),
                            repeatMode = RepeatMode.Reverse,
                        ),
                    label = "eq_$index",
                )
            val level = if (active) animated else 0.16f
            Box(
                modifier =
                    Modifier
                        .width(7.dp)
                        .fillMaxHeight()
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 1f)
                            scaleY = level
                        }.clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = if (active) 1f else 0.4f)),
            )
        }
    }
}

@Composable
fun WrappedIntroSlide(
    year: Int,
    onColor: Color,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .size(300.dp)
                    .align(Alignment.TopEnd)
                    .graphicsLayer { translationX = 90f; translationY = -40f }
                    .clip(RoundedCornerShape(50)),
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)))
        }
        Box(
            modifier =
                Modifier
                    .size(140.dp)
                    .align(Alignment.BottomStart)
                    .graphicsLayer { rotationZ = 18f; translationX = -40f }
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 26.dp)
                    .padding(top = 48.dp, bottom = 36.dp),
        ) {
            Entrance(Entrance.Up) { WrappedWordmark(stringResource(R.string.wrapped_story_label), onColor) }
            Spacer(modifier = Modifier.weight(1f))
            Entrance(
                kind = Entrance.Up,
                delayMs = 120,
            ) {
                Text(
                    text = stringResource(R.string.wrapped_story_title, year),
                    style =
                        MaterialTheme.typography.displayLarge.copy(
                            fontSize = 76.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 76.sp,
                            letterSpacing = (-2.5).sp,
                        ),
                    color = onColor,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Entrance(
                kind = Entrance.Up,
                delayMs = 240,
            ) {
                Text(
                    text = stringResource(R.string.wrapped_story_intro_sub, year),
                    style = MaterialTheme.typography.bodyLarge,
                    color = onColor.copy(alpha = 0.85f),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun WrappedMinutesSlide(
    totalMinutes: Long,
    onColor: Color,
) {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(totalMinutes) {
        animated.animateTo(totalMinutes.toFloat(), tween(durationMillis = 1400, easing = EaseOutCubic))
    }
    val display = String.format(LocaleId, "%,d", animated.value.toLong())

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .padding(top = 48.dp, bottom = 36.dp),
    ) {
        Entrance(Entrance.Up) { SectionLabel(stringResource(R.string.wrapped_story_minutes_label), onColor) }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = display,
            style =
                MaterialTheme.typography.displayLarge.copy(
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-2.5).sp,
                ),
            color = onColor,
        )
        Entrance(
            kind = Entrance.Up,
            delayMs = 240,
        ) {
            Text(
                text = stringResource(R.string.wrapped_story_minutes_unit),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = onColor,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Entrance(
            kind = Entrance.Up,
            delayMs = 360,
        ) {
            Text(
                text = stringResource(R.string.wrapped_story_minutes_sub, totalMinutes / 1440f),
                style = MaterialTheme.typography.bodyLarge,
                color = onColor.copy(alpha = 0.82f),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun WrappedTopArtistSlide(
    artist: StoryArtist?,
    onColor: Color,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .padding(top = 48.dp, bottom = 36.dp),
    ) {
        Entrance(Entrance.Up) { SectionLabel(stringResource(R.string.wrapped_story_artist_label), onColor) }
        Spacer(modifier = Modifier.height(24.dp))
        Entrance(
            kind = Entrance.Pop,
            delayMs = 120,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(46)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(46))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                )
                if (artist?.thumbnailUrl != null) {
                    CoverArt(
                        url = artist.thumbnailUrl,
                        modifier = Modifier.fillMaxSize(),
                        corner = RoundedCornerShape(46),
                    )
                } else {
                    Text(
                        text = artist?.name?.take(1)?.uppercase(LocaleId) ?: "?",
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = onColor,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Entrance(
            kind = Entrance.Up,
            delayMs = 240,
        ) {
            Text(
                text = artist?.name ?: "—",
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-2).sp,
                    ),
                color = onColor,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Entrance(
            kind = Entrance.Up,
            delayMs = 360,
        ) {
            Text(
                text = stringResource(R.string.wrapped_story_artist_sub, artist?.playCount ?: 0),
                style = MaterialTheme.typography.bodyLarge,
                color = onColor.copy(alpha = 0.82f),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun WrappedTopSongSlide(
    song: StorySong?,
    rank: Int,
    onColor: Color,
    audioActive: Boolean,
    isMuted: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .padding(top = 48.dp, bottom = 36.dp),
    ) {
        Entrance(
            Entrance.Up,
        ) {
            SectionLabel(
                stringResource(if (rank == 1) R.string.wrapped_story_song_1_label else R.string.wrapped_story_song_2_label),
                onColor,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Entrance(
            kind = Entrance.Pop,
            delayMs = 120,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(205.dp)
                        .clip(RoundedCornerShape(28.dp)),
            ) {
                CoverArt(url = song?.thumbnailUrl, modifier = Modifier.fillMaxSize())
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .graphicsLayer { translationX = 35f; translationY = 35f },
                ) {
                    VinylAccent(MaterialTheme.colorScheme.primary, onColor)
                }
                Text(
                    text = (song?.year ?: "").uppercase(LocaleId),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Entrance(
            kind = Entrance.Up,
            delayMs = 240,
        ) {
            Text(
                text = song?.title ?: "—",
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontSize = if (rank == 1) 46.sp else 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1.5).sp,
                        lineHeight = 46.sp,
                    ),
                color = onColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Entrance(
            kind = Entrance.Up,
            delayMs = 240,
        ) {
            Text(
                text = stringResource(R.string.wrapped_story_song_sub, song?.artist ?: "", song?.playCount ?: 0),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = onColor.copy(alpha = 0.82f),
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Entrance(
            kind = Entrance.Up,
            delayMs = 360,
        ) {
            Equalizer(active = audioActive && !isMuted, color = onColor)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text =
                    stringResource(
                        if (isMuted) R.string.wrapped_story_song_muted else R.string.wrapped_story_song_playing,
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = onColor.copy(alpha = 0.7f),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun WrappedTop5SongsSlide(
    songs: List<StorySong>,
    year: Int,
    onColor: Color,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .padding(top = 48.dp, bottom = 36.dp),
    ) {
        Entrance(Entrance.Up) { SectionLabel(stringResource(R.string.wrapped_story_top5_label, year), onColor) }
        Spacer(modifier = Modifier.height(16.dp))
        songs.forEachIndexed { index, song ->
            Entrance(
                kind = Entrance.Up,
                delayMs = 120 + (index / 2) * 120,
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(onColor.copy(alpha = 0.08f))
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(onColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.surface,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = onColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = onColor.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = "${song.playCount}x",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = onColor.copy(alpha = 0.8f),
                    )
                }
            }
            if (index < songs.lastIndex) Spacer(modifier = Modifier.height(6.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Entrance(
            kind = Entrance.Up,
            delayMs = 360,
        ) {
            Text(
                text = stringResource(R.string.wrapped_story_top5_footer),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = onColor,
            )
        }
    }
}

@Composable
fun WrappedHourSlide(
    data: WrappedStoryData,
    onColor: Color,
) {
    val persona =
        when (data.peakHour) {
            in 22..23, in 0..3 -> stringResource(R.string.wrapped_story_habit_night)
            in 4..10 -> stringResource(R.string.wrapped_story_habit_morning)
            in 11..17 -> stringResource(R.string.wrapped_story_habit_day)
            else -> stringResource(R.string.wrapped_story_habit_evening)
        }
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        started = true
    }
    val max = (data.hourlyCounts.maxOrNull() ?: 0).coerceAtLeast(1)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .padding(top = 48.dp, bottom = 36.dp),
    ) {
        Entrance(Entrance.Up) { SectionLabel(stringResource(R.string.wrapped_story_habit_label), onColor) }
        Entrance(
            kind = Entrance.Up,
            delayMs = 120,
        ) {
            Text(
                text = persona,
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1.5).sp,
                        lineHeight = 52.sp,
                    ),
                color = onColor,
            )
        }
        Entrance(
            kind = Entrance.Up,
            delayMs = 240,
        ) {
            Text(
                text = stringResource(R.string.wrapped_story_habit_sub, data.peakHour),
                style = MaterialTheme.typography.bodyLarge,
                color = onColor.copy(alpha = 0.82f),
            )
        }
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().height(145.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            data.hourlyCounts.forEachIndexed { hour, count ->
                val animated by
                    animateFloatAsStateCompat(
                        targetValue = if (started) 1f else 0f,
                        delayMs = hour * 30,
                    )
                val target = (count.toFloat() / max).coerceAtLeast(0.04f)
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(0.5f, 1f)
                                scaleY = target * animated
                            }.clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(
                                if (hour == data.peakHour) {
                                    onColor
                                } else {
                                    onColor.copy(alpha = 0.25f)
                                },
                            ),
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("00", "06", "12", "18", "23").forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = onColor.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun animateFloatAsStateCompat(
    targetValue: Float,
    delayMs: Int,
): androidx.compose.runtime.State<Float> {
    val value = remember { Animatable(0f) }
    LaunchedEffect(targetValue) {
        delay(delayMs.toLong())
        value.animateTo(targetValue, tween(durationMillis = 800, easing = EaseOutCubic))
    }
    return value.asState()
}

private fun Animatable<Float, androidx.compose.animation.core.AnimationVector1D>.asState(): androidx.compose.runtime.State<Float> =
    androidx.compose.runtime.derivedStateOf { value }

@Composable
fun WrappedSummarySlide(
    data: WrappedStoryData,
    onColor: Color,
    onShare: () -> Unit,
    onReplay: () -> Unit,
) {
    val topTwo = data.topSongs.take(2)
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .padding(top = 48.dp, bottom = 36.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WrappedWordmark(stringResource(R.string.wrapped_story_summary_header, data.year), onColor)
            Text(
                text = stringResource(R.string.wrapped_story_handle),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = onColor.copy(alpha = 0.6f),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Entrance(
            kind = Entrance.Up,
            delayMs = 120,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = onColor,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(86.dp)) {
                            CoverArt(
                                url = topTwo.getOrNull(0)?.thumbnailUrl,
                                modifier = Modifier.size(68.dp),
                                corner = RoundedCornerShape(16.dp),
                            )
                            Box(
                                modifier =
                                    Modifier
                                        .size(52.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surface),
                            ) {
                                CoverArt(
                                    url = topTwo.getOrNull(1)?.thumbnailUrl,
                                    modifier = Modifier.size(46.dp).align(Alignment.Center),
                                    corner = RoundedCornerShape(12.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.wrapped_story_summary_title, data.year),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = onColor,
                            )
                            Text(
                                text =
                                    stringResource(
                                        R.string.wrapped_story_summary_songs,
                                        topTwo.getOrNull(0)?.title ?: "—",
                                        topTwo.getOrNull(1)?.title ?: "—",
                                    ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = onColor.copy(alpha = 0.85f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            SummaryListTitle(stringResource(R.string.wrapped_story_top_artists), onColor)
                            data.topArtists.take(5).forEachIndexed { index, artist ->
                                SummaryLine(
                                    stringResource(R.string.wrapped_story_rank, index + 1, artist.name),
                                    onColor,
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            SummaryListTitle(stringResource(R.string.wrapped_story_top_songs), onColor)
                            data.topSongs.take(5).forEachIndexed { index, song ->
                                SummaryLine(
                                    stringResource(R.string.wrapped_story_rank, index + 1, song.title),
                                    onColor,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Entrance(
            kind = Entrance.Up,
            delayMs = 240,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatCell(
                    modifier = Modifier.weight(1f),
                    value = String.format(LocaleId, "%,d", data.totalMinutes),
                    label = stringResource(R.string.wrapped_story_minutes_stat),
                    onColor = onColor,
                )
                StatCell(
                    modifier = Modifier.weight(1f),
                    value = data.genre.ifBlank { stringResource(R.string.wrapped_story_genre_fallback) },
                    label = stringResource(R.string.wrapped_story_genre_stat),
                    onColor = onColor,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Entrance(
            kind = Entrance.Up,
            delayMs = 360,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = CircleShape,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.share),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.wrapped_story_share), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onReplay,
                    modifier = Modifier.height(50.dp),
                    shape = CircleShape,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.replay),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryListTitle(
    text: String,
    onColor: Color,
) {
    Text(
        text = text.uppercase(LocaleId),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = onColor.copy(alpha = 0.7f),
    )
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun SummaryLine(
    text: String,
    onColor: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = onColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun StatCell(
    modifier: Modifier,
    value: String,
    label: String,
    onColor: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = onColor,
            ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = onColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = onColor.copy(alpha = 0.75f),
            )
        }
    }
}
