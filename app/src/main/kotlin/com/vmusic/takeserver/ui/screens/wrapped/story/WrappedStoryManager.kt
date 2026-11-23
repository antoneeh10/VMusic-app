/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.wrapped.story

import android.content.Context
import com.vmusic.takeserver.db.DatabaseDao
import com.vmusic.takeserver.db.entities.SongWithStats
import com.vmusic.takeserver.ui.screens.wrapped.WrappedEntryPoint
import com.vmusic.takeserver.ui.utils.resize
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime

/** Supplies current-year listening data for the 8-slide VMusic Wrapped story. */
class WrappedStoryManager(
    private val databaseDao: DatabaseDao,
    year: Int = LocalDate.now().year,
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableStateFlow(WrappedStoryData(year = year))
    val state = _state.asStateFlow()

    suspend fun prepare() {
        if (_state.value.isDataReady) return
        val year = _state.value.year
        val from = LocalDateTime.of(year, 1, 1, 0, 0, 0)
        val to = LocalDateTime.of(year, 12, 31, 23, 59, 59)

        withContext(Dispatchers.IO) {
            runCatching {
                val songsDeferred = async { databaseDao.mostPlayedSongsStats(from, toTimeStamp = to, limit = 30).first() }
                val artistsDeferred = async { databaseDao.mostPlayedArtists(from, toTimeStamp = to, limit = 5).first() }
                val minutesDeferred = async { databaseDao.getTotalPlayTimeInRange(from, to).first() ?: 0L }
                val playsDeferred = async { databaseDao.getPlayCountInRange(from, to).first() }
                val uniqueSongsDeferred = async { databaseDao.getUniqueSongCountInRange(from, to).first() }
                val uniqueArtistsDeferred = async { databaseDao.getUniqueArtistCountInRange(from, to).first() }
                val hoursDeferred = async { databaseDao.getPlayCountsByHourInRange(from, to).first() }

                val results = awaitAll(
                    songsDeferred,
                    artistsDeferred,
                    minutesDeferred,
                    playsDeferred,
                    uniqueSongsDeferred,
                    uniqueArtistsDeferred,
                    hoursDeferred,
                )

                @Suppress("UNCHECKED_CAST")
                val songs = results[0] as List<SongWithStats>
                @Suppress("UNCHECKED_CAST")
                val artists = results[1] as List<com.vmusic.takeserver.db.entities.Artist>
                @Suppress("UNCHECKED_CAST")
                val hourRows = results[6] as List<com.vmusic.takeserver.db.entities.HourPlayCount>

                val hourly = MutableList(24) { 0 }
                hourRows.forEach { row ->
                    if (row.hour in 0..23) hourly[row.hour] = row.count
                }
                val peak = hourly.withIndex().maxByOrNull { it.value }?.index ?: 23

                val storySongs = songs.take(5).map { it.toStorySong() }
                _state.update { current ->
                    current.copy(
                        totalMinutes = (results[2] as Long) / 60_000L,
                        totalPlays = results[3] as Int,
                        uniqueSongs = results[4] as Int,
                        uniqueArtists = results[5] as Int,
                        topArtists =
                            artists.map { artist ->
                                StoryArtist(
                                    name = artist.artist.name,
                                    thumbnailUrl = artist.artist.thumbnailUrl?.resize(256, 256),
                                    playCount = artist.songCount,
                                )
                            },
                        topSongs = storySongs,
                        hourlyCounts = hourly,
                        peakHour = peak,
                        hasEnoughData = songs.isNotEmpty() || artists.isNotEmpty(),
                        isDataReady = true,
                    )
                }
            }.onFailure { error ->
                Timber.tag("WrappedStoryManager").e(error, "Failed to prepare Wrapped data")
                _state.update { it.copy(isDataReady = true, hasEnoughData = false) }
            }
        }
    }

    private suspend fun SongWithStats.toStorySong(): StorySong {
        val detailed = runCatching { databaseDao.song(id).first() }.getOrNull()
        val yearValue = detailed?.song?.year ?: detailed?.album?.year
        return StorySong(
            id = id,
            title = title,
            artist = artistName
                ?: detailed?.orderedArtists?.joinToString(", ") { it.name }
                ?: "",
            thumbnailUrl = thumbnailUrl.resize(512, 512),
            playCount = songCountListened,
            year = yearValue?.toString(),
            durationSeconds = detailed?.song?.duration ?: 0,
        )
    }

    fun dispose() {
        scope.cancel()
    }
}

internal fun provideWrappedStoryManager(context: Context): WrappedStoryManager {
    val entryPoint =
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            WrappedEntryPoint::class.java,
        )
    return WrappedStoryManager(databaseDao = entryPoint.databaseDao())
}
