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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import com.vmusic.takeserver.ui.component.ExpressiveTopAppBar as TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vmusic.takeserver.BuildConfig
import com.vmusic.takeserver.LocalPlayerAwareWindowInsets
import com.vmusic.takeserver.R
import com.vmusic.takeserver.constants.CheckForUpdatesKey
import com.vmusic.takeserver.constants.UpdateNotificationsEnabledKey
import com.vmusic.takeserver.ui.component.StickySettingsContent
import com.vmusic.takeserver.ui.component.ExpressiveSettingsGroup
import com.vmusic.takeserver.ui.component.ExpressiveSettingsItem
import com.vmusic.takeserver.ui.component.IconButton
import com.vmusic.takeserver.ui.utils.backToMain
import com.vmusic.takeserver.utils.Updater
import com.vmusic.takeserver.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val HeroBlue = Color(0xFF3F51B5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdaterScreen(
    navController: NavController,
) {
    val (checkForUpdates, onCheckForUpdatesChange) = rememberPreference(CheckForUpdatesKey, true)
    val (updateNotifications, onUpdateNotificationsChange) = rememberPreference(UpdateNotificationsEnabledKey, true)

    var isChecking by remember { mutableStateOf(false) }
    var updateAvailable by remember { mutableStateOf(false) }
    var latestVersion by remember { mutableStateOf<String?>(null) }
    var showChangelog by remember { mutableStateOf(false) }
    var changelogContent by remember { mutableStateOf<String?>(null) }
    var checkError by remember { mutableStateOf<String?>(null) }
    val failedToCheckUpdatesTemplate = stringResource(R.string.failed_to_check_updates)

    val coroutineScope = rememberCoroutineScope()

    fun performManualCheck() {
        coroutineScope.launch {
            isChecking = true
            checkError = null
            withContext(Dispatchers.IO) {
                Updater
                    .checkForUpdate(forceRefresh = true)
                    .onSuccess { (releaseInfo, hasUpdate) ->
                        if (releaseInfo != null) {
                            latestVersion = releaseInfo.versionName
                            updateAvailable = hasUpdate
                            changelogContent = releaseInfo.description
                        }
                    }.onFailure {
                        checkError = String.format(failedToCheckUpdatesTemplate, it.message ?: "Unknown error")
                    }
            }
            isChecking = false
        }
    }

    StickySettingsContent(
        title = stringResource(R.string.updater),
        navController = navController,
    ) {

        Spacer(Modifier.height(40.dp))

        // Current version hero card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = HeroBlue,
                    contentColor = Color.White,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.current_version),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = BuildConfig.VERSION_NAME,
                    style =
                        MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = Color.White,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VersionChip(text = BuildConfig.ARCHITECTURE.lowercase())
                    VersionChip(text = if (BuildConfig.CAST_AVAILABLE) "GMS" else "FOSS")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text =
                                stringResource(
                                    if (updateAvailable) R.string.update_available_title else R.string.up_to_date,
                                ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        ExpressiveSettingsGroup(
            title = stringResource(R.string.update_settings_section),
            items =
                buildList {
                    add(
                        ExpressiveSettingsItem(
                            icon = painterResource(R.drawable.update),
                            title = { Text(stringResource(R.string.check_for_updates)) },
                            description = { Text(stringResource(R.string.check_for_updates_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = checkForUpdates,
                                    onCheckedChange = onCheckForUpdatesChange,
                                )
                            },
                            onClick = { onCheckForUpdatesChange(!checkForUpdates) },
                        ),
                    )

                    if (checkForUpdates) {
                        add(
                            ExpressiveSettingsItem(
                                icon = painterResource(R.drawable.notification),
                                title = { Text(stringResource(R.string.update_notifications)) },
                                description = { Text(stringResource(R.string.update_notifications_desc)) },
                                trailingContent = {
                                    Switch(
                                        checked = updateNotifications,
                                        onCheckedChange = onUpdateNotificationsChange,
                                    )
                                },
                                onClick = { onUpdateNotificationsChange(!updateNotifications) },
                            ),
                        )
                    }
                },
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { if (!isChecking) performManualCheck() },
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text =
                    if (isChecking) {
                        stringResource(R.string.checking_for_updates)
                    } else {
                        stringResource(R.string.check_again)
                    },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        }

        checkError?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (updateAvailable && latestVersion != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { showChangelog = !showChangelog },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
            ) {
                Text(if (showChangelog) stringResource(R.string.hide_changelog) else stringResource(R.string.view_changelog))
            }

            if (showChangelog && changelogContent != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = changelogContent!!,
                    style = MaterialTheme.typography.bodySmall,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    }

@Composable
private fun VersionChip(text: String) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.15f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}
