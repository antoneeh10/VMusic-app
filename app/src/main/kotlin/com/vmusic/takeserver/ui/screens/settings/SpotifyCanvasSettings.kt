/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.settings

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.vmusic.takeserver.ui.component.ExpressiveTopAppBar as TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vmusic.takeserver.LocalPlayerAwareWindowInsets
import com.vmusic.takeserver.R
import com.vmusic.takeserver.constants.PlayerBackgroundStyle
import com.vmusic.takeserver.constants.PlayerBackgroundStyleKey
import com.vmusic.takeserver.constants.SpotifyCanvasDiagnosticsKey
import com.vmusic.takeserver.constants.SpotifyCanvasEnabledKey
import com.vmusic.takeserver.constants.SpotifyCanvasFallbackKey
import com.vmusic.takeserver.constants.SpotifySpDcTokenKey
import com.vmusic.takeserver.ui.component.DefaultDialog
import com.vmusic.takeserver.ui.component.StickySettingsContent
import com.vmusic.takeserver.ui.component.ExpressiveSettingsGroup
import com.vmusic.takeserver.ui.component.ExpressiveSettingsItem
import com.vmusic.takeserver.ui.component.IconButton
import com.vmusic.takeserver.ui.component.InfoLabel
import com.vmusic.takeserver.ui.utils.backToMain
import com.vmusic.takeserver.utils.rememberEnumPreference
import com.vmusic.takeserver.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyCanvasSettings(
    navController: NavController,
) {
    val context = LocalContext.current
    val (canvasEnabled, onCanvasEnabledChange) =
        rememberPreference(
            key = SpotifyCanvasEnabledKey,
            defaultValue = false,
        )
    val (canvasFallback, onCanvasFallbackChange) =
        rememberPreference(
            key = SpotifyCanvasFallbackKey,
            defaultValue = true,
        )
    val (diagnostics, onDiagnosticsChange) =
        rememberPreference(
            key = SpotifyCanvasDiagnosticsKey,
            defaultValue = "Idle",
        )
    val (spDcToken, onSpDcTokenChange) =
        rememberPreference(
            key = SpotifySpDcTokenKey,
            defaultValue = "",
        )
    val (playerBackground, onPlayerBackgroundChange) =
        rememberEnumPreference<PlayerBackgroundStyle>(
            PlayerBackgroundStyleKey,
            defaultValue = PlayerBackgroundStyle.BLUR,
        )
    var previousBackground by remember {
        mutableStateOf(
            if (playerBackground == PlayerBackgroundStyle.CANVAS) {
                PlayerBackgroundStyle.BLUR
            } else {
                playerBackground
            },
        )
    }
    var showTokenDialog by remember { mutableStateOf(false) }

    fun setCanvasEnabled(enabled: Boolean) {
        onCanvasEnabledChange(enabled)
        if (enabled) {
            if (playerBackground != PlayerBackgroundStyle.CANVAS) {
                previousBackground = playerBackground
            }
            onPlayerBackgroundChange(PlayerBackgroundStyle.CANVAS)
        } else {
            onPlayerBackgroundChange(previousBackground)
        }
        onDiagnosticsChange(
            if (enabled) "Canvas enabled as player background" else "Canvas disabled by user",
        )
    }

    StickySettingsContent(
        title = stringResource(R.string.spotify_canvas_api),
        navController = navController,
    ) {

        ExpressiveSettingsGroup(
            title = stringResource(R.string.canvas_settings),
            items =
                listOf(
                    ExpressiveSettingsItem(
                        icon = painterResource(R.drawable.play),
                        title = { Text(stringResource(R.string.enable_spotify_canvas)) },
                        description = { Text(stringResource(R.string.enable_spotify_canvas_desc)) },
                        trailingContent = {
                            Switch(
                                checked = canvasEnabled,
                                onCheckedChange = ::setCanvasEnabled,
                                thumbContent = {
                                    Icon(
                                        painter =
                                            painterResource(
                                                if (canvasEnabled) R.drawable.check else R.drawable.close,
                                            ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                },
                            )
                        },
                        onClick = { setCanvasEnabled(!canvasEnabled) },
                    ),
                    ExpressiveSettingsItem(
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
                                        painter =
                                            painterResource(
                                                if (canvasFallback) R.drawable.check else R.drawable.close,
                                            ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                },
                            )
                        },
                        enabled = canvasEnabled,
                        onClick = { if (canvasEnabled) onCanvasFallbackChange(!canvasFallback) },
                    ),
                    ExpressiveSettingsItem(
                        icon = painterResource(R.drawable.token),
                        title = { Text(stringResource(R.string.spotify_canvas_token)) },
                        description = {
                            Text(
                                if (spDcToken.isBlank()) {
                                    stringResource(R.string.spotify_canvas_token_unset)
                                } else {
                                    stringResource(R.string.spotify_canvas_token_set)
                                },
                            )
                        },
                        onClick = { showTokenDialog = true },
                    ),
                ),
        )

        Spacer(modifier = Modifier.height(12.dp))
        InfoLabel(text = stringResource(R.string.canvas_preview_info))
        Spacer(modifier = Modifier.height(16.dp))

        ExpressiveSettingsGroup(
            title = stringResource(R.string.diagnostics),
            items =
                listOf(
                    ExpressiveSettingsItem(
                        icon = painterResource(R.drawable.info),
                        title = { Text(stringResource(R.string.diagnostics)) },
                        description = { Text(diagnostics) },
                        onClick = null,
                    ),
                ),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

        if (showTokenDialog) {
        var tempToken by remember { mutableStateOf(spDcToken) }

        DefaultDialog(
            onDismiss = { showTokenDialog = false },
            title = { Text(stringResource(R.string.spotify_canvas_token_title)) },
            buttons = {
                TextButton(onClick = { showTokenDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(
                    onClick = {
                        onSpDcTokenChange(tempToken.trim())
                        onDiagnosticsChange(if (tempToken.isBlank()) "sp_dc cleared" else "sp_dc saved")
                        showTokenDialog = false
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 18.dp),
            ) {
                OutlinedTextField(
                    value = tempToken,
                    onValueChange = { tempToken = it },
                    label = { Text(stringResource(R.string.spotify_canvas_token_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = {
                        showTokenDialog = false
                        navController.navigate("settings/appearance/spotify_login")
                    },
                ) {
                    Text(stringResource(R.string.spotify_login_in_app))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val pasted =
                                clipboard.primaryClip
                                    ?.getItemAt(0)
                                    ?.text
                                    ?.toString()
                                    .orEmpty()
                                    .trim()
                            tempToken = pasted.removePrefix("sp_dc=").substringBefore(";")
                        },
                    ) {
                        Text(stringResource(R.string.spotify_paste))
                    }
                    TextButton(
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://accounts.spotify.com/login?continue=https%3A%2F%2Fopen.spotify.com%2F"),
                                ),
                            )
                        },
                    ) {
                        Text(stringResource(R.string.spotify_open))
                    }
                }
                Text(
                    text = stringResource(R.string.spotify_token_help),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
