/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared expressive card styling for every settings screen: a grouped, spring-animated card
 * list with tinted icon containers. Source-compatible with the old Material3Settings* items.
 */
@Composable
fun ExpressiveSettingsGroup(
    title: String? = null,
    items: List<ExpressiveSettingsItem>,
    useLowContrast: Boolean = false,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 6.dp),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEachIndexed { index, item ->
                ExpressiveSettingsCard(item, index, items.size, useLowContrast)
            }
        }
    }
}

@Composable
private fun ExpressiveSettingsCard(
    item: ExpressiveSettingsItem,
    index: Int,
    totalItems: Int,
    useLowContrast: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topCorner by animateDpAsState(
        targetValue = if (isPressed) 12.dp else if (index == 0) 24.dp else 6.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "settings_top_corner",
    )
    val bottomCorner by animateDpAsState(
        targetValue = if (isPressed) 12.dp else if (index == totalItems - 1) 24.dp else 6.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "settings_bottom_corner",
    )
    val shape =
        RoundedCornerShape(
            topStart = topCorner,
            topEnd = topCorner,
            bottomStart = bottomCorner,
            bottomEnd = bottomCorner,
        )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = item.enabled && item.onClick != null,
                    onClick = { item.onClick?.invoke() },
                ),
        shape = shape,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (useLowContrast) {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                item.leadingContent != null -> {
                    item.leadingContent.invoke()
                    Spacer(modifier = Modifier.width(16.dp))
                }

                item.icon != null -> {
                    val colorSet =
                        when (index % 3) {
                            0 ->
                                MaterialTheme.colorScheme.primaryContainer to
                                    MaterialTheme.colorScheme.primary
                            1 ->
                                MaterialTheme.colorScheme.secondaryContainer to
                                    MaterialTheme.colorScheme.secondary
                            else ->
                                MaterialTheme.colorScheme.tertiaryContainer to
                                    MaterialTheme.colorScheme.tertiary
                        }
                    val iconShape =
                        when (index % 4) {
                            0 -> CircleShape
                            1 -> RoundedCornerShape(12.dp)
                            2 ->
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    bottomEnd = 16.dp,
                                    topEnd = 4.dp,
                                    bottomStart = 4.dp,
                                )
                            else -> CircleShape
                        }
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(iconShape)
                                .background(colorSet.first),
                        contentAlignment = Alignment.Center,
                    ) {
                        val icon: @Composable () -> Unit = {
                            Icon(
                                painter = item.icon,
                                contentDescription = null,
                                tint = colorSet.second,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        if (item.showBadge) {
                            BadgedBox(badge = { Badge(containerColor = MaterialTheme.colorScheme.error) }) {
                                icon()
                            }
                        } else {
                            icon()
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                ProvideTextStyle(
                    MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color =
                            if (item.enabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            },
                    ),
                ) {
                    item.title()
                }
                if (item.description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    ProvideTextStyle(
                        MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color =
                                if (item.enabled) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                },
                        ),
                    ) {
                        item.description.invoke()
                    }
                }
            }

            if (item.trailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                item.trailingContent.invoke()
            } else if (item.onClick != null) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(com.vmusic.takeserver.R.drawable.navigate_next),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

/** Same field names as the legacy Material3SettingsItem so call sites only need a rename. */
data class ExpressiveSettingsItem(
    val icon: Painter? = null,
    val leadingContent: (@Composable () -> Unit)? = null,
    val title: @Composable () -> Unit,
    val description: (@Composable () -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null,
    val showBadge: Boolean = false,
    val isHighlighted: Boolean = false,
    val enabled: Boolean = true,
    val onClick: (() -> Unit)? = null,
)
