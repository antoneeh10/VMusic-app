/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.settings.integrations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.vmusic.takeserver.ui.component.ExpressiveTopAppBar as TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vmusic.takeserver.LocalPlayerAwareWindowInsets
import com.vmusic.takeserver.R
import com.vmusic.takeserver.ui.component.StickySettingsContent
import com.vmusic.takeserver.ui.component.ExpressiveSettingsGroup
import com.vmusic.takeserver.ui.component.ExpressiveSettingsItem
import com.vmusic.takeserver.ui.component.IconButton
import com.vmusic.takeserver.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationScreen(
    navController: NavController,
) {
    StickySettingsContent(
        title = stringResource(R.string.integrations),
        navController = navController,
    ) {

        ExpressiveSettingsGroup(
            title = stringResource(R.string.general),
            items =
                listOf(
                    ExpressiveSettingsItem(
                        icon = painterResource(R.drawable.music_note),
                        title = { Text(stringResource(R.string.lastfm_integration)) },
                        onClick = {
                            navController.navigate("settings/integrations/lastfm")
                        },
                    ),
                    ExpressiveSettingsItem(
                        icon = painterResource(R.drawable.album),
                        title = { Text(stringResource(R.string.spotify_canvas)) },
                        description = { Text(stringResource(R.string.spotify_canvas_desc)) },
                        onClick = {
                            navController.navigate("settings/integrations/spotify_canvas")
                        },
                    ),
                    ExpressiveSettingsItem(
                        icon = painterResource(R.drawable.discord),
                        title = { Text("Discord Rich Presence") },
                        description = {
                            Text(
                                "The current version does not support Discord Rich Presence. " +
                                    "Please wait for the developer to add it in a future update.",
                            )
                        },
                        enabled = false,
                        onClick = null,
                    ),
                ),
        )
    }

    }
