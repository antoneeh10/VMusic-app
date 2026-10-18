/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.settings

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vmusic.takeserver.LocalPlayerAwareWindowInsets
import com.vmusic.takeserver.R
import com.vmusic.takeserver.constants.SpotifyCanvasDiagnosticsKey
import com.vmusic.takeserver.constants.SpotifyCanvasEnabledKey
import com.vmusic.takeserver.constants.SpotifyCanvasFallbackKey
import com.vmusic.takeserver.constants.SpotifySpDcTokenKey
import com.vmusic.takeserver.ui.component.IconButton
import com.vmusic.takeserver.ui.component.InfoLabel
import com.vmusic.takeserver.ui.component.Material3SettingsGroup
import com.vmusic.takeserver.ui.component.Material3SettingsItem
import com.vmusic.takeserver.ui.utils.backToMain
import com.vmusic.takeserver.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyCanvasSettings(
    navController: NavController
) {
    val (canvasEnabled, onCanvasEnabledChange) = rememberPreference(
        key = SpotifyCanvasEnabledKey,
        defaultValue = false
    )
    val (canvasFallback, onCanvasFallbackChange) = rememberPreference(
        key = SpotifyCanvasFallbackKey,
        defaultValue = true
    )
    val (diagnostics, onDiagnosticsChange) = rememberPreference(
        key = SpotifyCanvasDiagnosticsKey,
        defaultValue = "Idle"
    )
    val (spDcToken, onSpDcTokenChange) = rememberPreference(
        key = SpotifySpDcTokenKey,
        defaultValue = ""
    )
    var spDcInput by remember(spDcToken) { mutableStateOf(spDcToken) }

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

        // Canvas settings section
        Material3SettingsGroup(
            title = stringResource(R.string.canvas_settings),
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.play),
                    title = { Text(stringResource(R.string.enable_spotify_canvas)) },
                    description = { Text(stringResource(R.string.enable_spotify_canvas_desc)) },
                    trailingContent = {
                        Switch(
                            checked = canvasEnabled,
                            onCheckedChange = { enabled ->
                                onCanvasEnabledChange(enabled)
                                onDiagnosticsChange(
                                    if (enabled) "Canvas enabled" else "Canvas disabled by user"
                                )
                            },
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (canvasEnabled) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(androidx.compose.material3.SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    onClick = {
                        val enabled = !canvasEnabled
                        onCanvasEnabledChange(enabled)
                        onDiagnosticsChange(
                            if (enabled) "Canvas enabled" else "Canvas disabled by user"
                        )
                    }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.insert_photo),
                    title = { Text(stringResource(R.string.fallback_to_album_art)) },
                    description = { Text(stringResource(R.string.fallback_to_album_art_desc)) },
                    trailingContent = {
                        Switch(
                            checked = canvasFallback,
                            enabled = canvasEnabled,
                            onCheckedChange = onCanvasFallbackChange,
                            thumbContent = {
                                Icon(
                                    painter = painterResource(
                                        id = if (canvasFallback) R.drawable.check else R.drawable.close
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(androidx.compose.material3.SwitchDefaults.IconSize)
                                )
                            }
                        )
                    },
                    enabled = canvasEnabled,
                    onClick = { if (canvasEnabled) onCanvasFallbackChange(!canvasFallback) }
                )
            )
        )

        Spacer(modifier = Modifier.height(12.dp))
        InfoLabel(text = stringResource(R.string.canvas_preview_info))
        Spacer(modifier = Modifier.height(16.dp))

        // sp_dc input (manual paste only)
        Text(
            text = "Spotify sp_dc cookie",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = spDcInput,
            onValueChange = { spDcInput = it },
            label = { Text("sp_dc") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(
                onClick = {
                    val value = spDcInput.trim()
                    onSpDcTokenChange(value)
                    onDiagnosticsChange(if (value.isEmpty()) "sp_dc cleared" else "sp_dc saved")
                }
            ) { Text("Save") }
            Spacer(modifier = Modifier.size(8.dp))
            OutlinedButton(
                onClick = {
                    spDcInput = ""
                    onSpDcTokenChange("")
                    onDiagnosticsChange("sp_dc cleared")
                }
            ) { Text("Clear") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diagnostics Section
        Material3SettingsGroup(
            title = stringResource(R.string.diagnostics),
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.info),
                    title = { Text(stringResource(R.string.diagnostics)) },
                    description = { Text(diagnostics) },
                    onClick = null
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.spotify_canvas_api)) },
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