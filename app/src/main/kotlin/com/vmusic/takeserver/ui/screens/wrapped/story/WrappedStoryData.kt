/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.wrapped.story

data class StorySong(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val playCount: Int,
    val year: String?,
    val durationSeconds: Int = 0,
)

data class StoryArtist(
    val name: String,
    val thumbnailUrl: String?,
    val playCount: Int,
)

data class WrappedStoryData(
    val year: Int,
    val totalMinutes: Long = 0L,
    val totalPlays: Int = 0,
    val uniqueSongs: Int = 0,
    val uniqueArtists: Int = 0,
    val topArtists: List<StoryArtist> = emptyList(),
    val topSongs: List<StorySong> = emptyList(),
    val hourlyCounts: List<Int> = List(24) { 0 },
    val peakHour: Int = 23,
    val genre: String = "",
    val isDataReady: Boolean = false,
    val hasEnoughData: Boolean = false,
)
