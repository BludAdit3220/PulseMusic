package com.pulsemusic.innertube.pages

import com.pulsemusic.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
