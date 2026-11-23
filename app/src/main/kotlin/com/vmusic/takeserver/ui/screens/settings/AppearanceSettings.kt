/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.settings

import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vmusic.takeserver.LocalPlayerAwareWindowInsets
import com.vmusic.takeserver.R
import com.vmusic.takeserver.constants.ChipSortTypeKey
import com.vmusic.takeserver.constants.CropAlbumArtKey
import com.vmusic.takeserver.constants.DefaultOpenTabKey
import com.vmusic.takeserver.constants.DensityScale
import com.vmusic.takeserver.constants.DensityScaleKey
import com.vmusic.takeserver.constants.DynamicThemeKey
import com.vmusic.takeserver.constants.EnableDynamicIconKey
import com.vmusic.takeserver.constants.EnableHighRefreshRateKey
import com.vmusic.takeserver.constants.EnableLandscapeScalingKey
import com.vmusic.takeserver.constants.ExperimentalLyricsKey
import com.vmusic.takeserver.constants.ForceBottomNavBarKey
import com.vmusic.takeserver.constants.GridItemSize
import com.vmusic.takeserver.constants.GridItemsSizeKey
import com.vmusic.takeserver.constants.HidePlayerThumbnailKey
import com.vmusic.takeserver.constants.HideStatusBarOnFullscreenKey
import com.vmusic.takeserver.constants.LibraryFilter
import com.vmusic.takeserver.constants.ListenTogetherInTopBarKey
import com.vmusic.takeserver.constants.LyricsAnimationStyle
import com.vmusic.takeserver.constants.LyricsAnimationStyleKey
import com.vmusic.takeserver.constants.LyricsBlurIntensityKey
import com.vmusic.takeserver.constants.LyricsClickKey
import com.vmusic.takeserver.constants.LyricsGlowEffectKey
import com.vmusic.takeserver.constants.LyricsLineSpacingKey
import com.vmusic.takeserver.constants.LyricsScrollKey
import com.vmusic.takeserver.constants.LyricsTextPositionKey
import com.vmusic.takeserver.constants.LyricsTextSizeKey
import com.vmusic.takeserver.constants.MiniPlayerBackgroundStyle
import com.vmusic.takeserver.constants.MiniPlayerBackgroundStyleKey
import com.vmusic.takeserver.constants.PlayerBackgroundStyle
import com.vmusic.takeserver.constants.PlayerBackgroundStyleKey
import com.vmusic.takeserver.constants.PlayerButtonsStyle
import com.vmusic.takeserver.constants.PlayerButtonsStyleKey
import com.vmusic.takeserver.constants.PureBlackMiniPlayerKey
import com.vmusic.takeserver.constants.RespectAgentPositioningKey
import com.vmusic.takeserver.constants.SelectedThemeColorKey
import com.vmusic.takeserver.constants.ShowCachedPlaylistKey
import com.vmusic.takeserver.constants.ShowDownloadedPlaylistKey
import com.vmusic.takeserver.constants.ShowLikedPlaylistKey
import com.vmusic.takeserver.constants.ShowTopPlaylistKey
import com.vmusic.takeserver.constants.ShowUploadedPlaylistKey
import com.vmusic.takeserver.constants.SliderStyle
import com.vmusic.takeserver.constants.SliderStyleKey
import com.vmusic.takeserver.constants.SlimNavBarKey
import com.vmusic.takeserver.constants.SquigglySliderKey
import com.vmusic.takeserver.constants.SwipeSensitivityKey
import com.vmusic.takeserver.constants.SwipeThumbnailKey
import com.vmusic.takeserver.constants.SwipeToSongKey
import com.vmusic.takeserver.constants.SwipeToRemoveSongKey
import com.vmusic.takeserver.constants.UseNewMiniPlayerDesignKey
import com.vmusic.takeserver.constants.UseNewPlayerDesignKey
import com.vmusic.takeserver.ui.component.DefaultDialog
import com.vmusic.takeserver.ui.component.EnumDialog
import com.vmusic.takeserver.ui.component.PlayerSliderTrack
import com.vmusic.takeserver.ui.component.SquigglySlider
import com.vmusic.takeserver.ui.component.WavySlider
import com.vmusic.takeserver.ui.theme.DefaultThemeColor
import com.vmusic.takeserver.ui.theme.PlayerSliderColors
import com.vmusic.takeserver.ui.utils.backToMain
import com.vmusic.takeserver.utils.IconUtils
import com.vmusic.takeserver.utils.rememberEnumPreference
import com.vmusic.takeserver.utils.rememberPreference
import java.util.Locale
import kotlin.math.roundToInt

enum class DarkMode {
    AUTO,
    ON,
    OFF,
}

enum class NavigationTab {
    HOME,
    SEARCH,
    LIBRARY,
}

enum class LyricsPosition {
    LEFT,
    CENTER,
    RIGHT,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
    navController: NavController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val context = LocalContext.current
    val (enableHighRefreshRate, onEnableHighRefreshRateChange) = rememberPreference(EnableHighRefreshRateKey, defaultValue = true)
    val (enableLandscapeScaling, onEnableLandscapeScalingChange) = rememberPreference(EnableLandscapeScalingKey, defaultValue = false)
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(DynamicThemeKey, defaultValue = true)
    val (enableDynamicIcon, onEnableDynamicIconPrefChange) = rememberPreference(EnableDynamicIconKey, defaultValue = true)
    val onEnableDynamicIconChange: (Boolean) -> Unit = { newValue ->
        onEnableDynamicIconPrefChange(newValue)
        IconUtils.setIcon(context, newValue)
    }
    val (selectedThemeColorInt) = rememberPreference(SelectedThemeColorKey, defaultValue = DefaultThemeColor.toArgb())
    val isUsingCustomColor = selectedThemeColorInt != DefaultThemeColor.toArgb()

    val (useNewPlayerDesign, onUseNewPlayerDesignChange) = rememberPreference(UseNewPlayerDesignKey, defaultValue = true)
    val (useNewMiniPlayerDesign, onUseNewMiniPlayerDesignChange) = rememberPreference(UseNewMiniPlayerDesignKey, defaultValue = true)
    val (pureBlackMiniPlayer, onPureBlackMiniPlayerChange) = rememberPreference(PureBlackMiniPlayerKey, defaultValue = false)
    val (miniPlayerBackground, onMiniPlayerBackgroundChange) =
        rememberEnumPreference(MiniPlayerBackgroundStyleKey, defaultValue = MiniPlayerBackgroundStyle.DEFAULT)
    val availableMiniPlayerBackgroundStyles =
        MiniPlayerBackgroundStyle.entries.filter {
            it != MiniPlayerBackgroundStyle.BLUR || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        }
    var showMiniPlayerBackgroundDialog by rememberSaveable { mutableStateOf(false) }

    val (playerBackground, onPlayerBackgroundChange) =
        rememberEnumPreference(PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.BLUR)
    val availableBackgroundStyles =
        PlayerBackgroundStyle.entries.filter {
            it != PlayerBackgroundStyle.BLUR || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        }
    var showPlayerBackgroundDialog by rememberSaveable { mutableStateOf(false) }
    val (playerButtonsStyle, onPlayerButtonsStyleChange) =
        rememberEnumPreference(PlayerButtonsStyleKey, defaultValue = PlayerButtonsStyle.DEFAULT)
    var showPlayerButtonsStyleDialog by rememberSaveable { mutableStateOf(false) }
    val (sliderStyle, onSliderStyleChange) = rememberEnumPreference(SliderStyleKey, defaultValue = SliderStyle.SLIM)
    val (squigglySlider, onSquigglySliderChange) = rememberPreference(SquigglySliderKey, defaultValue = true)
    var showSliderOptionDialog by rememberSaveable { mutableStateOf(false) }
    val (hidePlayerThumbnail, onHidePlayerThumbnailChange) = rememberPreference(HidePlayerThumbnailKey, defaultValue = false)
    val (cropAlbumArt, onCropAlbumArtChange) = rememberPreference(CropAlbumArtKey, defaultValue = false)
    val (hideStatusBarOnFullscreen, onHideStatusBarOnFullscreenChange) = rememberPreference(HideStatusBarOnFullscreenKey, defaultValue = false)
    val (lyricsBlurIntensity, onLyricsBlurIntensityChange) = rememberPreference(LyricsBlurIntensityKey, defaultValue = 25f)
    var showLyricsBlurIntensityDialog by remember { mutableStateOf(false) }
    val (swipeThumbnail, onSwipeThumbnailChange) = rememberPreference(SwipeThumbnailKey, defaultValue = true)
    val (swipeSensitivity, onSwipeSensitivityChange) = rememberPreference(SwipeSensitivityKey, defaultValue = 0.73f)
    var showSensitivityDialog by rememberSaveable { mutableStateOf(false) }

    val (lyricsTextPosition, onLyricsTextPositionChange) =
        rememberEnumPreference(LyricsTextPositionKey, defaultValue = LyricsPosition.LEFT)
    var showLyricsTextPositionDialog by rememberSaveable { mutableStateOf(false) }
    val (lyricsAnimationStyle, onLyricsAnimationStyleChange) =
        rememberEnumPreference(LyricsAnimationStyleKey, defaultValue = LyricsAnimationStyle.APPLE)
    var showLyricsAnimationStyleDialog by rememberSaveable { mutableStateOf(false) }
    val (lyricsGlowEffect, onLyricsGlowEffectChange) = rememberPreference(LyricsGlowEffectKey, defaultValue = false)
    val (lyricsTextSize, onLyricsTextSizeChange) = rememberPreference(LyricsTextSizeKey, defaultValue = 24f)
    var showLyricsTextSizeDialog by remember { mutableStateOf(false) }
    val (lyricsLineSpacing, onLyricsLineSpacingChange) = rememberPreference(LyricsLineSpacingKey, defaultValue = 1.2f)
    var showLyricsLineSpacingDialog by remember { mutableStateOf(false) }
    val (lyricsClick, onLyricsClickChange) = rememberPreference(LyricsClickKey, defaultValue = true)
    val (lyricsScroll, onLyricsScrollChange) = rememberPreference(LyricsScrollKey, defaultValue = true)
    val (respectAgentPositioning, onRespectAgentPositioningChange) = rememberPreference(RespectAgentPositioningKey, defaultValue = true)
    val (experimentalLyrics, onExperimentalLyricsChange) = rememberPreference(ExperimentalLyricsKey, defaultValue = true)
    var showExperimentalLyricsBetaDialog by remember { mutableStateOf(false) }

    val (defaultOpenTab, onDefaultOpenTabChange) = rememberEnumPreference(DefaultOpenTabKey, defaultValue = NavigationTab.HOME)
    var showDefaultOpenTabDialog by rememberSaveable { mutableStateOf(false) }
    val (defaultChip, onDefaultChipChange) = rememberEnumPreference(ChipSortTypeKey, defaultValue = LibraryFilter.LIBRARY)
    var showDefaultChipDialog by rememberSaveable { mutableStateOf(false) }
    val (swipeToSong, onSwipeToSongChange) = rememberPreference(SwipeToSongKey, defaultValue = false)
    val (swipeToRemoveSong, onSwipeToRemoveSongChange) = rememberPreference(SwipeToRemoveSongKey, defaultValue = false)
    val (forceBottomNav, onForceBottomNavChange) = rememberPreference(ForceBottomNavBarKey, defaultValue = false)
    val (slimNav, onSlimNavChange) = rememberPreference(SlimNavBarKey, defaultValue = false)
    val (listenTogetherInTopBar, onListenTogetherInTopBarChange) = rememberPreference(ListenTogetherInTopBarKey, defaultValue = true)
    val (gridItemSize, onGridItemSizeChange) = rememberEnumPreference(GridItemsSizeKey, defaultValue = GridItemSize.SMALL)
    var showGridSizeDialog by rememberSaveable { mutableStateOf(false) }
    val (densityScale, onDensityScaleChange) = rememberPreference(DensityScaleKey, defaultValue = 1f)
    var showDensityScaleDialog by rememberSaveable { mutableStateOf(false) }

    val (showLikedPlaylist, onShowLikedPlaylistChange) = rememberPreference(ShowLikedPlaylistKey, defaultValue = true)
    val (showDownloadedPlaylist, onShowDownloadedPlaylistChange) = rememberPreference(ShowDownloadedPlaylistKey, defaultValue = true)
    val (showTopPlaylist, onShowTopPlaylistChange) = rememberPreference(ShowTopPlaylistKey, defaultValue = true)
    val (showCachedPlaylist, onShowCachedPlaylistChange) = rememberPreference(ShowCachedPlaylistKey, defaultValue = true)
    val (showUploadedPlaylist, onShowUploadedPlaylistChange) = rememberPreference(ShowUploadedPlaylistKey, defaultValue = true)

    val playerInsets = LocalPlayerAwareWindowInsets.current
    val lyricsItems = if (experimentalLyrics) 5 else 9
    val playerItems = if (swipeThumbnail) 10 else 9

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(playerInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
        contentPadding = playerInsets.only(WindowInsetsSides.Bottom).asPaddingValues(),
    ) {
        item(key = "appearance_header") {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                IconButton(
                    onClick = navController::navigateUp,
                    modifier =
                        Modifier
                            .size(40.dp)
                            .pointerInput(Unit) { detectTapGestures(onLongPress = { navController.backToMain() }) },
                    colors =
                        IconButtonDefaults.iconButtonColors(
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

                Text(
                    text = stringResource(R.string.appearance_title),
                    style =
                        MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item(key = "section_theme_header") {
            ExpressiveSectionHeader(text = stringResource(R.string.appearance_section_theme))
        }

        item(key = "section_theme_group") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppearanceToggleCardItem(
                    title = stringResource(R.string.appearance_high_refresh_rate),
                    description = stringResource(R.string.appearance_high_refresh_rate_desc),
                    iconResId = R.drawable.speed,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    checked = enableHighRefreshRate,
                    onCheckedChange = onEnableHighRefreshRateChange,
                    index = 0,
                    totalItems = if (isUsingCustomColor) 4 else 5,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.appearance_landscape_scaling),
                    description = stringResource(R.string.appearance_landscape_scaling_desc),
                    iconResId = R.drawable.crop,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    checked = enableLandscapeScaling,
                    onCheckedChange = onEnableLandscapeScalingChange,
                    index = 1,
                    totalItems = if (isUsingCustomColor) 4 else 5,
                )

                if (!isUsingCustomColor) {
                    AppearanceToggleCardItem(
                        title = stringResource(R.string.appearance_dynamic_theme),
                        description = stringResource(R.string.appearance_dynamic_theme_desc),
                        iconResId = R.drawable.palette,
                        iconContainerShape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        checked = dynamicTheme,
                        onCheckedChange = onDynamicThemeChange,
                        index = 2,
                        totalItems = 5,
                    )
                }

                AppearanceToggleCardItem(
                    title = stringResource(R.string.appearance_dynamic_icon_color),
                    description = stringResource(R.string.appearance_dynamic_icon_color_desc),
                    iconResId = R.drawable.palette,
                    iconContainerShape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp, topEnd = 4.dp, bottomStart = 4.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    checked = enableDynamicIcon,
                    onCheckedChange = onEnableDynamicIconChange,
                    index = if (isUsingCustomColor) 2 else 3,
                    totalItems = if (isUsingCustomColor) 4 else 5,
                )

                AppearanceNavCardItem(
                    title = stringResource(R.string.appearance_theme),
                    description = stringResource(R.string.appearance_theme_desc),
                    iconResId = R.drawable.palette,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = { navController.navigate("settings/appearance/theme") },
                    index = if (isUsingCustomColor) 3 else 4,
                    totalItems = if (isUsingCustomColor) 4 else 5,
                )
            }
        }

        item(key = "section_mini_player_header") {
            ExpressiveSectionHeader(text = stringResource(R.string.appearance_section_mini_player))
        }

        item(key = "section_mini_player_group") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppearanceToggleCardItem(
                    title = stringResource(R.string.appearance_new_mini_player_design),
                    description = stringResource(R.string.appearance_new_mini_player_design_desc),
                    iconResId = R.drawable.nav_bar,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    checked = useNewMiniPlayerDesign,
                    onCheckedChange = onUseNewMiniPlayerDesignChange,
                    index = 0,
                    totalItems = 3,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.cd_pure_black_mode),
                    description = "",
                    iconResId = R.drawable.contrast,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    checked = pureBlackMiniPlayer,
                    onCheckedChange = onPureBlackMiniPlayerChange,
                    index = 1,
                    totalItems = 3,
                )

                AppearanceNavCardItem(
                    title = stringResource(R.string.mini_player_background_style),
                    description =
                        if (!useNewMiniPlayerDesign) {
                            stringResource(R.string.mini_player_background_not_available)
                        } else {
                            when (miniPlayerBackground) {
                                MiniPlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                                MiniPlayerBackgroundStyle.TRANSPARENT -> stringResource(R.string.transparent)
                                MiniPlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                                MiniPlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                                MiniPlayerBackgroundStyle.PURE_BLACK -> stringResource(R.string.pure_black)
                            }
                        },
                    iconResId = R.drawable.gradient,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { if (useNewMiniPlayerDesign) showMiniPlayerBackgroundDialog = true },
                    index = 2,
                    totalItems = 3,
                )
            }
        }

        item(key = "section_player_header") {
            ExpressiveSectionHeader(text = stringResource(R.string.player))
        }

        item(key = "section_player_group") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppearanceNavCardItem(
                    title = stringResource(R.string.player_background_style),
                    description =
                        when (playerBackground) {
                            PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                            PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                            PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                            PlayerBackgroundStyle.CANVAS -> "Canvas (video Spotify)"
                        },
                    iconResId = R.drawable.gradient,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = { showPlayerBackgroundDialog = true },
                    index = 0,
                    totalItems = playerItems,
                )

                AppearanceNavCardItem(
                    title = stringResource(R.string.player_background_blur),
                    description = "${lyricsBlurIntensity.roundToInt()} dp",
                    iconResId = R.drawable.gradient,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    onClick = { showLyricsBlurIntensityDialog = true },
                    index = 1,
                    totalItems = playerItems,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.new_player_design),
                    description = "",
                    iconResId = R.drawable.palette,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    checked = useNewPlayerDesign,
                    onCheckedChange = onUseNewPlayerDesignChange,
                    index = 2,
                    totalItems = playerItems,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.hide_player_thumbnail),
                    description = stringResource(R.string.hide_player_thumbnail_desc),
                    iconResId = R.drawable.hide_image,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    checked = hidePlayerThumbnail,
                    onCheckedChange = onHidePlayerThumbnailChange,
                    index = 3,
                    totalItems = playerItems,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.crop_album_art),
                    description = stringResource(R.string.crop_album_art_desc),
                    iconResId = R.drawable.crop,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    checked = cropAlbumArt,
                    onCheckedChange = onCropAlbumArtChange,
                    index = 4,
                    totalItems = playerItems,
                )

                AppearanceNavCardItem(
                    title = stringResource(R.string.player_buttons_style),
                    description =
                        when (playerButtonsStyle) {
                            PlayerButtonsStyle.DEFAULT -> stringResource(R.string.default_style)
                            PlayerButtonsStyle.PRIMARY -> stringResource(R.string.primary_color_style)
                            PlayerButtonsStyle.TERTIARY -> stringResource(R.string.tertiary_color_style)
                        },
                    iconResId = R.drawable.sliders,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { showPlayerButtonsStyleDialog = true },
                    index = 5,
                    totalItems = playerItems,
                )

                AppearanceNavCardItem(
                    title = stringResource(R.string.player_slider_style),
                    description =
                        when (sliderStyle) {
                            SliderStyle.DEFAULT -> stringResource(R.string.default_)
                            SliderStyle.WAVY ->
                                if (squigglySlider) {
                                    stringResource(R.string.squiggly)
                                } else {
                                    stringResource(R.string.wavy)
                                }
                            SliderStyle.SLIM -> stringResource(R.string.slim)
                        },
                    iconResId = R.drawable.linear_scale,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = { showSliderOptionDialog = true },
                    index = 6,
                    totalItems = playerItems,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.enable_swipe_thumbnail),
                    description = "",
                    iconResId = R.drawable.swipe,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    checked = swipeThumbnail,
                    onCheckedChange = onSwipeThumbnailChange,
                    index = 7,
                    totalItems = playerItems,
                )

                if (swipeThumbnail) {
                    AppearanceNavCardItem(
                        title = stringResource(R.string.swipe_sensitivity),
                        description = stringResource(R.string.sensitivity_percentage, (swipeSensitivity * 100).roundToInt()),
                        iconResId = R.drawable.tune,
                        iconContainerShape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        onClick = { showSensitivityDialog = true },
                        index = 8,
                        totalItems = playerItems,
                    )
                }

                AppearanceToggleCardItem(
                    title = stringResource(R.string.hide_status_bar_fullscreen),
                    description = stringResource(R.string.hide_status_bar_fullscreen_desc),
                    iconResId = R.drawable.fullscreen,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    checked = hideStatusBarOnFullscreen,
                    onCheckedChange = onHideStatusBarOnFullscreenChange,
                    index = if (swipeThumbnail) 9 else 8,
                    totalItems = playerItems,
                )
            }
        }

        item(key = "section_lyrics_header") {
            ExpressiveSectionHeader(text = stringResource(R.string.lyrics))
        }

        item(key = "section_lyrics_group") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppearanceToggleCardItem(
                    title = stringResource(R.string.experimental_lyrics),
                    description = stringResource(R.string.experimental_lyrics_desc),
                    iconResId = R.drawable.discover_tune,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    checked = experimentalLyrics,
                    onCheckedChange = {
                        if (!experimentalLyrics) {
                            showExperimentalLyricsBetaDialog = true
                        } else {
                            onExperimentalLyricsChange(false)
                        }
                    },
                    index = 0,
                    totalItems = lyricsItems,
                )

                if (!experimentalLyrics) {
                    AppearanceToggleCardItem(
                        title = stringResource(R.string.lyrics_glow_effect),
                        description = stringResource(R.string.lyrics_glow_effect_desc),
                        iconResId = R.drawable.lyrics,
                        iconContainerShape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        checked = lyricsGlowEffect,
                        onCheckedChange = onLyricsGlowEffectChange,
                        index = 1,
                        totalItems = lyricsItems,
                    )

                    AppearanceNavCardItem(
                        title = stringResource(R.string.lyrics_animation_style_title),
                        description =
                            when (lyricsAnimationStyle) {
                                LyricsAnimationStyle.NONE -> stringResource(R.string.lyrics_animation_none)
                                LyricsAnimationStyle.FADE -> stringResource(R.string.lyrics_animation_fade)
                                LyricsAnimationStyle.GLOW -> stringResource(R.string.lyrics_animation_glow)
                                LyricsAnimationStyle.SLIDE -> stringResource(R.string.lyrics_animation_slide)
                                LyricsAnimationStyle.KARAOKE -> stringResource(R.string.lyrics_animation_karaoke)
                                LyricsAnimationStyle.APPLE -> stringResource(R.string.lyrics_animation_apple)
                            },
                        iconResId = R.drawable.lyrics,
                        iconContainerShape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        onClick = { showLyricsAnimationStyleDialog = true },
                        index = 2,
                        totalItems = lyricsItems,
                    )

                    AppearanceNavCardItem(
                        title = stringResource(R.string.lyrics_text_size),
                        description = "${lyricsTextSize.roundToInt()} sp",
                        iconResId = R.drawable.lyrics,
                        iconContainerShape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconColor = MaterialTheme.colorScheme.primary,
                        onClick = { showLyricsTextSizeDialog = true },
                        index = 3,
                        totalItems = lyricsItems,
                    )

                    AppearanceNavCardItem(
                        title = stringResource(R.string.lyrics_line_spacing),
                        description = String.format(Locale.US, "%.1f", lyricsLineSpacing),
                        iconResId = R.drawable.lyrics,
                        iconContainerShape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        onClick = { showLyricsLineSpacingDialog = true },
                        index = 4,
                        totalItems = lyricsItems,
                    )
                }

                AppearanceNavCardItem(
                    title = stringResource(R.string.lyrics_text_position),
                    description =
                        when (lyricsTextPosition) {
                            LyricsPosition.LEFT -> stringResource(R.string.left)
                            LyricsPosition.CENTER -> stringResource(R.string.center)
                            LyricsPosition.RIGHT -> stringResource(R.string.right)
                        },
                    iconResId = R.drawable.lyrics,
                    iconContainerShape = if (experimentalLyrics) CircleShape else RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { showLyricsTextPositionDialog = true },
                    index = if (experimentalLyrics) 1 else 5,
                    totalItems = lyricsItems,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.respect_agent_positioning),
                    description = stringResource(R.string.respect_agent_positioning_desc),
                    iconResId = R.drawable.lyrics,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    checked = respectAgentPositioning,
                    onCheckedChange = onRespectAgentPositioningChange,
                    index = if (experimentalLyrics) 2 else 6,
                    totalItems = lyricsItems,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.lyrics_click_change),
                    description = "",
                    iconResId = R.drawable.lyrics,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    checked = lyricsClick,
                    onCheckedChange = onLyricsClickChange,
                    index = if (experimentalLyrics) 3 else 7,
                    totalItems = lyricsItems,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.lyrics_auto_scroll),
                    description = "",
                    iconResId = R.drawable.lyrics,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    checked = lyricsScroll,
                    onCheckedChange = onLyricsScrollChange,
                    index = if (experimentalLyrics) 4 else 8,
                    totalItems = lyricsItems,
                )
            }
        }

        item(key = "section_misc_header") {
            ExpressiveSectionHeader(text = stringResource(R.string.misc))
        }

        item(key = "section_misc_group") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppearanceNavCardItem(
                    title = stringResource(R.string.default_open_tab),
                    description =
                        when (defaultOpenTab) {
                            NavigationTab.HOME -> stringResource(R.string.home)
                            NavigationTab.SEARCH -> stringResource(R.string.search)
                            NavigationTab.LIBRARY -> stringResource(R.string.filter_library)
                        },
                    iconResId = R.drawable.nav_bar,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = { showDefaultOpenTabDialog = true },
                    index = 0,
                    totalItems = 9,
                )

                AppearanceNavCardItem(
                    title = stringResource(R.string.default_lib_chips),
                    description =
                        when (defaultChip) {
                            LibraryFilter.SONGS -> stringResource(R.string.songs)
                            LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                            LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                            LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                            LibraryFilter.PODCASTS -> stringResource(R.string.filter_podcasts)
                            LibraryFilter.LIBRARY -> stringResource(R.string.filter_library)
                        },
                    iconResId = R.drawable.tab,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    onClick = { showDefaultChipDialog = true },
                    index = 1,
                    totalItems = 9,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.swipe_song_to_add),
                    description = "",
                    iconResId = R.drawable.swipe,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    checked = swipeToSong,
                    onCheckedChange = onSwipeToSongChange,
                    index = 2,
                    totalItems = 9,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.swipe_song_to_remove),
                    description = "",
                    iconResId = R.drawable.swipe,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    checked = swipeToRemoveSong,
                    onCheckedChange = onSwipeToRemoveSongChange,
                    index = 3,
                    totalItems = 9,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.force_bottom_navbar),
                    description = "",
                    iconResId = R.drawable.nav_bar,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    checked = forceBottomNav,
                    onCheckedChange = onForceBottomNavChange,
                    index = 4,
                    totalItems = 9,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.slim_navbar),
                    description = "",
                    iconResId = R.drawable.nav_bar,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    checked = slimNav,
                    onCheckedChange = onSlimNavChange,
                    index = 5,
                    totalItems = 9,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.listen_together_in_top_bar),
                    description = stringResource(R.string.listen_together_in_top_bar_desc),
                    iconResId = R.drawable.group_outlined,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    checked = listenTogetherInTopBar,
                    onCheckedChange = onListenTogetherInTopBarChange,
                    index = 6,
                    totalItems = 9,
                )

                AppearanceNavCardItem(
                    title = stringResource(R.string.grid_cell_size),
                    description =
                        when (gridItemSize) {
                            GridItemSize.BIG -> stringResource(R.string.big)
                            GridItemSize.SMALL -> stringResource(R.string.small)
                        },
                    iconResId = R.drawable.grid_view,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    onClick = { showGridSizeDialog = true },
                    index = 7,
                    totalItems = 9,
                )

                AppearanceNavCardItem(
                    title = stringResource(R.string.display_density),
                    description = DensityScale.fromValue(densityScale).label,
                    iconResId = R.drawable.grid_view,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { showDensityScaleDialog = true },
                    index = 8,
                    totalItems = 9,
                )
            }
        }

        item(key = "section_auto_playlists_header") {
            ExpressiveSectionHeader(text = stringResource(R.string.auto_playlists))
        }

        item(key = "section_auto_playlists_group") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppearanceToggleCardItem(
                    title = stringResource(R.string.show_liked_playlist),
                    description = "",
                    iconResId = R.drawable.favorite,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    checked = showLikedPlaylist,
                    onCheckedChange = onShowLikedPlaylistChange,
                    index = 0,
                    totalItems = 5,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.show_downloaded_playlist),
                    description = "",
                    iconResId = R.drawable.offline,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    checked = showDownloadedPlaylist,
                    onCheckedChange = onShowDownloadedPlaylistChange,
                    index = 1,
                    totalItems = 5,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.show_top_playlist),
                    description = "",
                    iconResId = R.drawable.trending_up,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    checked = showTopPlaylist,
                    onCheckedChange = onShowTopPlaylistChange,
                    index = 2,
                    totalItems = 5,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.show_cached_playlist),
                    description = "",
                    iconResId = R.drawable.cached,
                    iconContainerShape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    checked = showCachedPlaylist,
                    onCheckedChange = onShowCachedPlaylistChange,
                    index = 3,
                    totalItems = 5,
                )

                AppearanceToggleCardItem(
                    title = stringResource(R.string.show_uploaded_playlist),
                    description = "",
                    iconResId = R.drawable.backup,
                    iconContainerShape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    checked = showUploadedPlaylist,
                    onCheckedChange = onShowUploadedPlaylistChange,
                    index = 4,
                    totalItems = 5,
                )
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showMiniPlayerBackgroundDialog) {
        EnumDialog<MiniPlayerBackgroundStyle>(
            onDismiss = { showMiniPlayerBackgroundDialog = false },
            onSelect = onMiniPlayerBackgroundChange,
            title = stringResource(R.string.mini_player_background_style),
            current = miniPlayerBackground,
            values = availableMiniPlayerBackgroundStyles,
            valueText = {
                when (it) {
                    MiniPlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                    MiniPlayerBackgroundStyle.TRANSPARENT -> stringResource(R.string.transparent)
                    MiniPlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                    MiniPlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                    MiniPlayerBackgroundStyle.PURE_BLACK -> stringResource(R.string.pure_black)
                }
            },
        )
    }

    if (showPlayerBackgroundDialog) {
        EnumDialog<PlayerBackgroundStyle>(
            onDismiss = { showPlayerBackgroundDialog = false },
            onSelect = onPlayerBackgroundChange,
            title = stringResource(R.string.player_background_style),
            current = playerBackground,
            values = availableBackgroundStyles,
            valueText = {
                when (it) {
                    PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                    PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                    PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                    PlayerBackgroundStyle.CANVAS -> "Canvas (video Spotify)"
                }
            },
        )
    }

    if (showPlayerButtonsStyleDialog) {
        EnumDialog<PlayerButtonsStyle>(
            onDismiss = { showPlayerButtonsStyleDialog = false },
            onSelect = onPlayerButtonsStyleChange,
            title = stringResource(R.string.player_buttons_style),
            current = playerButtonsStyle,
            values = PlayerButtonsStyle.entries,
            valueText = {
                when (it) {
                    PlayerButtonsStyle.DEFAULT -> stringResource(R.string.default_style)
                    PlayerButtonsStyle.PRIMARY -> stringResource(R.string.primary_color_style)
                    PlayerButtonsStyle.TERTIARY -> stringResource(R.string.tertiary_color_style)
                }
            },
        )
    }

    if (showSliderOptionDialog) {
        DefaultDialog(
            onDismiss = { showSliderOptionDialog = false },
            buttons = {
                TextButton(onClick = { showSliderOptionDialog = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        ) {
            val sliderPreviewColors =
                PlayerSliderColors.getSliderColors(
                    MaterialTheme.colorScheme.primary,
                    PlayerBackgroundStyle.DEFAULT,
                    isSystemInDarkTheme(),
                )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SliderPreviewCard(
                        selected = sliderStyle == SliderStyle.DEFAULT && !squigglySlider,
                        label = stringResource(R.string.default_),
                        onClick = {
                            onSliderStyleChange(SliderStyle.DEFAULT)
                            onSquigglySliderChange(false)
                            showSliderOptionDialog = false
                        },
                    ) {
                        Slider(
                            value = 0.35f,
                            valueRange = 0f..1f,
                            onValueChange = {},
                            colors = sliderPreviewColors,
                            enabled = false,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    SliderPreviewCard(
                        selected = sliderStyle == SliderStyle.WAVY && !squigglySlider,
                        label = stringResource(R.string.wavy),
                        onClick = {
                            onSliderStyleChange(SliderStyle.WAVY)
                            onSquigglySliderChange(false)
                            showSliderOptionDialog = false
                        },
                    ) {
                        WavySlider(
                            value = 0.5f,
                            valueRange = 0f..1f,
                            onValueChange = {},
                            colors = sliderPreviewColors,
                            modifier = Modifier.weight(1f),
                            isPlaying = true,
                            enabled = false,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SliderPreviewCard(
                        selected = sliderStyle == SliderStyle.SLIM,
                        label = stringResource(R.string.slim),
                        onClick = {
                            onSliderStyleChange(SliderStyle.SLIM)
                            onSquigglySliderChange(false)
                            showSliderOptionDialog = false
                        },
                    ) {
                        val sliderState = remember { SliderState(value = 0.65f) }
                        Slider(
                            state = sliderState,
                            thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                            track = { state ->
                                PlayerSliderTrack(sliderState = state, colors = sliderPreviewColors)
                            },
                            colors = sliderPreviewColors,
                            enabled = false,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    SliderPreviewCard(
                        selected = sliderStyle == SliderStyle.WAVY && squigglySlider,
                        label = stringResource(R.string.squiggly),
                        onClick = {
                            onSliderStyleChange(SliderStyle.WAVY)
                            onSquigglySliderChange(true)
                            showSliderOptionDialog = false
                        },
                    ) {
                        SquigglySlider(
                            value = 0.5f,
                            valueRange = 0f..1f,
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            enabled = false,
                            colors = sliderPreviewColors,
                            isPlaying = true,
                        )
                    }
                }
            }
        }
    }

    if (showSensitivityDialog) {
        var tempSensitivity by remember { mutableFloatStateOf(swipeSensitivity) }

        DefaultDialog(
            onDismiss = {
                tempSensitivity = swipeSensitivity
                showSensitivityDialog = false
            },
            buttons = {
                TextButton(onClick = { tempSensitivity = 0.73f }) {
                    Text(stringResource(R.string.reset))
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showSensitivityDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(onClick = {
                    onSwipeSensitivityChange(tempSensitivity)
                    showSensitivityDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.swipe_sensitivity),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Text(
                    text = stringResource(R.string.sensitivity_percentage, (tempSensitivity * 100).roundToInt()),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Slider(
                    value = tempSensitivity,
                    onValueChange = { tempSensitivity = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showLyricsBlurIntensityDialog) {
        var tempBlurIntensity by remember { mutableFloatStateOf(lyricsBlurIntensity) }

        DefaultDialog(
            onDismiss = {
                tempBlurIntensity = lyricsBlurIntensity
                showLyricsBlurIntensityDialog = false
            },
            buttons = {
                TextButton(onClick = { tempBlurIntensity = 25f }) {
                    Text(stringResource(R.string.reset))
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showLyricsBlurIntensityDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(onClick = {
                    onLyricsBlurIntensityChange(tempBlurIntensity)
                    showLyricsBlurIntensityDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.lyrics_background_blur_intensity),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Text(
                    text = "${tempBlurIntensity.roundToInt()} dp",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Slider(
                    value = tempBlurIntensity,
                    onValueChange = { tempBlurIntensity = it },
                    valueRange = 5f..80f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showLyricsTextSizeDialog) {
        var tempTextSize by remember { mutableFloatStateOf(lyricsTextSize) }

        DefaultDialog(
            onDismiss = {
                tempTextSize = lyricsTextSize
                showLyricsTextSizeDialog = false
            },
            buttons = {
                TextButton(onClick = { tempTextSize = 24f }) {
                    Text(stringResource(R.string.reset))
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showLyricsTextSizeDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(onClick = {
                    onLyricsTextSizeChange(tempTextSize)
                    showLyricsTextSizeDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.lyrics_text_size),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Text(
                    text = "${tempTextSize.roundToInt()} sp",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Slider(
                    value = tempTextSize,
                    onValueChange = { tempTextSize = it },
                    valueRange = 12f..48f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showLyricsLineSpacingDialog) {
        var tempLineSpacing by remember { mutableFloatStateOf(lyricsLineSpacing) }

        DefaultDialog(
            onDismiss = {
                tempLineSpacing = lyricsLineSpacing
                showLyricsLineSpacingDialog = false
            },
            buttons = {
                TextButton(onClick = { tempLineSpacing = 1.3f }) {
                    Text(stringResource(R.string.reset))
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showLyricsLineSpacingDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(onClick = {
                    onLyricsLineSpacingChange(tempLineSpacing)
                    showLyricsLineSpacingDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.lyrics_line_spacing),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Text(
                    text = String.format(Locale.US, "%.1f", tempLineSpacing),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Slider(
                    value = tempLineSpacing,
                    onValueChange = { tempLineSpacing = it },
                    valueRange = 1.0f..3.0f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showLyricsTextPositionDialog) {
        EnumDialog<LyricsPosition>(
            onDismiss = { showLyricsTextPositionDialog = false },
            onSelect = onLyricsTextPositionChange,
            title = stringResource(R.string.lyrics_text_position),
            current = lyricsTextPosition,
            values = LyricsPosition.entries,
            valueText = {
                when (it) {
                    LyricsPosition.LEFT -> stringResource(R.string.left)
                    LyricsPosition.CENTER -> stringResource(R.string.center)
                    LyricsPosition.RIGHT -> stringResource(R.string.right)
                }
            },
        )
    }

    if (showLyricsAnimationStyleDialog) {
        EnumDialog<LyricsAnimationStyle>(
            onDismiss = { showLyricsAnimationStyleDialog = false },
            onSelect = onLyricsAnimationStyleChange,
            title = stringResource(R.string.lyrics_animation_style_title),
            current = lyricsAnimationStyle,
            values = LyricsAnimationStyle.entries,
            valueText = {
                when (it) {
                    LyricsAnimationStyle.NONE -> stringResource(R.string.lyrics_animation_none)
                    LyricsAnimationStyle.FADE -> stringResource(R.string.lyrics_animation_fade)
                    LyricsAnimationStyle.GLOW -> stringResource(R.string.lyrics_animation_glow)
                    LyricsAnimationStyle.SLIDE -> stringResource(R.string.lyrics_animation_slide)
                    LyricsAnimationStyle.KARAOKE -> stringResource(R.string.lyrics_animation_karaoke)
                    LyricsAnimationStyle.APPLE -> stringResource(R.string.lyrics_animation_apple)
                }
            },
        )
    }

    if (showDefaultOpenTabDialog) {
        EnumDialog<NavigationTab>(
            onDismiss = { showDefaultOpenTabDialog = false },
            onSelect = onDefaultOpenTabChange,
            title = stringResource(R.string.default_open_tab),
            current = defaultOpenTab,
            values = NavigationTab.entries,
            valueText = {
                when (it) {
                    NavigationTab.HOME -> stringResource(R.string.home)
                    NavigationTab.SEARCH -> stringResource(R.string.search)
                    NavigationTab.LIBRARY -> stringResource(R.string.filter_library)
                }
            },
        )
    }

    if (showDefaultChipDialog) {
        EnumDialog<LibraryFilter>(
            onDismiss = { showDefaultChipDialog = false },
            onSelect = onDefaultChipChange,
            title = stringResource(R.string.default_lib_chips),
            current = defaultChip,
            values = LibraryFilter.entries,
            valueText = {
                when (it) {
                    LibraryFilter.SONGS -> stringResource(R.string.songs)
                    LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                    LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                    LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                    LibraryFilter.PODCASTS -> stringResource(R.string.filter_podcasts)
                    LibraryFilter.LIBRARY -> stringResource(R.string.filter_library)
                }
            },
        )
    }

    if (showGridSizeDialog) {
        EnumDialog<GridItemSize>(
            onDismiss = { showGridSizeDialog = false },
            onSelect = onGridItemSizeChange,
            title = stringResource(R.string.grid_cell_size),
            current = gridItemSize,
            values = GridItemSize.entries,
            valueText = {
                when (it) {
                    GridItemSize.BIG -> stringResource(R.string.big)
                    GridItemSize.SMALL -> stringResource(R.string.small)
                }
            },
        )
    }

    if (showDensityScaleDialog) {
        EnumDialog<DensityScale>(
            onDismiss = { showDensityScaleDialog = false },
            onSelect = { onDensityScaleChange(it.value) },
            title = stringResource(R.string.display_density),
            current = DensityScale.fromValue(densityScale),
            values = DensityScale.entries,
            valueText = { it.label },
        )
    }

    if (showExperimentalLyricsBetaDialog) {
        DefaultDialog(
            onDismiss = { showExperimentalLyricsBetaDialog = false },
            title = { Text(stringResource(R.string.experimental_lyrics_beta_title)) },
            buttons = {
                TextButton(onClick = { showExperimentalLyricsBetaDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(onClick = {
                    showExperimentalLyricsBetaDialog = false
                    onExperimentalLyricsChange(true)
                }) {
                    Text(stringResource(R.string.enable))
                }
            },
        ) {
            Text(stringResource(R.string.experimental_lyrics_beta_message))
        }
    }
}

@Composable
private fun RowScope.SliderPreviewCard(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            Modifier
                .aspectRatio(1f)
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    RoundedCornerShape(16.dp),
                ).clickable(onClick = onClick)
                .padding(12.dp),
        content = content,
    )
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun ExpressiveSectionHeader(text: String) {
    Text(
        text = text,
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

@Composable
fun AppearanceToggleCardItem(
    title: String,
    description: String,
    iconResId: Int,
    iconContainerShape: Shape,
    containerColor: Color,
    iconColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    index: Int,
    totalItems: Int,
    modifier: Modifier = Modifier,
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
        label = "top_corner_anim",
    )

    val bottomCorner by animateDpAsState(
        targetValue = if (isPressed) 12.dp else if (index == totalItems - 1) 24.dp else 6.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "bottom_corner_anim",
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
            modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onCheckedChange(!checked) },
                ),
        shape = shape,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(iconContainerShape)
                        .background(containerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                            ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbContent = {
                    Icon(
                        painter = painterResource(
                            if (checked) R.drawable.check else R.drawable.close,
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                },
            )
        }
    }
}

@Composable
fun AppearanceNavCardItem(
    title: String,
    description: String,
    iconResId: Int,
    iconContainerShape: Shape,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    index: Int,
    totalItems: Int,
    modifier: Modifier = Modifier,
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
        label = "top_corner_anim",
    )

    val bottomCorner by animateDpAsState(
        targetValue = if (isPressed) 12.dp else if (index == totalItems - 1) 24.dp else 6.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "bottom_corner_anim",
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
            modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
        shape = shape,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
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
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(iconContainerShape)
                        .background(containerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                            ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Icon(
                painter = painterResource(R.drawable.navigate_next),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
