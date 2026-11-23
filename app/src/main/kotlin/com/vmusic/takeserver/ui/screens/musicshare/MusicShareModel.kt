/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.musicshare

import android.net.Uri
import com.vmusic.takeserver.db.entities.Song
import com.vmusic.takeserver.models.MediaMetadata
import com.vmusic.takeserver.models.toMediaMetadata
import com.vmusic.takeserver.ui.utils.resize
import org.json.JSONObject

/** Song metadata carried to the editor, so every entry point shares one implementation. */
data class ShareSongInfo(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
    val duration: Int,
) {
    fun toJson(): String =
        JSONObject()
            .put("id", id)
            .put("title", title)
            .put("artist", artist)
            .put("art", thumbnailUrl ?: "")
            .put("dur", duration)
            .toString()

    companion object {
        fun fromJson(value: String): ShareSongInfo? =
            try {
                JSONObject(value).let { json ->
                    ShareSongInfo(
                        id = json.getString("id"),
                        title = json.getString("title"),
                        artist = json.optString("artist"),
                        thumbnailUrl = json.optString("art").takeIf { it.isNotBlank() },
                        duration = json.optInt("dur"),
                    )
                }
            } catch (_: Exception) {
                null
            }

        fun from(media: MediaMetadata): ShareSongInfo {
            val raw = media.thumbnailUrl?.takeIf { it.isNotBlank() }
                ?: "https://i.ytimg.com/vi/${media.id}/hqdefault.jpg"
            return ShareSongInfo(
                id = media.id,
                title = media.title,
                artist = media.artists.joinToString(", ") { it.name },
                thumbnailUrl = raw.replace("/maxresdefault.jpg", "/hqdefault.jpg").resize(1080, 1080),
                duration = media.duration,
            )
        }

        fun from(song: Song): ShareSongInfo = from(song.toMediaMetadata())
    }
}

object MusicShareRoute {
    const val ROUTE = "custom_music_share/{data}"
    const val ARG = "data"

    fun create(info: ShareSongInfo): String = "custom_music_share/${Uri.encode(info.toJson())}"
}

enum class MusicPlacement {
    LEFT,
    CENTER,
    RIGHT,
}

/** Normalized offsets keep the preview and the exported bitmap in the same coordinate space. */
data class MusicShareState(
    val placement: MusicPlacement = MusicPlacement.LEFT,
    val photoScale: Float = 1f,
    val photoOffsetX: Float = 0f,
    val photoOffsetY: Float = 0f,
    val photoRotation: Float = 0f,
    val photoAlpha: Float = 1f,
    val musicScale: Float = 1f,
    val musicOffsetX: Float = 0f,
    val musicOffsetY: Float = 0f,
    val musicRotation: Float = -6f,
    val musicSkew: Float = 0.08f,
    val musicAlpha: Float = 1f,
    val musicOnTop: Boolean = true,
)
