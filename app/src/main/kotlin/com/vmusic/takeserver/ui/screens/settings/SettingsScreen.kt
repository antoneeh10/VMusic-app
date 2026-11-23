/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.metrolist.innertube.utils.parseCookieString
import com.vmusic.takeserver.BuildConfig
import com.vmusic.takeserver.LocalPlayerAwareWindowInsets
import com.vmusic.takeserver.R
import com.vmusic.takeserver.constants.InnerTubeCookieKey
import com.vmusic.takeserver.ui.component.ReleaseNotesCard
import com.vmusic.takeserver.ui.utils.backToMain
import com.vmusic.takeserver.utils.Updater
import com.vmusic.takeserver.utils.rememberPreference
import com.vmusic.takeserver.viewmodels.HomeViewModel

data class ExpressiveSettingItem(
    val id: String,
    val titleResId: Int,
    val descriptionResId: Int,
    val iconResId: Int,
    val route: String? = null,
    val onClick: (() -> Unit)? = null,
)

data class ExpressiveSettingSection(
    val sectionTitleResId: Int,
    val items: List<ExpressiveSettingItem>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    latestVersionName: String,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val isAndroid12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val hasAndroidAuto = remember {
        try {
            context.packageManager.getPackageInfo("com.google.android.projection.gearhead", 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    val showChangelog = com.vmusic.takeserver.LocalChangelogState.current

    // Account & Login State
    val (innerTubeCookie) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val homeViewModel: HomeViewModel = hiltViewModel()
    val accountName by homeViewModel.accountName.collectAsStateWithLifecycle()
    val accountImageUrl by homeViewModel.accountImageUrl.collectAsStateWithLifecycle()

    // Build Menu Structure with String Resources
    val allSections = remember(hasAndroidAuto, isAndroid12OrLater, latestVersionName) {
        buildList {
            add(
                ExpressiveSettingSection(
                    sectionTitleResId = R.string.settings_section_interface,
                    items = listOf(
                        ExpressiveSettingItem(
                            id = "appearance",
                            titleResId = R.string.settings_appearance_title,
                            descriptionResId = R.string.settings_appearance_desc,
                            iconResId = R.drawable.palette,
                            route = "settings/appearance",
                        ),
                        ExpressiveSettingItem(
                            id = "integrations",
                            titleResId = R.string.settings_integrations_title,
                            descriptionResId = R.string.settings_integrations_desc,
                            iconResId = R.drawable.integration,
                            route = "settings/integrations",
                        ),
                    ),
                ),
            )

            add(
                ExpressiveSettingSection(
                    sectionTitleResId = R.string.settings_section_player_and_content,
                    items = listOf(
                        ExpressiveSettingItem(
                            id = "player",
                            titleResId = R.string.settings_player_title,
                            descriptionResId = R.string.settings_player_desc,
                            iconResId = R.drawable.play,
                            route = "settings/player",
                        ),
                        ExpressiveSettingItem(
                            id = "content",
                            titleResId = R.string.settings_content_title,
                            descriptionResId = R.string.settings_content_desc,
                            iconResId = R.drawable.language,
                            route = "settings/content",
                        ),
                        ExpressiveSettingItem(
                            id = "ai",
                            titleResId = R.string.settings_ai_lyrics_title,
                            descriptionResId = R.string.settings_ai_lyrics_desc,
                            iconResId = R.drawable.translate,
                            route = "settings/ai",
                        ),
                        ExpressiveSettingItem(
                            id = "wrapped",
                            titleResId = R.string.wrapped_story_settings_title,
                            descriptionResId = R.string.wrapped_story_settings_desc,
                            iconResId = R.drawable.crown,
                            route = "wrapped_story",
                        ),
                    ),
                ),
            )

            if (hasAndroidAuto) {
                add(
                    ExpressiveSettingSection(
                        sectionTitleResId = R.string.settings_section_android_auto,
                        items = listOf(
                            ExpressiveSettingItem(
                                id = "android_auto",
                                titleResId = R.string.settings_android_auto_title,
                                descriptionResId = R.string.settings_android_auto_desc,
                                iconResId = R.drawable.ic_android_auto,
                                route = "settings/android_auto",
                            ),
                        ),
                    ),
                )
            }

            add(
                ExpressiveSettingSection(
                    sectionTitleResId = R.string.settings_section_privacy_security,
                    items = listOf(
                        ExpressiveSettingItem(
                            id = "privacy",
                            titleResId = R.string.settings_privacy_title,
                            descriptionResId = R.string.settings_privacy_desc,
                            iconResId = R.drawable.security,
                            route = "settings/privacy",
                        ),
                    ),
                ),
            )

            add(
                ExpressiveSettingSection(
                    sectionTitleResId = R.string.settings_section_storage_data,
                    items = listOf(
                        ExpressiveSettingItem(
                            id = "storage",
                            titleResId = R.string.settings_storage_title,
                            descriptionResId = R.string.settings_storage_desc,
                            iconResId = R.drawable.storage,
                            route = "settings/storage",
                        ),
                        ExpressiveSettingItem(
                            id = "backup_restore",
                            titleResId = R.string.settings_backup_restore_title,
                            descriptionResId = R.string.settings_backup_restore_desc,
                            iconResId = R.drawable.restore,
                            route = "settings/backup_restore",
                        ),
                    ),
                ),
            )

            add(
                ExpressiveSettingSection(
                    sectionTitleResId = R.string.settings_section_experimental,
                    items = listOf(
                        ExpressiveSettingItem(
                            id = "experimental",
                            titleResId = R.string.settings_experimental_title,
                            descriptionResId = R.string.settings_experimental_desc,
                            iconResId = R.drawable.discover_tune,
                            route = "settings/experimental",
                        ),
                    ),
                ),
            )

            add(
                ExpressiveSettingSection(
                    sectionTitleResId = R.string.settings_section_system_about,
                    items = buildList {
                        if (isAndroid12OrLater) {
                            add(
                                ExpressiveSettingItem(
                                    id = "default_links",
                                    titleResId = R.string.settings_default_apps_title,
                                    descriptionResId = R.string.settings_default_apps_desc,
                                    iconResId = R.drawable.link,
                                    onClick = {
                                        try {
                                            val intent = Intent(
                                                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                                                "package:${context.packageName}".toUri(),
                                            )
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(
                                                context,
                                                R.string.open_app_settings_error,
                                                Toast.LENGTH_LONG,
                                            ).show()
                                        }
                                    },
                                ),
                            )
                        }
                        if (BuildConfig.UPDATER_AVAILABLE) {
                            add(
                                ExpressiveSettingItem(
                                    id = "updater",
                                    titleResId = R.string.settings_updater_title,
                                    descriptionResId = R.string.settings_updater_desc,
                                    iconResId = R.drawable.update,
                                    route = "settings/updater",
                                ),
                            )
                        }
                        add(
                            ExpressiveSettingItem(
                                id = "changelog",
                                titleResId = R.string.settings_changelog_title,
                                descriptionResId = R.string.settings_changelog_desc,
                                iconResId = R.drawable.newspaper,
                                onClick = { showChangelog.value = true },
                            ),
                        )
                        add(
                            ExpressiveSettingItem(
                                id = "about",
                                titleResId = R.string.settings_about_title,
                                descriptionResId = R.string.settings_about_desc,
                                iconResId = R.drawable.info,
                                route = "settings/about",
                            ),
                        )
                    },
                ),
            )
        }
    }

    // Filtered sections for Search
    val filteredSections = remember(searchQuery, allSections) {
        if (searchQuery.isBlank()) {
            allSections
        } else {
            val query = searchQuery.trim().lowercase()
            allSections.mapNotNull { section ->
                val matchingItems = section.items.filter { item ->
                    item.id.contains(query) || item.route?.contains(query) == true
                }
                if (matchingItems.isNotEmpty()) {
                    section.copy(items = matchingItems)
                } else {
                    null
                }
            }
        }
    }

    val playerInsets = LocalPlayerAwareWindowInsets.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(playerInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
        contentPadding = playerInsets.only(WindowInsetsSides.Bottom).asPaddingValues(),
    ) {
        // 1. Header & Large Title (Zero excess top space, snug with status bar)
        item(key = "settings_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                // Circular Back Button
                IconButton(
                    onClick = navController::navigateUp,
                    modifier =
                        Modifier
                            .size(40.dp)
                            .pointerInput(Unit) { detectTapGestures(onLongPress = { navController.backToMain() }) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = stringResource(R.string.back),
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Expressive Large Bold Title
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar (Capsule)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.search_settings),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(26.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 2. Profile Hero Card (Dynamic Login vs Guest Avatar)
        if (searchQuery.isBlank() || "profile".contains(searchQuery.trim().lowercase()) || "akun".contains(searchQuery.trim().lowercase())) {
            item(key = "hero_profile_card") {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                    HeroProfileCard(
                        isLoggedIn = isLoggedIn,
                        accountName = accountName,
                        accountImageUrl = accountImageUrl,
                        onClick = {
                            navController.navigate("settings/profile")
                        },
                    )
                }
            }
        }

        // 3. Update Available Banner (if available)
        if (BuildConfig.UPDATER_AVAILABLE && Updater.isUpdateAvailable(BuildConfig.BASE_VERSION_NAME, latestVersionName)) {
            item(key = "update_banner") {
                val releaseInfo = Updater.getCachedLatestRelease()
                val versionClean = releaseInfo?.tagName?.removePrefix("v") ?: latestVersionName.removePrefix("v")
                val downloadUrl = if (BuildConfig.CAST_AVAILABLE) {
                    "https://github.com/antoneeh10/VMusic-app/releases/download/${releaseInfo?.tagName ?: "v$latestVersionName"}/vmusic-gms-cast-$versionClean.apk"
                } else {
                    "https://github.com/antoneeh10/VMusic-app/releases/download/${releaseInfo?.tagName ?: "v$latestVersionName"}/vmusic-$versionClean.apk"
                }

                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Card(
                        onClick = { uriHandler.openUri(downloadUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.update),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.new_version_available),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = latestVersionName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Expressive Grouped Settings Sections
        filteredSections.forEach { section ->
            item(key = "section_header_${section.sectionTitleResId}") {
                Text(
                    text = stringResource(section.sectionTitleResId),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp),
                )
            }

            item(key = "section_group_${section.sectionTitleResId}") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    section.items.forEachIndexed { index, item ->
                        ExpressiveSettingsCardItem(
                            item = item,
                            index = index,
                            totalItems = section.items.size,
                            onClick = {
                                if (item.onClick != null) {
                                    item.onClick.invoke()
                                } else if (item.route != null) {
                                    navController.navigate(item.route)
                                }
                            },
                        )
                    }
                }
            }
        }

        if (BuildConfig.UPDATER_AVAILABLE && latestVersionName != BuildConfig.BASE_VERSION_NAME) {
            item(key = "release_notes") {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    ReleaseNotesCard()
                }
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HeroProfileCard(
    isLoggedIn: Boolean,
    accountName: String,
    accountImageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 14.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "hero_corner_anim",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Squircle Avatar Badge (Dynamic)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoggedIn && !accountImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = accountImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                } else if (isLoggedIn && accountName.isNotBlank()) {
                    Text(
                        text = accountName.first().uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.person),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title & Subtext
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isLoggedIn) {
                        accountName.ifBlank { stringResource(R.string.settings_profile_title) }
                    } else {
                        stringResource(R.string.action_login)
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isLoggedIn) {
                        stringResource(R.string.settings_profile_desc)
                    } else {
                        stringResource(R.string.settings_profile_login_prompt)
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                )
            }

            // Chevron Right
            Icon(
                painter = painterResource(R.drawable.navigate_next),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
fun ExpressiveSettingsCardItem(
    item: ExpressiveSettingItem,
    index: Int,
    totalItems: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topCorner by animateDpAsState(
        targetValue = if (isPressed) 12.dp else if (index == 0) 24.dp else 6.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "top_corner_anim",
    )

    val bottomCorner by animateDpAsState(
        targetValue = if (isPressed) 12.dp else if (index == totalItems - 1) 24.dp else 6.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "bottom_corner_anim",
    )

    val shape = RoundedCornerShape(
        topStart = topCorner,
        topEnd = topCorner,
        bottomStart = bottomCorner,
        bottomEnd = bottomCorner,
    )

    // Dynamic Color assignment based on item ID
    val (containerColor, iconColor) = when (item.id) {
        "appearance", "privacy", "default_links", "about" ->
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f) to MaterialTheme.colorScheme.primary
        "integrations", "content", "storage", "updater" ->
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f) to MaterialTheme.colorScheme.secondary
        "player", "ai", "android_auto", "backup_restore", "changelog" ->
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f) to MaterialTheme.colorScheme.tertiary
        "experimental" ->
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f) to MaterialTheme.colorScheme.error
        else ->
            MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Dynamic Color Tinted Icon Container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(item.iconResId),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title & Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(item.titleResId),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(item.descriptionResId),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Chevron Right
            Icon(
                painter = painterResource(R.drawable.navigate_next),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
