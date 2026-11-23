/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.wrapped.story

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

private val EaseOutCubic = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)

enum class Entrance { Pop, Up }

/** Reusable staggered entrance: pop (scale + rotate with spring) or up (translate + fade). */
@Composable
fun Entrance(
    kind: Entrance,
    delayMs: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        if (kind == Entrance.Pop) {
            progress.animateTo(
                1f,
                spring(
                    dampingRatio = 0.55f,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        } else {
            progress.animateTo(1f, tween(durationMillis = 600, easing = EaseOutCubic))
        }
    }

    val value = progress.value.coerceIn(0f, 1.2f)
    androidx.compose.foundation.layout.Box(
        modifier =
            modifier.graphicsLayer(
                alpha = value.coerceIn(0f, 1f),
                scaleX = if (kind == Entrance.Pop) value else 1f,
                scaleY = if (kind == Entrance.Pop) value else 1f,
                rotationZ = if (kind == Entrance.Pop) (1f - value) * -25f else 0f,
                translationY = if (kind == Entrance.Up) (1f - value) * 24f else 0f,
            ),
        content = { content() },
    )
}
