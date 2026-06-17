/*
 * PulseMusic (2026)
 * © Aditya Parasher — github.com/BludAdit3220
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.pulsemusic.music.db.entities

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

@Immutable
data class SongWithStats(
    val id: String,
    val title: String,
    @Relation(
        entity = ArtistEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SortedSongArtistMap::class,
            parentColumn = "songId",
            entityColumn = "artistId"
        )
    )
    val artists: List<ArtistEntity>,
    val thumbnailUrl: String,
    val artistName: String?,
    val songCountListened: Int,
    val timeListened: Long?,
    val isVideo: Boolean = false,
)
