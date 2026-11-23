/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.vmusic.takeserver.LocalNavController
import com.vmusic.takeserver.LocalPlayerAwareWindowInsets
import com.vmusic.takeserver.R
import com.vmusic.takeserver.constants.AlbumViewTypeKey
import com.vmusic.takeserver.constants.ChipSortTypeKey
import com.vmusic.takeserver.constants.LibraryFilter
import com.vmusic.takeserver.constants.LibraryViewType
import com.vmusic.takeserver.constants.PlaylistViewTypeKey
import com.vmusic.takeserver.ui.component.ChipsRow
import com.vmusic.takeserver.ui.component.UnifiedHeader
import com.vmusic.takeserver.utils.rememberEnumPreference

@Composable
fun LibraryScreen() {
    val navController = LocalNavController.current
    var filterType by rememberEnumPreference(ChipSortTypeKey, LibraryFilter.LIBRARY)
    var libraryViewType by rememberEnumPreference(AlbumViewTypeKey, LibraryViewType.GRID)
    var playlistViewType by rememberEnumPreference(PlaylistViewTypeKey, LibraryViewType.GRID)

    var scrollOffset by remember { mutableFloatStateOf(0f) }
    val isScrolled by remember { derivedStateOf { scrollOffset > 0f } }
    val scrollConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    scrollOffset = (scrollOffset - consumed.y).coerceAtLeast(0f)
                    return Offset.Zero
                }
            }
        }

    LaunchedEffect(filterType) {
        scrollOffset = 0f
    }

    val density = LocalDensity.current
    var headerHeight by remember { mutableIntStateOf(0) }

    val filterContent = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            ChipsRow(
                chips = listOf(
                    LibraryFilter.PLAYLISTS to stringResource(R.string.filter_playlists),
                    LibraryFilter.SONGS to stringResource(R.string.filter_songs),
                    LibraryFilter.ALBUMS to stringResource(R.string.filter_albums),
                    LibraryFilter.ARTISTS to stringResource(R.string.filter_artists),
                    LibraryFilter.PODCASTS to stringResource(R.string.filter_podcasts),
                ),
                currentValue = filterType,
                onValueUpdate = {
                    filterType = if (filterType == it) LibraryFilter.LIBRARY else it
                },
                modifier = Modifier.weight(1f),
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        UnifiedHeader(
            title = "Pustaka",
            isScrolled = isScrolled,
            animateTransition = true,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1f)
                // Keep the unscrolled height so the title shrink doesn't shift content.
                .onSizeChanged { headerHeight = maxOf(headerHeight, it.height) },
        )

        // Let the content fill the whole screen so it scrolls *behind* the header,
        // which is what makes the gradient scrim visible. The header height is passed
        // as top contentPadding instead, so the first item still starts below it.
        // AppBarHeight from LocalPlayerAwareWindowInsets is deliberately dropped.
        CompositionLocalProvider(
            LocalPlayerAwareWindowInsets provides LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                .add(WindowInsets(top = with(density) { headerHeight.toDp() })),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollConnection),
            ) {
                when (filterType) {
                    LibraryFilter.LIBRARY -> LibraryMixScreen(
                        navController = navController,
                        filterContent = filterContent,
                        viewType = libraryViewType,
                        onViewTypeChange = { libraryViewType = it },
                    )
                    LibraryFilter.PLAYLISTS -> LibraryPlaylistsScreen(
                        navController = navController,
                        filterContent = filterContent,
                        viewType = playlistViewType,
                        onViewTypeChange = { playlistViewType = it },
                    )
                    LibraryFilter.SONGS -> LibrarySongsScreen(
                        navController,
                        { filterType = LibraryFilter.LIBRARY },
                    )
                    LibraryFilter.ALBUMS -> LibraryAlbumsScreen(
                        navController,
                        { filterType = LibraryFilter.LIBRARY },
                    )
                    LibraryFilter.ARTISTS -> LibraryArtistsScreen(
                        navController,
                        { filterType = LibraryFilter.LIBRARY },
                    )
                    LibraryFilter.PODCASTS -> LibraryPodcastsScreen(
                        navController,
                        { filterType = LibraryFilter.LIBRARY },
                    )
                }
            }
        }
    }
}
