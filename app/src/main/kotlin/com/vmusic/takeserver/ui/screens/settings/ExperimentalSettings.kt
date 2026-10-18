/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vmusic.takeserver.LocalPlayerAwareWindowInsets
import com.vmusic.takeserver.R
import com.vmusic.takeserver.constants.ExpressiveFloatingCapsuleMenuKey
import com.vmusic.takeserver.constants.SpatialModeKey
import com.vmusic.takeserver.constants.SpatialStrengthKey
import com.vmusic.takeserver.ui.component.DefaultDialog
import com.vmusic.takeserver.ui.component.IconButton
import com.vmusic.takeserver.ui.component.Material3SettingsGroup
import com.vmusic.takeserver.ui.component.Material3SettingsItem
import com.vmusic.takeserver.ui.utils.backToMain
import com.vmusic.takeserver.utils.rememberPreference
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalSettings(
    navController: NavController
) {
    val (floatingMenu, onFloatingMenuChange) = rememberPreference(
        key = ExpressiveFloatingCapsuleMenuKey,
        defaultValue = false
    )
    val (spatialMode, onSpatialModeChange) = rememberPreference(
        key = SpatialModeKey,
        defaultValue = false
    )
    val (spatialStrength, onSpatialStrengthChange) = rememberPreference(
        key = SpatialStrengthKey,
        defaultValue = 50
    )

    var pendingToggleAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    if (showConfirmationDialog) {
        DefaultDialog(
            onDismiss = {
                pendingToggleAction = null
                showConfirmationDialog = false
            },
            title = { Text(stringResource(R.string.warning)) },
            content = {
                Text(
                    text = stringResource(R.string.experimental_feature_bug_warning),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            },
            buttons = {
                TextButton(
                    onClick = {
                        pendingToggleAction = null
                        showConfirmationDialog = false
                    }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(
                    onClick = {
                        pendingToggleAction?.invoke()
                        pendingToggleAction = null
                        showConfirmationDialog = false
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }

    Column(
        Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)
            )
        )

        Material3SettingsGroup(
            title = stringResource(R.string.experimental_features),
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.discover_tune),
                    title = { Text(stringResource(R.string.expressive_floating_capsule_menu)) },
                    description = { Text(stringResource(R.string.expressive_floating_capsule_menu_desc)) },
                    trailingContent = {
                        Switch(
                            checked = floatingMenu,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    pendingToggleAction = { onFloatingMenuChange(true) }
                                    showConfirmationDialog = true
                                } else {
                                    onFloatingMenuChange(false)
                                }
                            },
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (floatingMenu) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(androidx.compose.material3.SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = {
                        if (!floatingMenu) {
                            pendingToggleAction = { onFloatingMenuChange(true) }
                            showConfirmationDialog = true
                        } else {
                            onFloatingMenuChange(false)
                        }
                    }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.equalizer),
                    title = { Text(stringResource(R.string.spatial_mode)) },
                    description = { Text(stringResource(R.string.spatial_mode_desc)) },
                    trailingContent = {
                        Switch(
                            checked = spatialMode,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    pendingToggleAction = { onSpatialModeChange(true) }
                                    showConfirmationDialog = true
                                } else {
                                    onSpatialModeChange(false)
                                }
                            },
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (spatialMode) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(androidx.compose.material3.SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = {
                        if (!spatialMode) {
                            pendingToggleAction = { onSpatialModeChange(true) }
                            showConfirmationDialog = true
                        } else {
                            onSpatialModeChange(false)
                        }
                    }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.linear_scale),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.spatial_strength))
                            Text(
                                text = "$spatialStrength%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (spatialMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    description = {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            Slider(
                                value = spatialStrength.toFloat(),
                                enabled = spatialMode,
                                onValueChange = { onSpatialStrengthChange(it.roundToInt()) },
                                valueRange = 0f..100f,
                                steps = 19
                            )
                        }
                    },
                    enabled = spatialMode,
                    onClick = null
                )
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.experimental_features)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        }
    )
}