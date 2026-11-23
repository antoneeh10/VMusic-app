/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.db.entities

/** Number of play events in one local hour of day, used by VMusic Wrapped. */
data class HourPlayCount(
    val hour: Int,
    val count: Int,
)
